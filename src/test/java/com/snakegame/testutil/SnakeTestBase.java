package com.snakegame.testutil;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class that ensures tests never use real per-user persistence locations.
 *
 * <p>This sets {@code -Dsnakegame.appDir} when not already provided (e.g., via Maven Surefire),
 * so tests run from an IDE remain isolated from AppData/home directories.</p>
 */
public abstract class SnakeTestBase {

    static {
        ensureIsolatedAppDir();
    }

    private static void ensureIsolatedAppDir() {
        String existing = System.getProperty("snakegame.appDir");
        if (existing != null && !existing.isBlank()) {
            return;
        }

        Path cwd = Path.of(System.getProperty("user.dir", "")).toAbsolutePath().normalize();

        // Prefer locating the Snake Game Full module root when tests are run from the workspace root.
        Path moduleRoot = cwd;
        Path snakeModule = cwd.resolve("Snake Game Full");
        if (Files.exists(snakeModule.resolve("pom.xml"))) {
            moduleRoot = snakeModule;
        }

        Path testAppDir = moduleRoot.resolve("target").resolve("test-work").resolve("SnakeGameTest");
        System.setProperty("snakegame.appDir", testAppDir.toString());
    }
}

