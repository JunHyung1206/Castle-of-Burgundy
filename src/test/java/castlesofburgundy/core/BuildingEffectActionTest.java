package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.goods.GoodsSupply;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingEffectActionTest {

    private GameContext simpleContext() {
        GameBoardLayout layout = new GameBoardLayout();
        BoardState boardState = new BoardState(layout);
        TileSupplier supplier = new TileSupplier(1L);
        boardState.fillAllFromSupply(supplier);
        return new GameContext(1L, layout, boardState, supplier, new Dice(1L), new GoodsSupply(1L));
    }

    private Player newPlayer() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        return new Player(board, 3, "P1");
    }

    @Test
    @DisplayName("시장 건물을 놓으면 선박 또는 동물 타일 하나를 가져온다")
    void marketTakesShipOrAnimalTile() {
        GameContext ctx = simpleContext();
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player player = newPlayer();
        player.addToStorage(new BuildingTile(100, BuildingKind.MARKET));

        long before = countBoardTiles(ctx, Set.of(TileType.SHIP, TileType.ANIMAL));

        placeBuildingAt26(tm, player).apply().getAsBoolean();

        assertThat(countBoardTiles(ctx, Set.of(TileType.SHIP, TileType.ANIMAL))).isEqualTo(before - 1);
        assertThat(player.storedTiles()).hasSize(1);
        assertThat(player.storedTiles().get(0).type()).isIn(TileType.SHIP, TileType.ANIMAL);
    }

    @Test
    @DisplayName("목수 작업장을 놓으면 건물 타일 하나를 가져온다")
    void carpenterTakesBuildingTile() {
        GameContext ctx = simpleContext();
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player player = newPlayer();
        player.addToStorage(new BuildingTile(100, BuildingKind.CARPENTER));

        long before = countBoardTiles(ctx, Set.of(TileType.BUILDING));

        placeBuildingAt26(tm, player).apply().getAsBoolean();

        assertThat(countBoardTiles(ctx, Set.of(TileType.BUILDING))).isEqualTo(before - 1);
        assertThat(player.storedTiles()).hasSize(1);
        assertThat(player.storedTiles().get(0).type()).isEqualTo(TileType.BUILDING);
    }

    @Test
    @DisplayName("교회를 놓으면 광산, 지식, 성 타일 중 하나를 가져온다")
    void churchTakesMineKnowledgeOrCastleTile() {
        GameContext ctx = simpleContext();
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player player = newPlayer();
        player.addToStorage(new BuildingTile(100, BuildingKind.CHURCH));

        Set<TileType> allowed = Set.of(TileType.MINE, TileType.KNOWLEDGE, TileType.CASTLE);
        long before = countBoardTiles(ctx, allowed);

        placeBuildingAt26(tm, player).apply().getAsBoolean();

        assertThat(countBoardTiles(ctx, allowed)).isEqualTo(before - 1);
        assertThat(player.storedTiles()).hasSize(1);
        assertThat(player.storedTiles().get(0).type()).isIn(TileType.MINE, TileType.KNOWLEDGE, TileType.CASTLE);
    }

    @Test
    @DisplayName("창고를 놓으면 원하는 상품 색 하나를 판매한다")
    void warehouseSellsOneGoodsColor() {
        GameContext ctx = simpleContext();
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player player = newPlayer();
        player.addGoods(GoodsColor.RED);
        player.addGoods(GoodsColor.RED);
        player.addToStorage(new BuildingTile(100, BuildingKind.WAREHOUSE));

        placeBuildingAt26(tm, player).apply().getAsBoolean();

        assertThat(player.goods().count(GoodsColor.RED)).isZero();
        assertThat(player.getSilver()).isEqualTo(1);
        assertThat(player.getScore()).isEqualTo(4);
    }

    private TurnManager.Action placeBuildingAt26(TurnManager tm, Player player) {
        return tm.buildOptionsForDie(player, 3, Phase.A).stream()
                .filter(action -> action.description().contains("저장소[0]의 BUILDING")
                        && action.description().contains("cell 26"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("cell 26에 건물을 놓는 액션이 있어야 한다"));
    }

    private long countBoardTiles(GameContext ctx, Set<TileType> types) {
        return ctx.boardState().snapshot().values().stream()
                .filter(tile -> types.contains(tile.type()))
                .count();
    }
}
