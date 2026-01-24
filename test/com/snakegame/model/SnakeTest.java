package com.snakegame.model;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Snake}.
 */
class SnakeTest {

    @Test
    void constructor_buildsBodyToTheLeftWithUnitSizeSpacing() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        List<Point> body = new ArrayList<>(snake.getBody());

        assertEquals(3, body.size());
        assertEquals(new Point(100, 100), body.get(0));
        assertEquals(new Point(75, 100), body.get(1));
        assertEquals(new Point(50, 100), body.get(2));
    }

    @Test
    void move_withoutGrow_keepsLengthAndAdvancesHead() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.move(false);

        Deque<Point> body = snake.getBody();
        assertEquals(3, body.size());
        assertEquals(new Point(125, 100), snake.getHead());
        assertEquals(new Point(100, 100), body.toArray(new Point[0])[1]);
    }

    @Test
    void move_withGrow_increasesLength() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.move(true);

        assertEquals(4, snake.getBody().size());
        assertEquals(new Point(125, 100), snake.getHead());
        assertEquals(new Point(50, 100), snake.getBody().peekLast());
    }

    @Test
    void setDirection_rejectsOppositeDirection() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.setDirection(Direction.LEFT);
        assertEquals(Direction.RIGHT, snake.getDirection());

        snake.setDirection(Direction.UP);
        assertEquals(Direction.UP, snake.getDirection());
    }

    @Test
    void isSelfColliding_detectsCollision() {
        List<Point> body = List.of(
                new Point(0, 0),
                new Point(25, 0),
                new Point(0, 0)
        );
        Snake snake = Snake.fromBody(body, Direction.RIGHT);
        assertTrue(snake.isSelfColliding());
    }

    @Test
    void fromBody_copiesPointsDefensively() {
        List<Point> src = new ArrayList<>();
        src.add(new Point(0, 0));
        src.add(new Point(25, 0));

        Snake snake = Snake.fromBody(src, Direction.RIGHT);
        src.get(0).x = 999;

        assertEquals(new Point(0, 0), snake.getHead());
    }
}
