package com.snakegame.ai;

/**
 * High-level strategies available for the built-in AI controller.
 *
 * <p>Modes trade off apple-chasing aggressiveness versus safety/space management.</p>
 */
public enum AiMode {
    CHASE,     // plain A* to apple
    SAFE,      // A* to apple only if escape exists
    SURVIVAL   // prioritize survival (tail/space), apple only when safe
}
