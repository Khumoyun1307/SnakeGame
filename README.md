# SnakeGame (Java + Swing)

[![CI](https://github.com/Khumoyun1307/SnakeGame/actions/workflows/ci.yml/badge.svg)](https://github.com/Khumoyun1307/SnakeGame/actions/workflows/ci.yml)
[![Coverage](https://img.shields.io/badge/coverage-JaCoCo-informational)](https://github.com/Khumoyun1307/SnakeGame/actions/workflows/ci.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Snake Game built with Java 17 and Swing, with multiple modes (Standard / Map Select / Race / AI), deterministic replays, save/continue, and an online leaderboard client.

## Highlights

- **Deterministic core simulation**: `GameState` runs on a tick-based clock + seed-based RNG, enabling reliable replays and stable tests.
- **Power-ups & effects**: big/golden/slowdown/reverse apples with durations implemented in the simulation layer.
- **AI modes**: A* pathfinding plus “SAFE”/“SURVIVAL” heuristics (space scoring + tail reachability).
- **Replay system**: records input events and replays deterministically (watch-only snapshot settings).
- **Persistence**: settings, progress/unlocks, savegame, and replay files stored as simple text/Properties under `data/`.
- **Online leaderboard**: Java `HttpClient` + lightweight JSON parsing; UI degrades gracefully when offline.

## Project Structure

- `src/com/snakegame/model` — simulation (`GameState`, `Snake`, `Apple`, obstacles, events, snapshots)
- `src/com/snakegame/ai` — A* + grid utilities
- `src/com/snakegame/controller` — loop/flow + input providers
- `src/com/snakegame/replay` — replay persistence + playback controller
- `src/com/snakegame/util` — save/progress/score persistence helpers
- `src/com/snakegame/net` — leaderboard client + models
- `src/com/snakegame/ui`, `src/com/snakegame/view` — Swing UI + renderer
- `resources/` — maps, sounds, icons
- `test/` — JUnit 5 test suite

## Running Locally (IntelliJ IDEA)

**Requirements**
- Java **17**

**Run the game**
1. Open the project folder in IntelliJ.
2. Ensure Project SDK is set to Java 17.
3. Run `com.snakegame.SnakeGame` (main class).

**Run tests**
- Run all tests under `test/` (JUnit 5).

## Modes

- **Standard**: classic snake, optional random obstacles and moving obstacles.
- **Map Select**: play unlocked packaged maps (from `resources/maps/map1..map10.txt`).
- **Race**: eat apples to advance through maps; unlock progress persists.
- **AI**: choose between A* chase, safe, or survival modes.

## Save / Continue

- Press **Space** in-game to open the pause menu.
- “Save & Quit” persists a snapshot so you can continue later.

## Developer Mode (Map Editor)

Developer mode is intentionally hidden to keep normal gameplay clean:
- On the main menu, **hold Ctrl** and type `713Developer`.
- A **Developer** button appears; maps are saved as `resources/maps/map<ID>_developer.txt`.

## CI + Coverage (GitHub Actions)

This repo intentionally avoids Maven/Gradle. CI compiles with `javac`, runs tests in headless mode, and generates a JaCoCo report.

- Workflow: `.github/workflows/ci.yml`
- Artifacts:
  - `test-reports` (JUnit XML)
  - `coverage` (JaCoCo HTML + XML)

To view coverage locally from CI:
1. Open a workflow run in GitHub Actions.
2. Download the `coverage` artifact.
3. Open `ci-out/coverage/html/index.html` in a browser.

## License

GPL-3.0 — see `LICENSE`.
