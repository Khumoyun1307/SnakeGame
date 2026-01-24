# Resume Bullets (SnakeGame)

- Built a Java 17 + Swing Snake game with a deterministic, tick-driven simulation (`GameState`) backed by seed-based RNG to enable reproducible replays and stable automated tests.
- Implemented multiple gameplay modes (Standard / Map Select / Race / AI), including map progression and unlock persistence across sessions.
- Designed an AI system using A* pathfinding plus safety heuristics (reachable-space scoring + tail reachability checks) to avoid traps and prioritize survivability.
- Added a replay pipeline that records input events and replays runs deterministically using a watch-only settings snapshot (no global setting mutation during playback).
- Implemented persistence for settings, saves, unlock progress, and replays using Java NIO + `Properties`, including guardrails for corrupted files.
- Integrated an online leaderboard client using Java `HttpClient` and lightweight JSON parsing; UI handles offline/network failures gracefully.
- Set up GitHub Actions CI without Maven/Gradle: direct `javac` compilation, JUnit 5 execution, JaCoCo coverage reports (HTML/XML artifacts), and coverage gating.
- Authored a comprehensive JUnit 5 test suite covering core simulation logic, AI/pathfinding, persistence/replay formats, and headless renderer smoke tests.
