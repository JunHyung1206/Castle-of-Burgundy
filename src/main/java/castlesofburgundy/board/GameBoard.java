package castlesofburgundy.board;

import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 공용 보드: 번호 창고 1~6, 중앙 검은 창고(중앙 시장), 턴 순서 트랙.
 * 타일을 어디서 뽑을지는 모르고, 채울 때 뽑는 함수를 받는다.
 */
public final class GameBoard {
    public static final int DEPOT_COUNT = 6;
    private static final int CENTRAL_MARKET_SIZE = 8;
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    /** 창고마다 칸 순서대로 쓰이는 최소 인원: 2인 12칸, 3인 18칸, 4인 24칸. */
    private static final List<Integer> SLOT_MIN_PLAYERS = List.of(2, 2, 3, 4);

    private final List<Depot> depots = List.of(
            new Depot(1, List.of(TileType.BUILDING, TileType.SHIP, TileType.KNOWLEDGE, TileType.ANIMAL), SLOT_MIN_PLAYERS),
            new Depot(2, List.of(TileType.KNOWLEDGE, TileType.CASTLE, TileType.BUILDING, TileType.BUILDING), SLOT_MIN_PLAYERS),
            new Depot(3, List.of(TileType.ANIMAL, TileType.BUILDING, TileType.SHIP, TileType.KNOWLEDGE), SLOT_MIN_PLAYERS),
            new Depot(4, List.of(TileType.SHIP, TileType.BUILDING, TileType.ANIMAL, TileType.MINE), SLOT_MIN_PLAYERS),
            new Depot(5, List.of(TileType.MINE, TileType.KNOWLEDGE, TileType.BUILDING, TileType.BUILDING), SLOT_MIN_PLAYERS),
            new Depot(6, List.of(TileType.BUILDING, TileType.ANIMAL, TileType.CASTLE, TileType.SHIP), SLOT_MIN_PLAYERS)
    );
    private final List<Tile> centralMarket = new ArrayList<>();
    private final TurnOrder turnOrder = new TurnOrder();

    public List<Depot> depots() {
        return depots;
    }

    public TurnOrder turnOrder() {
        return turnOrder;
    }

    public Depot depot(int number) {
        if (number < 1 || number > DEPOT_COUNT) {
            throw new IllegalArgumentException("창고 번호는 1~" + DEPOT_COUNT + ": " + number);
        }
        return depots.get(number - 1);
    }

    /**
     * 페이즈 시작: 인원수에 맞는 칸만 육각 타일을 새로 채운다. 상품은 그대로 둔다.
     * 1인 게임은 룰에 없으므로 2인 칸만 채운다.
     */
    public void refillDepots(int playerCount, Function<TileType, Optional<Tile>> draw) {
        if (playerCount < 1 || playerCount > MAX_PLAYERS) {
            throw new IllegalArgumentException("플레이어 수는 1~" + MAX_PLAYERS + ": " + playerCount);
        }
        int slotPlayers = Math.max(playerCount, MIN_PLAYERS);
        for (Depot depot : depots) {
            depot.refill(slotPlayers, draw);
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
