package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.config.SettingsSnapshot;
import com.snakegame.model.GameState;
import com.snakegame.replay.ReplayController;
import com.snakegame.replay.ReplayData;
import com.snakegame.replay.ReplayManager;
import com.snakegame.view.GameRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * UI panel for viewing deterministic replays in watch-only mode.
 *
 * <p>Uses a local {@link SettingsSnapshot} from the replay file so that playback does not mutate
 * global {@link GameSettings}.</p>
 */
public class ReplayPanel extends JPanel {

    private final Runnable backToMenu;

    private GameState state;
    private ReplayController controller;

    // WATCH-ONLY: keep replay settings local, do not touch global GameSettings
    private SettingsSnapshot replaySettings;

    private final JComboBox<String> replaySelect;
    private final JButton playPauseBtn;
    private final JButton stepBtn;
    private final JButton restartBtn;
    private final JSlider speedSlider;

    private final JPanel gameCanvas;

    /**
     * Creates the replay panel.
     *
     * @param backToMenu callback invoked when returning to the main menu
     */
    public ReplayPanel(Runnable backToMenu) {
        this.backToMenu = backToMenu;

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // ----- Top controls -----
        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(Color.DARK_GRAY);
        top.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JButton back = new JButton("Back");
        back.setMargin(new Insets(2, 8, 2, 8));
        back.addActionListener(e -> {
            onHide();              // stop replay cleanly
            this.backToMenu.run(); // go back
        });

        replaySelect = new JComboBox<>(new String[]{"Last Game", "Best Game"});
        replaySelect.setPrototypeDisplayValue("Best Game");
        replaySelect.addActionListener(this::onReplaySelected);

        playPauseBtn = new JButton("Play");
        playPauseBtn.setMargin(new Insets(2, 10, 2, 10));
        playPauseBtn.addActionListener(e -> togglePlayPause());

        stepBtn = new JButton("Step");
        stepBtn.setMargin(new Insets(2, 10, 2, 10));
        stepBtn.addActionListener(e -> {
            if (controller != null) controller.stepOnce();
        });

        restartBtn = new JButton("Restart");
        restartBtn.setMargin(new Insets(2, 10, 2, 10));
        restartBtn.addActionListener(e -> reloadCurrent());

        speedSlider = new JSlider(25, 300, 100); // 0.25x .. 3.0x
        speedSlider.setToolTipText("Speed");
        speedSlider.addChangeListener(e -> {
            if (controller != null) {
                double mult = speedSlider.getValue() / 100.0;
                controller.setSpeedMultiplier(mult);
            }
        });

        JPanel speedPanel = new JPanel(new BorderLayout());
        speedPanel.setOpaque(false);
        speedPanel.setBorder(BorderFactory.createTitledBorder("Speed"));
        speedPanel.add(speedSlider, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0;
        top.add(back, gbc);

        gbc.gridx = 1;
        top.add(replaySelect, gbc);

        gbc.gridx = 2;
        top.add(playPauseBtn, gbc);

        gbc.gridx = 3;
        top.add(stepBtn, gbc);

        gbc.gridx = 4;
        top.add(restartBtn, gbc);

        gbc.gridx = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        top.add(speedPanel, gbc);

        add(top, BorderLayout.NORTH);

        // ----- Game canvas -----
        gameCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (state != null) {
                    // WATCH-ONLY: render using replay snapshot settings, not global settings
                    GameRenderer.renderScaleToFit(g, state, getWidth(), getHeight(), replaySettings);
                }
            }
        };
        gameCanvas.setOpaque(true);
        gameCanvas.setBackground(Color.BLACK);
        gameCanvas.setDoubleBuffered(true);

        add(gameCanvas, BorderLayout.CENTER);
    }

    /**
     * Call this every time the Replay screen is shown.
     * Fixes: "quit mid replay and next time replay shows the old position"
     */
    public void onShow() {
        reloadCurrent();                 // ALWAYS start from beginning of selected replay
        playPauseBtn.setText("Play");  // reset button
    }

    /**
     * Call this when leaving Replay screen.
     */
    public void onHide() {
        stopIfRunning();
        playPauseBtn.setText("Play");
    }

    private void stopIfRunning() {
        if (controller != null && controller.isPlaying()) controller.pause();
    }

    private void onReplaySelected(ActionEvent e) {
        reloadCurrent();
    }

    private void reloadCurrent() {
        stopIfRunning();

        String sel = (String) replaySelect.getSelectedItem();
        if ("Best Game".equals(sel)) loadReplay(ReplayManager.loadBest());
        else loadReplay(ReplayManager.loadLast());
    }

    private void loadReplay(Optional<ReplayData> opt) {
        stopIfRunning();
        controller = null;

        if (opt.isEmpty()) {
            state = null;
            replaySettings = null;
            playPauseBtn.setText("Play");
            JOptionPane.showMessageDialog(this, "No replay found for this selection.");
            gameCanvas.repaint();
            return;
        }

        ReplayData data = opt.get();

        // WATCH-ONLY: do NOT restore global GameSettings. Keep snapshot local.
        replaySettings = data.runSettingsSnapshot;

        // Fresh deterministic state => prevents "Game Over persists" and stale state  
        state = new GameState(data.seed, true, replaySettings);
        // Tick speed should match the run (from snapshot) without mutating session settings
        int baseDelay = (replaySettings != null)
                ? GameSettings.speedDelayFromDifficultyLevel(replaySettings.difficultyLevel())
                : GameSettings.getSpeedDelayFromDifficultyLevel();

        state.setTickMs(baseDelay);

        controller = new ReplayController(state, baseDelay, data.events, gameCanvas::repaint);
        controller.setSpeedMultiplier(speedSlider.getValue() / 100.0);

        playPauseBtn.setText("Play");
        gameCanvas.repaint();
    }

    private void togglePlayPause() {
        if (controller == null) return;

        if (controller.isPlaying()) {
            controller.pause();
            playPauseBtn.setText("Play");
        } else {
            controller.play();
            playPauseBtn.setText("Pause");
        }
    }
}




