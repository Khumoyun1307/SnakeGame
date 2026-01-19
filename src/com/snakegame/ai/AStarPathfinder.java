package com.snakegame.ai;

import java.util.*;

public class AStarPathfinder {

    public List<Cell> findPath(Cell start,
                               Cell goal,
                               Set<Cell> blocked,
                               int cols,
                               int rows) {

        if (start.equals(goal)) return List.of(start);

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Cell, Integer> gScore = new HashMap<>();
        Map<Cell, Cell> cameFrom = new HashMap<>();
        Set<Cell> closed = new HashSet<>();

        gScore.put(start, 0);
        open.add(new Node(start, heuristic(start, goal, cols, rows), 0));

        while (!open.isEmpty()) {
            Node current = open.poll();
            Cell c = current.cell;

            if (c.equals(goal)) {
                return reconstruct(cameFrom, c);
            }
            if (!closed.add(c)) continue;

            for (Cell nb : neighbors(c, cols, rows)) {
                if (blocked.contains(nb) || closed.contains(nb)) continue;

                int tentativeG = gScore.getOrDefault(c, Integer.MAX_VALUE) + 1;
                if (tentativeG < gScore.getOrDefault(nb, Integer.MAX_VALUE)) {
                    cameFrom.put(nb, c);
                    gScore.put(nb, tentativeG);
                    int f = tentativeG + heuristic(nb, goal, cols, rows);
                    open.add(new Node(nb, f, tentativeG));
                }
            }
        }
        return null;
    }

    private List<Cell> neighbors(Cell c, int cols, int rows) {
        // 4-neighborhood with wrap-around
        return List.of(
                new Cell((c.x + 1) % cols, c.y),
                new Cell((c.x - 1 + cols) % cols, c.y),
                new Cell(c.x, (c.y + 1) % rows),
                new Cell(c.x, (c.y - 1 + rows) % rows)
        );
    }

    private int heuristic(Cell a, Cell b, int cols, int rows) {
        // Manhattan distance with wrap-around (toroidal)
        int dx = Math.abs(a.x - b.x);
        int dy = Math.abs(a.y - b.y);
        dx = Math.min(dx, cols - dx);
        dy = Math.min(dy, rows - dy);
        return dx + dy;
    }

    private List<Cell> reconstruct(Map<Cell, Cell> cameFrom, Cell current) {
        LinkedList<Cell> path = new LinkedList<>();
        path.addFirst(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.addFirst(current);
        }
        return path;
    }

    private static class Node {
        final Cell cell;
        final int f;
        final int g;
        Node(Cell cell, int f, int g) {
            this.cell = cell;
            this.f = f;
            this.g = g;
        }
    }
}
