// AppMain.java
import castlesofburgundy.core.*;
import castlesofburgundy.player.*;
import castlesofburgundy.view.*;

import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        long seed = 42L;

        // 1) 게임 컨텍스트 생성 (시장 보드 + 공급 + 섹션 초기 타일)
        GameContext ctx = GameStarter.startNewGame(seed);

        // 2) 플레이어 생성
        PersonalLayout pl = new PersonalLayout();
        PersonalBoard pb = new PersonalBoard(pl);
        pb.setupInitialCastle(19);   // 시작 성

        Player player = new Player(pb, 3);

        // 3) 라운드/턴 매니저 준비
        Scanner in = new Scanner(System.in);
        TurnManager tm = new TurnManager(ctx, in);
        RoundManager rm = new RoundManager(ctx.boardState(), ctx.tileSupplier());

        // 4) 페이즈 A 시작
        rm.startPhase(Phase.A);
        System.out.println("=== 게임 시작: Phase " + rm.phase() + " ===");

        // 5) 이 페이즈의 5라운드 돌리기
        while (rm.hasMoreRounds()) {
            int round = rm.nextRound();
            System.out.println("\n=== Phase " + rm.phase() + " / Round " + round + " ===");

            // 라운드 시작 시 현재 시장 / 개인 보드 상태 보여주기
            System.out.println(GameBoardConsoleView.render(ctx.layout(), ctx.boardState()));
            System.out.println(PlayerConsoleView.render(player));

            // 이 라운드에서 플레이어 턴(주사위 2개 액션) 수행
            tm.playerTurn(player);
        }

        System.out.println("\n=== Phase " + rm.phase() + " 종료 ===");
        System.out.println(GameBoardConsoleView.render(ctx.layout(), ctx.boardState()));
        System.out.println(PlayerConsoleView.render(player));
    }
}