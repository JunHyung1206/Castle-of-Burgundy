package castlesofburgundy.core;

import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Game의 읽기 전용 스냅샷. 바깥(화면·API)은 이것만 읽고, 상태는 Game.apply로만 바뀐다.
 * 플레이어는 자리 번호(seat, 시작 플레이어 0)로 가리킨다.
 * 타일(Tile)과 개인 보드의 칸 배치(PersonalLayout)는 바뀌지 않는 값이라 그대로 담는다.
 *
 * @param currentSeat 지금 차례인 플레이어의 자리. 게임이 끝났으면 -1
 * @param turnOrder   턴 순서 트랙의 자리 순서(앞이 먼저)
 */
public record GameState(
        Phase phase,
        int round,
        int currentSeat,
        List<Integer> turnOrder,
        List<PlayerState> players,
        BoardState board
) {
    public boolean isOver() {
        return currentSeat < 0;
    }

    public PlayerState current() {
        if (isOver()) {
            throw new IllegalStateException("게임이 끝났습니다.");
        }
        return players.get(currentSeat);
    }

    public PlayerState player(int seat) {
        return players.get(seat);
    }

    /**
     * @param storage 타일 저장소(앞에서부터 칸 번호 0, 1, 2)
     * @param goods   보관 중인 상품 색별 개수(0개인 색은 없음)
     * @param estate  영지에 놓인 타일: 칸 번호 → 타일
     */
    public record PlayerState(
            int seat,
            String name,
            int workers,
            int silver,
            int score,
            List<Tile> storage,
            Map<GoodsColor, Integer> goods,
            PersonalLayout layout,
            Map<Integer, Tile> estate
    ) {
        public int goodsCount(GoodsColor color) {
            return goods.getOrDefault(color, 0);
        }

        public Optional<Tile> placedAt(int cellId) {
            return Optional.ofNullable(estate.get(cellId));
        }
    }

    public record BoardState(List<DepotState> depots, List<Tile> centralMarket) {
        public DepotState depot(int number) {
            return depots.get(number - 1);
        }
    }

    public record DepotState(int number, List<SlotState> slots, List<GoodsColor> goods) {
        public Optional<Tile> tileAt(int slot) {
            return Optional.ofNullable(slots.get(slot).tile());
        }
    }

    /** 번호 창고의 칸 하나. tile이 null이면 빈 칸(이번 인원에 쓰지 않는 칸 포함). */
    public record SlotState(TileType allowedType, Tile tile) {
    }
}
