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

            // Load new settings
            GameSettings.setCurrentMode(
                    GameMode.valueOf(props.getProperty("currentMode", "STANDARD"))
            );
            GameSettings.setSelectedMapId(
                    Integer.parseInt(props.getProperty("selectedMapId", "1"))
            );
            GameSettings.setRaceThreshold(
                    Integer.parseInt(props.getProperty("raceThreshold", "20"))
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

            // Save new settings
            props.setProperty("currentMode", GameSettings.getCurrentMode().name());
            props.setProperty("selectedMapId", String.valueOf(GameSettings.getSelectedMapId()));
            props.setProperty("raceThreshold", String.valueOf(GameSettings.getRaceThreshold()));

            props.store(writer, "Game Settings");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
}