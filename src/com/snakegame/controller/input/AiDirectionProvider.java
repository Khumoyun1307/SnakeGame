package com.snakegame.controller.input;

import com.snakegame.ai.AStarPathfinder;
import com.snakegame.ai.AiMode;
import com.snakegame.ai.Cell;
import com.snakegame.config.GameSettings;
import com.snakegame.model.*;

import java.awt.Point;
import java.util.*;

public class AiDirectionProvider implements DirectionProvider {

    private final AStarPathfinder pathfinder = new AStarPathfinder();
    private final AiMode mode;

    // loop avoidance: remember recent head cells
    private final ArrayDeque<Cell> recentHeads = new ArrayDeque<>();
    private static final int RECENT_LIMIT = 12;

    public AiDirectionProvider(AiMode mode) {
        this.mode = (mode == null) ? AiMode.SAFE : mode;
    }

    @Override
    public Direction nextDirection(GameState state) {
        int cols = GameConfig.SCREEN_WIDTH / GameConfig.UNIT_SIZE;
        int rows = GameConfig.SCREEN_HEIGHT / GameConfig.UNIT_SIZE;

        return switch (mode) {
            case CHASE -> chase(state, cols, rows);
            case SAFE -> chooseByScoring(state, cols, rows, false);
            case SURVIVAL -> chooseByScoring(state, cols, rows, true);
        };
    }

    // -------------------- CHASE: plain A* to apple (your original) --------------------
    private Direction chase(GameState state, int cols, int rows) {
        Snake snake = state.getSnake();
        Cell head = Cell.fromPixel(snake.getHead());
        Cell apple = Cell.fromPixel(state.getApple().getPosition());

        Set<Cell> blocked = buildBlockedCellsFromState(state, false);
        blocked.remove(apple);

        List<Cell> path = pathfinder.findPath(head, apple, blocked, cols, rows);
        if (path != null && path.size() >= 2) {
            Direction d = directionFromStep(head, path.get(1), cols, rows);
            if (d != null && !d.isOpposite(snake.getDirection())) return d;
        }
        return fallbackSafe(state, cols, rows);
    }

    // -------------------- SAFE / SURVIVAL: scored one-step planning --------------------
    private Direction chooseByScoring(GameState state, int cols, int rows, boolean survivalMode) {
        Snake snake = state.getSnake();
        Direction currentDir = snake.getDirection();

        Cell head = Cell.fromPixel(snake.getHead());
        Cell apple = Cell.fromPixel(state.getApple().getPosition());

        MoveChoice best = null;

        for (Direction d : List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)) {
            if (d.isOpposite(currentDir)) continue;

            MoveChoice mc = evaluateMove(state, head, apple, d, cols, rows);
            if (!mc.valid) continue;

            // SAFE: must be able to reach tail (escape) unless literally impossible
            if (!survivalMode && !mc.tailReachable) continue;

            if (best == null || mc.score > best.score) best = mc;
        }

