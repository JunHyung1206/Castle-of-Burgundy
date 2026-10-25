package castlesofburgundy.core;

import castlesofburgundy.board.Depot;
import castlesofburgundy.core.Command.TakeWorkers;
import castlesofburgundy.core.Command.UseDie;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 게임 시작, 주사위·차례·라운드·페이즈 진행, 명령 검증. */
class GameFlowTest {

    @Test
    @DisplayName("시작하면 순서대로 일꾼 1·2개, 은화 1개, 상품 3개를 받고 성은 19번 칸에 놓인다")
    void startGivesStartingResources() {
        Game game = Game.start(42L, List.of("Alice", "Bob"));

        Player alice = game.players().get(0);
        Player bob = game.players().get(1);
        assertThat(alice.getWorkers()).isEqualTo(1);
        assertThat(bob.getWorkers()).isEqualTo(2);
        assertThat(game.players()).allSatisfy(player -> {
            assertThat(player.getSilver()).isEqualTo(1);
            assertThat(player.getBoard().getGoods().totalStored()).isEqualTo(3);
            assertThat(player.getBoard().isOccupied(19)).isTrue();
        });
        assertThat(game.board().turnOrder().currentOrder()).containsExactly(alice, bob);
    }

    @Test
    @DisplayName("시작하면 페이즈 A 1라운드: 인원수만큼 창고를 채우고, 흰 주사위 창고에 상품을 놓고, 첫 플레이어 차례가 된다")
    void startPreparesFirstRound() {
        Game game = Game.start(42L, List.of("Alice", "Bob"));

        assertThat(game.phase()).isEqualTo(Phase.A);
        assertThat(game.round()).isEqualTo(1);
        assertThat(game.currentPlayer().getName()).isEqualTo("Alice");
        assertThat(filledDepotSlots(game)).isEqualTo(12);
        assertThat(game.board().centralMarket()).hasSize(8);
        assertThat(game.board().depots().stream().mapToInt(depot -> depot.goods().size()).sum()).isEqualTo(1);

        List<GameEvent> events = game.drainEvents();
        assertThat(events.get(0)).isInstanceOf(GameEvent.PhaseStarted.class);
        assertThat(events.get(1)).isInstanceOf(GameEvent.RoundStarted.class);
        assertThat(events.get(2)).isInstanceOf(GameEvent.TurnStarted.class);
    }

    @Test
    @DisplayName("플레이어는 1~4명이어야 한다")
    void startRejectsPlayerCount() {
        assertThrows(IllegalArgumentException.class, () -> Game.start(1L, List.of()));
        assertThrows(IllegalArgumentException.class, () -> Game.start(1L, List.of("a", "b", "c", "d", "e")));
    }

    @Test
    @DisplayName("서로 다른 주사위가 남으면 쓸 주사위를 먼저 고르고, 남은 하나는 자동으로 다음 행동에 쓴다")
    void chooseDieThenAutoUseRemaining() {
        Game game = TestGames.game(List.of(TestGames.player("P1")), 6, 4, 5);
        game.drainEvents();

        assertThat(game.pending()).isEqualTo(new Decision.ChooseDie(List.of(4, 5)));
        assertThat(game.availableActions()).containsExactly(new UseDie(4), new UseDie(5));

        game.apply(new UseDie(5));
        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(5));
        assertThat(game.drainEvents()).containsExactly(new GameEvent.DieActionStarted(1, 5));

        game.apply(new TakeWorkers());
        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(4));
        assertThat(game.drainEvents()).containsExactly(
                new GameEvent.ActionCompleted(), new GameEvent.DieActionStarted(2, 4));
    }

    @Test
    @DisplayName("두 주사위 눈이 같으면 주사위를 고르지 않는다")
    void sameDiceSkipChoice() {
        Game game = TestGames.game(List.of(TestGames.player("P1")), 6, 3, 3);

        assertThat(game.pending()).isEqualTo(new Decision.ChooseDieAction(3));
    }

    @Test
    @DisplayName("지금 할 수 없는 명령은 거부하고 상태를 바꾸지 않는다")
    void rejectsUnavailableCommand() {
        Player player = TestGames.player("P1");
        Game game = TestGames.game(List.of(player), 6, 3, 3);
        Decision before = game.pending();

        assertThrows(IllegalArgumentException.class, () -> game.apply(new UseDie(3)));
        // cell 13은 die 1 칸이라 die 3으로는 놓을 수 없다
        player.getBoard().getStorage().add(BaseTile.of(TileType.KNOWLEDGE, 100));
        assertThrows(IllegalArgumentException.class, () -> game.apply(new Command.PlaceTile(3, 0, 13)));

        assertThat(game.pending()).isEqualTo(before);
        assertThat(player.getWorkers()).isEqualTo(2);
        assertThat(player.getBoard().getStorage().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("두 주사위를 다 쓰면 다음 플레이어 차례가 되고, 모두 끝나면 다음 라운드가 흰 주사위 상품과 함께 시작된다")
    void turnsAndRoundsAdvance() {
        Player p1 = TestGames.player("P1");
        Player p2 = TestGames.player("P2");
        // 흰 주사위 6, P1 (3,3), P2 (2,2), 다음 라운드 흰 주사위 6 ...
        Game game = TestGames.game(List.of(p1, p2), 6, 3, 3, 2, 2);

        game.apply(new TakeWorkers());
        game.apply(new TakeWorkers());
        assertThat(game.currentPlayer()).isSameAs(p2);

        game.drainEvents();
        game.apply(new TakeWorkers());
        game.apply(new TakeWorkers());

        assertThat(game.round()).isEqualTo(2);
        assertThat(game.currentPlayer()).isSameAs(p1);
        assertThat(game.drainEvents()).contains(new GameEvent.RoundStarted(Phase.A, 2, 6, game.board().depot(6).goods().get(1)));
    }

    @Test
    @DisplayName("페이즈가 끝나면 광산마다 은화를 주고, 다섯 페이즈 뒤 게임이 끝난다")
    void phasesEndAndGameFinishes() {
        Player player = TestGames.player("P1");
        // 광산 {25(die4), 30(die1)}을 성(19)에서부터 연결해 둔다
        player.getBoard().place(25, BaseTile.of(TileType.MINE, 1), 4);
        player.getBoard().place(30, BaseTile.of(TileType.MINE, 2), 1);
        Game game = TestGames.game(List.of(player), 6, 3, 3);

        int phaseEnds = 0;
        int minesPaid = 0;
        while (!game.isOver()) {
            game.apply(game.pending() instanceof Decision.OfferCentralMarket
                    ? new Command.EndTurn()
                    : new TakeWorkers());
            for (GameEvent event : game.drainEvents()) {
                if (event instanceof GameEvent.PhaseEnded) {
                    phaseEnds++;
                }
                if (event instanceof GameEvent.MinesPaid paid) {
                    assertThat(paid.silver()).isEqualTo(2);
                    minesPaid++;
                }
            }
        }

        assertThat(phaseEnds).isEqualTo(5);
        assertThat(minesPaid).isEqualTo(5);
        assertThat(game.availableActions()).isEmpty();
        assertThrows(IllegalStateException.class, game::currentPlayer);
    }

    private static long filledDepotSlots(Game game) {
        return game.board().depots().stream()
                .flatMap((Depot depot) -> IntStream.range(0, depot.slotCount()).mapToObj(depot::tileAt))
                .filter(java.util.Optional::isPresent)
                .count();
    }
}
