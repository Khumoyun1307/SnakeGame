# Snake Game (Full)

A Java 21 Swing Snake game with multiple modes (Standard, Map Select, Race, and AI), power-up apples, save/continue, deterministic replays, local stats, and an optional online leaderboard.

## Features

- **Toroidal board**: exiting one edge wraps you to the opposite side.
- **Power-up apples**:
  - **Big** (every 8 apples): +4 points
  - **Golden** (every 17 apples): double points for ~10s
  - **Slowdown** (every 25 apples): slower tick for ~8s
  - **Reverse** (when score is a multiple of 50): reversed controls for ~8s
- **Obstacles**
  - Random obstacles (Standard / AI)
  - Moving obstacles (Standard / AI) with optional auto-increment over time
- **Modes**
  - **Standard**: classic endless Snake with optional random/moving obstacles
  - **Map Select**: pick an unlocked map to play
  - **Race**: advance through maps by eating `raceThreshold` apples per map
  - **AI (A*)**: the game plays itself (Chase / Safe / Survival)
- **Save/Continue**: pause and save a run, then continue later from the main menu.
- **Replays**: automatically saves the **last** run and the **best** run (highest score) for watch-only playback.
- **Online leaderboard (optional)**: submits non-AI runs in the background (safe to play offline).

## Controls

- **Arrow keys**: move the snake (player runs)
- **Space**: pause (player runs + AI runs)

## Build and Run

### Requirements

- Java **21** (project targets `--release 21`)

### Build a runnable JAR

Windows (PowerShell):

```powershell
.\mvnw -B clean package
java -jar .\target\SnakeGame.jar
```

macOS/Linux:

```bash
./mvnw -B clean package
java -jar ./target/SnakeGame.jar
```

### Run tests

```powershell
.\mvnw -B test
```

## Persistence / Save Files

The game stores settings, progress, saves, replays, and scores in a per-user writable directory so installed builds can run without writing inside the app folder.

Default locations:

- **Windows**: `%LOCALAPPDATA%\SnakeGame\` (fallback: `%APPDATA%\SnakeGame\`)
- **macOS**: `~/Library/Application Support/SnakeGame/`
- **Linux**: `$XDG_DATA_HOME/SnakeGame/` (fallback: `~/.local/share/SnakeGame/`)

Key files:

- `data/settings.txt`: difficulty, mode, audio, theme, etc.
- `data/progress.txt`: unlocked map IDs
- `data/savegame.txt`: saved run snapshot for **Continue**
- `data/replay_last.txt`, `data/replay_best.txt`: deterministic replay data
- `scores.txt`: local score history

You can override the app directory (useful for portable runs/testing):

```powershell
java -Dsnakegame.appDir="C:\path\to\SnakeGameData" -jar .\target\SnakeGame.jar
```

## Online Leaderboard (optional)

Scores from **non-AI** runs are submitted asynchronously after a run finishes. If the service is unreachable, the game continues normally.

Override the leaderboard base URL:

- JVM property: `-Dsnakegame.leaderboard.baseUrl=https://example.com`
- Env var: `SNAKEGAME_LEADERBOARD_BASE_URL=https://example.com`

## Developer Mode (map editor)

Developer mode is **session-only** and unlocks a simple map editor.

From the main menu:

1. Hold **Ctrl**
2. Type **713Developer**

Developer maps are saved under your app data directory in `data/dev-maps/` and will appear in Map Select while developer mode is enabled.

## License

See `LICENSE`.

