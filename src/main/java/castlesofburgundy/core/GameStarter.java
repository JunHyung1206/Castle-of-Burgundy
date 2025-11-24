package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.tile.TileType;

import java.util.Map;

public final class GameStarter {

    public static GameContext startNewGame(long seed) {
        GameBoardLayout layout = new GameBoardLayout();

        // 2) 타입별 공급 수량 (예시값; 필요 시 조정)
        Map<TileType, Integer> counts = Map.of(
                TileType.CASTLE, 8,
                TileType.SHIP, 100,
                TileType.MINE, 100,
                TileType.ANIMAL, 200,
                TileType.BUILDING, 280,
                TileType.KNOWLEDGE, 180
        );

        // 3) 타일 공급 섹션에 맞게 채우기
        TileSupplier supplier = new TileSupplier(counts, seed);
        BoardState boardState = new BoardState(layout);

        boardState.fillAllFromSupply(supplier);
        return new GameContext(seed, layout, boardState, supplier);
    }
}
