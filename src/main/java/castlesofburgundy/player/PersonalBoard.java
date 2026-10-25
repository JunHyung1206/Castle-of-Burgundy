package castlesofburgundy.player;

import castlesofburgundy.goods.GoodsStorage;
import castlesofburgundy.tile.*;

import java.util.*;

/**
 * 개인 보드 판 위에 인쇄된 것 전부: 영지(37칸), 타일 저장소(3칸), 상품 저장소(3색).
 * 저장소와 상품 저장소는 각자 자기 규칙을 지키므로 그대로 내주고,
 * 저장소 → 영지처럼 부품 사이를 오가는 동작만 보드가 맡는다.
 */
public final class PersonalBoard {
    private static final int STORAGE_CAPACITY = 3;

    private final PersonalLayout layout;
    private final Map<Integer, Tile> placed = new HashMap<>(); // cellId→tile
    private final Storage storage = new Storage(STORAGE_CAPACITY);
    private final GoodsStorage goods = new GoodsStorage();

    public PersonalBoard(PersonalLayout layout) {
        this.layout = Objects.requireNonNull(layout);
    }

    /**
     * 게임 시작 시 1회: 영지 가운데 시작 성 칸에 성을 놓는다.
     */
    public void setupStartingCastle() {
        setupInitialCastle(layout.startingCastleCell());
    }

    /**
     * 성 칸 하나에 성을 무료 배치 (주사위/인접성 체크 없음).
     * 게임은 setupStartingCastle을 쓰고, 이 메서드는 다른 칸에서 시작하는 상황을 만들 때 쓴다.
     */
    public void setupInitialCastle(int cellId) {
        PersonalCell cell = layout.get(cellId);
        if (cell == null) {
            throw new IllegalArgumentException("존재하지 않는 칸: " + cellId);
        }
        if (isOccupied(cellId)) {
            throw new IllegalStateException("이미 점유된 칸: " + cellId);
        }
        if (cell.type() != TileType.CASTLE) {
            throw new IllegalArgumentException("초기 배치는 성 칸에만 가능합니다: " + cellId);
        }

        placed.put(cellId, BaseTile.of(TileType.CASTLE, -1));
    }

    public boolean isOccupied(int cellId) {
        return placed.containsKey(cellId);
    }

    public Optional<Tile> getPlacedTile(int cellId) {
        return Optional.ofNullable(placed.get(cellId));
    }

    public boolean canPlace(int cellId, Tile tile, int dieUsed) {
        PersonalCell cell = layout.get(cellId);
        if (cell == null || tile == null) {
            return false;
        }
        if (isOccupied(cellId)) {// 빈칸만
            return false;
        }
        if (cell.type() != tile.type()) { // 타입 일치
            return false;
        }
        if (cell.die() != dieUsed) { // 눈 일치
            return false;
        }
        // 인접성: 이미 놓인 칸과 인접해야 함(시작 성 제외)
        List<Integer> neighbors = layout.getNeighbors(cellId);
        if (neighbors.stream().noneMatch(placed::containsKey)) {
            return false;
        }

        // 건물: 한 도시(연결된 베이지 영역)에 같은 종류는 하나만
        if (tile instanceof BuildingTile building && cityHasBuilding(cellId, building.kind())) {
            return false;
        }

        return true;
    }

    private boolean cityHasBuilding(int cellId, BuildingKind kind) {
        for (Tile t : placedTilesInRegion(cellId)) {
            if (t instanceof BuildingTile b && b.kind() == kind) {
                return true;
            }
        }
        return false;
    }

    public void place(int cellId, Tile tile, int dieUsed) {
        if (!canPlace(cellId, tile, dieUsed))
            throw new IllegalArgumentException("배치 불가: cell=" + cellId + ", die=" + dieUsed + ", tile=" + tile);
        placed.put(cellId, tile);
    }

    /**
     * 저장소의 타일을 영지에 놓는다. 배치가 불가능하면 예외를 던지고 저장소는 그대로 둔다.
     */
    public Tile placeFromStorage(int storageIndex, int cellId, int dieUsed) {
        Tile tile = storage.get(storageIndex);
        place(cellId, tile, dieUsed);
        storage.removeAt(storageIndex);
        return tile;
    }

    public Set<Integer> legalPlacements(Tile tile, int dieUsed) {
        if (tile == null) return Set.of();
        Set<Integer> out = new LinkedHashSet<>();
        for (PersonalCell cell : layout.all()) {
            if (canPlace(cell.id(), tile, dieUsed)) out.add(cell.id());
        }
        return out;
    }

    /**
     * startCellId가 속한 "같은 타입의 연결된 구역" 전체를 반환.
     * (레이아웃 기준, 배치 여부와는 무관하게 타입만 보고 연결 컴포넌트 구성)
     */
    public Set<Integer> sameTypeRegion(int startCellId) {
        PersonalCell start = layout.get(startCellId);
        if (start == null) {
            return Set.of();
        }
        TileType targetType = start.type();

        Set<Integer> visited = new LinkedHashSet<>();
        Deque<Integer> dq = new ArrayDeque<>();

        visited.add(startCellId);
        dq.add(startCellId);

        while (!dq.isEmpty()) {
            int cur = dq.poll();
            for (int nb : layout.getNeighbors(cur)) {
                if (visited.contains(nb)) {
                    continue;
                }
                PersonalCell nbCell = layout.get(nb);
                if (nbCell != null && nbCell.type() == targetType) {
                    visited.add(nb);
                    dq.add(nb);
                }
            }
        }
        return visited;
    }

    public boolean isRegionCompleted(int startCellId) {
        Set<Integer> region = sameTypeRegion(startCellId);
        if (region.isEmpty()) {
            return false;
        }
        for (int id : region) {
            if (!placed.containsKey(id)) {
                return false;
            }
        }
        return true;
    }

    /**
     * startCellId가 속한 같은 타입 구역에서 실제로 타일이 놓인 칸들의 타일 목록.
     */
    public List<Tile> placedTilesInRegion(int startCellId) {
        List<Tile> result = new ArrayList<>();
        for (int id : sameTypeRegion(startCellId)) {
            Tile t = placed.get(id);
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }

    public int completedRegionSizeIfAny(int startCellId) {
        Set<Integer> region = sameTypeRegion(startCellId);
        if (region.isEmpty()) {
            return 0;
        }
        for (int id : region) {
            if (!placed.containsKey(id)) {
                return 0;
            }
        }
        return region.size();
    }

    /**
     * 영지에 실제로 놓인 타일 중 해당 타입의 개수.
     */
    public int countPlacedOfType(TileType type) {
        int n = 0;
        for (Tile t : placed.values()) {
            if (t.type() == type) {
                n++;
            }
        }
        return n;
    }

    public PersonalLayout getLayout() {
        return layout;
    }

    public Storage getStorage() {
        return storage;
    }

    public GoodsStorage getGoods() {
        return goods;
    }

}
