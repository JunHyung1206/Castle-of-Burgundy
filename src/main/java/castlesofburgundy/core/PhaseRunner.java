package castlesofburgundy.core;

import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.TileType;

import java.util.List;

public class PhaseRunner {

    private final GameContext ctx;
    private final RoundManager roundManager;
    private final TurnManager turnManager;
    private final GameUi ui;

    public PhaseRunner(GameContext ctx, RoundManager roundManager, TurnManager turnManager, GameUi ui) {
        this.ctx = ctx;
        this.roundManager = roundManager;
        this.turnManager = turnManager;
        this.ui = ui;
    }

    /** 공용 보드의 턴 순서 트랙에 앉은 플레이어들로 다섯 페이즈를 진행한다. */
    public void runAllPhases() {
        for (Phase phase : Phase.values()) {
            runPhase(phase);
        }
        ui.message("\n=== 모든 페이즈 종료 ===");
    }

    public void runPhase(Phase phase) {

        startNewPhase();
        ui.message("\n================================");
        ui.message("=== Phase " + phase + " 시작 ===");
        ui.message("================================");

        while (roundManager.hasMoreRounds()) {
            int round = roundManager.nextRound();
            ui.message("\n--- Phase " + phase + " / Round " + round + " ---");

            // 흰 주사위: 이번 라운드 상품을 해당 번호 창고에 배치
            placeRoundGoods();

            // 라운드 시작 시 시장 상태 출력
            ui.showGameBoard(ctx.board());

            // 각 플레이어 차례대로
            // 라운드 도중 배로 순서가 바뀌어도 다음 라운드부터 반영된다
            for (Player player : ctx.board().turnOrder().currentOrder()) {
                ui.message("\n현재 보드 상태 (" + player.getName() + ")");
                ui.showPlayer(player);

                turnManager.playerTurn(player, phase);
            }
        }

        endPhase(ctx.board().turnOrder().currentOrder());

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
        ctx.board().depot(whiteDie).addGoods(goods);
        ui.message("흰 주사위 " + whiteDie + " → 창고 " + whiteDie + "에 상품(" + goods + ") 배치");
        return whiteDie;
    }

    /**
     * 페이즈 종료 정산: 영지에 놓인 광산 1개당 은화 1개를 받는다.
     */
    void endPhase(List<Player> players) {
        for (Player player : players) {
            int mines = player.getBoard().countPlacedOfType(TileType.MINE);
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
        ctx.board().refillDepots(ctx.tileSupplier()::draw);
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        roundManager.resetForNewPhase();
    }
}
