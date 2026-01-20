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

public class LeaderboardClient {

    private final HttpClient client;

    private volatile UUID sessionId;
    private volatile String sessionToken;

    public LeaderboardClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(LeaderboardConfig.CONNECT_TIMEOUT))
                .build();
    }

    // ---------------- Session ----------------

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
        this.sessionId = UUID.fromString(extract(json, "sessionId"));
        this.sessionToken = extract(json, "sessionToken");
    }

    // ---------------- Submit Score ----------------

    public CompletableFuture<Void> submitScoreAsync(
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
                            playerName, score, mapId, mode, difficulty, timeSurvivedMs, gameVersion
                    ));
        }
        return doSubmit(playerName, score, mapId, mode, difficulty, timeSurvivedMs, gameVersion);
    }

    private CompletableFuture<Void> doSubmit(
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
                  "playerName": "%s",
                  "score": %d,
                  "mapId": %d,
                  "mode": "%s",
                  "difficulty": "%s",
                  "timeSurvivedMs": %d,
                  "gameVersion": "%s"
                }
                """.formatted(
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

    private static String extract(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) throw new IllegalStateException("Missing key: " + key);
        start += pattern.length();
        // Find closing quote, accounting for escaped quotes
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
        return json.substring(start, end);
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

        // Find entries array
        int entriesIdx = json.indexOf("\"entries\":[");
        if (entriesIdx >= 0) {
            int start = json.indexOf('[', entriesIdx);
            int end = findMatchingBracket(json, start);
            String arr = json.substring(start + 1, end).trim(); // inside [ ...]

            if (!arr.isEmpty()) {
                // split objects by "},{" boundary (safe for our simple objects)
                String[] objs = arr.split("\\},\\{");
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
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) return "";
        start += pattern.length();
        // Find closing quote, accounting for escaped quotes
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
        // Unescape the string
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
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) return "0";
        start += pattern.length();
        // Skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        // Handle optional minus sign
        if (end < json.length() && json.charAt(end) == '-') end++;
        // Consume digits
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return json.substring(start, end).trim();
    }

    private static int findMatchingBracket(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return s.length() - 1;
    }

}
