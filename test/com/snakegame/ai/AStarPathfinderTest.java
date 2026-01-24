package com.snakegame.ai;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AStarPathfinder}.
 */
class AStarPathfinderTest {

    private final AStarPathfinder pathfinder = new AStarPathfinder();

    @Test
    void findPath_whenStartEqualsGoal_returnsSingletonPath() {
        Cell start = new Cell(1, 1);
        List<Cell> path = pathfinder.findPath(start, start, Set.of(), 5, 5);
        assertEquals(List.of(start), path);
    }

    @Test
    void findPath_findsShortestPathInEmptyGrid() {
        Cell start = new Cell(0, 0);
        Cell goal = new Cell(2, 0);
        List<Cell> path = pathfinder.findPath(start, goal, Set.of(), 5, 5);

        assertNotNull(path);
        assertEquals(start, path.get(0));
        assertEquals(goal, path.get(path.size() - 1));
        assertEquals(3, path.size());
    }

    @Test
    void findPath_avoidsBlockedCells() {
        Cell start = new Cell(0, 0);
        Cell goal = new Cell(2, 0);
        Set<Cell> blocked = new HashSet<>();
        blocked.add(new Cell(1, 0));

        List<Cell> path = pathfinder.findPath(start, goal, blocked, 5, 5);
        assertNotNull(path);
        assertFalse(path.contains(new Cell(1, 0)));
    }

    @Test
    void findPath_returnsNullWhenNoPath() {
        Cell start = new Cell(0, 0);
        Cell goal = new Cell(2, 0);
        Set<Cell> blocked = Set.of(
                new Cell(1, 0),
                new Cell(4, 0),
                new Cell(0, 1),
                new Cell(0, 4 % 5) // rows will be 5 in this test
        );

        List<Cell> path = pathfinder.findPath(start, goal, blocked, 5, 5);
        assertNull(path);
    }
}
