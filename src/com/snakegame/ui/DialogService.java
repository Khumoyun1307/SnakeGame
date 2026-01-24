package com.snakegame.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Small wrapper around common Swing dialogs used by the game flow.
 *
 * <p>Encapsulates pause/game-over UI to keep controller code focused on orchestration.</p>
 */
public class DialogService {

    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    public static final int NO_OPTION = JOptionPane.NO_OPTION;
    public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
    public static final int CLOSED_OPTION = JOptionPane.CLOSED_OPTION;

    /**
     * Shows the in-game settings dialog (modal).
     *
     * @param parent parent component used to locate the owning window
     */
    public void showInGameSettings(Component parent) {
        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Settings",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        SettingsPanel panel = new SettingsPanel(e -> dlg.dispose());
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    /**
     * Shows the in-game pause menu dialog.
     *
     * @param parent parent component used to locate the owning window
     * @param score current score to display
     * @param onResume callback to resume gameplay
     * @param onOpenSettings callback to open settings
     * @param onSaveQuit callback to save and quit to menu
     * @param onRestart callback to restart the run
     * @param onMainMenu callback to return to the main menu
     * @param onExit callback to exit the application
     */
    public void showPauseDialog(Component parent,
                               int score,
                               Runnable onResume,
                               Runnable onOpenSettings,
                               Runnable onSaveQuit,
                               Runnable onRestart,
                               Runnable onMainMenu,
                               Runnable onExit) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Game Paused",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.DARK_GRAY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("⏸ PAUSED");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setFont(new Font("Consolas", Font.PLAIN, 18));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(20));

        Consumer<JButton> styleButton = btn -> {
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);
        };

        JButton resume = new JButton("▶ Resume");
        styleButton.accept(resume);
        resume.addActionListener(e -> {
            dialog.dispose();
            onResume.run();
        });

        JButton settings = new JButton("⚙ Settings");
        styleButton.accept(settings);
        settings.addActionListener(e -> onOpenSettings.run());

        JButton saveQuit = new JButton("💾 Save & Quit");
        styleButton.accept(saveQuit);
        saveQuit.addActionListener(e -> {
            dialog.dispose();
            onSaveQuit.run();
        });

        JButton restart = new JButton("🔄 Restart");
        styleButton.accept(restart);
        restart.addActionListener(e -> {
            dialog.dispose();
            onRestart.run();
        });

        JButton menu = new JButton("🏠 Main Menu");
        styleButton.accept(menu);
        menu.addActionListener(e -> {
            dialog.dispose();
            onMainMenu.run();
        });

        JButton exit = new JButton("❌ Exit");
        styleButton.accept(exit);
        exit.addActionListener(e -> {
            dialog.dispose();
            onExit.run();
        });

        for (JButton b : new JButton[]{resume, settings, saveQuit, restart, menu, exit}) {
            panel.add(b);
            panel.add(Box.createVerticalStrut(10));
        }

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Shows the game-over dialog.
     *
     * @param finalScore score at the end of the run
     * @param onRestart callback to restart the run
     * @param onSettings callback to open settings
     * @param onMainMenu callback to return to the main menu
     */
    public void showGameOverDialog(int finalScore,
                                  Runnable onRestart,
                                  Runnable onSettings,
                                  Runnable onMainMenu) {
        JDialog dialog = new JDialog(
                (Frame) null,
                "Game Over",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel over = new JLabel("💀 GAME OVER");
        over.setForeground(Color.RED);
        over.setFont(new Font("Ink Free", Font.BOLD, 28));
        over.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Final Score: " + finalScore);
        scoreLabel.setForeground(Color.ORANGE);
        scoreLabel.setFont(new Font("Consolas", Font.PLAIN, 18));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(over);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(20));

        JButton restart = new JButton("🔄 Restart");
        JButton settings = new JButton("⚙ Settings");
        JButton menu = new JButton("🏠 Main Menu");
        JButton exit = new JButton("❌ Exit");

        Consumer<JButton> styleButton = btn -> {
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);
        };

        for (JButton b : new JButton[]{restart, settings, menu, exit}) {
            styleButton.accept(b);
            panel.add(b);
            panel.add(Box.createVerticalStrut(10));
        }

        restart.addActionListener(e -> {
            dialog.dispose();
            onRestart.run();
        });
        settings.addActionListener(e -> {
            dialog.dispose();
            onSettings.run();
        });
        menu.addActionListener(e -> {
            dialog.dispose();
            onMainMenu.run();
        });
        exit.addActionListener(e -> {
            if (confirmExit(dialog, "Are you sure you want to quit?")) {
                System.exit(0);
            }
        });

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Shows a confirmation dialog prompting to save the score before an action.
     *
     * @param parent parent component
     * @param actionVerb verb phrase used in the prompt (e.g., "quitting")
     * @return one of {@link JOptionPane}'s option constants
     */
    public int confirmSaveScore(Component parent, String actionVerb) {
        return JOptionPane.showConfirmDialog(
                parent,
                "Do you want to save your score before " + actionVerb + "?",
                "Save Score?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Shows an exit confirmation dialog.
     *
     * @param parent parent component
     * @param message message to display
     * @return {@code true} if the user confirms exit
     */
    public boolean confirmExit(Component parent, String message) {
        int choice = JOptionPane.showConfirmDialog(
                parent,
                message,
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return choice == JOptionPane.YES_OPTION;
    }
}
