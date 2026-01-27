package com.snakegame.model;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Represents the snake as an ordered list of grid-aligned body segments.
 *
 * <p>All positions are stored as pixel coordinates aligned to {@link GameConfig#UNIT_SIZE}. The head
 * is the first element in the deque.</p>
 */
public class Snake {
    private Deque<Point> body;
    private Direction currentDirection;

    /**
     * Creates a new snake starting at the given position.
     *
     * @param start starting head position in pixels
     * @param length initial length in segments
     * @param initialDirection initial movement direction
     */
    public Snake(Point start, int length, Direction initialDirection) {
        this.body = new LinkedList<>();
        this.currentDirection = initialDirection;

        for (int i = 0; i < length; i++) {
            body.addLast(new Point(start.x - i * GameConfig.UNIT_SIZE, start.y));
        }
    }

    private Snake(Deque<Point> body, Direction direction) {
        this.body = body;
        this.currentDirection = direction;
    }

    /**
     * Advances the snake by one cell in the current direction.
     *
     * @param grow whether the snake should grow this move (i.e., do not drop the tail)
     */
    public void move(boolean grow) {
        Point head = getHead();
        Point newHead = new Point(head);

        switch (currentDirection) {
            case UP -> newHead.y -= GameConfig.UNIT_SIZE;
            case DOWN -> newHead.y += GameConfig.UNIT_SIZE;
            case LEFT -> newHead.x -= GameConfig.UNIT_SIZE;
            case RIGHT -> newHead.x += GameConfig.UNIT_SIZE;
        }

        body.addFirst(newHead);
        if (!grow) body.removeLast();
    }

    /**
     * Updates the movement direction unless the new direction would be an immediate reversal.
     *
     * @param newDirection requested direction
     */
    public void setDirection(Direction newDirection) {
        if (!newDirection.isOpposite(currentDirection)) {
            this.currentDirection = newDirection;
        }
    }

    /**
     * Returns the current movement direction.
     *
     * @return current direction
     */
    public Direction getDirection() {
        return currentDirection;
    }

    /**
     * Returns the current head position.
     *
     * @return head position in pixels
     */
    public Point getHead() {
        return body.peekFirst();
    }

    /**
     * Returns the underlying body deque (head-first).
     *
     * @return body segments as a deque
     */
    public Deque<Point> getBody() {
        return body;
    }

    /**
     * Checks whether the head overlaps any other segment.
     *
     * @return {@code true} if the snake is currently self-colliding
     */
    public boolean isSelfColliding() {
        Point head = getHead();
        return body.stream().skip(1).anyMatch(p -> p.equals(head));
    }

    /**
     * Reconstructs a snake instance from a list of body points (e.g., from a snapshot).
     *
     * @param bodyPoints body segments in head-to-tail order
     * @param direction current direction
     * @return reconstructed snake
     */
    public static Snake fromBody(java.util.List<Point> bodyPoints, Direction direction) {
        java.util.Deque<Point> dq = new java.util.LinkedList<>();
        for (Point p : bodyPoints) dq.addLast(new Point(p));
        Snake s = new Snake(new Point(0,0), 1, direction); // temporary
        s.body.clear();
        s.body.addAll(dq);
        s.currentDirection = direction;
        return s;
    }
}
