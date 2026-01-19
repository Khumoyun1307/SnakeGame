package com.snakegame.replay;

import com.snakegame.config.GameSettings;
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

    public ReplayController(GameState state, List<ReplayEvent> events, Runnable repaintCallback) {
        this.state = state;
        this.events = (events == null) ? List.of() : events;
        this.repaintCallback = repaintCallback;

        int baseDelay = GameSettings.getSpeedDelayFromDifficultyLevel();
        this.timer = new Timer(baseDelay, this);
        this.state.setTickMs(baseDelay);
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
        int baseDelay = GameSettings.getSpeedDelayFromDifficultyLevel();
        int delay = (int) Math.max(1, Math.round(baseDelay / mult));
        timer.setDelay(delay);
        state.setTickMs(delay);
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
}
