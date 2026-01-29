package com.snakegame.ui.flow;

import com.snakegame.mode.GameMode;
import com.snakegame.testutil.SnakeTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuRouterTest extends SnakeTestBase {

    @Test
    void route_returnsStartRunForPlay() {
        MenuCommand cmd = MenuRouter.route("play", new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        assertInstanceOf(MenuCommand.StartRun.class, cmd);
    }

    @Test
    void route_returnsStartRunForRace() {
        MenuCommand cmd = MenuRouter.route("race", new MenuFlowDecider.MenuState(GameMode.STANDARD, 7, false));
        MenuCommand.StartRun start = assertInstanceOf(MenuCommand.StartRun.class, cmd);
        assertEquals(GameMode.RACE, start.decision().modeToSet());
        assertEquals(Integer.valueOf(1), start.decision().selectedMapIdToSet());
    }

    @Test
    void route_returnsNavigateForAiMenu() {
        MenuCommand cmd = MenuRouter.route("aiMenu", new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        MenuCommand.Navigate nav = assertInstanceOf(MenuCommand.Navigate.class, cmd);
        assertEquals(MenuCommand.Destination.AI_MENU, nav.destination());
    }

    @Test
    void route_returnsContinueForContinue() {
        MenuCommand cmd = MenuRouter.route("continue", new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        assertInstanceOf(MenuCommand.Continue.class, cmd);
    }

    @Test
    void route_returnsExitForExit() {
        MenuCommand cmd = MenuRouter.route("exit", new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        assertInstanceOf(MenuCommand.Exit.class, cmd);
    }

    @Test
    void route_returnsNoOpForUnknownCommands() {
        MenuCommand cmd = MenuRouter.route("nope", new MenuFlowDecider.MenuState(GameMode.STANDARD, 1, false));
        assertInstanceOf(MenuCommand.NoOp.class, cmd);
    }
}

