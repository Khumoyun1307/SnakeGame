package com.snakegame.testutil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience wrapper for backing up multiple files and restoring them in reverse order.
 */
public final class FileBackups implements AutoCloseable {
    private final List<FileBackup> backups = new ArrayList<>();

    public FileBackups(Path... paths) {
        for (Path p : paths) backups.add(new FileBackup(p));
    }

    @Override
    public void close() {
        RuntimeException first = null;
        for (int i = backups.size() - 1; i >= 0; i--) {
            try {
                backups.get(i).close();
            } catch (RuntimeException e) {
                if (first == null) first = e;
            }
        }
        if (first != null) throw first;
    }
}

