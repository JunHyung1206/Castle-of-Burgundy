package castlesofburgundy.core;

import castlesofburgundy.core.Command.BuyCentralMarket;
import castlesofburgundy.core.Command.EndTurn;
import castlesofburgundy.core.Command.PlaceTile;
import castlesofburgundy.core.Command.SellGoods;
import castlesofburgundy.core.Command.TakeTile;
import castlesofburgundy.core.Command.TakeWorkers;
import castlesofburgundy.core.Command.UseDie;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 주사위 행동(가져오기·배치·판매·일꾼)과 중앙 시장 구매. */
class GameDieActionTest {

    @Test
    @DisplayName("주사위 눈의 창고에 타일이 있으면 가져오기, 일꾼 받기는 항상 할 수 있다")
    void takeTileAndTakeWorkersOffered() {
        Player player = TestGames.player("P1", 19);
        Game game = TestGames.game(List.of(player), 6, 1, 1);

        assertThat(game.availableActions())
                .contains(new TakeTile(1, 0, null), new TakeTile(1, 1, null), new TakeWorkers());

        Tile onBoard = game.board().depot(1).tileAt(0).orElseThrow();
        game.apply(new TakeTile(1, 0, null));

        assertThat(player.getBoard().getStorage().view()).containsExactly(onBoard);
        assertThat(game.board().depot(1).tileAt(0)).isEmpty();
    }

    @Test
    @DisplayName("저장소가 가득 차면 버릴 칸마다 가져오기 선택지가 생기고, 고른 칸을 버리고 가져온다")
    void takeTileWithFullStorageDiscards() {
        Player player = TestGames.player("P1");
        Tile keep0 = BaseTile.of(TileType.ANIMAL, 1);
        Tile drop = BaseTile.of(TileType.ANIMAL, 2);
        Tile keep2 = BaseTile.of(TileType.ANIMAL, 3);
        player.getBoard().getStorage().add(keep0);
        player.getBoard().getStorage().add(drop);
        player.getBoard().getStorage().add(keep2);
        Game game = TestGames.game(List.of(player), 6, 1, 1);

        assertThat(game.availableActions())
                .doesNotContain(new TakeTile(1, 0, null))
                .contains(new TakeTile(1, 0, 0), new TakeTile(1, 0, 1), new TakeTile(1, 0, 2));

        Tile onBoard = game.board().depot(1).tileAt(0).orElseThrow();
        game.apply(new TakeTile(1, 0, 1));

        assertThat(player.getBoard().getStorage().view()).containsExactly(keep0, keep2, onBoard);
    }

