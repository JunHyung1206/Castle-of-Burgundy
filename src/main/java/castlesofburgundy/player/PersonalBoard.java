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
        // TODO: 배치 효과(성/동물/건물/지식 등)는 이후 단계에서 훅 추가
    }

    public Set<Integer> occupiedCells() {
        return Collections.unmodifiableSet(placed.keySet());
    }

    // PersonalBoard.java (추가)
    public Set<Integer> legalPlacements(Tile tile, int dieUsed) {
        if (tile == null) return Set.of();
        Set<Integer> out = new LinkedHashSet<>();
        for (PersonalCell cell : layout.all()) {
            if (canPlace(cell.id(), tile, dieUsed)) out.add(cell.id());
        }
        return out;
    }
    public PersonalLayout getLayout() {
        return layout;
    }

}
