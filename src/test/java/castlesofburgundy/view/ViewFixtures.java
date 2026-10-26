package castlesofburgundy.view;

import castlesofburgundy.core.GameState;
import castlesofburgundy.core.GameState.PlayerState;
import castlesofburgundy.core.Phase;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** view 테스트용 스냅샷. view는 GameState만 읽으므로 게임을 돌리지 않고 원하는 화면 상태를 만든다. */
final class ViewFixtures {
    private ViewFixtures() {
    }

    /** 성이 19번 칸에 있고, 저장소·상품·추가로 놓인 타일을 정한 플레이어. */
    static PlayerState player(int seat, String name, List<Tile> storage, Map<GoodsColor, Integer> goods, Map<Integer, Tile> placed) {
        Map<Integer, Tile> estate = new HashMap<>(placed);
        estate.put(19, BaseTile.of(TileType.CASTLE, -1));
        return new PlayerState(seat, name, 2, 1, 0, List.copyOf(storage), Map.copyOf(goods), new PersonalLayout(), Map.copyOf(estate));
    }

    /** 번호 창고가 모두 빈 Phase A 1라운드, seat 0 차례. */
    static GameState state(PlayerState... players) {
        List<GameState.DepotState> depots = new ArrayList<>();
        for (int number = 1; number <= 6; number++) {
            List<GameState.SlotState> slots = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                slots.add(new GameState.SlotState(TileType.BUILDING, null));
            }
            depots.add(new GameState.DepotState(number, slots, List.of()));
        }
        List<Integer> order = new ArrayList<>();
        for (PlayerState player : players) {
            order.add(player.seat());
        }
        return new GameState(Phase.A, 1, 0, order, List.of(players), new GameState.BoardState(depots, List.of()));
    }
}
