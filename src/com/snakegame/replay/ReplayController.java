package com.snakegame.replay;

import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Timer-driven controller that replays a recorded run in "watch-only" mode.
 *
 * <p>Inputs are applied according to their recorded tick before advancing the simulation. Playback
 * speed can be adjusted independently from the simulation tick rate.</p>
 */
public class ReplayController implements ActionListener {

    private final GameState state;
    private final Timer timer;
    private final Runnable repaintCallback;

    private final List<ReplayEvent> events;
    private int eventIndex = 0;

    private boolean playing = false;

    private final int baseTickMs;
    private double speedMultiplier = 1.0;

    /**
     * Creates a replay controller.
     *
     * @param state game state seeded/configured for watch-only replay
     * @param baseTickMs base simulation tick in milliseconds
     * @param events recorded input events
     * @param repaintCallback callback used to repaint the UI after each tick
     */
    public ReplayController(GameState state, int baseTickMs, List<ReplayEvent> events, Runnable repaintCallback) {
        this.state = state;
        this.baseTickMs = Math.max(1, baseTickMs);
        this.events = (events == null) ? List.of() : events;
        this.repaintCallback = repaintCallback;

        this.timer = new Timer(this.baseTickMs, this);
        this.state.setTickMs(this.baseTickMs);
    }

    /**
     * Starts playback.
     */
    public void play() {
        if (playing) return;
        playing = true;
        timer.start();
    }

    /**
     * Pauses playback.
     */
    public void pause() {
        playing = false;
        timer.stop();
    }

    /**
     * Returns whether the replay is currently playing.
     *
     * @return {@code true} if playing
     */
    public boolean isPlaying() { return playing; }

    /**
     * Sets a playback speed multiplier (e.g., 2.0 for 2x speed).
     *
     * @param mult speed multiplier (values &lt;= 0 default to 1.0)
     */
    public void setSpeedMultiplier(double mult) {
        if (mult <= 0) mult = 1.0;
        this.speedMultiplier = mult;
        // Playback only: do NOT change simulation tickMs.
        timer.setDelay(scaledDelayMs(currentEffectiveTickMs()));
    }

    /**
     * Advances the replay by exactly one simulation tick when paused.
     */
    public void stepOnce() {
        if (playing) return;
        tickOnce();
    }

    public void restart(GameState newState, List<ReplayEvent> newEvents) {
        pause();
        // NOTE: panel will replace controller instance, so this method may not be used.
    }

    /** {@inheritDoc} */
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
