package castlesofburgundy.view;

import castlesofburgundy.core.ActionOption;
import castlesofburgundy.core.Command;
import castlesofburgundy.core.Decision;
import castlesofburgundy.core.Game;
import castlesofburgundy.core.GameState;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class CommandMenuTest {

    @Test
    @DisplayName("무작위로 진행한 게임의 모든 선택에서, 메뉴 끝의 명령은 가능한 명령과 정확히 같다(빠짐·중복 없음)")
    void menuLeavesMatchOptions() {
        for (long seed = 1; seed <= 12; seed++) {
            int players = (int) (seed % 4) + 1;
            List<String> names = new ArrayList<>();
            for (int i = 0; i < players; i++) {
                names.add("P" + i);
            }
            Game game = Game.start(seed, names);
            Random random = new Random(seed);

            while (!game.isOver()) {
                List<ActionOption> options = game.options();
                List<Command> commands = options.stream().map(ActionOption::command).toList();
                List<Command> leaves = CommandMenu.leaves(CommandMenu.build(game.state(), game.pending(), options));

                assertThat(leaves).doesNotHaveDuplicates().containsExactlyInAnyOrderElementsOf(commands);

                game.apply(commands.get(random.nextInt(commands.size())));
                game.drainEvents();
            }
        }
    }

    @Test
    @DisplayName("주사위 행동은 행동 종류를 먼저 고르고, 보기가 하나뿐인 종류는 바로 실행한다")
    void dieActionGroupsByKind() {
        GameState state = ViewFixtures.state(ViewFixtures.player(0, "P1", List.of(), Map.of(), Map.of()));
        Decision decision = new Decision.ChooseDieAction(1);
        List<ActionOption> options = List.of(
                new ActionOption(new Command.PlaceTile(1, 0, 13), 0, 0, 0, 0),
                new ActionOption(new Command.PlaceTile(2, 0, 31), 1, 0, 0, 0),
                new ActionOption(new Command.TakeWorkers(), 0, 0, 0, 0));
        GameState withStorage = new GameState(state.phase(), state.round(), 0, state.turnOrder(),
                List.of(ViewFixtures.player(0, "P1", List.of(BaseTile.of(TileType.KNOWLEDGE, 1)), Map.of(), Map.of())),
                state.board());

        List<CommandMenu.Item> top = CommandMenu.build(withStorage, decision, options);

        assertThat(top).first().isInstanceOf(CommandMenu.Group.class)
                .extracting(CommandMenu.Item::label).isEqualTo("저장소 타일 배치 (2)");
        assertThat(top).last().isEqualTo(new CommandMenu.Leaf("일꾼 2개 받기", new Command.TakeWorkers()));
    }

    @Test
    @DisplayName("저장소가 가득 차면 대상을 고른 뒤에 버릴 칸을 고른다")
    void fullStorageAsksDiscardAfterTarget() {
        Game game = gameWithFullStorageAndTakeTile();

        CommandMenu.Group takeTile = (CommandMenu.Group) CommandMenu.build(game.state(), game.pending(), game.options()).stream()
                .filter(item -> item.label().startsWith("타일 가져오기"))
                .findFirst().orElseThrow();
        CommandMenu.Group target = (CommandMenu.Group) takeTile.items().get(0);

        assertThat(target.label()).endsWith("버릴 칸 선택");
        assertThat(target.items()).extracting(CommandMenu.Item::label)
                .allMatch(label -> label.matches("저장소\\[[0-2]] \\w+ 버리기")).hasSize(3);
    }

    @Test
    @DisplayName("이득이 있는 선택지는 Game이 계산한 은화·점수를 붙인다")
    void showsGainFromOptions() {
        GameState state = ViewFixtures.state(ViewFixtures.player(0, "P1",
                List.of(), Map.of(castlesofburgundy.goods.GoodsColor.RED, 2), Map.of()));
        List<ActionOption> options = List.of(
                new ActionOption(new Command.SellGoods(1), 0, 0, 4, 1),
                new ActionOption(new Command.TakeWorkers(), 0, 0, 0, 0));

        List<CommandMenu.Item> top = CommandMenu.build(state, new Decision.ChooseDieAction(1), options);

        assertThat(top).first().extracting(CommandMenu.Item::label)
                .isEqualTo("RED 상품 판매 (2개) → 은화 +1, 점수 +4");
    }

    @Test
    @DisplayName("종류가 하나뿐인 선택(주사위 고르기 등)은 첫 단계를 건너뛴다")
    void singleKindSkipsFirstLevel() {
        Game game = LongStream.rangeClosed(1, 50)
                .mapToObj(seed -> Game.start(seed, List.of("P1")))
                .filter(g -> g.pending() instanceof Decision.ChooseDie)
                .findFirst().orElseThrow();

        List<CommandMenu.Item> top = CommandMenu.build(game.state(), game.pending(), game.options());

        assertThat(top).allMatch(CommandMenu.Leaf.class::isInstance);
        assertThat(top).extracting(CommandMenu.Item::label).allMatch(label -> label.startsWith("주사위 "));
    }

    /** 공개 API(명령)만으로 저장소를 채워, 가져오기 선택지가 있는 주사위 행동 차례를 만든다. */
    private static Game gameWithFullStorageAndTakeTile() {
        for (long seed = 1; seed <= 200; seed++) {
            Game game = Game.start(seed, List.of("P1"));
            for (int step = 0; step < 200 && !game.isOver(); step++) {
                List<ActionOption> options = game.options();
                boolean takeAvailable = options.stream().anyMatch(o -> o.command() instanceof Command.TakeTile);
                if (game.state().current().storage().size() == 3 && takeAvailable) {
                    return game;
                }
                Command next = options.stream().map(ActionOption::command)
                        .filter(c -> c instanceof Command.TakeTile || c instanceof Command.UseDie)
                        .findFirst()
                        .orElse(options.get(options.size() - 1).command());
                game.apply(next);
                game.drainEvents();
            }
        }
        throw new AssertionError("저장소가 가득 찬 상태를 만들지 못했다");
    }
}
