package castlesofburgundy.core;

import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TurnOrderTest {

    private Player newPlayer(String name) {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        return new Player(board, 3, name);
    }

    @Test
    @DisplayName("다리에서 전진하면 턴 순서가 한 칸 앞으로 당겨진다")
    void advanceMovesPlayerOneStepForward() {
        Player p1 = newPlayer("P1");
        Player p2 = newPlayer("P2");
        Player p3 = newPlayer("P3");
        TurnOrder turnOrder = new TurnOrder(new Player[]{p1, p2, p3});

        turnOrder.advance(p3);

        assertThat(turnOrder.currentOrder()).containsExactly(p1, p3, p2);

        turnOrder.advance(p3);

        assertThat(turnOrder.currentOrder()).containsExactly(p3, p1, p2);
    }

    @Test
    @DisplayName("이미 첫 번째인 플레이어가 전진하면 순서가 바뀌지 않는다")
    void firstPlayerCannotAdvanceFurther() {
        Player p1 = newPlayer("P1");
        Player p2 = newPlayer("P2");
        TurnOrder turnOrder = new TurnOrder(new Player[]{p1, p2});

        turnOrder.advance(p1);

        assertThat(turnOrder.currentOrder()).containsExactly(p1, p2);
    }
}
