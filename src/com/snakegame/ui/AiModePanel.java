package com.snakegame.ui;

import com.snakegame.ai.AiMode;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Menu panel that lets the user choose an {@link AiMode} and start an AI run.
 */
public class AiModePanel extends JPanel {

    /**
     * Creates the AI mode selection panel.
     *
     * @param onBack callback invoked when navigating back
     * @param onStart callback invoked with the selected mode to begin an AI run
     */
    public AiModePanel(Runnable onBack, Consumer<AiMode> onStart) {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("🤖 AI Modes");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JButton chase = new JButton("⚡ Chase (Fast A*)");
        JButton safe  = new JButton("🛡 Safe (Avoid traps)");
        JButton surv  = new JButton("🏆 Survival (Max safe)");
        JButton back  = new JButton("⬅ Back");

        chase.addActionListener(e -> onStart.accept(AiMode.CHASE));
        safe.addActionListener(e -> onStart.accept(AiMode.SAFE));
        surv.addActionListener(e -> onStart.accept(AiMode.SURVIVAL));
        back.addActionListener(e -> onBack.run());

        gbc.gridy = 0; add(title, gbc);
        gbc.gridy = 1; add(chase, gbc);
        gbc.gridy = 2; add(safe, gbc);
        gbc.gridy = 3; add(surv, gbc);
        gbc.gridy = 4; add(back, gbc);
    }
}
