package castlesofburgundy.core;

import castlesofburgundy.core.GameState.PlayerState;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Game이 바깥에 약속하는 것: 상태는 스냅샷으로만 읽히고, 선택지의 비용·이득은 실제 결과와 같다. */
class GameContractTest {

    @Test
    @DisplayName("state()는 그 순간의 스냅샷이라, 이후 명령을 적용해도 바뀌지 않고 직접 고칠 수도 없다")
    void stateIsReadOnlySnapshot() {
        Game game = Game.start(42L, List.of("P1", "P2"));
        GameState before = game.state();
        int workersBefore = before.current().workers();

        game.apply(game.options().stream()
                .map(ActionOption::command)
                .filter(c -> c instanceof Command.UseDie || c instanceof Command.TakeWorkers)
                .findFirst().orElseThrow());
        if (game.pending() instanceof Decision.ChooseDieAction) {
            game.apply(new Command.TakeWorkers());
        }

        assertThat(before.current().workers()).isEqualTo(workersBefore);
        assertThrows(UnsupportedOperationException.class, () -> before.players().clear());
        assertThrows(UnsupportedOperationException.class, () -> before.current().storage().add(BaseTile.of(TileType.SHIP, 1)));
        assertThrows(UnsupportedOperationException.class, () -> before.current().estate().clear());
        assertThrows(UnsupportedOperationException.class, () -> before.board().depots().clear());
    }

    @Test
    @DisplayName("무작위로 진행한 게임에서, 선택지에 적힌 일꾼·은화 비용과 점수·은화 이득은 적용한 결과와 같다")
    void optionPreviewMatchesResult() {
        int checked = 0;
        for (long seed = 1; seed <= 12; seed++) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i <= seed % 4; i++) {
                names.add("P" + i);
            }
            Game game = Game.start(seed, names);
            Random random = new Random(seed);

            while (!game.isOver()) {
                List<ActionOption> options = game.options();
                ActionOption option = options.get(random.nextInt(options.size()));
                GameState before = game.state();

                game.apply(option.command());
                game.drainEvents();

                GameState after = game.state();
                if (after.phase() != before.phase() || after.isOver()) {
                    continue; // 페이즈 종료 정산(광산 은화)이 같은 명령에 섞이면 비교하지 않는다
                }
                PlayerState was = before.current();
                PlayerState now = after.player(was.seat());
                assertThat(now.score() - was.score()).as("%s 점수", option).isEqualTo(option.scoreGain());
                assertThat(now.silver() - was.silver()).as("%s 은화", option).isEqualTo(option.silverGain() - option.silverCost());
                if (option.command() instanceof Command.TakeTile || option.command() instanceof Command.SellGoods) {
                    assertThat(was.workers() - now.workers()).as("%s 일꾼", option).isEqualTo(option.workerCost());
                }
                checked++;
            }
        }
        assertThat(checked).isGreaterThan(1000);
    }
}
