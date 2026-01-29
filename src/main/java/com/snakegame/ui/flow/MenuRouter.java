package com.snakegame.ui.flow;

import java.util.Objects;

/**
 * Pure router that maps a menu action command to a higher-level {@link MenuCommand}.
 */
public final class MenuRouter {
    private MenuRouter() {}

    /**
     * Routes a Swing action command from the main menu to a {@link MenuCommand}.
     *
     * @param actionCommand action command from Swing
     * @param state current menu state snapshot
     * @return routed command (possibly {@link MenuCommand.NoOp})
     */
    public static MenuCommand route(String actionCommand, MenuFlowDecider.MenuState state) {
        if (actionCommand == null) return new MenuCommand.NoOp();
        Objects.requireNonNull(state, "state");

        return switch (actionCommand) {
            case "play" -> new MenuCommand.StartRun(MenuFlowDecider.decidePlay(state));
            case "race" -> new MenuCommand.StartRun(MenuFlowDecider.decideRace(state));

            case "aiMenu" -> new MenuCommand.Navigate(MenuCommand.Destination.AI_MENU);
            case "mode" -> new MenuCommand.Navigate(MenuCommand.Destination.MODE);
            case "replay" -> new MenuCommand.Navigate(MenuCommand.Destination.REPLAY);
            case "difficulty" -> new MenuCommand.Navigate(MenuCommand.Destination.DIFFICULTY);
            case "leaderboard" -> new MenuCommand.Navigate(MenuCommand.Destination.LEADERBOARD);
            case "settings" -> new MenuCommand.Navigate(MenuCommand.Destination.SETTINGS);
            case "stats" -> new MenuCommand.Navigate(MenuCommand.Destination.STATS);
            case "developer" -> new MenuCommand.Navigate(MenuCommand.Destination.DEVELOPER);

            case "continue" -> new MenuCommand.Continue();
            case "exit" -> new MenuCommand.Exit();

            default -> new MenuCommand.NoOp();
        };
    }
}

