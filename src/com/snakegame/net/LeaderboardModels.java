package com.snakegame.net;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LeaderboardModels {

    private LeaderboardModels() {}

    public record SessionResponse(
            UUID sessionId,
            String sessionToken,
            Instant expiresAt
    ) {}

    public record SubmitScoreResponse(
            UUID scoreId
    ) {}

    public record LeaderboardResponse(
            int mapId,
            String mode,
            String difficulty,
            List<LeaderboardEntry> entries
    ) {}

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
