package com.snakegame.replay;

import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ReplayController implements ActionListener {

    private final GameState state;
    private final Timer timer;
    private final Runnable repaintCallback;

    private final List<ReplayEvent> events;
    private int eventIndex = 0;

    private boolean playing = false;

    private final int baseTickMs;
    private double speedMultiplier = 1.0;

    public ReplayController(GameState state, int baseTickMs, List<ReplayEvent> events, Runnable repaintCallback) {
        this.state = state;
        this.baseTickMs = Math.max(1, baseTickMs);
        this.events = (events == null) ? List.of() : events;
        this.repaintCallback = repaintCallback;

        this.timer = new Timer(this.baseTickMs, this);
        this.state.setTickMs(this.baseTickMs);
    }

    public void play() {
        if (playing) return;
        playing = true;
        timer.start();
    }

    public void pause() {
        playing = false;
        timer.stop();
    }

    public boolean isPlaying() { return playing; }

    public void setSpeedMultiplier(double mult) {
        if (mult <= 0) mult = 1.0;
        this.speedMultiplier = mult;
        // Playback only: do NOT change simulation tickMs.
        timer.setDelay(scaledDelayMs(currentEffectiveTickMs()));
    }

    public void stepOnce() {
        if (playing) return;
        tickOnce();
    }

    public void restart(GameState newState, List<ReplayEvent> newEvents) {
        pause();
        // NOTE: panel will replace controller instance, so this method may not be used.
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tickOnce();
    }

    private void tickOnce() {
        // Keep tickMs deterministic and independent from playback speed.
        int effectiveTickMs = currentEffectiveTickMs();
        state.setTickMs(effectiveTickMs);
        timer.setDelay(scaledDelayMs(effectiveTickMs));

        if (!state.isRunning()) {
            pause();
            repaintCallback.run();
            return;
        }

        // Apply all inputs scheduled for the current tick BEFORE update
        long t = state.getTick();
        while (eventIndex < events.size() && events.get(eventIndex).tick == t) {
            Direction dir = events.get(eventIndex).direction;
            if (dir != null) state.setDirection(dir);
            eventIndex++;
        }

        state.update();
        repaintCallback.run();

        if (!state.isRunning()) {
            pause();
        }
    }

    private int currentEffectiveTickMs() {
        return state.isSlowed()
                ? baseTickMs + GameConfig.SLOWDOWN_OFFSET_MS
                : baseTickMs;
    }

    private int scaledDelayMs(int simulationTickMs) {
        if (speedMultiplier <= 0) speedMultiplier = 1.0;
        return (int) Math.max(1, Math.round(simulationTickMs / speedMultiplier));
    }
}
