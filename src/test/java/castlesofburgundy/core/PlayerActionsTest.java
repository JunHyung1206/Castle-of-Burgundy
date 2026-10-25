package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerActionsTest {

    private GameContext simpleContext() {
        TileSupplier supplier = new TileSupplier(42L);
        GameBoard board = new GameBoard();
        board.refillDepots(supplier::draw);
        return new GameContext(1L, board, supplier, new Dice(1L), new castlesofburgundy.goods.GoodsSupply(1L));
    }

    private Player newPlayer() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(19);
        return new Player(pb, "p");
    }

    @Test
    @DisplayName("공용 보드에서 타일을 하나 가져와서 플레이어 저장소에 넣는다")
    void takeTileFromMarket() {
        GameContext ctx = simpleContext();
        Player p = newPlayer();

        assertThat(ctx.board().depot(1).tileAt(0).isPresent()).isTrue();
        int beforeSize = p.getBoard().getStorage().size();
        Tile expectedTile = ctx.board().depot(1).tileAt(0).get();

        TestGames.actions(ctx).takeTileFromBoard(p, 0, 1, 0);

        assertThat(p.getBoard().getStorage().size()).isEqualTo(beforeSize + 1); // 개인 보드의 저장소에 한개 추가된다.
        assertThat(ctx.board().depot(1).tileAt(0).isPresent()).isFalse();  // 가져온 후 공용보드에는 타일이 없다.

        assertThat(p.getBoard().getStorage().view().get(0)).isEqualTo(expectedTile);
    }

    @Test
    @DisplayName("저장소가 가득 차 있으면 명시적으로 하나 버리고 시장 타일을 가져온다")
    void replaceTileFromMarketWhenStorageFull() {
        GameContext ctx = simpleContext();
        Player p = newPlayer();

        // 저장소를 가득 채우기
        Tile keep1 = BaseTile.of(TileType.ANIMAL, 1);
        Tile discarded = BaseTile.of(TileType.ANIMAL, 2);
        Tile keep2 = BaseTile.of(TileType.ANIMAL, 3);
        p.getBoard().getStorage().add(keep1);
        p.getBoard().getStorage().add(discarded);
        p.getBoard().getStorage().add(keep2);
        assertThat(p.getBoard().getStorage().isFull()).isTrue();

        Tile expectedTile = ctx.board().depot(1).tileAt(0).get();

        Tile actualDiscarded = TestGames.actions(ctx).replaceTileFromBoard(p, 0, 1, 0, 1);

        assertThat(actualDiscarded).isEqualTo(discarded);
        assertThat(ctx.board().depot(1).tileAt(0).isPresent()).isFalse();
        assertThat(p.getBoard().getStorage().view()).containsExactly(keep1, keep2, expectedTile);
    }

    @Test
    @DisplayName("은화 2개를 내고 중앙 시장 타일을 구매한다")
    void buyTileFromCentralMarket() {
        GameContext ctx = simpleContext();
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        Player p = newPlayer();
        p.addSilver(2);

        Tile expectedTile = ctx.board().centralMarket().get(0);

        TestGames.actions(ctx).buyTileFromCentralMarket(p, 0);

        assertThat(p.getSilver()).isZero();
        assertThat(ctx.board().centralMarket()).hasSize(7);
        assertThat(ctx.board().centralMarket()).doesNotContain(expectedTile);
        assertThat(p.getBoard().getStorage().view()).containsExactly(expectedTile);
    }

    @Test
    @DisplayName("저장소가 가득 차 있으면 하나 버리고 중앙 시장 타일을 구매한다")
    void replaceTileFromCentralMarketWhenStorageFull() {
        GameContext ctx = simpleContext();
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        Player p = newPlayer();
        p.addSilver(2);

        Tile keep1 = BaseTile.of(TileType.ANIMAL, 1);
        Tile discarded = BaseTile.of(TileType.ANIMAL, 2);
        Tile keep2 = BaseTile.of(TileType.ANIMAL, 3);
        p.getBoard().getStorage().add(keep1);
        p.getBoard().getStorage().add(discarded);
        p.getBoard().getStorage().add(keep2);

        Tile expectedTile = ctx.board().centralMarket().get(0);

        Tile actualDiscarded = TestGames.actions(ctx).replaceTileFromCentralMarket(p, 0, 1);

        assertThat(actualDiscarded).isEqualTo(discarded);
        assertThat(p.getSilver()).isZero();
        assertThat(ctx.board().centralMarket()).hasSize(7);
        assertThat(p.getBoard().getStorage().view()).containsExactly(keep1, keep2, expectedTile);
    }

    @Test
    @DisplayName("성 타일을 배치하면 추가 행동(grantsExtraAction)이 부여된다")
    void placeCastleGrantsExtraAction() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(7); // 성 군집 {2,3,7}의 7번에서 시작
        Player p = new Player(pb, "p");
        p.getBoard().getStorage().add(BaseTile.of(TileType.CASTLE, 99));

        // cell 2: CASTLE / die 5, 7번과 인접
        PlacementOutcome outcome = TestGames.actions(2).placeTileFromStorage(p, 0, 0, 2, 5, Phase.A);

        assertThat(outcome.grantsExtraAction()).isTrue();
        assertThat(outcome.completedRegionSize()).isZero(); // {2,3,7} 중 3은 아직 비어 미완성
    }

    @Test
    @DisplayName("성이 아닌 타일 배치는 추가 행동을 주지 않는다")
    void placeNonCastleNoExtraAction() {
        Player p = newPlayer();
        p.getBoard().getStorage().add(BaseTile.of(TileType.KNOWLEDGE, 100));

        // cell 13: KNOWLEDGE / die 1, 시작 성(19)과 인접
        PlacementOutcome outcome = TestGames.actions(2).placeTileFromStorage(p, 0, 0, 13, 1, Phase.A);

        assertThat(outcome.grantsExtraAction()).isFalse();
        assertThat(outcome.completedRegionSize()).isZero();
    }

    @Test
    @DisplayName("구역을 완성한 배치는 완성 크기와 점수를 결과로 알려준다")
    void placeCompletingRegionReportsOutcome() {
        Player p = newPlayer(); // 시작 성 19
        // KNOWLEDGE 구역 {4,8,13}을 13→8→4 순으로 채운다 (성과 연결)
        place(p, TileType.KNOWLEDGE, 13, 1);
        place(p, TileType.KNOWLEDGE, 8, 5);
        PlacementOutcome last = place(p, TileType.KNOWLEDGE, 4, 3);

        assertThat(last.completedRegionSize()).isEqualTo(3);
        // 페이즈 A(10) + 크기 3(6) = 16
        assertThat(last.regionScore()).isEqualTo(16);
    }

    private PlacementOutcome place(Player p, TileType type, int cellId, int die) {
        p.getBoard().getStorage().add(BaseTile.of(type, cellId));
        return TestGames.actions(2).placeTileFromStorage(p, 0, 0, cellId, die, Phase.A);
    }

    @Test
    @DisplayName("상품 판매: 해당 색 전체를 팔아 은화 1개 + 타일당 플레이어 수만큼 점수")
    void sellGoodsGivesSilverAndScore() {
        Player p = newPlayer();
        p.getBoard().getGoods().add(GoodsColor.RED);
        p.getBoard().getGoods().add(GoodsColor.RED);
        p.getBoard().getGoods().add(GoodsColor.RED);

        int sold = TestGames.actions(3).sellGoods(p, 0, GoodsColor.RED);

        assertThat(sold).isEqualTo(3);
        assertThat(p.getSilver()).isEqualTo(1);     // 개수와 무관하게 은화 1
        assertThat(p.getScore()).isEqualTo(3 * 3);  // 타일당 3점(3인) × 3개
        assertThat(p.getBoard().getGoods().count(GoodsColor.RED)).isZero();
    }

    @Test
    @DisplayName("판매할 상품이 없으면 아무 보상도 없다")
    void sellNothingNoReward() {
        Player p = newPlayer();

        int sold = TestGames.actions(4).sellGoods(p, 0, GoodsColor.BROWN);

        assertThat(sold).isZero();
        assertThat(p.getSilver()).isZero();
        assertThat(p.getScore()).isZero();
    }

    @Test
    @DisplayName("배치할 수 없으면 일꾼을 쓰지 않고 저장소의 타일도 그대로 둔다")
    void illegalPlacementChangesNothing() {
        Player p = newPlayer(); // 일꾼 2
        Tile knowledge = BaseTile.of(TileType.KNOWLEDGE, 100);
        p.getBoard().getStorage().add(knowledge);

        // cell 13은 die 1 칸이라 die 2로는 놓을 수 없다
        assertThrows(IllegalArgumentException.class,
                () -> TestGames.actions(2).placeTileFromStorage(p, 1, 0, 13, 2, Phase.A));

        assertThat(p.getWorkers()).isEqualTo(2);
        assertThat(p.getBoard().getStorage().view()).containsExactly(knowledge);
        assertThat(p.getBoard().isOccupied(13)).isFalse();
    }

    @Test
    @DisplayName("저장소가 가득 차면 공용 보드 타일과 일꾼은 그대로 남는다")
    void takeTileWithFullStorageChangesNothing() {
        GameContext ctx = simpleContext();
        Player p = newPlayer(); // 일꾼 2
        p.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 1));
        p.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 2));
        p.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 3));
        Tile onBoard = ctx.board().depot(1).tileAt(0).orElseThrow();

        assertThrows(IllegalStateException.class,
                () -> TestGames.actions(ctx).takeTileFromBoard(p, 1, 1, 0));

        assertThat(ctx.board().depot(1).tileAt(0)).contains(onBoard);
        assertThat(p.getWorkers()).isEqualTo(2);
    }

    @Test
    @DisplayName("타일 가져오기에 쓴 일꾼만큼 차감된다")
    void takeTileSpendsWorkers() {
        GameContext ctx = simpleContext();
        Player p = newPlayer(); // 일꾼 2

        TestGames.actions(ctx).takeTileFromBoard(p, 2, 1, 0);

        assertThat(p.getWorkers()).isZero();
        assertThat(p.getBoard().getStorage().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("팔 상품이 없으면 일꾼도 쓰지 않는다")
    void sellNothingKeepsWorkers() {
        Player p = newPlayer(); // 일꾼 2

        TestGames.actions(2).sellGoods(p, 1, GoodsColor.BROWN);

        assertThat(p.getWorkers()).isEqualTo(2);
    }
}
