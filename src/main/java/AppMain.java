import castlesofburgundy.core.*;
import castlesofburgundy.player.*;
import castlesofburgundy.view.ConsoleUi;

import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        long seed = 42L;
        Scanner in = new Scanner(System.in);
        GameUi ui = new ConsoleUi(in);

        GameContext ctx = GameContextFactory.startNewGame(seed);

        Player[] players = PlayerSetup.setupPlayers(ui);

        TurnManager tm = new TurnManager(ctx, ui);
        RoundManager rm = new RoundManager();
        PhaseRunner pr = new PhaseRunner(ctx, rm, tm, ui);

        pr.runAllPhases(players);

        ui.message("\n=== 최종 게임 보드 ===");
        ui.showGameBoard(ctx.layout(), ctx.boardState());

        for (Player p : players) {
            ui.message("\n=== 최종 플레이어 보드 (" + p.getName() + ") ===");
            ui.showPlayer(p);
        }
    }
}
