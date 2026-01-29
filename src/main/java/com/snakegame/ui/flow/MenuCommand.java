package com.snakegame.ui.flow;

/**
 * High-level menu commands produced by {@link MenuRouter} and executed by {@link GameFrameMenuController}.
 */
public sealed interface MenuCommand
        permits MenuCommand.StartRun, MenuCommand.Navigate, MenuCommand.Continue, MenuCommand.Exit, MenuCommand.NoOp {

    /**
     * Starts gameplay using the provided start decision.
     */
    record StartRun(MenuFlowDecider.StartDecision decision) implements MenuCommand {}

    /**
     * Navigates to another screen/panel (non-gameplay).
     */
    record Navigate(Destination destination) implements MenuCommand {}

    /**
     * Continues a previously saved game.
     */
    record Continue() implements MenuCommand {}

    /**
     * Exits the application after confirmation.
     */
    record Exit() implements MenuCommand {}

    /**
     * Represents an unknown/unhandled command (no-op).
     */
    record NoOp() implements MenuCommand {}

    /**
     * Known non-gameplay navigation destinations from the main menu.
     */
    enum Destination {
        AI_MENU,
        MODE,
        REPLAY,
        DIFFICULTY,
        LEADERBOARD,
        SETTINGS,
        STATS,
        DEVELOPER
    }
}

