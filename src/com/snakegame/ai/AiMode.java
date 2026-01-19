package com.snakegame.ai;

public enum AiMode {
    CHASE,     // plain A* to apple
    SAFE,      // A* to apple only if escape exists
    SURVIVAL   // prioritize survival (tail/space), apple only when safe
}
