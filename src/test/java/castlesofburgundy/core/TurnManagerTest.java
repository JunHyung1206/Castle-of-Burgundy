package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
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
        return simpleContext(1L);
    }

    private GameContext simpleContext(long diceSeed) {
        TileSupplier supplier = new TileSupplier(1L);
        GameBoard board = new GameBoard();
        board.refillDepots(4, supplier::draw);
        return new GameContext(1L, board, supplier, new Dice(diceSeed), new castlesofburgundy.goods.GoodsSupply(1L));
    }

    private Player newPlayer() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(19);
        return new Player(pb, "P1");
    }

    @Test
    @DisplayName("시장에 타일이 있고 저장소가 비어 있으면 '타일 가져오기' 옵션이 생긴다")
    void buildOptions_marketAndEmptyStorage() {
        GameContext ctx = simpleContext();
        TurnManager tm = new TurnManager(ctx, new ConsoleUi(new Scanner(System.in)));
        Player p = newPlayer();

        int die = 1; // 섹션 1

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, die, Phase.A, new Turn());


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
        p.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 1));
        p.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 2));
        p.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 3));
        assertThat(p.getBoard().getStorage().isFull()).isTrue();

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, 1, Phase.A, new Turn());

        assertThat(options).anyMatch(opt -> opt.description().contains("저장소[0] 버리고")
                && opt.description().contains("섹션 1 슬롯 0"));

        TurnManager.Action replaceAction = options.stream()
                .filter(opt -> opt.description().contains("저장소[1] 버리고")
                        && opt.description().contains("섹션 1 슬롯 0"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("저장소[1]을 버리고 섹션 1 슬롯 0 타일을 가져오는 옵션이 있어야 한다"));

        replaceAction.apply().getAsBoolean();

        assertThat(p.getBoard().getStorage().size()).isEqualTo(3);
        assertThat(ctx.board().depot(1).tileAt(0).isPresent()).isFalse();
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
        p.getBoard().getStorage().add(t);

        int die = 1; // 13번 칸이 die=1이라고 가정

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, die, Phase.A, new Turn());

        assertThat(options).anyMatch(opt -> opt.description().contains("저장소[0]의 KNOWLEDGE"));
    }

    @Test
    @DisplayName("일꾼이 있다면, 그에 해당하는 일꾼을 소모하여 액션을 할 수 있다.")
    void actionConsumesWorkers() {
        GameContext ctx = simpleContext();
        TurnManager tm = new TurnManager(ctx, new ConsoleUi(new Scanner(System.in)));
        Player p = newPlayer(); // 기본 일꾼 2개

        int die = 1;

        List<TurnManager.Action> options = tm.buildOptionsForDie(p, die, Phase.A, new Turn());

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
        ctx.board().depot(3).addGoods(GoodsColor.PINK);
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0, 0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player p = newPlayer();
        p.getBoard().getStorage().add(BaseTile.of(TileType.SHIP, 100));
        p.getBoard().getGoods().add(GoodsColor.RED);

        tm.playerTurn(p, Phase.A);

        assertThat(ui.messaged("[현재 상태]")).isTrue();
        assertThat(ui.messaged("타일 저장소")).isTrue();
        assertThat(ui.messaged("보유 상품")).isTrue();
        assertThat(ui.messaged("창고 상품")).isTrue();
    }

    @Test
    @DisplayName("액션 선택지 전에 현재 주사위로 팔 수 있는 상품을 보여준다")
    void playerTurnShowsSellableGoodsBeforeChoices() {
        GameContext ctx = simpleContext(5L); // 첫 주사위 6
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0, 0);
        TurnManager tm = new TurnManager(ctx, ui, 2);
        Player p = newPlayer();
        p.getBoard().getGoods().add(GoodsColor.TEAL);

        tm.playerTurn(p, Phase.A);

        assertThat(ui.messaged("판매 가능")).isTrue();
        assertThat(ui.messaged("현재 주사위 6")).isTrue();
        assertThat(ui.messaged("TEAL 1개")).isTrue();
    }

    @Test
    @DisplayName("액션 선택지 전에 동물 배치 점수 후보를 보여준다")
    void playerTurnShowsAnimalScoreCandidatesBeforeChoices() {
        GameContext ctx = simpleContext(5L); // 첫 주사위 6
        RecordingGameUi ui = new RecordingGameUi();
        ui.scriptChoices(0, 0);
        TurnManager tm = new TurnManager(ctx, ui, 2);

        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(2);
        Player p = new Player(board, "P1");
        p.getBoard().getStorage().add(new AnimalTile(100, AnimalSpecies.SHEEP, 3));

        tm.playerTurn(p, Phase.A);

        assertThat(ui.messaged("동물 점수 후보")).isTrue();
        assertThat(ui.messaged("저장소[0] SHEEP x3")).isTrue();
        assertThat(ui.messaged("cell 1")).isTrue();
        assertThat(ui.messaged("+3점")).isTrue();
    }
}
