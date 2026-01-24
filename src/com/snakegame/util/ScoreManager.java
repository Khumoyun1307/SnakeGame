package com.snakegame.util;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.net.LeaderboardClient;
import com.snakegame.model.GameState;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScoreManager {

    private static String scoreFilePath = "scores.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> scores = new ArrayList<>();
    private static final LeaderboardClient leaderboardClient = new LeaderboardClient();

    // Load scores from file once on class load
    static {
        loadFromFile();
    }

    private static void loadFromFile() {
        scores.clear();
        try {
            Path path = Paths.get(scoreFilePath);
            if (Files.exists(path)) {
                scores.addAll(Files.readAllLines(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setScoreFilePath(String path) {
        scoreFilePath = path;
        loadFromFile();
    }

    public static void clearScores() {
        scores.clear();
    }

    public static void addScore(int score) {
        String entry = FORMATTER.format(LocalDateTime.now()) + " - Score: " + score;
        scores.add(entry);
        appendToFile(entry);
    }

    public static void recordFinishedRun(GameState gameState) {
        if (gameState == null) return;

        int score = gameState.getScore();
        if (score <= 0) return;

        // Always save locally
        addScore(score);

        long timeSurvivedMs = gameState.getTick() * (long) gameState.getTickMs();

        String mode = GameSettings.getCurrentMode().name();

        int mapIdToSubmit;
        if (mode.equals(GameMode.MAP_SELECT.name())) {
            mapIdToSubmit = GameSettings.getSelectedMapId(); // 1..10
        } else if (mode.equals(GameMode.RACE.name())) {
            // FIX: RACE should submit the current/furthest map reached
            mapIdToSubmit = GameSettings.getSelectedMapId();
        } else {
            mapIdToSubmit = 0; // STANDARD
        }

        leaderboardClient.submitScoreAsync(
                GameSettings.getPlayerId(),
                GameSettings.getPlayerName(),
                score,
                mapIdToSubmit,
                mode,
                GameSettings.getDifficulty().name(),
                timeSurvivedMs,
                "1.0.0"
        ).exceptionally(ex -> {
            System.out.println("Leaderboard submit failed: " + ex.getMessage());
            return null;
        });

    }

    private static void appendToFile(String entry) {
        try {
            Files.write(
                    Paths.get(scoreFilePath),
                    Collections.singletonList(entry),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getScores() {
        return new ArrayList<>(scores);
    }

    public static void saveAllToFile() {
        try {
            Files.write(
                    Paths.get(scoreFilePath),
                    scores,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getHighScore() {
        return scores.stream()
                .mapToInt(line -> {
                    try {
                        String[] parts = line.split("Score: ");
                        return Integer.parseInt(parts[1].trim());
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
    }

}
