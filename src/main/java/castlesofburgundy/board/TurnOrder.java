package castlesofburgundy.board;

import castlesofburgundy.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 공용 보드의 턴 순서 트랙(다리). 게임 준비 때 한 번 자리를 정하고, 배를 놓으면 한 칸 앞으로 간다.
 */
public final class TurnOrder {
    private final List<Player> order = new ArrayList<>();

    /** 시작 플레이어부터 차례로 자리를 정한다. */
    public void seat(List<Player> players) {
        if (!order.isEmpty()) {
            throw new IllegalStateException("턴 순서는 이미 정해졌습니다.");
        }
        order.addAll(players);
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
