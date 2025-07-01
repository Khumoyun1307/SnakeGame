package com.snakegame.config;

import java.io.*;
import java.util.Properties;
import com.snakegame.mode.GameMode;

public class GameSettingsManager {
    private static final String FILE_PATH = "data/settings.txt";

    public static void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            Properties props = new Properties();
            props.load(reader);

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

        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
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

            props.store(writer, "Game Settings");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
}