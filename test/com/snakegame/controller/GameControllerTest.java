package com.snakegame.controller;

import com.snakegame.config.GameSettings;
import com.snakegame.model.Direction;
import com.snakegame.model.GameConfig;
import com.snakegame.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.Timer;
import java.awt.Button;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {
    private GameState stubState;
    private AtomicBoolean didUpdate;
    private AtomicReference<Direction> directionSet;
    private AtomicBoolean repaintCalled;
    private AtomicBoolean restartCalled;
    private AtomicBoolean menuCalled;
    private AtomicBoolean settingsCalled;
    private TestController controller;

    /**
     * Subclass of GameController that overrides the dialog methods
     * so no real Swing UI appears during tests.
     */
    static class TestController extends GameController {
        TestController(GameState gs,
                       Runnable repaint,
                       Runnable restart,
                       Runnable menu,
                       Runnable settings) {
            super(gs, repaint, restart, menu, settings);
        }

        @Override
        protected void showPauseDialog(Component parent) {
            // simulate “Resume” being clicked immediately
            resumeGame();
        }

        @Override
        protected void showGameOverDialog() {
            // no‐op to avoid blocking UI
        }
    }

    @BeforeEach
    void setUp() {
        // 1) Stub out GameState behavior
        didUpdate    = new AtomicBoolean(false);
        directionSet = new AtomicReference<>(null);

        stubState = new GameState() {
            @Override public void update()                 { didUpdate.set(true); }
            @Override public boolean isRunning()            { return true; }
            @Override public boolean isSlowed()             { return false; }
            @Override public boolean isReversedControls()   { return false; }
            @Override public void setDirection(Direction d) { directionSet.set(d); }
        };

        // 2) Prepare callback spies
        repaintCalled  = new AtomicBoolean(false);
        restartCalled  = new AtomicBoolean(false);
        menuCalled     = new AtomicBoolean(false);
        settingsCalled = new AtomicBoolean(false);

        // 3) Use TestController to bypass real dialogs
        controller = new TestController(
                stubState,
                () -> repaintCalled.set(true),
                () -> restartCalled.set(true),
                () -> menuCalled.set(true),
                () -> settingsCalled.set(true)
        );
    }

    /** Reflectively grab the Swing Timer inside GameController */
    private Timer getTimer() throws Exception {
        Field f = GameController.class.getDeclaredField("timer");
        f.setAccessible(true);
        return (Timer) f.get(controller);
    }

    /** Reflectively read the private inputLocked flag */
    private boolean isInputLocked() throws Exception {
        Field f = GameController.class.getDeclaredField("inputLocked");
        f.setAccessible(true);
        return f.getBoolean(controller);
    }

    /** Reflectively read the private paused flag */
    private boolean isPaused() throws Exception {
        Field f = GameController.class.getDeclaredField("paused");
        f.setAccessible(true);
        return f.getBoolean(controller);
    }

    @Test
    void start_shouldStartTheTimer() throws Exception {
        controller.start();
        assertTrue(getTimer().isRunning(), "Timer must be running after start()");
    }

    @Test
    void actionPerformed_updatesGameState_andRepaints_andResetsInputLock() throws Exception {
        // Pre-lock input
        Field lock = GameController.class.getDeclaredField("inputLocked");
        lock.setAccessible(true);
        lock.setBoolean(controller, true);

        // Simulate the timer tick
        controller.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "tick"));

        assertTrue(didUpdate.get(), "GameState.update() should be invoked");
        assertTrue(repaintCalled.get(), "repaintCallback should be invoked");
        assertFalse(isInputLocked(), "inputLocked should reset to false after tick");
    }

    @Test
    void actionPerformed_adjustsTimerDelay_basedOnSlowedState() throws Exception {
        // 1) Not slowed
        controller.actionPerformed(new ActionEvent(this, 0, ""));
        int baseDelay = GameSettings.getSpeedDelayFromDifficultyLevel();
        assertEquals(baseDelay, getTimer().getDelay());

        // 2) Slowed = true
        GameState slowedStub = new GameState() {
            @Override public void update()                {}
            @Override public boolean isRunning()           { return true; }
            @Override public boolean isSlowed()            { return true; }
            @Override public boolean isReversedControls()  { return false; }
        };
        controller = new TestController(
                slowedStub,
                () -> repaintCalled.set(true),
                () -> restartCalled.set(true),
                () -> menuCalled.set(true),
                () -> settingsCalled.set(true)
        );
        controller.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals(baseDelay + GameConfig.SLOWDOWN_OFFSET_MS,
                getTimer().getDelay());
    }

    @Test
    void keyPressed_arrowKeys_setDirectionOnce_andLockInput() throws Exception {
        Button dummy = new Button();
        KeyEvent upEvt = new KeyEvent(dummy,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_UP,
                KeyEvent.CHAR_UNDEFINED);

        controller.keyPressed(upEvt);
        assertEquals(Direction.UP, directionSet.get());
        assertTrue(isInputLocked());

        // Second press before next tick is ignored
        directionSet.set(null);
        controller.keyPressed(upEvt);
        assertNull(directionSet.get(), "Direction should not change while inputLocked");
    }

    @Test
    void keyPressed_space_pausesThenResumesImmediately() throws Exception {
        Button dummy = new Button();
        KeyEvent spaceEvt = new KeyEvent(dummy,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_SPACE,
                KeyEvent.CHAR_UNDEFINED);

        controller.keyPressed(spaceEvt);

        // Our TestController override calls resumeGame() right away
        assertFalse(isPaused(), "Controller should no longer be paused");
        assertTrue(getTimer().isRunning(), "Timer must be running after simulated resume");
    }
}
