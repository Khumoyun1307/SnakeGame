package com.snakegame.build;

import com.snakegame.testutil.SnakeTestBase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies build-time resource exclusions (Maven copies filtered resources to target/classes).
 */
class ResourcesPackagingTest extends SnakeTestBase {

    @Test
    void mapsResources_doNotIncludeDeveloperMapFiles() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("snakegame.test.resourcesFiltered"));

        URL url = ResourcesPackagingTest.class.getResource("/maps");
        assertNotNull(url, "maps resource directory should exist");
        Assumptions.assumeTrue("file".equalsIgnoreCase(url.getProtocol()), "Expected file-based resources for this check");

        Path mapsDir = Paths.get(url.toURI());
        List<String> names;
        try (Stream<Path> s = Files.list(mapsDir)) {
            names = s.map(p -> p.getFileName().toString()).sorted().toList();
        }

        List<String> developer = names.stream().filter(n -> n.endsWith("_developer.txt")).toList();
        assertTrue(developer.isEmpty(), "Developer maps should not be packaged: " + developer);
    }
}

