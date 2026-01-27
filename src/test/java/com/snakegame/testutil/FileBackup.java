package com.snakegame.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Best-effort backup/restore for a single file, to avoid tests mutating a developer's local data.
 */
public final class FileBackup implements AutoCloseable {
    private final Path path;
    private final boolean existed;
    private final byte[] bytes;

    public FileBackup(Path path) {
        this.path = path;
        this.existed = Files.exists(path);
        if (existed) {
            try {
                this.bytes = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read backup file: " + path, e);
            }
        } else {
            this.bytes = null;
        }
    }

    @Override
    public void close() {
        try {
            if (existed) {
                Files.createDirectories(path.getParent() == null ? Path.of(".") : path.getParent());
                Files.write(path, bytes);
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore backup file: " + path, e);
        }
    }
}

