package com.snakegame.model;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import org.junit.jupiter.api.*;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import static com.snakegame.model.AppleType.*;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    private SettingsSnapshot beforeSettings;
    private GameState gs;
    private Snake snake;
    private Apple apple;

    @BeforeEach
    void setUp() {
        beforeSettings = GameSettings.snapshot();
        GameSettings.setObstaclesEnabled(false);
        GameSettings.setMovingObstaclesEnabled(false);

        gs    = new GameState();
        snake = gs.getSnake();
        apple = gs.getApple();
    }

    @AfterEach
    void tearDown() {
        GameSettings.restore(beforeSettings);
    }

    private void placeAppleAtHead(AppleType type) {
        Set<Point> forbidden = new HashSet<>(snake.getBody());
        apple.spawnNew(type, forbidden);
        apple.setPosition(snake.getHead());
    }

    @Test
    void moveWithoutEating_doesNothing() {
        int initialScore = gs.getScore();
        int initialLen   = snake.getBody().size();
        apple.setPosition(new Point(-1, -1));

        gs.update();

        assertEquals(initialScore, gs.getScore());
        assertEquals(initialLen,   snake.getBody().size());
    }

    @Test
    void normalAppleEating_growsAndScores() {
        int initScore = gs.getScore(), initLen = snake.getBody().size();
        placeAppleAtHead(NORMAL);

        gs.update();

        assertEquals(initScore + NORMAL.getScoreValue(), gs.getScore());
        assertEquals(initLen + 1,                       snake.getBody().size());
        assertNotEquals(snake.getHead(), apple.getPosition());
    }

    @Test
    void bigAppleEating_growsByOneAndAddsFourPoints() {
        int initScore = gs.getScore(), initLen = snake.getBody().size();
        placeAppleAtHead(BIG);

        gs.update();

        assertEquals(initScore + BIG.getScoreValue(), gs.getScore());
        assertEquals(initLen + 1,                    snake.getBody().size());
    }

    @Test
    void goldenAppleActivatesDoubleScore() {
        placeAppleAtHead(GOLDEN);
        gs.update();
        assertTrue(gs.isDoubleScoreActive());
        assertEquals(GOLDEN.getScoreValue(), gs.getScore());
    }

    @Test
    void slowdownAppleActivatesSlow() {
        placeAppleAtHead(SLOWDOWN);
        gs.update();
        assertTrue(gs.isSlowed());
        assertEquals(SLOWDOWN.getScoreValue(), gs.getScore());
    }

    @Test
    void reverseAppleActivatesReverseControls() {
        placeAppleAtHead(REVERSE);
        gs.update();
        assertTrue(gs.isReversedControls());
        assertEquals(REVERSE.getScoreValue(), gs.getScore());
    }

    @Test
    void borderCollisionStopsGame() {
        gs.setDirection(Direction.LEFT);
        int steps = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE + 1;
        for (int i = 0; i < steps; i++) {
            gs.update();
            if (!gs.isRunning()) break;
        }
        assertFalse(gs.isRunning());
    }

    @Test
    void selfCollisionStopsGame() {
        Deque<Point> body = snake.getBody();
        body.clear();
        body.addLast(new Point(50, 50));  // head
        body.addLast(new Point(75, 50));  // next segment
        body.addLast(new Point(100, 50)); // tail

        apple.setPosition(new Point(-1, -1));
        gs.update();

        assertFalse(gs.isRunning(), "Game should end when snake runs into itself");
    }

    @Test
    void staticObstacleCollisionStopsGame() {
        Point next = new Point(
                snake.getHead().x + GameConfig.UNIT_SIZE,
                snake.getHead().y
        );
        gs.getObstacles().add(next);

        apple.setPosition(new Point(-1, -1));
        gs.update();

        assertFalse(gs.isRunning(), "Game should end when snake hits a static obstacle");
    }

    // —— New tests for time‐based expiration —— //

    @Test
    void appleExpiration_resetsToNormalAfterDuration() throws Exception {
        // spawn a BIG apple so it has a visibleDuration
        Set<Point> forbidden = new HashSet<>(snake.getBody());
        apple.spawnNew(BIG, forbidden);

        // backdate its spawnTime to expire it
        long dur = apple.getVisibleDurationMs();
        Field spawnField = Apple.class.getDeclaredField("spawnTime");
        spawnField.setAccessible(true);
        spawnField.setLong(apple, System.currentTimeMillis() - dur - 1);

        // prevent eating
        apple.setPosition(new Point(-1, -1));

        // one update should respawn as NORMAL
        gs.update();
        assertEquals(NORMAL, apple.getType(), "Expired apple should respawn as NORMAL");
    }

    @Test
    void goldenEffect_expiresAfterDuration() throws Exception {
        placeAppleAtHead(GOLDEN);
        gs.update();
        assertTrue(gs.isDoubleScoreActive(), "Golden effect should be active");

        // backdate its end time
        Field endField = GameState.class.getDeclaredField("doubleScoreEndTime");
        endField.setAccessible(true);
        endField.setLong(gs, System.currentTimeMillis() - 1);

        // next update should clear the effect
        apple.setPosition(new Point(-1, -1));
        gs.update();
        assertFalse(gs.isDoubleScoreActive(), "Golden effect should expire after duration");
    }

    @Test
    void slowdownEffect_expiresAfterDuration() throws Exception {
        placeAppleAtHead(SLOWDOWN);
        gs.update();
        assertTrue(gs.isSlowed(), "Slowdown effect should be active");

        Field slowEnd = GameState.class.getDeclaredField("slowEndTime");
        slowEnd.setAccessible(true);
        slowEnd.setLong(gs, System.currentTimeMillis() - 1);

        apple.setPosition(new Point(-1, -1));
        gs.update();
        assertFalse(gs.isSlowed(), "Slowdown effect should expire after duration");
    }

    @Test
    void reverseEffect_expiresAfterDuration() throws Exception {
        placeAppleAtHead(REVERSE);
        gs.update();
        assertTrue(gs.isReversedControls(), "Reverse effect should be active");

        Field revEnd = GameState.class.getDeclaredField("reverseEndTime");
        revEnd.setAccessible(true);
        revEnd.setLong(gs, System.currentTimeMillis() - 1);

        apple.setPosition(new Point(-1, -1));
        gs.update();
        assertFalse(gs.isReversedControls(), "Reverse effect should expire after duration");
    }
}
