# Contributing

Thanks for your interest in contributing!

## Development Setup

- Install Java **21**
- Build and test with the Maven wrapper

Windows (PowerShell):

```powershell
.\mvnw -B test
.\mvnw -B clean package
```

macOS/Linux:

```bash
./mvnw -B test
./mvnw -B clean package
```

## Project Structure (high level)

- `src/main/java`: game code (Swing UI, simulation, persistence, replay, AI)
- `src/main/resources`: packaged maps, icon, and sounds
- `packaging/`: icons used for `jpackage` builds
- `.github/workflows/`: CI/release automation

## Making Changes

- Keep PRs focused (one fix/feature per PR).
- Prefer small, readable commits.
- Match existing code style (no project-wide formatting changes unless requested).
- Add or update tests when behavior changes (`src/test/java`).

## Reporting Bugs

Please use GitHub issues and include:

- Steps to reproduce
- Expected vs actual behavior
- Your OS + Java version
- The selected mode (Standard / Map Select / Race / AI) and relevant settings (difficulty, obstacles, moving obstacles, map id)
- Any relevant files from the app data directory (see `README.md`), especially `data/settings.txt` and `data/replay_last.txt`

## Suggesting Features

Feature requests are welcome. A great request includes:

- The problem you’re trying to solve
- A concrete proposed solution
- Alternatives you’ve considered

