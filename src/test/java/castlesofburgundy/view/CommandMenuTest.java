package castlesofburgundy.view;

import castlesofburgundy.core.Command;
import castlesofburgundy.core.Decision;
import castlesofburgundy.core.Game;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class CommandMenuTest {

    @Test
    @DisplayName("무작위로 진행한 게임의 모든 선택에서, 메뉴 끝의 명령은 가능한 명령과 정확히 같다(빠짐·중복 없음)")
    void menuLeavesMatchAvailableActions() {
        for (long seed = 1; seed <= 12; seed++) {
            int players = (int) (seed % 4) + 1;
            List<String> names = new ArrayList<>();
            for (int i = 0; i < players; i++) {
                names.add("P" + i);
            }
            Game game = Game.start(seed, names);
            Random random = new Random(seed);

            while (!game.isOver()) {
                List<Command> available = game.availableActions();
                List<Command> leaves = CommandMenu.leaves(CommandMenu.build(game));

                assertThat(leaves).doesNotHaveDuplicates().containsExactlyInAnyOrderElementsOf(available);

                game.apply(available.get(random.nextInt(available.size())));
                game.drainEvents();
            }
        }
    }

    @Test
    @DisplayName("주사위 행동은 행동 종류를 먼저 고르고, 보기가 하나뿐인 종류는 바로 실행한다")
    void dieActionGroupsByKind() {
        Game game = gameAtDieAction();

        List<CommandMenu.Item> top = CommandMenu.build(game);

        assertThat(top).anySatisfy(item -> {
            assertThat(item).isInstanceOf(CommandMenu.Group.class);
            assertThat(item.label()).startsWith("타일 가져오기 (");
        });
        assertThat(top).last().isEqualTo(new CommandMenu.Leaf("일꾼 2개 받기", new Command.TakeWorkers()));
    }

    @Test
    @DisplayName("저장소가 가득 차면 대상을 고른 뒤에 버릴 칸을 고른다")
    void fullStorageAsksDiscardAfterTarget() {
        Game game = gameAtDieAction();
        Player player = game.currentPlayer();
        while (!player.getBoard().getStorage().isFull()) {
            player.getBoard().getStorage().add(BaseTile.of(TileType.ANIMAL, 900 + player.getBoard().getStorage().size()));
        }

        CommandMenu.Group takeTile = (CommandMenu.Group) CommandMenu.build(game).stream()
                .filter(item -> item.label().startsWith("타일 가져오기"))
                .findFirst().orElseThrow();
        CommandMenu.Group target = (CommandMenu.Group) takeTile.items().get(0);

        assertThat(target.label()).endsWith("버릴 칸 선택");
        assertThat(target.items()).extracting(CommandMenu.Item::label)
                .containsExactly("저장소[0] ANIMAL 버리기", "저장소[1] ANIMAL 버리기", "저장소[2] ANIMAL 버리기");
    }

    @Test
    @DisplayName("종류가 하나뿐인 선택(주사위 고르기 등)은 첫 단계를 건너뛴다")
    void singleKindSkipsFirstLevel() {
        Game game = java.util.stream.LongStream.rangeClosed(1, 50)
                .mapToObj(seed -> Game.start(seed, List.of("P1")))
                .filter(g -> g.pending() instanceof Decision.ChooseDie)
                .findFirst().orElseThrow();

        List<CommandMenu.Item> top = CommandMenu.build(game);

        assertThat(top).allMatch(CommandMenu.Leaf.class::isInstance);
        assertThat(top).extracting(CommandMenu.Item::label).allMatch(label -> label.startsWith("주사위 "));
    }

    /** 주사위 행동을 고르는 차례까지 진행한 게임(번호 창고가 차 있어 가져오기 보기가 여럿). */
    private static Game gameAtDieAction() {
        Game game = Game.start(42L, List.of("P1", "P2"));
        if (game.pending() instanceof Decision.ChooseDie d) {
            game.apply(new Command.UseDie(d.remainingDice().get(0)));
        }
        return game;
    }
}
