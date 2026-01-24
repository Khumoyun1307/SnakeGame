package com.snakegame.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.snakegame.ai.AiMode;
import com.snakegame.mode.GameMode;
import com.snakegame.mode.MapManager;
import com.snakegame.model.GameConfig;
import com.snakegame.util.AppPaths;


public class GameSettingsManager {
    private static final String FILE_PATH = AppPaths.SETTINGS_FILE.toString();
    private static final Logger log = Logger.getLogger(GameSettingsManager.class.getName());

    public static void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            GameSettings.ensurePlayerId();
            GameSettingsManager.save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8))  {
            Properties props = new Properties();
            props.load(reader);

            GameSettings.withAutosaveSuppressed(() -> {
                String playerIdValue = props.getProperty("playerId", "");
                if (playerIdValue != null && !playerIdValue.isBlank()) {
                    try {
                        GameSettings.setPlayerId(java.util.UUID.fromString(playerIdValue));
                    } catch (IllegalArgumentException e) {
                        GameSettings.ensurePlayerId();
                    }
                } else {
                    GameSettings.ensurePlayerId();
                }

                GameSettings.setDifficultyLevel(
                        Integer.parseInt(props.getProperty("difficultyLevel", "20"))
                );
                GameSettings.setObstaclesEnabled(
                        Boolean.parseBoolean(props.getProperty("obstaclesEnabled", "false"))
                );

                GameSettings.setCurrentMode(
                        GameMode.valueOf(props.getProperty("currentMode", "STANDARD"))
                );
                GameSettings.setSelectedMapId(
                        Integer.parseInt(props.getProperty("selectedMapId", "1"))
                );
                GameSettings.setRaceThreshold(
                        Integer.parseInt(props.getProperty("raceThreshold", "20"))
                );

                // Load new settings
                GameSettings.setSoundEnabled(
                        Boolean.parseBoolean(props.getProperty("soundEnabled", "true"))
                );
                GameSettings.setMusicEnabled(
                        Boolean.parseBoolean(props.getProperty("musicEnabled", "true"))
                );
                GameSettings.setShowGrid(
                        Boolean.parseBoolean(props.getProperty("showGrid", "true"))
                );
                GameSettings.setPlayerName(
                        props.getProperty("playerName", "Player")
                );

                try {
                    GameSettings.setSelectedTheme(
                            GameSettings.Theme.valueOf(props.getProperty("theme", "RETRO"))
                    );
                } catch (IllegalArgumentException e) {
                    GameSettings.setSelectedTheme(GameSettings.Theme.RETRO);
                }

                GameSettings.setMovingObstaclesEnabled(
                        Boolean.parseBoolean(props.getProperty("movingObstaclesEnabled", "false"))
                );
                GameSettings.setMovingObstacleCount(
                        Integer.parseInt(props.getProperty("movingObstacleCount",
                                String.valueOf(GameConfig.DEFAULT_MOVING_OBSTACLE_COUNT)))
                );
                GameSettings.setMovingObstaclesAutoIncrement(
                        Boolean.parseBoolean(props.getProperty("movingObstaclesAutoIncrement", "false"))
                );

                try {
                    GameSettings.setAiMode(AiMode.valueOf(props.getProperty("aiMode", "SAFE")));
                } catch (IllegalArgumentException ex) {
                    GameSettings.setAiMode(AiMode.SAFE);
                }

                // Developer mode is session-only. If settings point to a developer-only map while dev mode is locked,
                // sanitize to a normal playable configuration so Continue/save flow behaves as expected.
                if (!GameSettings.isDeveloperModeEnabled()) {
                    int mapId = GameSettings.getSelectedMapId();
                    if (mapId > 0 && !MapManager.isPackagedMapId(mapId)) {
                        GameSettings.setSelectedMapId(1);
                        if (GameSettings.getCurrentMode() == GameMode.MAP_SELECT) {
                            GameSettings.setCurrentMode(GameMode.STANDARD);
                        }
                    }
                }
            });

            // Persist once (normalizes missing/invalid values like UUID).
            GameSettingsManager.save();

        } catch (IOException | NumberFormatException e) {
            log.log(Level.SEVERE, "Failed to load settings", e);
        }
    }

    public static void save() {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            log.log(Level.SEVERE, "Failed to create settings directory: " + parent.getPath());
            return; // or throw if you’d rather abort hard
        }

        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))  {
            Properties props = new Properties();
            props.setProperty("difficultyLevel", String.valueOf(GameSettings.getDifficultyLevel()));
            props.setProperty("obstaclesEnabled", String.valueOf(GameSettings.isObstaclesEnabled()));

            props.setProperty("currentMode", GameSettings.getCurrentMode().name());
            props.setProperty("selectedMapId", String.valueOf(GameSettings.getSelectedMapId()));
            props.setProperty("raceThreshold", String.valueOf(GameSettings.getRaceThreshold()));

            // Save new settings
            props.setProperty("soundEnabled", String.valueOf(GameSettings.isSoundEnabled()));
            props.setProperty("musicEnabled", String.valueOf(GameSettings.isMusicEnabled()));
            props.setProperty("showGrid", String.valueOf(GameSettings.isShowGrid()));
            props.setProperty("playerName", GameSettings.getPlayerName());
            props.setProperty("playerId", String.valueOf(GameSettings.getPlayerId()));
            props.setProperty("theme", GameSettings.getSelectedTheme().name());
            props.setProperty("movingObstaclesEnabled",
                    String.valueOf(GameSettings.isMovingObstaclesEnabled()));
            props.setProperty("movingObstacleCount",
                    String.valueOf(GameSettings.getMovingObstacleCount()));
            props.setProperty("movingObstaclesAutoIncrement",
                    String.valueOf(GameSettings.isMovingObstaclesAutoIncrement()));
            props.setProperty("aiMode", GameSettings.getAiMode().name());
            props.store(writer, "Game Settings");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to save settings", e);
        }
    }
}
