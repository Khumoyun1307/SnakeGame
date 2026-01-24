package com.snakegame.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {
    private AppPaths() {}

    public static final Path DATA_DIR = Paths.get("data");

    public static final Path SETTINGS_FILE = DATA_DIR.resolve("settings.txt");
    public static final Path SAVE_FILE = DATA_DIR.resolve("savegame.txt");
    public static final Path PROGRESS_FILE = DATA_DIR.resolve("progress.txt");
    public static final Path REPLAY_LAST_FILE = DATA_DIR.resolve("replay_last.txt");
    public static final Path REPLAY_BEST_FILE = DATA_DIR.resolve("replay_best.txt");

    public static final Path SCORES_FILE = Paths.get("scores.txt");
}

