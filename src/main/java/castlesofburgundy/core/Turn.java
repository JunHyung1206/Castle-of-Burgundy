package castlesofburgundy.core;

/**
 * 한 플레이어의 턴 동안만 유지되는 상태.
 * 중앙 시장 구매는 주사위를 쓰지 않는 대신 턴당 한 번만 할 수 있다.
 */
final class Turn {
    private boolean centralMarketBought;

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
