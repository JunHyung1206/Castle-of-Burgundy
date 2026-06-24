package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import castlesofburgundy.view.ConsoleUi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class TurnManagerTest {

    private GameContext simpleContext() {
        GameBoardLayout layout = new GameBoardLayout();
        TileSupplier supplier = new TileSupplier(1L);
        BoardState boardState = new BoardState(layout);
        boardState.fillAllFromSupply(supplier);
        return new GameContext(1L, layout, boardState, supplier, new Dice(1L), new castlesofburgundy.goods.GoodsSupply(1L));
    }

    private Player newPlayer() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(19);
        return new Player(pb, 3, "P1");
    }

    @Test
    @DisplayName("시장에 타일이 있고 저장소가 비어 있으면 '타일 가져오기' 옵션이 생긴다")
    void buildOptions_marketAndEmptyStorage() {
        GameContext ctx = simpleContext();
        TurnManager tm = new TurnManager(ctx, new ConsoleUi(new Scanner(System.in)));
        Player p = newPlayer();

        int die = 1; // 섹션 1

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, die, Phase.A);


        assertThat(options).anyMatch(opt -> opt.description().contains("섹션 1")); // 설명 문자열에 '시장 섹션 1 슬롯' 이 포함된 옵션이 있는지 체크
        assertThat(options).anyMatch(opt -> opt.description().contains("일꾼 2개 받기")); // 일꾼 2개 받기 옵션도 항상 존재
    }

    @Test
    @DisplayName("저장소가 가득 차면 '버리고 타일 가져오기' 옵션이 생긴다")
    void buildOptions_storageFull_replacesStoredTile() {
        GameContext ctx = simpleContext();
        TurnManager tm = new TurnManager(ctx, new ConsoleUi(new Scanner(System.in)));
        Player p = newPlayer();

        // 저장소 가득 채우기
        p.addToStorage(BaseTile.of(TileType.ANIMAL, 1));
        p.addToStorage(BaseTile.of(TileType.ANIMAL, 2));
        p.addToStorage(BaseTile.of(TileType.ANIMAL, 3));
        assertThat(p.getStorage().isFull()).isTrue();

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, 1, Phase.A);

        assertThat(options).anyMatch(opt -> opt.description().contains("저장소[0] 버리고")
                && opt.description().contains("섹션 1 슬롯 0"));

        TurnManager.Action replaceAction = options.stream()
                .filter(opt -> opt.description().contains("저장소[1] 버리고")
                        && opt.description().contains("섹션 1 슬롯 0"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("저장소[1]을 버리고 섹션 1 슬롯 0 타일을 가져오는 옵션이 있어야 한다"));

        replaceAction.apply().getAsBoolean();

        assertThat(p.getStorage().size()).isEqualTo(3);
        assertThat(ctx.boardState().hasTile(new castlesofburgundy.board.BoardSlot(1, 0))).isFalse();
    }

    @Test
    @DisplayName("저장소에 있는 타일을 현재 주사위 눈으로 둘 수 있으면 '배치' 옵션이 생긴다")
    void buildOptions_placeFromStorage() {
        GameContext ctx = simpleContext();
        TurnManager tm = new TurnManager(ctx, new ConsoleUi(new Scanner(System.in)));
        Player p = newPlayer();

        // 플레이어 보드에서, 예를 들어 13번 칸은 KNOWLEDGE / die=1 (지금 레이아웃에 맞게 수정)
        // 그 칸에 맞는 타일을 저장소에 넣기
        Tile t = BaseTile.of(TileType.KNOWLEDGE, 100);
        p.addToStorage(t);

        int die = 1; // 13번 칸이 die=1이라고 가정

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, die, Phase.A);

        assertThat(options).anyMatch(opt -> opt.description().contains("저장소[0]의 KNOWLEDGE"));
    }

    @Test
    @DisplayName("일꾼이 있다면, 그에 해당하는 일꾼을 소모하여 액션을 할 수 있다.")
    void actionConsumesWorkers() {
        GameContext ctx = simpleContext();
        TurnManager tm = new TurnManager(ctx, new ConsoleUi(new Scanner(System.in)));
        Player p = newPlayer(); // 기본 일꾼 2개

        int die = 1;

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, die, Phase.A);

        // [일꾼1, +] 또는 [일꾼1, -] 로 시작하는 옵션 하나를 찾는다.
        TurnManager.Action opt = options.stream()
                .filter(o -> o.description().startsWith("[일꾼 1개 소모]"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("일꾼 1개 사용 옵션이 존재해야 합니다."));

        int beforeWorkers = p.getWorkers();

        // 옵션 실행 (실제 게임에서는 사용자가 선택해서 실행하게 될 부분)
        opt.apply().getAsBoolean();

        // 일꾼 1개가 소모됐는지 확인
        assertThat(p.getWorkers()).isEqualTo(beforeWorkers - 1);
    }

    @Test
    @DisplayName("액션 선택지 전에 현재 타일 저장소와 상품 상태를 함께 보여준다")
    void playerTurnShowsCompactContextBeforeChoices() {
        GameContext ctx = simpleContext();
        ctx.boardState().addGoods(3, GoodsColor.PINK);
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0, 0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player p = newPlayer();
        p.addToStorage(BaseTile.of(TileType.SHIP, 100));
        p.addGoods(GoodsColor.RED);

        tm.playerTurn(p, Phase.A);

        assertThat(ui.messaged("[현재 상태]")).isTrue();
        assertThat(ui.messaged("타일 저장소")).isTrue();
        assertThat(ui.messaged("보유 상품")).isTrue();
        assertThat(ui.messaged("창고 상품")).isTrue();
    }
}
