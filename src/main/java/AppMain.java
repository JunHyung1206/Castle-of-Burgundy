import castlesofburgundy.core.*;
import castlesofburgundy.player.*;
import castlesofburgundy.view.*;

import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        long seed = 42L;
        Scanner in = new Scanner(System.in);

        GameContext ctx = GameContextFactory.startNewGame(seed);

        Player[] players = PlayerSetup.setupPlayers(in, 1); // 나중에 2로 바꾸면 2인플

        TurnManager tm = new TurnManager(ctx, in);
        RoundManager rm = new RoundManager(ctx.boardState(), ctx.tileSupplier());
        PhaseRunner pr = new PhaseRunner(ctx, rm, tm);

        pr.runAllPhases(players);

        System.out.println("\n=== 최종 게임 보드 ===");
        System.out.println(GameBoardConsoleView.render(ctx.layout(), ctx.boardState()));

        for (Player p : players) {
            System.out.println("\n=== 최종 플레이어 보드 (" + p.getName() + ") ===");
            System.out.println(PlayerConsoleView.render(p));
        }
    }
}