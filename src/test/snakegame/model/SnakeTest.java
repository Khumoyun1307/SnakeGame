package test.snakegame.model;

import com.snakegame.model.Direction;
import com.snakegame.model.Snake;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

public class SnakeTest {

    @Test
    public void testInitialSnakeHead() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        Point head = snake.getHead();
        assertEquals(new Point(100, 100), head);
    }

    @Test
    public void testSnakeMovesForward() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.move(false); // not growing
        Point newHead = snake.getHead();
        assertEquals(new Point(125, 100), newHead); // moved right
    }

    @Test
    public void testSnakeGrows() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.move(true); // should grow
        Deque<Point> body = snake.getBody();
        assertEquals(4, body.size());
    }

    @Test
    public void testNoReverseDirection() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.setDirection(Direction.LEFT); // illegal reverse
        assertEquals(Direction.RIGHT, snake.getDirection()); // should ignore
    }

    @Test
    public void testValidDirectionChange() {
        Snake snake = new Snake(new Point(100, 100), 3, Direction.RIGHT);
        snake.setDirection(Direction.DOWN);
        assertEquals(Direction.DOWN, snake.getDirection());
    }
}
