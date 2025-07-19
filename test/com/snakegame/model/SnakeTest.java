package com.snakegame.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class SnakeTest {
    private Snake snake;

    @BeforeEach
    void setUp() {
        // Start at (50,50), length=3, facing RIGHT
        snake = new Snake(new Point(50, 50), 3, Direction.RIGHT);
    }

    @Test
    void initialBody_hasCorrectLengthAndPositions() {
        Deque<Point> body = snake.getBody();
        assertEquals(3, body.size(), "Initial length should be 3");

        // Expect head at (50,50), then (25,50), then (0,50)
        Point[] expected = {
                new Point(50, 50),
                new Point(25, 50),
                new Point(0, 50)
        };
        assertArrayEquals(expected, body.toArray(), "Body coords should decrement by UNIT_SIZE");
    }

    @Test
    void moveWithoutGrow_movesHeadAndDropsTail() {
        snake.move(false);

        // New head should be at (75,50)
        assertEquals(new Point(75, 50), snake.getHead());
        // Length stays 3
        assertEquals(3, snake.getBody().size());
        // Tail should no longer contain the old tail (0,50)
        assertFalse(snake.getBody().contains(new Point(0, 50)));
    }

    @Test
    void moveWithGrow_movesHeadAndKeepsTail() {
        snake.move(true);

        // Head moves
        assertEquals(new Point(75, 50), snake.getHead());
        // Length grows to 4
        assertEquals(4, snake.getBody().size());
        // Tail still contains the old tail
        assertTrue(snake.getBody().contains(new Point(0, 50)));
    }

    @Test
    void setDirection_allows90DegreeTurns() {
        snake.setDirection(Direction.UP);
        assertEquals(Direction.UP, snake.getDirection());

        snake.setDirection(Direction.LEFT);
        assertEquals(Direction.LEFT, snake.getDirection());
    }

    @Test
    void setDirection_ignores180DegreeReversal() {
        // Currently RIGHT → cannot go LEFT
        snake.setDirection(Direction.LEFT);
        assertEquals(Direction.RIGHT, snake.getDirection(),
                "Should ignore direct opposite");
    }

    @Test
    void isSelfColliding_detectsWhenHeadOverlapsBody() {
        // Simulate a collision: manually craft a body
        Deque<Point> body = snake.getBody();
        // Make head equal to the second segment
        Point second = body.stream().skip(1).findFirst().get();
        body.addFirst(new Point(second));
        assertTrue(snake.isSelfColliding());
    }
}
