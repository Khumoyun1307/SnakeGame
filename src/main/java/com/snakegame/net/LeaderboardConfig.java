package com.snakegame.net;

/**
 * Configuration values for the online leaderboard client.
 *
 * <p>Contains the service base URL and HTTP timeout settings.</p>
 */
public final class LeaderboardConfig {
    private LeaderboardConfig() {}

    // Dev URL (change later to Render URL)
    public static final String BASE_URL = "https://fit-maisie-khumoyun-7d8e10bd.koyeb.app";

    // Timeouts (ms)
    public static final int CONNECT_TIMEOUT = 8000;
    public static final int REQUEST_TIMEOUT = 15000;

}
