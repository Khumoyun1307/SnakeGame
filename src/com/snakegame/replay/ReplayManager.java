package com.snakegame.replay;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.model.Direction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.snakegame.util.AppPaths;

public class ReplayManager {
    private static final Logger log = Logger.getLogger(ReplayManager.class.getName());

    private static final String LAST_PATH = AppPaths.REPLAY_LAST_FILE.toString();
    private static final String BEST_PATH = AppPaths.REPLAY_BEST_FILE.toString();

    public static boolean hasLast() { return new File(LAST_PATH).exists(); }
    public static boolean hasBest() { return new File(BEST_PATH).exists(); }

    public static Optional<ReplayData> loadLast() { return load(LAST_PATH); }
    public static Optional<ReplayData> loadBest() { return load(BEST_PATH); }

    public static void saveLast(ReplayData data) { save(LAST_PATH, data); }

    public static void saveBestIfHigher(ReplayData data) {
        int currentBest = load(BEST_PATH).map(d -> d.finalScore).orElse(-1);
        if (data.finalScore > currentBest) {
            save(BEST_PATH, data);
        }
    }

    // ---------------- IO ----------------

    private static void save(String path, ReplayData data) {
        if (data == null || data.runSettingsSnapshot == null) return;

        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.severe("Failed to create replay directory: " + parent.getPath());
            return;
        }

        Properties p = new Properties();
        p.setProperty("version", String.valueOf(ReplayData.CURRENT_VERSION));
        p.setProperty("savedAtMillis", String.valueOf(System.currentTimeMillis()));
        p.setProperty("seed", String.valueOf(data.seed));
        p.setProperty("finalScore", String.valueOf(data.finalScore));
        p.setProperty("startMapId", String.valueOf(data.startMapId));

        // Flatten SettingsSnapshot (same fields order as your snapshot constructor)
        SettingsSnapshot s = data.runSettingsSnapshot;
        p.setProperty("difficultyLevel", String.valueOf(s.difficultyLevel()));
        p.setProperty("obstaclesEnabled", String.valueOf(s.obstaclesEnabled()));
        p.setProperty("currentMode", s.currentMode().name());
        p.setProperty("selectedMapId", String.valueOf(s.selectedMapId()));
        p.setProperty("raceThreshold", String.valueOf(s.raceThreshold()));
        p.setProperty("soundEnabled", String.valueOf(s.soundEnabled()));
        p.setProperty("musicEnabled", String.valueOf(s.musicEnabled()));
        p.setProperty("showGrid", String.valueOf(s.showGrid()));
        p.setProperty("playerName", s.playerName() == null ? "Player" : s.playerName());
        p.setProperty("playerId", s.playerId() == null ? "" : s.playerId().toString());
        p.setProperty("theme", s.selectedTheme().name());
        p.setProperty("movingObstaclesEnabled", String.valueOf(s.movingObstaclesEnabled()));
        p.setProperty("movingObstacleCount", String.valueOf(s.movingObstacleCount()));
        p.setProperty("movingObstaclesAutoIncrement", String.valueOf(s.movingObstaclesAutoIncrement()));

        // Encode events: tick:DIR;tick:DIR;...
        p.setProperty("events", encodeEvents(data.events));

        try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            p.store(w, "Snake Replay");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to save replay: " + path, e);
        }
    }

    private static Optional<ReplayData> load(String path) {
        File file = new File(path);
        if (!file.exists()) return Optional.empty();

        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to load replay: " + path, e);
            return Optional.empty();
        }

        try {
            ReplayData d = new ReplayData();
            d.version = Integer.parseInt(p.getProperty("version", "1"));
            d.seed = Long.parseLong(p.getProperty("seed", "0"));
            d.finalScore = Integer.parseInt(p.getProperty("finalScore", "0"));

            java.util.UUID playerId;
            String playerIdValue = p.getProperty("playerId", "");
            if (playerIdValue != null && !playerIdValue.isBlank()) {
                try {
                    playerId = java.util.UUID.fromString(playerIdValue);
                } catch (IllegalArgumentException e) {
                    playerId = GameSettings.getPlayerId();
                }
            } else {
                playerId = GameSettings.getPlayerId();
            }

            SettingsSnapshot ss = new SettingsSnapshot(
                    Integer.parseInt(p.getProperty("difficultyLevel", "20")),
                    Boolean.parseBoolean(p.getProperty("obstaclesEnabled", "false")),
                    com.snakegame.mode.GameMode.valueOf(p.getProperty("currentMode", "STANDARD")),
                    Integer.parseInt(p.getProperty("selectedMapId", "1")),
                    Integer.parseInt(p.getProperty("raceThreshold", "20")),
                    Boolean.parseBoolean(p.getProperty("soundEnabled", "true")),
                    Boolean.parseBoolean(p.getProperty("musicEnabled", "true")),
                    Boolean.parseBoolean(p.getProperty("showGrid", "true")),
                    p.getProperty("playerName", "Player"),
                    playerId,
                    GameSettings.Theme.valueOf(p.getProperty("theme", "RETRO")),
                    Boolean.parseBoolean(p.getProperty("movingObstaclesEnabled", "false")),
                    Integer.parseInt(p.getProperty("movingObstacleCount", "0")),
                    Boolean.parseBoolean(p.getProperty("movingObstaclesAutoIncrement", "false")),
                    false
            );
            d.runSettingsSnapshot = ss;
            d.startMapId = Integer.parseInt(p.getProperty("startMapId", String.valueOf(ss.selectedMapId())));

            d.events = decodeEvents(p.getProperty("events", ""));

            return Optional.of(d);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Replay file corrupted: " + path, ex);
            return Optional.empty();
        }
    }

    private static String encodeEvents(List<ReplayEvent> events) {
        if (events == null || events.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (ReplayEvent ev : events) {
            if (ev == null || ev.direction == null) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(ev.tick).append(':').append(ev.direction.name());
        }
        return sb.toString();
    }

    private static List<ReplayEvent> decodeEvents(String s) {
        List<ReplayEvent> out = new ArrayList<>();
        if (s == null || s.isBlank()) return out;

        String[] items = s.split(";");
        for (String item : items) {
            if (item.isBlank()) continue;
            String[] parts = item.split(":");
            if (parts.length != 2) continue;
            long tick = Long.parseLong(parts[0].trim());
            Direction dir = Direction.valueOf(parts[1].trim());
            out.add(new ReplayEvent(tick, dir));
        }
        // sort safety
        out.sort(Comparator.comparingLong(e -> e.tick));
        return out;
    }
}
