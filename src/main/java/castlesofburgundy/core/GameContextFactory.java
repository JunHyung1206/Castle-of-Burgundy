package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.tile.TileType;

import java.util.Map;

public final class GameContextFactory {

    public static GameContext startNewGame(long seed) {
        GameBoardLayout layout = new GameBoardLayout();

        // 타입별 공급 수량 = 룰북의 색타일(검은색 중앙창고 몫 제외) 수량
        // 건물40 가축20 수도원20 성14 광산10 배20 (검은색: 건물16 가축8 수도원6 성2 광산2 배6)
        Map<TileType, Integer> counts = Map.of(
                TileType.CASTLE, 14,
                TileType.SHIP, 20,
                TileType.MINE, 10,
                TileType.ANIMAL, 20,
                TileType.BUILDING, 40,
                TileType.KNOWLEDGE, 20
        );

        TileSupplier supplier = new TileSupplier(counts, seed);
        // 보드 채우기는 페이즈 시작(PhaseRunner)에서 하므로 여기서는 비워둔 채로 둔다.
        BoardState boardState = new BoardState(layout);

        return new GameContext(seed, layout, boardState, supplier, new Dice(seed));
    }
}
