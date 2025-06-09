package com.snakegame.model;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;

public class Snake {
    private final Deque<Point> body;
    private Direction currentDirection;

    public Snake(Point start, int length, Direction initialDirection) {
        this.body = new LinkedList<>();
        this.currentDirection = initialDirection;

        for (int i = 0; i < length; i++) {
            body.addLast(new Point(start.x - i * GameConfig.UNIT_SIZE, start.y));
        }
    }

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

    public void setDirection(Direction newDirection) {
        if (!newDirection.isOpposite(currentDirection)) {
            this.currentDirection = newDirection;
        }
    }

    public Direction getDirection() {
        return currentDirection;
    }

    public Point getHead() {
        return body.peekFirst();
    }

    public Deque<Point> getBody() {
        return body;
    }

    public boolean isSelfColliding() {
        Point head = getHead();
        return body.stream().skip(1).anyMatch(p -> p.equals(head));
    }
}
