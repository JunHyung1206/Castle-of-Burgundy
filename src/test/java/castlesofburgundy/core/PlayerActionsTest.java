package castlesofburgundy.core;

import castlesofburgundy.board.*;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.CastleTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerActionsTest {

    private GameContext simpleContext() {
        GameBoardLayout layout = new GameBoardLayout();
        TileSupplier supplier = new TileSupplier(42L);
        BoardState boardState = new BoardState(layout);
        boardState.fillAllFromSupply(supplier);
        return new GameContext(1L, layout, boardState, supplier, new Dice(1L), new castlesofburgundy.goods.GoodsSupply(1L));
    }

    private Player newPlayer() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(19);
        return new Player(pb, 3, "p");
    }

    @Test
    @DisplayName("공용 보드에서 타일을 하나 가져와서 플레이어 저장소에 넣는다")
    void takeTileFromMarket() {
        GameContext ctx = simpleContext();
        Player p = newPlayer();

        BoardSlot slot = new BoardSlot(1, 0);
        assertThat(ctx.boardState().hasTile(slot)).isTrue();
        int beforeSize = p.getStorage().size();
        Tile expectedTile = ctx.boardState().get(slot).get();

        PlayerActions.takeTileFromBoard(ctx, p, 1, 0);

        assertThat(p.getStorage().size()).isEqualTo(beforeSize + 1); // 개인 보드의 저장소에 한개 추가된다.
        assertThat(ctx.boardState().hasTile(slot)).isFalse();  // 가져온 후 공용보드에는 타일이 없다.

        assertThat(p.getStorage().view().get(0)).isEqualTo(expectedTile);
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
        p.addToStorage(keep1);
        p.addToStorage(discarded);
        p.addToStorage(keep2);
        assertThat(p.getStorage().isFull()).isTrue();

        BoardSlot slot = new BoardSlot(1, 0);
        Tile expectedTile = ctx.boardState().get(slot).get();

        Tile actualDiscarded = PlayerActions.replaceTileFromBoard(ctx, p, 1, 0, 1);

        assertThat(actualDiscarded).isEqualTo(discarded);
        assertThat(ctx.boardState().hasTile(slot)).isFalse();
        assertThat(p.getStorage().view()).containsExactly(keep1, keep2, expectedTile);
    }

    @Test
    @DisplayName("은화 2개를 내고 중앙 시장 타일을 구매한다")
    void buyTileFromCentralMarket() {
        GameContext ctx = simpleContext();
        ctx.boardState().fillCentralMarketFromSupply(ctx.tileSupplier());
        Player p = newPlayer();
        p.addSilver(2);

        Tile expectedTile = ctx.boardState().centralMarket().get(0);

        PlayerActions.buyTileFromCentralMarket(ctx, p, 0);

        assertThat(p.getSilver()).isZero();
        assertThat(ctx.boardState().centralMarket()).hasSize(7);
        assertThat(ctx.boardState().centralMarket()).doesNotContain(expectedTile);
        assertThat(p.getStorage().view()).containsExactly(expectedTile);
    }

    @Test
    @DisplayName("저장소가 가득 차 있으면 하나 버리고 중앙 시장 타일을 구매한다")
    void replaceTileFromCentralMarketWhenStorageFull() {
        GameContext ctx = simpleContext();
        ctx.boardState().fillCentralMarketFromSupply(ctx.tileSupplier());
        Player p = newPlayer();
        p.addSilver(2);

        Tile keep1 = BaseTile.of(TileType.ANIMAL, 1);
        Tile discarded = BaseTile.of(TileType.ANIMAL, 2);
        Tile keep2 = BaseTile.of(TileType.ANIMAL, 3);
        p.addToStorage(keep1);
        p.addToStorage(discarded);
        p.addToStorage(keep2);

        Tile expectedTile = ctx.boardState().centralMarket().get(0);

        Tile actualDiscarded = PlayerActions.replaceTileFromCentralMarket(ctx, p, 0, 1);

        assertThat(actualDiscarded).isEqualTo(discarded);
        assertThat(p.getSilver()).isZero();
        assertThat(ctx.boardState().centralMarket()).hasSize(7);
        assertThat(p.getStorage().view()).containsExactly(keep1, keep2, expectedTile);
    }

    @Test
    @DisplayName("성 타일을 배치하면 추가 행동(grantsExtraAction)이 부여된다")
    void placeCastleGrantsExtraAction() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(7); // 성 군집 {2,3,7}의 7번에서 시작
        Player p = new Player(pb, 3, "p");
        p.addToStorage(new CastleTile(99));

        // cell 2: CASTLE / die 5, 7번과 인접
        PlacementOutcome outcome = PlayerActions.placeTileFromStorage(p, 0, 2, 5, Phase.A);

        assertThat(outcome.grantsExtraAction()).isTrue();
        assertThat(outcome.completedRegionSize()).isZero(); // {2,3,7} 중 3은 아직 비어 미완성
    }

    @Test
    @DisplayName("성이 아닌 타일 배치는 추가 행동을 주지 않는다")
    void placeNonCastleNoExtraAction() {
        Player p = newPlayer();
        p.addToStorage(BaseTile.of(TileType.KNOWLEDGE, 100));

        // cell 13: KNOWLEDGE / die 1, 시작 성(19)과 인접
        PlacementOutcome outcome = PlayerActions.placeTileFromStorage(p, 0, 13, 1, Phase.A);

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
        p.addToStorage(BaseTile.of(type, cellId));
        return PlayerActions.placeTileFromStorage(p, 0, cellId, die, Phase.A);
    }

    @Test
    @DisplayName("상품 판매: 해당 색 전체를 팔아 은화 1개 + 타일당 플레이어 수만큼 점수")
    void sellGoodsGivesSilverAndScore() {
        Player p = newPlayer();
        p.addGoods(GoodsColor.RED);
        p.addGoods(GoodsColor.RED);
        p.addGoods(GoodsColor.RED);

        int sold = PlayerActions.sellGoods(p, GoodsColor.RED, 3);

        assertThat(sold).isEqualTo(3);
        assertThat(p.getSilver()).isEqualTo(1);     // 개수와 무관하게 은화 1
        assertThat(p.getScore()).isEqualTo(3 * 3);  // 타일당 3점(3인) × 3개
        assertThat(p.goods().count(GoodsColor.RED)).isZero();
    }

    @Test
    @DisplayName("판매할 상품이 없으면 아무 보상도 없다")
    void sellNothingNoReward() {
        Player p = newPlayer();

        int sold = PlayerActions.sellGoods(p, GoodsColor.BROWN, 4);

        assertThat(sold).isZero();
        assertThat(p.getSilver()).isZero();
        assertThat(p.getScore()).isZero();
    }
}
