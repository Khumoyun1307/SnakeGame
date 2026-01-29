package com.snakegame.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Centralized file system paths used for local persistence.
 *
 * <p>Paths are stored under a per-user writable application directory to support installed builds
 * (for example Windows installs under {@code Program Files/} where normal users cannot write).</p>
 */
public final class AppPaths {
    private AppPaths() {}

    private static final String APP_DIR_NAME = "SnakeGame";
    private static final String APP_DIR_OVERRIDE_PROPERTY = "snakegame.appDir";

    /** Root directory for all per-user persisted files. */
    public static final Path APP_DIR = resolveAppDir();

    public static final Path DATA_DIR = APP_DIR.resolve("data");

    public static final Path SETTINGS_FILE = DATA_DIR.resolve("settings.txt");
    public static final Path SAVE_FILE = DATA_DIR.resolve("savegame.txt");
    public static final Path PROGRESS_FILE = DATA_DIR.resolve("progress.txt");
    public static final Path REPLAY_LAST_FILE = DATA_DIR.resolve("replay_last.txt");
    public static final Path REPLAY_BEST_FILE = DATA_DIR.resolve("replay_best.txt");

    public static final Path SCORES_FILE = APP_DIR.resolve("scores.txt");

    private static Path resolveAppDir() {
        String override = System.getProperty(APP_DIR_OVERRIDE_PROPERTY);
        if (override != null && !override.isBlank()) {
            try {
                return Paths.get(override).toAbsolutePath().normalize();
            } catch (Exception ignored) {
                // Fall through to OS-specific defaults.
            }
        }

        String os = System.getProperty("os.name", "");
        String home = System.getProperty("user.home", "");
        String osLower = os.toLowerCase(Locale.ROOT);

        try {
            if (osLower.contains("win")) {
                String localAppData = System.getenv("LOCALAPPDATA");
                if (localAppData != null && !localAppData.isBlank()) {
                    return Paths.get(localAppData, APP_DIR_NAME);
                }

                String appData = System.getenv("APPDATA");
                if (appData != null && !appData.isBlank()) {
                    return Paths.get(appData, APP_DIR_NAME);
                }

                if (!home.isBlank()) {
                    return Paths.get(home, "AppData", "Local", APP_DIR_NAME);
                }
            } else if (osLower.contains("mac")) {
                if (!home.isBlank()) {
                    return Paths.get(home, "Library", "Application Support", APP_DIR_NAME);
                }
            } else {
                String xdgDataHome = System.getenv("XDG_DATA_HOME");
                if (xdgDataHome != null && !xdgDataHome.isBlank()) {
                    return Paths.get(xdgDataHome, APP_DIR_NAME);
                }

                if (!home.isBlank()) {
                    return Paths.get(home, ".local", "share", APP_DIR_NAME);
                }
            }
        } catch (Exception ignored) {
            // Fall through to a safe default.
        }

        String userDir = System.getProperty("user.dir", "");
        if (!userDir.isBlank()) {
            return Paths.get(userDir, APP_DIR_NAME);
        }

        return Paths.get(APP_DIR_NAME);
    }
}
