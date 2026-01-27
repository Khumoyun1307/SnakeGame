package com.snakegame.model;

/**
 * Cardinal movement directions for the snake.
 */
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    /**
     * Determines whether moving in {@code other} would be a direct reversal from this direction.
     *
     * @param other direction to compare against
     * @return {@code true} if {@code other} is the opposite direction
     */
    public boolean isOpposite(Direction other) {
        return (this == UP && other == DOWN)
                || (this == DOWN && other == UP)
                || (this == LEFT && other == RIGHT)
                || (this == RIGHT && other == LEFT);
    }
}