        // If SAFE filtered everything (tail not reachable from any), relax and pick best valid.
        if (best == null && !survivalMode) {
            for (Direction d : List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)) {
                if (d.isOpposite(currentDir)) continue;
                MoveChoice mc = evaluateMove(state, head, apple, d, cols, rows);
                if (!mc.valid) continue;
                if (best == null || mc.score > best.score) best = mc;
            }
        }

        if (best != null) {
            rememberHead(best.nextHead);
            return best.dir;
        }

        // fallback if boxed in
        return fallbackSafe(state, cols, rows);
    }

    private MoveChoice evaluateMove(GameState state,
                                    Cell head,
                                    Cell apple,
                                    Direction d,
                                    int cols,
                                    int rows) {

        Snake snake = state.getSnake();

        Cell nextHead = head.step(d, cols, rows);

        // Build base blocked cells from current state (body + obstacles + moving obstacles).
        // We will simulate body movement to be more accurate.
        Deque<Cell> simBody = new ArrayDeque<>();
        for (Point p : snake.getBody()) simBody.addLast(Cell.fromPixel(p));

        boolean willEat = nextHead.equals(apple);

        // simulate snake move
        simBody.addFirst(nextHead);
        if (!willEat) simBody.removeLast();

        // collision with self (after sim)
        // (head is first; if it appears elsewhere -> collision)
        int count = 0;
        for (Cell c : simBody) {
            if (++count == 1) continue;
            if (c.equals(nextHead)) return MoveChoice.invalid(d);
        }

        // Build blocked cells from sim body (exclude head; optionally allow tail if not growing)
        Set<Cell> blocked = new HashSet<>(simBody);
        blocked.remove(nextHead);

        // allow tail cell when not growing (it is “movable space”)
        Cell tail = simBody.peekLast();
        if (!willEat && tail != null) blocked.remove(tail);

        // Add static obstacles
        for (Point o : state.getObstacles()) blocked.add(Cell.fromPixel(o));

        // Add moving obstacles (treat as blocked)
        if (GameSettings.isMovingObstaclesEnabled()) {
            for (MovingObstacle mo : state.getMovingObstacles()) {
                for (Point seg : mo.getSegments()) blocked.add(Cell.fromPixel(seg));
            }
        }

        // If nextHead hits obstacle/moving obstacle
        if (blocked.contains(nextHead)) return MoveChoice.invalid(d);

        // reachable area from nextHead (bigger is safer)
        int area = floodFillArea(nextHead, blocked, cols, rows);

        // tail reachable = can we “escape” by reaching our tail
        boolean tailReachable = false;
        if (tail != null) {
            Set<Cell> blockedForTail = new HashSet<>(blocked);
            blockedForTail.remove(tail);
            List<Cell> tailPath = pathfinder.findPath(nextHead, tail, blockedForTail, cols, rows);
            tailReachable = (tailPath != null && tailPath.size() >= 2);
        }

        // apple path length (if possible)
        int appleLen = Integer.MAX_VALUE;
        Set<Cell> blockedForApple = new HashSet<>(blocked);
        blockedForApple.remove(apple);
        List<Cell> applePath = pathfinder.findPath(nextHead, apple, blockedForApple, cols, rows);
        if (applePath != null) appleLen = applePath.size();

        // loop penalty: discourage revisiting recent head positions
        int loopPenalty = recentHeads.contains(nextHead) ? 25 : 0;

        // score: tweak weights based on behavior
        double score = 0;

        // always prioritize not dying
        if (tailReachable) score += 2000;
        score += area * 2.0;
        score -= loopPenalty;

        // Encourage apple pursuit, but only if it doesn't destroy safety
        if (appleLen != Integer.MAX_VALUE) {
            // closer apple = better
            score += (300.0 / appleLen);
        }

        // Extra safety rule: if reachable area too small relative to snake length, penalize hard
        int snakeLen = snake.getBody().size();
        if (area < snakeLen + 3) score -= 1500;

        // Survival mode: much stronger emphasis on area and anti-loop
        // (we don't pass flag here; instead modify upstream by using SAFE filter)
        // If you want survival weights even stronger, you can bump the constants above.

        return new MoveChoice(true, d, nextHead, tailReachable, area, appleLen, score);
    }

    private void rememberHead(Cell head) {
        recentHeads.addLast(head);
        while (recentHeads.size() > RECENT_LIMIT) recentHeads.removeFirst();
    }

    private int floodFillArea(Cell start, Set<Cell> blocked, int cols, int rows) {
        ArrayDeque<Cell> q = new ArrayDeque<>();
        HashSet<Cell> vis = new HashSet<>();
        if (blocked.contains(start)) return 0;

        q.add(start);
        vis.add(start);

        while (!q.isEmpty()) {
            Cell c = q.poll();
            for (Cell nb : neighbors(c, cols, rows)) {
                if (blocked.contains(nb) || vis.contains(nb)) continue;
                vis.add(nb);
                q.add(nb);
            }
        }
        return vis.size();
    }

    private List<Cell> neighbors(Cell c, int cols, int rows) {
        return List.of(
                new Cell((c.x + 1) % cols, c.y),
                new Cell((c.x - 1 + cols) % cols, c.y),
                new Cell(c.x, (c.y + 1) % rows),
                new Cell(c.x, (c.y - 1 + rows) % rows)
        );
    }

    private Set<Cell> buildBlockedCellsFromState(GameState state, boolean willGrowNext) {
        Set<Cell> blocked = new HashSet<>();

        Deque<Point> body = state.getSnake().getBody();
        if (!body.isEmpty()) {
            Iterator<Point> it = body.iterator();
            it.next(); // skip head
            while (it.hasNext()) blocked.add(Cell.fromPixel(it.next()));

            if (!willGrowNext) {
                Point tail = body.peekLast();
                if (tail != null) blocked.remove(Cell.fromPixel(tail));
            }
        }

        for (Point o : state.getObstacles()) blocked.add(Cell.fromPixel(o));

        if (GameSettings.isMovingObstaclesEnabled()) {
            for (MovingObstacle mo : state.getMovingObstacles()) {
                for (Point seg : mo.getSegments()) blocked.add(Cell.fromPixel(seg));
            }
        }
        return blocked;
    }

    private Direction fallbackSafe(GameState state, int cols, int rows) {
        Snake snake = state.getSnake();
        Cell head = Cell.fromPixel(snake.getHead());

        Set<Cell> blocked = buildBlockedCellsFromState(state, false);

        for (Direction d : List.of(snake.getDirection(), Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)) {
            if (d == null) continue;
            if (d.isOpposite(snake.getDirection())) continue;
            Cell next = head.step(d, cols, rows);
            if (!blocked.contains(next)) return d;
        }
        return snake.getDirection();
    }

    private Direction directionFromStep(Cell a, Cell b, int cols, int rows) {
        if (a.step(Direction.UP, cols, rows).equals(b)) return Direction.UP;
        if (a.step(Direction.DOWN, cols, rows).equals(b)) return Direction.DOWN;
        if (a.step(Direction.LEFT, cols, rows).equals(b)) return Direction.LEFT;
        if (a.step(Direction.RIGHT, cols, rows).equals(b)) return Direction.RIGHT;
        return null;
    }

    private static class MoveChoice {
        final boolean valid;
        final Direction dir;
        final Cell nextHead;
        final boolean tailReachable;
        final int area;
        final int appleLen;
        final double score;

        private MoveChoice(boolean valid, Direction dir, Cell nextHead,
                           boolean tailReachable, int area, int appleLen, double score) {
            this.valid = valid;
            this.dir = dir;
            this.nextHead = nextHead;
            this.tailReachable = tailReachable;
            this.area = area;
            this.appleLen = appleLen;
            this.score = score;
        }

        static MoveChoice invalid(Direction d) {
            return new MoveChoice(false, d, null, false, 0, Integer.MAX_VALUE, Double.NEGATIVE_INFINITY);
        }
    }
}
