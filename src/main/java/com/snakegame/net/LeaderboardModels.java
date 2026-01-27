package com.snakegame.net;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data models used by the online leaderboard API client.
 *
 * <p>Records are simple DTOs for parsed JSON responses.</p>
 */
public final class LeaderboardModels {

    private LeaderboardModels() {}

    /**
     * Response returned when a session is created with the leaderboard service.
     */
    public record SessionResponse(
            UUID sessionId,
            String sessionToken,
            Instant expiresAt
    ) {}

    /**
     * Response returned after successfully submitting a score.
     */
    public record SubmitScoreResponse(
            UUID scoreId
    ) {}

    /**
     * Leaderboard response for a given filter set.
     */
    public record LeaderboardResponse(
            int mapId,
            String mode,
            String difficulty,
            List<LeaderboardEntry> entries
    ) {}

    /**
     * A single leaderboard entry.
     */
    public record LeaderboardEntry(
            int rank,
            String playerName,
            int mapId,
            String difficulty,
            int score,
            long timeSurvivedMs,
            Instant createdAt
    ) {}
}
