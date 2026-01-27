package com.snakegame.ai;

import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;

import java.awt.Point;
import java.util.Objects;

/**
 * Immutable grid coordinate used by AI/pathfinding logic.
 *
 * <p>A {@code Cell} is expressed in grid units (not pixels). Conversion helpers translate between
 * grid coordinates and pixel-space positions used by the simulation and renderer.</p>
 */
public final class Cell {
    public final int x; // grid coord
    public final int y;

    /**
     * Creates a new grid cell coordinate.
     *
     * @param x column index (0-based)
     * @param y row index (0-based)
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Converts a pixel-space point to a grid cell coordinate.
     *
     * @param p pixel coordinate (typically aligned to {@link GameConfig#UNIT_SIZE})
     * @return the corresponding grid cell
     */
    public static Cell fromPixel(Point p) {
        return new Cell(p.x / GameConfig.UNIT_SIZE, p.y / GameConfig.UNIT_SIZE);
    }

    /**
     * Converts this grid cell to the pixel coordinate of its top-left corner.
     *
     * @return pixel coordinate aligned to {@link GameConfig#UNIT_SIZE}
     */
    public Point toPixel() {
        return new Point(x * GameConfig.UNIT_SIZE, y * GameConfig.UNIT_SIZE);
    }

    /**
     * Returns the adjacent cell after moving one step in the given direction, applying wrap-around.
     *
     * @param d movement direction
     * @param cols total number of columns in the grid
     * @param rows total number of rows in the grid
     * @return next cell coordinate
     */
    public Cell step(Direction d, int cols, int rows) {
        int nx = x, ny = y;
        switch (d) {
            case UP -> ny--;
            case DOWN -> ny++;
            case LEFT -> nx--;
            case RIGHT -> nx++;
        }
        // wrap-around like your GameState.update()
        if (nx < 0) nx = cols - 1;
        else if (nx >= cols) nx = 0;
        if (ny < 0) ny = rows - 1;
        else if (ny >= rows) ny = 0;
        return new Cell(nx, ny);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return x == cell.x && y == cell.y;
    }

    @Override public int hashCode() {
        return Objects.hash(x, y);
    }
}
