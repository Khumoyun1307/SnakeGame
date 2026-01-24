package com.snakegame.controller;

/**
 * Minimal control surface for the game loop, used by flow/UI code without exposing Timer details.
 */
public interface LoopControl {
    /**
     * Pauses the currently running loop.
     */
    void pause();
    /**
     * Resumes a previously paused loop.
     */
    void resume();
    /**
     * Stops the loop permanently for the current run.
     */
    void stop();
}