    @Test
    @DisplayName("저장소 타일을 주사위 눈에 맞는 인접 칸에 놓을 수 있다")
    void placeFromStorage() {
        Player player = TestGames.player("P1");
        player.getBoard().getStorage().add(BaseTile.of(TileType.KNOWLEDGE, 100));
        Game game = TestGames.game(List.of(player), 6, 1, 1);

        assertThat(game.availableActions()).contains(new PlaceTile(1, 0, 13));

        game.apply(new PlaceTile(1, 0, 13));

        assertThat(player.getBoard().isOccupied(13)).isTrue();
        assertThat(player.getBoard().getStorage().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("일꾼으로 바꾼 눈의 행동은 바꾼 만큼 일꾼을 쓴다 (1↔6 포함)")
    void adjustedDieSpendsWorkers() {
        Player player = TestGames.player("P1"); // 일꾼 2
        Game game = TestGames.game(List.of(player), 6, 1, 1);

        assertThat(game.availableActions())
                .contains(new TakeTile(6, 0, null), new TakeTile(2, 0, null), new TakeTile(5, 0, null), new TakeTile(3, 0, null))
                .doesNotContain(new TakeTile(4, 0, null)); // 1 → 4는 일꾼 3개

        game.apply(new TakeTile(6, 0, null));

        assertThat(player.getWorkers()).isEqualTo(1);
    }

    @Test
    @DisplayName("주사위 눈의 색 상품을 모두 팔면 은화 1개와 개수 × 인원수 점수를 얻는다")
    void sellGoods() {
        Player p1 = TestGames.player("P1");
        Player p2 = TestGames.player("P2");
        Player p3 = TestGames.player("P3");
        p1.getBoard().getGoods().add(GoodsColor.RED);
        p1.getBoard().getGoods().add(GoodsColor.RED);
        Game game = TestGames.game(List.of(p1, p2, p3), 6, 1, 1);
        game.drainEvents();

        game.apply(new SellGoods(1));

        assertThat(p1.getBoard().getGoods().count(GoodsColor.RED)).isZero();
        assertThat(p1.getSilver()).isEqualTo(1);
        assertThat(p1.getScore()).isEqualTo(6);
        assertThat(game.drainEvents()).first().isEqualTo(new GameEvent.GoodsSold(p1, GoodsColor.RED, 2, 6, false));
    }

    @Test
    @DisplayName("은화가 2개 이상일 때만 중앙 시장 구매가 보인다")
    void centralMarketNeedsTwoSilver() {
        Player player = TestGames.player("P1");
        player.addSilver(1);
        Game game = TestGames.game(List.of(player), 6, 1, 1);

        assertThat(game.availableActions()).noneMatch(BuyCentralMarket.class::isInstance);

        player.addSilver(1);
        assertThat(game.availableActions()).contains(new BuyCentralMarket(0, null));
    }

    @Test
    @DisplayName("중앙 시장 구매는 주사위를 쓰지 않고, 한 턴에 한 번뿐이다")
    void centralMarketDoesNotUseDieAndOncePerTurn() {
        Player player = TestGames.player("P1");
        player.addSilver(4);
        Game game = TestGames.game(List.of(player), 6, 1, 1);
        Tile bought = game.board().centralMarket().get(0);

        game.apply(new BuyCentralMarket(0, null));

        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(1));
        assertThat(player.getSilver()).isEqualTo(2);
        assertThat(player.getBoard().getStorage().view()).containsExactly(bought);
        assertThat(game.board().centralMarket()).hasSize(7);
        assertThat(game.availableActions()).noneMatch(BuyCentralMarket.class::isInstance);

        game.apply(new TakeWorkers());
        game.apply(new TakeWorkers());
        assertThat(game.pending()).isNotInstanceOf(Decision.OfferCentralMarket.class);
    }

    @Test
    @DisplayName("저장소가 가득 차도 버릴 칸을 고르면 중앙 시장 타일을 살 수 있다")
    void centralMarketWithFullStorage() {
        Player player = TestGames.player("P1");
        player.addSilver(2);
        player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 1));
        player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 2));
        player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 3));
        Game game = TestGames.game(List.of(player), 6, 1, 1);

        game.apply(new BuyCentralMarket(0, 1));

        assertThat(player.getSilver()).isZero();
        assertThat(player.getBoard().getStorage().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("주사위를 다 쓰고도 사지 않았다면 턴을 끝내기 전에 구매하거나 그냥 끝낼 수 있다")
    void offerCentralMarketAfterDice() {
        Player p1 = TestGames.player("P1");
        Player p2 = TestGames.player("P2");
        p1.addSilver(2);
        Game game = TestGames.game(List.of(p1, p2), 6, 1, 1);

        game.apply(new TakeWorkers());
        game.apply(new TakeWorkers());

        assertThat(game.pending()).isEqualTo(new Decision.OfferCentralMarket());
        assertThat(game.availableActions().get(0)).isEqualTo(new EndTurn());

        game.apply(new BuyCentralMarket(0, null));

        assertThat(p1.getSilver()).isZero();
        assertThat(game.currentPlayer()).isSameAs(p2);
    }

    @Test
    @DisplayName("턴 끝 구매 제안을 거절하면 아무것도 사지 않고 턴이 끝난다")
    void endTurnWithoutBuying() {
        Player p1 = TestGames.player("P1");
        Player p2 = TestGames.player("P2");
        p1.addSilver(2);
        Game game = TestGames.game(List.of(p1, p2), 6, 1, 1);
        game.apply(new TakeWorkers());
        game.apply(new TakeWorkers());

        game.apply(new EndTurn());

        assertThat(p1.getSilver()).isEqualTo(2);
        assertThat(game.currentPlayer()).isSameAs(p2);
    }

    @Test
    @DisplayName("두 주사위 중 원하는 것을 먼저 쓸 수 있다")
    void chooseDieOrder() {
        Player player = TestGames.player("P1");
        Game game = TestGames.game(List.of(player), 6, 4, 5);
        game.drainEvents();

        game.apply(new UseDie(5));
        game.apply(new TakeWorkers());

        assertThat(game.drainEvents()).containsSubsequence(
                new GameEvent.DieActionStarted(1, 5), new GameEvent.DieActionStarted(2, 4));
    }
}
