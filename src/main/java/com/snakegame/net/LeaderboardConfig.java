package com.snakegame.net;

/**
 * Configuration values for the online leaderboard client.
 *
 * <p>Contains the service base URL and HTTP timeout settings.</p>
 */
public final class LeaderboardConfig {
    private LeaderboardConfig() {}

    private static final String DEFAULT_BASE_URL = "https://fit-maisie-khumoyun-7d8e10bd.koyeb.app";

    /**
     * Base URL for the leaderboard API.
     *
     * <p>Override with {@code -Dsnakegame.leaderboard.baseUrl=...} or {@code SNAKEGAME_LEADERBOARD_BASE_URL}.</p>
     */
    public static final String BASE_URL = resolveBaseUrl();

    // Timeouts (ms)
    public static final int CONNECT_TIMEOUT = 8000;
    public static final int REQUEST_TIMEOUT = 15000;

    private static String resolveBaseUrl() {
        String prop = System.getProperty("snakegame.leaderboard.baseUrl");
        if (prop != null && !prop.isBlank()) {
            return trimTrailingSlash(prop.trim());
        }

        String env = System.getenv("SNAKEGAME_LEADERBOARD_BASE_URL");
        if (env != null && !env.isBlank()) {
            return trimTrailingSlash(env.trim());
        }

        return DEFAULT_BASE_URL;
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

}
