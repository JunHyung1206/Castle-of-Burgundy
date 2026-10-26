package castlesofburgundy.core;

import castlesofburgundy.core.Command.ChooseBoardTile;
import castlesofburgundy.core.Command.ChooseDepotGoods;
import castlesofburgundy.core.Command.ChooseGoodsColor;
import castlesofburgundy.core.Command.PlaceTile;
import castlesofburgundy.core.Command.TakeTile;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 배치 효과 중 선택이 필요한 것(배, 시장·목수·교회, 창고 건물, 성의 추가 행동)과 구역 완성. */
class GamePlacementEffectTest {

    @Test
    @DisplayName("배를 놓으면 상품이 있는 창고를 고르고, 그 상품을 모두 가져온 뒤 턴 순서가 앞으로 간다")
    void shipChoosesDepotAndAdvancesTurnOrder() {
        Player p1 = TestGames.player("P1");
        Player p2 = TestGames.player("P2");
        // 흰 주사위 6, P1 (1,1), P2 (5,5)
        Game game = TestGames.game(List.of(p1, p2), 6, 1, 1, 5, 5);
        game.apply(new Command.TakeWorkers());
        game.apply(new Command.TakeWorkers());

        game.board().depot(2).addGoods(GoodsColor.RED);
        game.board().depot(2).addGoods(GoodsColor.PINK);
        p2.getBoard().getStorage().add(BaseTile.of(TileType.SHIP, 100));
        game.drainEvents();

        // cell 20: SHIP / die 5, 시작 성(19)과 인접
        game.apply(new PlaceTile(5, 0, 20));

        assertThat(game.pending()).isEqualTo(new Decision.ChooseShipDepot());
        assertThat(game.availableActions()).containsExactly(new ChooseDepotGoods(2), new ChooseDepotGoods(6));
        assertThat(game.drainEvents()).isEmpty(); // 효과 선택이 끝나야 행동이 끝난다

        game.apply(new ChooseDepotGoods(2));

        assertThat(game.board().depot(2).goods()).isEmpty();
        assertThat(p2.getBoard().getGoods().count(GoodsColor.RED)).isEqualTo(1);
        assertThat(p2.getBoard().getGoods().count(GoodsColor.PINK)).isEqualTo(1);
        assertThat(game.board().turnOrder().currentOrder()).containsExactly(p2, p1);
        assertThat(game.drainEvents()).startsWith(
                new GameEvent.ShipGoodsTaken(1, 2, 2, 0),
                new GameEvent.TurnOrderAdvanced(1),
                new GameEvent.ActionCompleted());
    }

