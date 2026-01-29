package com.snakegame.util;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.model.*;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists and loads {@link GameSnapshot} instances for the save/continue feature.
 *
 * <p>Uses a simple {@link Properties}-based text format stored under {@link AppPaths#SAVE_FILE}.</p>
 */
public class GameSaveManager {
    private static String filePath = AppPaths.SAVE_FILE.toString();
    private static final Logger log = Logger.getLogger(GameSaveManager.class.getName());

    private static Path savePath() {
        return Paths.get(filePath);
    }

    private static Path resumePath(Path savePath) {
        return savePath.resolveSibling(savePath.getFileName().toString() + ".resume");
    }

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Handle for the "Continue" flow: moves the save aside, loads from the moved file, and supports commit/rollback.
     *
     * <p>Call {@link #commit()} after gameplay successfully starts. If not committed, {@link #close()} restores the
     * save file.</p>
     */
    public static final class ContinueSession implements AutoCloseable {
        private final Path saveFile;
        private final Path resumeFile;
        private final GameSnapshot snapshot;
        private boolean committed;

        private ContinueSession(Path saveFile, Path resumeFile, GameSnapshot snapshot) {
            this.saveFile = saveFile;
            this.resumeFile = resumeFile;
            this.snapshot = snapshot;
        }

        public GameSnapshot snapshot() {
            return snapshot;
        }

        public void commit() {
            if (committed) return;
            committed = true;
            try {
                Files.deleteIfExists(resumeFile);
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to delete resume save file: " + resumeFile, e);
            }
        }

        @Override
        public void close() {
            if (committed) return;
            if (!Files.exists(resumeFile) || Files.exists(saveFile)) return;
            try {
                move(resumeFile, saveFile);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to restore save after failed continue", e);
            }
        }
    }

    /**
     * Overrides the save file path (primarily for tests). If null/blank, resets to the default.
     */
    public static void setFilePath(String path) {
        if (path == null || path.isBlank()) {
            filePath = AppPaths.SAVE_FILE.toString();
        } else {
            filePath = path;
        }
    }

    /**
     * Returns whether a saved game exists on disk.
     *
     * @return {@code true} if the save file exists
     */
    public static boolean hasSave() {
        Path save = savePath();
        if (Files.exists(save)) return true;
        return Files.exists(resumePath(save));
    }

    /**
     * Deletes the saved game file if it exists.
     */
    public static void clearSave() {
        Path save = savePath();
        Path resume = resumePath(save);
        try {
            Files.deleteIfExists(save);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to delete save file: " + save, e);
        }
        try {
            Files.deleteIfExists(resume);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to delete resume save file: " + resume, e);
        }
    }

    /**
     * Writes a snapshot to disk as the current saved game.
     *
     * @param s snapshot to save
     */
    public static void save(GameSnapshot s) {
        if (s == null) return;

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.severe("Failed to create save directory: " + parent.getPath());
            return;
        }

        Properties p = new Properties();

        // meta
        p.setProperty("savedAtMillis", String.valueOf(System.currentTimeMillis()));

        // settings snapshot (flattened)
        SettingsSnapshot ss = s.settingsSnapshot;
        if (ss != null) {
            p.setProperty("difficultyLevel", String.valueOf(ss.difficultyLevel()));
            p.setProperty("obstaclesEnabled", String.valueOf(ss.obstaclesEnabled()));
            p.setProperty("currentMode", (s.mode != null ? s.mode.name() : ss.currentMode().name()));
            p.setProperty("selectedMapId", String.valueOf(s.selectedMapId));
            p.setProperty("raceThreshold", String.valueOf(ss.raceThreshold()));
            p.setProperty("soundEnabled", String.valueOf(ss.soundEnabled()));
            p.setProperty("musicEnabled", String.valueOf(ss.musicEnabled()));
            p.setProperty("showGrid", String.valueOf(ss.showGrid()));
            p.setProperty("playerName", ss.playerName() == null ? "Player" : ss.playerName());
            p.setProperty("playerId", ss.playerId() == null ? "" : ss.playerId().toString());
            p.setProperty("theme", ss.selectedTheme().name());
            p.setProperty("movingObstaclesEnabled", String.valueOf(ss.movingObstaclesEnabled()));
            p.setProperty("movingObstacleCount", String.valueOf(ss.movingObstacleCount()));
            p.setProperty("movingObstaclesAutoIncrement", String.valueOf(ss.movingObstaclesAutoIncrement()));
        }

        // gameplay
        p.setProperty("score", String.valueOf(s.score));
        p.setProperty("applesEaten", String.valueOf(s.applesEaten));
        p.setProperty("direction", (s.direction == null ? "RIGHT" : s.direction.name()));
        p.setProperty("snakeBody", encodePoints(s.snakeBody == null ? List.of() : s.snakeBody));

        // apple
        p.setProperty("applePos", encodePoint(s.applePos == null ? new Point(0, 0) : s.applePos));
        p.setProperty("appleType", (s.appleType == null ? "NORMAL" : s.appleType.name()));
        p.setProperty("appleSpawnTime", String.valueOf(s.appleSpawnTime));
        p.setProperty("appleVisibleDurationMs", String.valueOf(s.appleVisibleDurationMs));

        // effects
        p.setProperty("doubleScoreActive", String.valueOf(s.doubleScoreActive));
        p.setProperty("doubleScoreEndTime", String.valueOf(s.doubleScoreEndTime));
        p.setProperty("slowed", String.valueOf(s.slowed));
        p.setProperty("slowEndTime", String.valueOf(s.slowEndTime));
        p.setProperty("reversedControls", String.valueOf(s.reversedControls));
        p.setProperty("reverseEndTime", String.valueOf(s.reverseEndTime));

        // obstacles
        p.setProperty("obstacles", encodePoints(s.obstacles == null ? List.of() : s.obstacles));

        // moving obstacles
        List<MovingObstacleSnapshot> mosList = (s.movingObstacles == null) ? List.of() : s.movingObstacles;
        p.setProperty("mo.count", String.valueOf(mosList.size()));
        for (int i = 0; i < mosList.size(); i++) {
            MovingObstacleSnapshot mos = mosList.get(i);
            p.setProperty("mo." + i + ".segments", encodePoints(mos.segments == null ? List.of() : mos.segments));
            p.setProperty("mo." + i + ".dx", String.valueOf(mos.dx));
            p.setProperty("mo." + i + ".dy", String.valueOf(mos.dy));
        }

        try (Writer w = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            p.store(w, "Snake Game Save");

            // Clear any stale resume file after writing a fresh save.
            try {
                Files.deleteIfExists(resumePath(savePath()));
            } catch (IOException ignored) {
                // ignore
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to save game", e);
        }
    }

    /**
     * Starts a "Continue" operation by moving the save file to a temporary resume location and loading from it.
     *
     * <p>The caller must either {@link ContinueSession#commit()} once gameplay successfully starts, or allow the
     * session to close without committing to restore the save.</p>
     *
     * @return optional continue session
     */
    public static Optional<ContinueSession> beginContinue() {
        Path save = savePath();
        Path resume = resumePath(save);

        // If a previous continue crashed after moving the file, restore it first so Continue remains available.
        if (!Files.exists(save) && Files.exists(resume)) {
            try {
                move(resume, save);
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to restore prior resume save: " + resume, e);
            }
        }

        if (!Files.exists(save)) return Optional.empty();

        try {
            move(save, resume);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to begin continue (move save to resume): " + save + " -> " + resume, e);
            return Optional.empty();
        }

        Optional<GameSnapshot> snapOpt = load();
        if (snapOpt.isEmpty()) {
            if (!Files.exists(save) && Files.exists(resume)) {
                try {
                    move(resume, save);
                } catch (IOException ignored) {
                    // ignore
                }
            }
            return Optional.empty();
        }

        return Optional.of(new ContinueSession(save, resume, snapOpt.get()));
    }

    /**
     * Loads the saved game snapshot from disk.
     *
     * @return optional snapshot (empty if missing or unreadable)
     */
    public static Optional<GameSnapshot> load() {
        Path save = savePath();
        Path toLoad = Files.exists(save) ? save : resumePath(save);
        File file = toLoad.toFile();
        if (!file.exists()) return Optional.empty();

        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to load save", e);
            return Optional.empty();
        }

        try {
            GameSnapshot s = new GameSnapshot();

            s.savedAtMillis = Long.parseLong(p.getProperty("savedAtMillis", "0"));

            // restore SettingsSnapshot
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
            s.settingsSnapshot = ss;
            s.mode = ss.currentMode();
            s.selectedMapId = ss.selectedMapId();

            // gameplay
            s.score = Integer.parseInt(p.getProperty("score", "0"));
            s.applesEaten = Integer.parseInt(p.getProperty("applesEaten", "0"));
            s.direction = Direction.valueOf(p.getProperty("direction", "RIGHT"));
            s.snakeBody = decodePoints(p.getProperty("snakeBody", ""));

            // apple
            s.applePos = decodePoint(p.getProperty("applePos", "0,0"));
            s.appleType = AppleType.valueOf(p.getProperty("appleType", "NORMAL"));
            s.appleSpawnTime = Long.parseLong(p.getProperty("appleSpawnTime", "0"));
            s.appleVisibleDurationMs = Long.parseLong(p.getProperty("appleVisibleDurationMs", "0"));

            // effects
            s.doubleScoreActive = Boolean.parseBoolean(p.getProperty("doubleScoreActive", "false"));
            s.doubleScoreEndTime = Long.parseLong(p.getProperty("doubleScoreEndTime", "0"));
            s.slowed = Boolean.parseBoolean(p.getProperty("slowed", "false"));
            s.slowEndTime = Long.parseLong(p.getProperty("slowEndTime", "0"));
            s.reversedControls = Boolean.parseBoolean(p.getProperty("reversedControls", "false"));
            s.reverseEndTime = Long.parseLong(p.getProperty("reverseEndTime", "0"));

            // obstacles
            s.obstacles = decodePoints(p.getProperty("obstacles", ""));

            // moving obstacles
            int moCount = Integer.parseInt(p.getProperty("mo.count", "0"));
            s.movingObstacles = new ArrayList<>();
            for (int i = 0; i < moCount; i++) {
                List<Point> segs = decodePoints(p.getProperty("mo." + i + ".segments", ""));
                int dx = Integer.parseInt(p.getProperty("mo." + i + ".dx", "0"));
                int dy = Integer.parseInt(p.getProperty("mo." + i + ".dy", "0"));
                s.movingObstacles.add(new MovingObstacleSnapshot(segs, dx, dy));
            }

            return Optional.of(s);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Save file corrupted, clearing: " + toLoad, ex);
            clearSave();
            return Optional.empty();
        }
    }

    /**
     * Convenience helper to read the saved score without restoring the full snapshot in memory.
     *
     * @return saved score if a valid save exists
     */
    public static OptionalInt getSavedScore() {
        Optional<GameSnapshot> s = load();
        return s.isPresent() ? OptionalInt.of(s.get().score) : OptionalInt.empty();
    }

    // ---- encoding helpers ----

    private static String encodePoint(Point p) {
        return p.x + "," + p.y;
    }

    private static String encodePoints(List<Point> pts) {
        if (pts == null || pts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Point p : pts) {
            if (sb.length() > 0) sb.append(";");
            sb.append(p.x).append(",").append(p.y);
        }
        return sb.toString();
    }

    private static Point decodePoint(String s) {
        String[] parts = s.split(",");
        return new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }

    private static List<Point> decodePoints(String s) {
        List<Point> out = new ArrayList<>();
        if (s == null || s.isBlank()) return out;
        String[] pairs = s.split(";");
        for (String pair : pairs) {
            if (pair.isBlank()) continue;
            out.add(decodePoint(pair));
        }
        return out;
    }
}
