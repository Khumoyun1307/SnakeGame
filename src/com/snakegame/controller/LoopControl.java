package com.snakegame.controller;

/**
 * Minimal control surface for the game loop, used by flow/UI code without exposing Timer details.
 */
public interface LoopControl {
    void pause();
    void resume();
    void stop();
}

