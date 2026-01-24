package com.snakegame.net;

import com.snakegame.net.LeaderboardModels.*;
import com.snakegame.net.LeaderboardModels.LeaderboardEntry;
import com.snakegame.net.LeaderboardModels.LeaderboardResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Lightweight HTTP client for the online leaderboard service.
 *
 * <p>Uses {@link java.net.http.HttpClient} and minimal, purpose-built JSON parsing to avoid external
 * dependencies. All operations are asynchronous.</p>
 */
public class LeaderboardClient {

    private final HttpClient client;

    private volatile UUID sessionId;
    private volatile String sessionToken;

    /**
     * Creates a new leaderboard client with configured connect timeout.
     */
    public LeaderboardClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(LeaderboardConfig.CONNECT_TIMEOUT))
                .build();
    }

    // ---------------- Session ----------------

    /**
     * Starts a new leaderboard session asynchronously.
     *
     * @return future that completes when the session has been established and parsed
     */
    public CompletableFuture<Void> startSessionAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LeaderboardConfig.BASE_URL + "/api/session"))
                .timeout(Duration.ofMillis(LeaderboardConfig.REQUEST_TIMEOUT))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::parseSession);
    }

    private void parseSession(String json) {
        // Very small JSON, so we keep parsing simple (no libs)
        this.sessionId = UUID.fromString(requireString(json, "sessionId"));
        this.sessionToken = requireString(json, "sessionToken");
    }

    // ---------------- Submit Score ----------------

    /**
     * Submits a score to the leaderboard service, creating a session first if needed.
     *
     * @param playerId player UUID
     * @param playerName display name
     * @param score score value to submit
     * @param mapId map id for the run (0 for STANDARD)
     * @param mode game mode name
     * @param difficulty difficulty label
     * @param timeSurvivedMs survival time in milliseconds
     * @param gameVersion game version string
     * @return future that completes when the submission finishes successfully
     */
    public CompletableFuture<Void> submitScoreAsync(
            UUID playerId,
            String playerName,
            int score,
            int mapId,
            String mode,
            String difficulty,
            long timeSurvivedMs,
            String gameVersion
    ) {
        if (sessionId == null || sessionToken == null) {
            return startSessionAsync()
                    .thenCompose(v -> doSubmit(
                            playerId, playerName, score, mapId, mode, difficulty, timeSurvivedMs, gameVersion
                    ));
        }
        return doSubmit(playerId, playerName, score, mapId, mode, difficulty, timeSurvivedMs, gameVersion);
    }

    private CompletableFuture<Void> doSubmit(
            UUID playerId,
            String playerName,
            int score,
            int mapId,
            String mode,
            String difficulty,
            long timeSurvivedMs,
            String gameVersion
    ) {
        String json = """
                {
                  "playerId": "%s",
                  "playerName": "%s",
                  "score": %d,
                  "mapId": %d,
                  "mode": "%s",
                  "difficulty": "%s",
                  "timeSurvivedMs": %d,
                  "gameVersion": "%s"
                }
                """.formatted(
                playerId,
                escape(playerName),
                score,
                mapId,
                mode,
                difficulty,
                timeSurvivedMs,
                gameVersion
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LeaderboardConfig.BASE_URL + "/api/scores"))
                .timeout(Duration.ofMillis(LeaderboardConfig.REQUEST_TIMEOUT))
                .header("Content-Type", "application/json")
                .header("X-Session-Id", sessionId.toString())
                .header("X-Session-Token", sessionToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(resp -> {
                    if (resp.statusCode() != 201) {
                        throw new RuntimeException("Score submission failed: " + resp.statusCode());
                    }
                });
    }

    // ---------------- Utils ----------------

    private static int skipWhitespace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    /**
     * Finds the start quote index of the given JSON object key, ignoring whitespace and string literals.
     * Returns -1 if not found.
     */
    private static int findKey(String json, String key) {
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c != '"') continue;

            // Potential key start at i. Keys are unescaped in our API responses.
            int keyStart = i + 1;
            int keyEnd = keyStart + key.length();
            if (keyEnd >= json.length()) {
                inString = true;
                continue;
            }
            if (!json.regionMatches(keyStart, key, 0, key.length())) {
                inString = true;
                continue;
            }
            if (json.charAt(keyEnd) != '"') {
                inString = true;
                continue;
            }

            int j = skipWhitespace(json, keyEnd + 1);
            if (j < json.length() && json.charAt(j) == ':') {
                return i;
            }

            inString = true;
        }
        return -1;
    }

    private static int findValueStart(String json, String key) {
        int keyIdx = findKey(json, key);
        if (keyIdx < 0) return -1;

        int i = keyIdx + 1 + key.length() + 1; // after closing quote
        i = skipWhitespace(json, i);
        if (i >= json.length() || json.charAt(i) != ':') return -1;
        i++;
        i = skipWhitespace(json, i);
        return i;
    }

    private static String requireString(String json, String key) {
        int valueStart = findValueStart(json, key);
        if (valueStart < 0) throw new IllegalStateException("Missing key: " + key);
        String out = extractStringAt(json, valueStart);
        if (out == null) throw new IllegalStateException("Expected string value for key: " + key);
        return out;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Fetches leaderboard entries for the provided filters.
     *
     * @param mapId map id filter (0 may represent "basic"/"any" depending on mode)
     * @param mode mode filter
     * @param difficulty difficulty filter (or "ANY")
     * @param limit maximum number of results (clamped to 1..50)
     * @param offset pagination offset (clamped to &gt;= 0)
     * @return future producing a parsed leaderboard response
     */
    public CompletableFuture<LeaderboardResponse> fetchLeaderboardAsync(
            int mapId,
            String mode,
            String difficulty,
            int limit,
            int offset
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        int safeOffset = Math.max(offset, 0);

        String url = LeaderboardConfig.BASE_URL + "/api/leaderboard"
                + "?mapId=" + mapId
                + "&mode=" + encode(mode)
                + "&difficulty=" + encode(difficulty)
                + "&limit=" + safeLimit
                + "&offset=" + safeOffset;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(LeaderboardConfig.REQUEST_TIMEOUT))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() != 200) {
                        throw new RuntimeException("Leaderboard fetch failed: " + resp.statusCode() + " body=" + resp.body());
                    }
                    return parseLeaderboard(resp.body());
                });
    }


    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private LeaderboardResponse parseLeaderboard(String json) {
        int mapId = Integer.parseInt(extractNumber(json, "mapId"));
        String mode = extractString(json, "mode");
        String difficulty = extractString(json, "difficulty");

        List<LeaderboardEntry> entries = new ArrayList<>();

        // Find entries array (whitespace-tolerant)
        int start = findValueStart(json, "entries");
        if (start >= 0 && start < json.length() && json.charAt(start) == '[') {
            int end = findMatchingBracket(json, start);
            String arr = json.substring(start + 1, end).trim(); // inside [ ...]

            if (!arr.isEmpty()) {
                // split objects by "},{", allowing whitespace/newlines around the comma
                String[] objs = arr.split("\\}\\s*,\\s*\\{");
                for (String o : objs) {
                    String obj = o;
                    if (!obj.startsWith("{")) obj = "{" + obj;
                    if (!obj.endsWith("}")) obj = obj + "}";

                    int rank = Integer.parseInt(extractNumber(obj, "rank"));
                    String playerName = extractString(obj, "playerName");
                    int entryMapId = Integer.parseInt(extractNumber(obj, "mapId"));
                    String entryDifficulty = extractString(obj, "difficulty"); // NEW
                    int score = Integer.parseInt(extractNumber(obj, "score"));
                    long timeSurvivedMs = Long.parseLong(extractNumber(obj, "timeSurvivedMs"));
                    Instant createdAt = Instant.parse(extractString(obj, "createdAt"));

                    entries.add(new LeaderboardEntry(rank, playerName, entryMapId, entryDifficulty, score, timeSurvivedMs, createdAt));
                }
            }
        }

        return new LeaderboardResponse(mapId, mode, difficulty, entries);
    }

    private static String extractString(String json, String key) {
        int valueStart = findValueStart(json, key);
        if (valueStart < 0) return "";
        String out = extractStringAt(json, valueStart);
        return out == null ? "" : out;
    }

    /**
     * Extracts a JSON string value starting at {@code valueStart}. Returns null if value is not a string.
     */
    private static String extractStringAt(String json, int valueStart) {
        int i = skipWhitespace(json, valueStart);
        if (i < 0 || i >= json.length()) return null;
        if (json.charAt(i) != '"') return null;

        int start = i + 1;
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '\\' && end + 1 < json.length()) {
                end += 2; // Skip escaped character
            } else if (json.charAt(end) == '"') {
                break;
            } else {
                end++;
            }
        }
        String raw = json.substring(start, end);
        return unescape(raw);
    }

    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        // Unicode escape \XXXX
                        if (i + 5 < s.length()) {
                            try {
                                String hex = s.substring(i + 2, i + 6);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append(next);
                            }
                        } else {
                            sb.append(next);
                        }
                    }
                    default -> sb.append(next);
                }
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private static String extractNumber(String json, String key) {
        int start = findValueStart(json, key);
        if (start < 0) return "0";
        int end = start;
        // Handle optional minus sign
        if (end < json.length() && json.charAt(end) == '-') end++;
        // Consume digits
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return json.substring(start, end).trim();
    }

    private static int findMatchingBracket(String s, int openIdx) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return s.length() - 1;
    }

}
