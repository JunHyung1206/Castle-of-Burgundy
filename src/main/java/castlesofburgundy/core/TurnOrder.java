package castlesofburgundy.core;

import castlesofburgundy.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TurnOrder {
    private final List<Player> order;

    public TurnOrder(Player[] players) {
        this.order = new ArrayList<>(List.of(players));
    }

    public List<Player> currentOrder() {
        return List.copyOf(order);
    }

    public void advance(Player player) {
        int index = order.indexOf(player);
        if (index <= 0) {
            return;
        }
        Collections.swap(order, index, index - 1);
    }
}
