package castlesofburgundy.board;

import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 공용 보드: 번호 창고 1~6과 중앙 검은 창고(중앙 시장).
 * 타일을 어디서 뽑을지는 모르고, 채울 때 뽑는 함수를 받는다.
 */
public final class GameBoard {
    public static final int DEPOT_COUNT = 6;
    private static final int CENTRAL_MARKET_SIZE = 8;

    private final List<Depot> depots = List.of(
            new Depot(1, List.of(TileType.BUILDING, TileType.SHIP, TileType.KNOWLEDGE, TileType.ANIMAL)),
            new Depot(2, List.of(TileType.KNOWLEDGE, TileType.CASTLE, TileType.BUILDING, TileType.BUILDING)),
            new Depot(3, List.of(TileType.ANIMAL, TileType.BUILDING, TileType.SHIP, TileType.KNOWLEDGE)),
            new Depot(4, List.of(TileType.SHIP, TileType.BUILDING, TileType.ANIMAL, TileType.MINE)),
            new Depot(5, List.of(TileType.MINE, TileType.KNOWLEDGE, TileType.BUILDING, TileType.BUILDING)),
            new Depot(6, List.of(TileType.BUILDING, TileType.ANIMAL, TileType.CASTLE, TileType.SHIP))
    );
    private final List<Tile> centralMarket = new ArrayList<>();

    public List<Depot> depots() {
        return depots;
    }

    public Depot depot(int number) {
        if (number < 1 || number > DEPOT_COUNT) {
            throw new IllegalArgumentException("창고 번호는 1~" + DEPOT_COUNT + ": " + number);
        }
        return depots.get(number - 1);
    }

    /** 페이즈 시작: 모든 번호 창고의 육각 타일을 새로 채운다. 상품은 그대로 둔다. */
    public void refillDepots(Function<TileType, Optional<Tile>> draw) {
        for (Depot depot : depots) {
            depot.refill(draw);
        }
    }

    public void refillCentralMarket(Supplier<Optional<Tile>> draw) {
        centralMarket.clear();
        for (int i = 0; i < CENTRAL_MARKET_SIZE; i++) {
            draw.get().ifPresent(centralMarket::add);
        }
    }

    public List<Tile> centralMarket() {
        return List.copyOf(centralMarket);
    }

    public Optional<Tile> getCentralMarket(int index) {
        if (index < 0 || index >= centralMarket.size()) {
            return Optional.empty();
        }
        return Optional.of(centralMarket.get(index));
    }

    public Optional<Tile> removeCentralMarketAt(int index) {
        if (index < 0 || index >= centralMarket.size()) {
            return Optional.empty();
        }
        return Optional.of(centralMarket.remove(index));
    }
}
