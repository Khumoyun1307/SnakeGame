package com.snakegame.ai;

import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;

import java.awt.Point;
import java.util.Objects;

public final class Cell {
    public final int x; // grid coord
    public final int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Cell fromPixel(Point p) {
        return new Cell(p.x / GameConfig.UNIT_SIZE, p.y / GameConfig.UNIT_SIZE);
    }

    public Point toPixel() {
        return new Point(x * GameConfig.UNIT_SIZE, y * GameConfig.UNIT_SIZE);
    }

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
