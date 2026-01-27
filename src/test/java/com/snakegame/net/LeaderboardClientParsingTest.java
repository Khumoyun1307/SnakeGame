package com.snakegame.net;

import com.snakegame.net.LeaderboardModels.LeaderboardResponse;
import com.snakegame.testutil.Reflect;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link com.snakegame.net.LeaderboardClient} JSON parsing.
 */
class LeaderboardClientParsingTest {

    @Test
    void parseSession_extractsIdAndToken() {
        LeaderboardClient client = new LeaderboardClient();
        UUID id = UUID.randomUUID();
        String json = "{\"sessionId\":\"" + id + "\",\"sessionToken\":\"abc123\"}";

        Reflect.call(client, "parseSession", new Class<?>[]{String.class}, json);

        assertEquals(id, Reflect.getField(client, "sessionId"));
        assertEquals("abc123", Reflect.getField(client, "sessionToken"));
    }

    @Test
    void parseLeaderboard_parsesEntriesAndUnescapesStrings() {
        LeaderboardClient client = new LeaderboardClient();

        String json = """
                {
                  "mapId": 2,
                  "mode": "MAP_SELECT",
                  "difficulty": "ANY",
                    "entries": [
                      {
                        "rank": 1,
                      "playerName": "Bob \\\"The Snake\\\"",
                        "mapId": 2,
                        "difficulty": "HARD",
                        "score": 99,
                        "timeSurvivedMs": 12345,
                      "createdAt": "2024-01-01T00:00:00Z"
                    }
                  ]
                }
                """;

        LeaderboardResponse resp = Reflect.call(client, "parseLeaderboard", new Class<?>[]{String.class}, json);
        assertEquals(2, resp.mapId());
        assertEquals("MAP_SELECT", resp.mode());
        assertEquals("ANY", resp.difficulty());
        assertEquals(1, resp.entries().size());
        assertEquals("Bob \"The Snake\"", resp.entries().get(0).playerName());
        assertEquals("HARD", resp.entries().get(0).difficulty());
        assertEquals(99, resp.entries().get(0).score());
    }
}
