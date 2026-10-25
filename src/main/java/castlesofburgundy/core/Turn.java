package castlesofburgundy.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 한 플레이어의 턴 동안만 유지되는 상태.
 * 굴린 주사위는 원하는 순서로 하나씩 쓰고, 중앙 시장 구매는 주사위를 쓰지 않는 대신 턴당 한 번만 할 수 있다.
 */
final class Turn {
    private final List<Integer> remainingDice = new ArrayList<>();
    private boolean centralMarketBought;

    Turn(int... dice) {
        for (int die : dice) {
            remainingDice.add(die);
        }
    }

    List<Integer> remainingDice() {
        return List.copyOf(remainingDice);
    }

    void useDie(int die) {
        if (!remainingDice.remove(Integer.valueOf(die))) {
            throw new IllegalArgumentException("남아 있지 않은 주사위: " + die);
        }
    }

    boolean canBuyFromCentralMarket() {
        return !centralMarketBought;
    }

    void markCentralMarketBought() {
        if (centralMarketBought) {
            throw new IllegalStateException("중앙 시장 구매는 턴당 한 번입니다.");
        }
        centralMarketBought = true;
    }
}
