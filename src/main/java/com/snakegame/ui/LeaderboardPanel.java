package com.snakegame.ui;

import com.snakegame.config.GameSettings;
import com.snakegame.mode.GameMode;
import com.snakegame.net.LeaderboardClient;
import com.snakegame.net.LeaderboardModels.LeaderboardEntry;
import com.snakegame.net.LeaderboardModels.LeaderboardResponse;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * UI panel for browsing the online leaderboard with filters and pagination.
 */
public class LeaderboardPanel extends JPanel {

    private final Runnable onBack;
    private final LeaderboardClient client = new LeaderboardClient();

    private final JLabel mapLabel;
    private final JComboBox<Integer> mapBox;
    private final JComboBox<String> modeBox;
    private final JComboBox<String> diffBox;

    private final JLabel statusLabel = new JLabel(" ");
    private final JButton refreshButton = new JButton("⟳ Refresh");
    private final JButton prevButton = new JButton("◀ Prev");
    private final JButton nextButton = new JButton("Next ▶");

    private final DefaultTableModel tableModel;

    private boolean initialized = false;
    private boolean updatingMapBox = false;
    private ActionListener mapBoxListener;

    private boolean mapEnabledByMode = false;

    private static final DateTimeFormatter WHEN_FMT =
            DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault());

    // Pagination
    private int pageSize = 25;
    private int offset = 0;
    private int lastReturnedCount = 0;

    /**
     * Creates the leaderboard panel.
     *
     * @param onBack callback invoked when navigating back
     */
    public LeaderboardPanel(Runnable onBack) {
        this.onBack = onBack;

        setLayout(new BorderLayout(12, 12));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("🏆 ONLINE LEADERBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Consolas", Font.BOLD, 22));
        title.setForeground(new Color(255, 140, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        filters.setOpaque(false);

        mapLabel = label("Map:", new Color(255, 80, 80));
        filters.add(mapLabel);

        mapBox = new JComboBox<>(new Integer[]{0});
        styleCombo(mapBox);

        mapBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                String text;
                if (value instanceof Integer i) {
                    if (i == 0) {
                        String mode = (String) modeBox.getSelectedItem();
                        if (mode != null && mode.equals(GameMode.MAP_SELECT.name())) {
                            text = "ANY";
                        } else {
                            text = "Basic";
                        }
                    } else {
                        text = String.valueOf(i);
                    }
                } else {
                    text = String.valueOf(value);
                }
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });


        filters.add(mapBox);

        filters.add(label("Mode:", new Color(255, 80, 80)));
        modeBox = new JComboBox<>(new String[]{
                GameMode.STANDARD.name(),
                GameMode.MAP_SELECT.name(),
                GameMode.RACE.name()
        });
        styleCombo(modeBox);
        filters.add(modeBox);

        filters.add(label("Diff:", new Color(255, 80, 80)));
        diffBox = new JComboBox<>(new String[]{
                "ANY",
                GameSettings.Difficulty.EASY.name(),
                GameSettings.Difficulty.NORMAL.name(),
                GameSettings.Difficulty.HARD.name(),
                GameSettings.Difficulty.EXPERT.name(),
                GameSettings.Difficulty.INSANE.name()
        });
        styleCombo(diffBox);
        diffBox.setSelectedItem("ANY");
        filters.add(diffBox);

        top.add(filters, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Rank", "Name", "Score", "Time", "When"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Consolas", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 14));
        table.setBackground(new Color(18, 18, 18));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(30, 30, 30));
        table.getTableHeader().setForeground(new Color(255, 180, 80));
        table.setGridColor(new Color(60, 60, 60));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(18, 18, 18));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        add(scroll, BorderLayout.CENTER);

        JButton backButton = new JButton("← Back");
        styleButton(backButton);
        backButton.addActionListener(e -> onBack.run());

        styleButton(refreshButton);
        styleButton(prevButton);
        styleButton(nextButton);

        refreshButton.addActionListener(e -> {
            offset = 0;
            refresh();
        });

        prevButton.addActionListener(e -> {
            offset = Math.max(0, offset - pageSize);
            refresh();
        });

        nextButton.addActionListener(e -> {
            if (lastReturnedCount < pageSize) return;
            offset = offset + pageSize;
            refresh();
        });

        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(prevButton);
        rightButtons.add(nextButton);
        rightButtons.add(refreshButton);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(backButton, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);
        bottom.add(rightButtons, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        modeBox.addActionListener(e -> {
            if (!initialized) return;
            offset = 0;
            updateMapOptionsForSelectedMode();
            refresh();
        });

        mapBox.addActionListener(mapBoxListener = e -> {
            if (!initialized || updatingMapBox) return;
            offset = 0;
            String mode = (String) modeBox.getSelectedItem();
            if (mode != null && mode.equals(GameMode.MAP_SELECT.name())) {
                Integer mapIdObj = (Integer) mapBox.getSelectedItem();
                if (mapIdObj != null && mapIdObj > 0) {
                    GameSettings.setSelectedMapId(mapIdObj);
                }
            }
            updateTableColumnsForCurrentFilters();
            refresh();
        });

        diffBox.addActionListener(e -> {
            if (!initialized) return;
            offset = 0;
            updateTableColumnsForCurrentFilters();
            refresh();
        });

        modeBox.setSelectedItem(GameMode.STANDARD.name());
        updateMapOptionsForSelectedMode();

        initialized = true;
        refresh();
    }

    private void updateMapOptionsForSelectedMode() {
        String mode = (String) modeBox.getSelectedItem();
        if (mode == null) mode = GameMode.STANDARD.name();

        updatingMapBox = true;

        if (mode.equals(GameMode.STANDARD.name())) {
            // STANDARD is "Basic" only (no map filtering)
            DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();
            model.addElement(0); // show "Basic" doesn't apply anymore; in STANDARD we hide map anyway?
            mapBox.setModel(model);
            mapBox.setSelectedIndex(0);

            mapEnabledByMode = false;
            mapBox.setEnabled(false);

            mapLabel.setVisible(true);
            mapBox.setVisible(true);
            mapBox.setToolTipText("STANDARD uses the basic map.");
        } else if (mode.equals(GameMode.MAP_SELECT.name())) {
            // MAP_SELECT supports ANY + 1..10
            DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>();
            model.addElement(0); // ANY
            for (int i = 1; i <= 10; i++) model.addElement(i);
            mapBox.setModel(model);

            // Default to saved map, but keep it valid
            int selectedMapId = GameSettings.getSelectedMapId();
            if (selectedMapId < 1 || selectedMapId > 10) selectedMapId = 1;
            mapBox.setSelectedItem(selectedMapId);

            mapEnabledByMode = true;
            mapBox.setEnabled(true);

            mapLabel.setVisible(true);
            mapBox.setVisible(true);
            mapBox.setToolTipText("Choose a map, or ANY to view all maps.");
        } else if (mode.equals(GameMode.RACE.name())) {
            mapEnabledByMode = false;
            mapBox.setEnabled(false);

            mapLabel.setVisible(false);
            mapBox.setVisible(false);
            mapBox.setToolTipText(null);
        }

        updateTableColumnsForCurrentFilters();

        updatingMapBox = false;
        revalidate();
        repaint();
    }

    private void updateTableColumnsForCurrentFilters() {
        clearTable();

        String mode = (String) modeBox.getSelectedItem();
        if (mode == null) mode = GameMode.STANDARD.name();

        String diff = (String) diffBox.getSelectedItem();
        if (diff == null) diff = "ANY";

        Integer mapIdObj = (Integer) mapBox.getSelectedItem();
        int mapId = (mapIdObj == null) ? 0 : mapIdObj;

        boolean race = mode.equals(GameMode.RACE.name());
        boolean anyDiff = diff.equalsIgnoreCase("ANY");
        boolean mapSelectAnyMap = mode.equals(GameMode.MAP_SELECT.name()) && mapId == 0;

        if (race && anyDiff) {
            tableModel.setColumnIdentifiers(new Object[]{"Rank", "Name", "Map", "Diff", "Score", "Time", "When"});
        } else if (race) {
            tableModel.setColumnIdentifiers(new Object[]{"Rank", "Name", "Map", "Score", "Time", "When"});
        } else if (mapSelectAnyMap && anyDiff) {
            tableModel.setColumnIdentifiers(new Object[]{"Rank", "Name", "Map", "Diff", "Score", "Time", "When"});
        } else if (mapSelectAnyMap) {
            tableModel.setColumnIdentifiers(new Object[]{"Rank", "Name", "Map", "Score", "Time", "When"});
        } else if (anyDiff) {
            tableModel.setColumnIdentifiers(new Object[]{"Rank", "Name", "Diff", "Score", "Time", "When"});
        } else {
            tableModel.setColumnIdentifiers(new Object[]{"Rank", "Name", "Score", "Time", "When"});
        }
    }

    private JLabel label(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setForeground(c);
        l.setFont(new Font("Consolas", Font.BOLD, 14));
        return l;
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(new Font("Consolas", Font.PLAIN, 14));
        box.setBackground(new Color(30, 30, 30));
        box.setForeground(Color.WHITE);
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Consolas", Font.BOLD, 14));
        b.setBackground(new Color(30, 30, 30));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    /**
     * Fetches leaderboard data using the current UI filter selections and loads it into the table.
     */
    public void refresh() {
        Integer mapIdObj = (Integer) mapBox.getSelectedItem();
        int mapId = (mapIdObj == null) ? 0 : mapIdObj;

        String mode = (String) modeBox.getSelectedItem();
        if (mode == null) mode = GameMode.STANDARD.name();

        String diff = (String) diffBox.getSelectedItem();
        if (diff == null) diff = "ANY";

        if (mode.equals(GameMode.RACE.name())) {
            mapId = 0;
        }

        updateTableColumnsForCurrentFilters();
        setLoading(true, "Loading leaderboard...");

        client.fetchLeaderboardAsync(mapId, mode, diff, pageSize, offset)
                .thenAccept(resp -> SwingUtilities.invokeLater(() -> {
                    loadIntoTable(resp);
                    setLoading(false, "Showing " + (offset + 1) + "–" + (offset + Math.max(lastReturnedCount, 0)));
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        clearTable();
                        addEmptyRowForCurrentColumns();
                        lastReturnedCount = 0;
                        updatePagerButtons();
                        setLoading(false, "Failed to load (offline?)");
                    });
                    return null;
                });
    }

    private void setLoading(boolean loading, String msg) {
        statusLabel.setText(msg);
        refreshButton.setEnabled(!loading);
        prevButton.setEnabled(!loading && offset > 0);
        nextButton.setEnabled(!loading && lastReturnedCount == pageSize);

        modeBox.setEnabled(!loading);
        diffBox.setEnabled(!loading);
        mapBox.setEnabled(!loading && mapEnabledByMode);
    }

    private void updatePagerButtons() {
        prevButton.setEnabled(offset > 0);
        nextButton.setEnabled(lastReturnedCount == pageSize);
    }

    private void addEmptyRowForCurrentColumns() {
        int cols = tableModel.getColumnCount();
        Object[] row = new Object[cols];
        if (cols > 0) row[0] = "-";
        if (cols > 1) row[1] = "No scores yet";
        for (int i = 2; i < cols; i++) row[i] = "-";
        tableModel.addRow(row);
    }

    private void loadIntoTable(LeaderboardResponse resp) {
        clearTable();

        String mode = (String) modeBox.getSelectedItem();
        if (mode == null) mode = GameMode.STANDARD.name();

        String diff = (String) diffBox.getSelectedItem();
        if (diff == null) diff = "ANY";

        Integer mapIdObj = (Integer) mapBox.getSelectedItem();
        int mapId = (mapIdObj == null) ? 0 : mapIdObj;

        boolean race = mode.equals(GameMode.RACE.name());
        boolean anyDiff = diff.equalsIgnoreCase("ANY");
        boolean mapSelectAnyMap = mode.equals(GameMode.MAP_SELECT.name()) && mapId == 0;

        if (resp == null || resp.entries() == null || resp.entries().isEmpty()) {
            lastReturnedCount = 0;
            addEmptyRowForCurrentColumns();
            updatePagerButtons();
            return;
        }

        lastReturnedCount = resp.entries().size();

        for (LeaderboardEntry e : resp.entries()) {
            String time = (e.timeSurvivedMs() <= 0) ? "-" : (e.timeSurvivedMs() / 1000) + "s";
            String when = (e.createdAt() == null) ? "-" : WHEN_FMT.format(e.createdAt());
            String entryDiff = (e.difficulty() == null || e.difficulty().isBlank()) ? "-" : e.difficulty();

            if (race && anyDiff) {
                tableModel.addRow(new Object[]{ e.rank(), e.playerName(), e.mapId(), entryDiff, e.score(), time, when });
            } else if (race) {
                tableModel.addRow(new Object[]{ e.rank(), e.playerName(), e.mapId(), e.score(), time, when });
            } else if (mapSelectAnyMap && anyDiff) {
                tableModel.addRow(new Object[]{ e.rank(), e.playerName(), e.mapId(), entryDiff, e.score(), time, when });
            } else if (mapSelectAnyMap) {
                tableModel.addRow(new Object[]{ e.rank(), e.playerName(), e.mapId(), e.score(), time, when });
            } else if (anyDiff) {
                tableModel.addRow(new Object[]{ e.rank(), e.playerName(), entryDiff, e.score(), time, when });
            } else {
                tableModel.addRow(new Object[]{ e.rank(), e.playerName(), e.score(), time, when });
            }
        }

        updatePagerButtons();
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }
}
