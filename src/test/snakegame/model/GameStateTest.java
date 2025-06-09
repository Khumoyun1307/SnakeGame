package com.snakegame.model;

import org.junit.jupiter.api.Test;
import java.awt.*;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    public void testInitialGameState() {
        GameState game = new GameState();

        assertTrue(game.isRunning());
        assertEquals(0, game.getScore());
        assertNotNull(game.getSnake());
        assertNotNull(game.getApple());
    }

    @Test
    public void testAppleEatenIncreasesScore() {
        GameState game = new GameState();

        // Set snake head to apple's position
        Snake snake = game.getSnake();
        Apple apple = game.getApple();
        Point applePos = apple.getPosition();

        // Force snake's head onto apple
        snake.getBody().clear();
        snake.getBody().addFirst(new Point(applePos));
        game.setDirection(Direction.RIGHT);

        game.update();

        assertEquals(1, game.getScore());
    }

    @Test
    public void testCollisionWithWallEndsGame() {
        GameState game = new GameState();

        // Clear the snake and place head at x=0 (leftmost edge)
        Deque<Point> body = game.getSnake().getBody();
        body.clear();
        body.addFirst(new Point(0, 0)); // Head is at the wall

        System.out.println("After clear: " + body); // Debug

        game.setDirection(Direction.UP);
        game.update();  // Move LEFT from x=0 → x=-25, which is off-screen

        System.out.println("After update head: " + game.getSnake().getHead());

        // The game should stop because of wall collision
        assertFalse(game.isRunning(), "Game should stop after wall collision");
    }




    @Test
    public void testSelfCollisionEndsGame() {
        GameState game = new GameState();
        Snake snake = game.getSnake();

        // Manually create a circle: head collides with body
        Deque<Point> body = snake.getBody();
        body.clear();
        Point head = new Point(100, 100);
        body.add(new Point(head));
        body.add(new Point(125, 100));
        body.add(new Point(125, 125));
        body.add(new Point(100, 125));
        body.add(new Point(100, 100)); // back to head

        game.update();

        assertFalse(game.isRunning());
    }

    @Test
    void testAppleEatingIncreasesScoreAndGrowsSnake() {
        GameState game = new GameState();

        Snake snake = game.getSnake();
        Point head = snake.getHead();

        // Fake that the snake is currently ON the apple
        game.getApple().setPosition(new Point(head));

        int initialScore = game.getScore();
        int initialLength = snake.getBody().size();

        game.update();

        assertEquals(initialScore + 1, game.getScore(), "Score should increase by 1 after eating apple");
        assertEquals(initialLength + 1, snake.getBody().size(), "Snake should grow by 1 after eating apple");
        assertNotEquals(head, game.getApple().getPosition(), "Apple should respawn after being eaten");
    }

}
