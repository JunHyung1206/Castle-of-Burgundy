package castlesofburgundy.player;

import castlesofburgundy.tile.*;

import java.util.*;


public final class PersonalBoard {
    private final PersonalLayout layout;
    private final Map<Integer, Tile> placed = new HashMap<>(); // cellId→tile

    public PersonalBoard(PersonalLayout layout) {
        this.layout = Objects.requireNonNull(layout);
    }

    /**
     * 게임 시작 시 1회: 성 칸 아무 곳이나 무료 배치 (주사위/인접성 체크 없음)
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
        List<Integer> neighbors = PersonalGrid.getNeighbors(cellId);
        return neighbors.stream().anyMatch(placed::containsKey);
    }

    public void place(int cellId, Tile tile, int dieUsed) {
        if (!canPlace(cellId, tile, dieUsed))
            throw new IllegalArgumentException("배치 불가: cell=" + cellId + ", die=" + dieUsed + ", tile=" + tile);
        placed.put(cellId, tile);
    }

    public Set<Integer> legalPlacements(Tile tile, int dieUsed) {
        if (tile == null) return Set.of();
        Set<Integer> out = new LinkedHashSet<>();
        for (PersonalCell cell : layout.all()) {
            if (canPlace(cell.id(), tile, dieUsed)) out.add(cell.id());
        }
        return out;
    }

    // PersonalBoard.java 안에 추가

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
            for (int nb : PersonalGrid.getNeighbors(cur)) {
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

    public PersonalLayout getLayout() {
        return layout;
    }

}
