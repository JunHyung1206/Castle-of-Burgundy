package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.board.TurnOrder;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.goods.GoodsSupply;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurnManagerGoodsTest {

    private GameContext emptyContext() {
        GameBoard board = new GameBoard();
        TileSupplier supplier = new TileSupplier(1L);
        return new GameContext(1L, board, supplier, new Dice(1L), new GoodsSupply(1L));
    }

    private Player newPlayer(String name) {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        return new Player(board, name);
    }

    @Test
    @DisplayName("주사위 눈에 맞는 상품을 가지고 있으면 상품 판매 액션이 생긴다")
    void buildOptionsIncludesSellGoods() {
        GameContext ctx = emptyContext();
        RecordingGameUi ui = new RecordingGameUi();
        TurnManager tm = new TurnManager(ctx, ui, 3);
        Player player = newPlayer("P1");
        player.getBoard().getGoods().add(GoodsColor.RED);
        player.getBoard().getGoods().add(GoodsColor.RED);

        List<TurnManager.Action> options = tm.buildOptionsForDie(player, 1, Phase.A, new Turn());

        TurnManager.Action sellRed = options.stream()
                .filter(action -> action.description().contains("RED 상품 판매"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("RED 상품 판매 액션이 있어야 한다"));

        boolean extraAction = sellRed.apply().getAsBoolean();

        assertThat(extraAction).isFalse();
        assertThat(player.getBoard().getGoods().count(GoodsColor.RED)).isZero();
        assertThat(player.getSilver()).isEqualTo(1);
        assertThat(player.getScore()).isEqualTo(6);
    }

    @Test
    @DisplayName("은화 2개와 중앙 시장 타일이 있으면 구매 액션이 생긴다")
    void buildOptionsIncludesCentralMarketPurchase() {
        GameContext ctx = emptyContext();
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        RecordingGameUi ui = new RecordingGameUi();
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player player = newPlayer("P1");
        player.addSilver(2);

        TurnManager.Action purchase = tm.buildOptionsForDie(player, 1, Phase.A, new Turn()).stream()
                .filter(action -> action.description().contains("중앙 시장[0]")
                        && action.description().contains("은화 2개"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("중앙 시장 구매 액션이 있어야 한다"));

        boolean extraAction = purchase.apply().getAsBoolean();

        assertThat(extraAction).isFalse();
        assertThat(player.getSilver()).isZero();
        assertThat(player.getBoard().getStorage().size()).isEqualTo(1);
        assertThat(ctx.board().centralMarket()).hasSize(7);
    }

    @Test
    @DisplayName("은화가 2개 미만이면 중앙 시장 구매 액션은 생기지 않는다")
    void buildOptionsExcludesCentralMarketPurchaseWithoutSilver() {
        GameContext ctx = emptyContext();
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        TurnManager tm = new TurnManager(ctx, new RecordingGameUi(), 2);
        Player player = newPlayer("P1");
        player.addSilver(1);

        List<TurnManager.Action> options = tm.buildOptionsForDie(player, 1, Phase.A, new Turn());

        assertThat(options).noneMatch(action -> action.description().contains("중앙 시장"));
    }

    @Test
    @DisplayName("저장소가 가득 차 있어도 버릴 타일을 고르면 중앙 시장 타일을 구매할 수 있다")
    void buildOptionsCentralMarketPurchaseReplacesStoredTile() {
        GameContext ctx = emptyContext();
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        TurnManager tm = new TurnManager(ctx, new RecordingGameUi(), 2);
        Player player = newPlayer("P1");
        player.addSilver(2);
        player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 1));
        player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 2));
        player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 3));

        TurnManager.Action purchase = tm.buildOptionsForDie(player, 1, Phase.A, new Turn()).stream()
                .filter(action -> action.description().contains("저장소[1] 버리고")
                        && action.description().contains("중앙 시장[0]"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("저장소를 비우고 중앙 시장 타일을 구매하는 액션이 있어야 한다"));

        purchase.apply().getAsBoolean();

        assertThat(player.getSilver()).isZero();
        assertThat(player.getBoard().getStorage().size()).isEqualTo(3);
        assertThat(ctx.board().centralMarket()).hasSize(7);
    }

    @Test
    @DisplayName("배를 놓으면 선택한 창고의 상품을 모두 가져오고 턴 순서가 한 칸 앞으로 간다")
    void shipPlacementTakesGoodsAndAdvancesTurnOrder() {
        GameContext ctx = emptyContext();
        ctx.board().depot(2).addGoods(GoodsColor.RED);
        ctx.board().depot(2).addGoods(GoodsColor.PINK);

        Player p1 = newPlayer("P1");
        Player p2 = newPlayer("P2");
        p2.getBoard().getStorage().add(BaseTile.of(TileType.SHIP, 100));

        TurnOrder turnOrder = ctx.board().turnOrder();
        turnOrder.seat(List.of(p1, p2));
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0);
        TurnManager tm = new TurnManager(ctx, ui, 2);

        TurnManager.Action placeShip = tm.buildOptionsForDie(p2, 5, Phase.A, new Turn()).stream()
                .filter(action -> action.description().contains("SHIP")
                        && action.description().contains("cell 20"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("cell 20에 배를 놓는 액션이 있어야 한다"));

        placeShip.apply().getAsBoolean();

        assertThat(ctx.board().depot(2).goods()).isEmpty();
        assertThat(p2.getBoard().getGoods().count(GoodsColor.RED)).isEqualTo(1);
        assertThat(p2.getBoard().getGoods().count(GoodsColor.PINK)).isEqualTo(1);
        assertThat(turnOrder.currentOrder()).containsExactly(p2, p1);
    }
}
