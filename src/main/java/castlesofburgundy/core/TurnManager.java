package castlesofburgundy.core;

import castlesofburgundy.player.Player;
import java.util.Scanner;

public final class TurnManager {
    private final GameContext ctx;
    private final Scanner in;

    public TurnManager(GameContext ctx, Scanner in) {
        this.ctx = ctx;
        this.in = in;
    }

    public void playerTurn(Player player) {
        System.out.println("\n=== 플레이어의 턴 ===");
        int d1 = Dice.roll();
        int d2 = Dice.roll();
        System.out.println("굴린 주사위: " + d1 + ", " + d2);

        handleDie(player, d1, 1);
    }

    private void handleDie(Player player, int die, int dieIndex) {
        System.out.println("\n[" + dieIndex + "번째 주사위: " + die + "]");
    }
}
