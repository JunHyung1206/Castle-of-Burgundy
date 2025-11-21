package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.player.Player;
import castlesofburgundy.view.GameBoardConsoleView;
import castlesofburgundy.view.PlayerConsoleView;

public class PhaseRunner {

    private final GameContext ctx;
    private final RoundManager roundManager;
    private final TurnManager turnManager;

    public PhaseRunner(GameContext ctx, RoundManager roundManager, TurnManager turnManager) {
        this.ctx = ctx;
        this.roundManager = roundManager;
        this.turnManager = turnManager;
    }

    public void runAllPhases(Player[] players) {
        for (Phase phase : Phase.values()) {
            runPhase(phase, players);
        }
        System.out.println("\n=== 모든 페이즈 종료 ===");
    }

    public void runPhase(Phase phase, Player[] players) {

        roundManager.startPhase(phase);
        System.out.println("\n================================");
        System.out.println("=== Phase " + phase + " 시작 ===");
        System.out.println("================================");

        BoardState boardState = ctx.boardState();

        while (roundManager.hasMoreRounds()) {
            int round = roundManager.nextRound();
            System.out.println("\n--- Phase " + phase + " / Round " + round + " ---");

            // 라운드 시작 시 시장 상태 출력
            System.out.println(GameBoardConsoleView.render(ctx.layout(), boardState));

            // 각 플레이어 차례대로
            for (int i = 0; i < players.length; i++) {
                Player p = players[i];


                System.out.println("\n현재 보드 상태 (" + p.getName() + ")");
                System.out.println(PlayerConsoleView.render(p));

                turnManager.playerTurn(p);
            }
        }

        System.out.println("\n=== Phase " + phase + " 종료 ===");
    }
}