import castlesofburgundy.core.*;
import castlesofburgundy.player.*;
import castlesofburgundy.view.*;

import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        long seed = 42L;

        GameContext ctx = GameContextFactory.startNewGame(seed);


        PersonalBoard pb1 = new PersonalBoard(new PersonalLayout());
        pb1.setupInitialCastle(19);
        Player p1 = new Player(pb1, 3, "플레이어1");

        Player[] players = {p1};

        TurnManager tm = new TurnManager(ctx, new Scanner(System.in));
        RoundManager rm = new RoundManager(ctx.boardState(), ctx.tileSupplier());
        PhaseRunner pr = new PhaseRunner(ctx, rm, tm);

        pr.runAllPhases(players);

        // 최종 상태 출력
        System.out.println("\n=== 최종 시장 보드 ===");
        System.out.println(GameBoardConsoleView.render(ctx.layout(), ctx.boardState()));
        System.out.println("\n=== 최종 플레이어1 보드 ===");
        System.out.println(PlayerConsoleView.render(p1));
    }
}