    @Test
    @DisplayName("상품이 있는 창고가 없어도 배를 놓으면 턴 순서는 앞으로 간다")
    void shipWithoutGoodsStillAdvances() {
        Player player = TestGames.player("P1");
        player.getBoard().getStorage().add(BaseTile.of(TileType.SHIP, 100));
        Game game = TestGames.game(List.of(player), 6, 5, 5);
        game.board().depot(6).takeAllGoods();
        game.drainEvents();

        game.apply(new PlaceTile(5, 0, 20));

        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(5));
        assertThat(game.drainEvents()).startsWith(
                new GameEvent.ShipFoundNoGoods(0), new GameEvent.TurnOrderAdvanced(0));
    }

    @Test
    @DisplayName("시장·목수·교회를 놓으면 허용된 종류의 번호 창고 타일 하나를 가져온다")
    void buildingTakesAllowedBoardTile() {
        for (BuildingKind kind : List.of(BuildingKind.MARKET, BuildingKind.CARPENTER, BuildingKind.CHURCH)) {
            Player player = TestGames.player("P1");
            player.getBoard().getStorage().add(new BuildingTile(100, kind));
            Game game = TestGames.game(List.of(player), 6, 3, 3);

            // cell 26: BUILDING / die 3, 시작 성(19)과 인접
            game.apply(new PlaceTile(3, 0, 26));

            Decision.ChooseBuildingTile decision = (Decision.ChooseBuildingTile) game.pending();
            assertThat(decision.kind()).isEqualTo(kind);
            ChooseBoardTile choice = (ChooseBoardTile) game.availableActions().get(0);
            TileType taken = game.board().depot(choice.depot()).tileAt(choice.slot()).orElseThrow().type();
            assertThat(decision.allowedTypes()).contains(taken);

            game.apply(choice);

            assertThat(player.getBoard().getStorage().view()).singleElement()
                    .extracting(tile -> tile.type()).isEqualTo(taken);
            assertThat(game.drainEvents()).contains(new GameEvent.BuildingTileTaken(kind));
        }
    }

    @Test
    @DisplayName("가져올 타일이 없으면 건물 효과는 건너뛴다")
    void buildingEffectSkippedWithoutCandidates() {
        Player player = TestGames.player("P1");
        player.getBoard().getStorage().add(new BuildingTile(100, BuildingKind.MARKET));
        Game game = TestGames.game(List.of(player), 6, 3, 3);
        TestGames.emptyDepots(game);

        game.apply(new PlaceTile(3, 0, 26));

        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(3));
        assertThat(game.drainEvents()).contains(new GameEvent.BuildingEffectSkipped(BuildingKind.MARKET));
    }

    @Test
    @DisplayName("창고 건물을 놓으면 고른 색 상품을 판다")
    void warehouseSellsChosenColor() {
        Player player = TestGames.player("P1");
        player.getBoard().getGoods().add(GoodsColor.RED);
        player.getBoard().getGoods().add(GoodsColor.RED);
        player.getBoard().getGoods().add(GoodsColor.TEAL);
        player.getBoard().getStorage().add(new BuildingTile(100, BuildingKind.WAREHOUSE));
        Game game = TestGames.game(List.of(player, TestGames.player("P2")), 6, 3, 3);

        game.apply(new PlaceTile(3, 0, 26));
        assertThat(game.availableActions())
                .containsExactly(new ChooseGoodsColor(GoodsColor.RED), new ChooseGoodsColor(GoodsColor.TEAL));

        game.apply(new ChooseGoodsColor(GoodsColor.RED));

        assertThat(player.getBoard().getGoods().count(GoodsColor.RED)).isZero();
        assertThat(player.getSilver()).isEqualTo(1);
        assertThat(player.getScore()).isEqualTo(4);
        assertThat(game.drainEvents()).contains(new GameEvent.GoodsSold(0, GoodsColor.RED, 2, 4, true));
    }

    @Test
    @DisplayName("성을 놓으면 원하는 눈 하나로 일꾼 없이 추가 행동을 한다")
    void castleGrantsFreeExtraAction() {
        Player player = TestGames.player("P1", 7); // 성 군집 {2,3,7}의 7번에서 시작
        player.getBoard().getStorage().add(BaseTile.of(TileType.CASTLE, 100));
        Game game = TestGames.game(List.of(player), 6, 5, 5);

        // cell 2: CASTLE / die 5, 7번과 인접
        game.apply(new PlaceTile(5, 0, 2));

        assertThat(game.pending()).isEqualTo(new Decision.ChooseCastleAction());
        assertThat(game.availableActions())
                .contains(new TakeTile(1, 0, null), new TakeTile(4, 0, null))
                .noneMatch(Command.TakeWorkers.class::isInstance);

        game.apply(new TakeTile(2, 0, null)); // 5에서 먼 눈이어도 일꾼이 들지 않는다

        assertThat(player.getWorkers()).isEqualTo(2);
        assertThat(player.getBoard().getStorage().size()).isEqualTo(1);
        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(5));
    }

    @Test
    @DisplayName("성의 추가 행동으로 할 수 있는 것이 없으면 알리고 넘어간다")
    void castleExtraActionUnavailable() {
        Player player = TestGames.player("P1", 7);
        player.getBoard().getStorage().add(BaseTile.of(TileType.CASTLE, 100));
        Game game = TestGames.game(List.of(player), 6, 5, 5);
        TestGames.emptyDepots(game);

        game.apply(new PlaceTile(5, 0, 2));

        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(5));
        assertThat(game.drainEvents()).contains(new GameEvent.CastleActionUnavailable(0));
    }

    @Test
    @DisplayName("구역을 완성하면 페이즈 보너스와 크기 점수를 알린다")
    void regionCompletion() {
        Player player = TestGames.player("P1");
        // KNOWLEDGE 구역 {4,8,13}을 13 → 8까지 채워 두고 마지막 4(die3)를 놓는다
        player.getBoard().place(13, BaseTile.of(TileType.KNOWLEDGE, 1), 1);
        player.getBoard().place(8, BaseTile.of(TileType.KNOWLEDGE, 2), 5);
        player.getBoard().getStorage().add(BaseTile.of(TileType.KNOWLEDGE, 3));
        Game game = TestGames.game(List.of(player), 6, 3, 3);
        game.drainEvents();

        game.apply(new PlaceTile(3, 0, 4));

        assertThat(player.getScore()).isEqualTo(16); // 페이즈 A(10) + 크기 3(6)
        assertThat(game.drainEvents()).startsWith(
                new GameEvent.RegionCompleted(0, 3, 16, Phase.A), new GameEvent.ActionCompleted());
    }
}
