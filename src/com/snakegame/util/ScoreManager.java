package com.snakegame.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScoreManager {

    private static String scoreFilePath = "scores.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> scores = new ArrayList<>();

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

    /**
     * Sets a custom file path for testing or alternate storage,
     * and reloads scores from that file.
     */
    public static void setScoreFilePath(String path) {
        scoreFilePath = path;
        loadFromFile();
    }

    /**
     * Clears in-memory scores. For testing only.
     */
    public static void clearScores() {
        scores.clear();
    }


    // Adds a new score to memory and appends it to the file
    public static void addScore(int score) {
        String entry = FORMATTER.format(LocalDateTime.now()) + " - Score: " + score;
        scores.add(entry);
        appendToFile(entry);
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

    // Returns a copy of the scores list to avoid external mutation
    public static List<String> getScores() {
        return new ArrayList<>(scores);
    }

    // Flushes the current memory list to file — full rewrite
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
