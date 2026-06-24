package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
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
        GameBoardLayout layout = new GameBoardLayout();
        BoardState boardState = new BoardState(layout);
        TileSupplier supplier = new TileSupplier(1L);
        return new GameContext(1L, layout, boardState, supplier, new Dice(1L), new GoodsSupply(1L));
    }

    private Player newPlayer(String name) {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        return new Player(board, 3, name);
    }

    @Test
    @DisplayName("주사위 눈에 맞는 상품을 가지고 있으면 상품 판매 액션이 생긴다")
    void buildOptionsIncludesSellGoods() {
        GameContext ctx = emptyContext();
        RecordingGameUi ui = new RecordingGameUi();
        TurnManager tm = new TurnManager(ctx, ui, 3);
        Player player = newPlayer("P1");
        player.addGoods(GoodsColor.RED);
        player.addGoods(GoodsColor.RED);

        List<TurnManager.Action> options = tm.buildOptionsForDie(player, 1, Phase.A);

        TurnManager.Action sellRed = options.stream()
                .filter(action -> action.description().contains("RED 상품 판매"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("RED 상품 판매 액션이 있어야 한다"));

        boolean extraAction = sellRed.apply().getAsBoolean();

        assertThat(extraAction).isFalse();
        assertThat(player.goods().count(GoodsColor.RED)).isZero();
        assertThat(player.getSilver()).isEqualTo(1);
        assertThat(player.getScore()).isEqualTo(6);
    }

    @Test
    @DisplayName("배를 놓으면 선택한 창고의 상품을 모두 가져오고 턴 순서가 한 칸 앞으로 간다")
    void shipPlacementTakesGoodsAndAdvancesTurnOrder() {
        GameContext ctx = emptyContext();
        ctx.boardState().addGoods(2, GoodsColor.RED);
        ctx.boardState().addGoods(2, GoodsColor.PINK);

        Player p1 = newPlayer("P1");
        Player p2 = newPlayer("P2");
        p2.addToStorage(BaseTile.of(TileType.SHIP, 100));

        TurnOrder turnOrder = new TurnOrder(new Player[]{p1, p2});
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0);
        TurnManager tm = new TurnManager(ctx, ui, 2, turnOrder);

        TurnManager.Action placeShip = tm.buildOptionsForDie(p2, 5, Phase.A).stream()
                .filter(action -> action.description().contains("SHIP")
                        && action.description().contains("cell 20"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("cell 20에 배를 놓는 액션이 있어야 한다"));

        placeShip.apply().getAsBoolean();

        assertThat(ctx.boardState().goodsAt(2)).isEmpty();
        assertThat(p2.goods().count(GoodsColor.RED)).isEqualTo(1);
        assertThat(p2.goods().count(GoodsColor.PINK)).isEqualTo(1);
        assertThat(turnOrder.currentOrder()).containsExactly(p2, p1);
    }
}
