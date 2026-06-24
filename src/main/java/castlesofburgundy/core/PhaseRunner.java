package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.TileType;

import java.util.List;

public class PhaseRunner {

    private final GameContext ctx;
    private final RoundManager roundManager;
    private final TurnManager turnManager;
    private final GameUi ui;
    private final TurnOrder turnOrder;

    public PhaseRunner(GameContext ctx, RoundManager roundManager, TurnManager turnManager, GameUi ui) {
        this(ctx, roundManager, turnManager, ui, null);
    }

    public PhaseRunner(GameContext ctx, RoundManager roundManager, TurnManager turnManager, GameUi ui, TurnOrder turnOrder) {
        this.ctx = ctx;
        this.roundManager = roundManager;
        this.turnManager = turnManager;
        this.ui = ui;
        this.turnOrder = turnOrder;
    }

    public void runAllPhases(Player[] players) {
        for (Phase phase : Phase.values()) {
            runPhase(phase, players);
        }
        ui.message("\n=== 모든 페이즈 종료 ===");
    }

    public void runPhase(Phase phase, Player[] players) {

        startNewPhase();
        ui.message("\n================================");
        ui.message("=== Phase " + phase + " 시작 ===");
        ui.message("================================");

        BoardState boardState = ctx.boardState();

        while (roundManager.hasMoreRounds()) {
            int round = roundManager.nextRound();
            ui.message("\n--- Phase " + phase + " / Round " + round + " ---");

            // 흰 주사위: 이번 라운드 상품을 해당 번호 창고에 배치
            placeRoundGoods();

            // 라운드 시작 시 시장 상태 출력
            ui.showGameBoard(ctx.layout(), boardState);

            // 각 플레이어 차례대로
            for (Player player : playersForRound(players)) {
                ui.message("\n현재 보드 상태 (" + player.getName() + ")");
                ui.showPlayer(player);

                turnManager.playerTurn(player, phase);
            }
        }

        endPhase(players);

        ui.message("\n=== Phase " + phase + " 종료 ===");
    }

    /**
     * 흰 주사위 단계: 주사위 눈에 해당하는 창고에 이번 라운드 상품 한 개를 놓는다.
     *
     * @return 상품을 놓은 창고 번호(1~6)
     */
    int placeRoundGoods() {
        int whiteDie = ctx.dice().roll();
        GoodsColor goods = ctx.goodsSupply().draw();
        ctx.boardState().addGoods(whiteDie, goods);
        ui.message("흰 주사위 " + whiteDie + " → 창고 " + whiteDie + "에 상품(" + goods + ") 배치");
        return whiteDie;
    }

    /**
     * 페이즈 종료 정산: 영지에 놓인 광산 1개당 은화 1개를 받는다.
     */
    void endPhase(Player[] players) {
        for (Player player : players) {
            int mines = player.countPlacedOfType(TileType.MINE);
            if (mines > 0) {
                player.addSilver(mines);
                ui.message(player.getName() + ": 광산 " + mines + "개 → 은화 +" + mines);
            }
        }
    }

    /**
     * 페이즈 시작 준비: 보드의 육각 타일을 전부 교체하고(공급원에서 다시 채움)
     * 라운드 카운터를 초기화한다.
     */
    private void startNewPhase() {
        ctx.boardState().fillAllFromSupply(ctx.tileSupplier());
        roundManager.resetForNewPhase();
    }

    private List<Player> playersForRound(Player[] players) {
        if (turnOrder == null) {
            return List.of(players);
        }
        return turnOrder.currentOrder();
    }
}
