package castlesofburgundy.core;

import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.section.SectionBoard;
import castlesofburgundy.tile.TileSupply;
import castlesofburgundy.tile.TileType;

import java.util.Map;

public final class GameStarter {

    public static GameContext startNewGame(long seed) {
        GameBoardLayout layout = new GameBoardLayout();

        // 2) 타입별 공급 수량 (예시값; 필요 시 조정)
        Map<TileType, Integer> counts = Map.of(
                TileType.CASTLE,    12,
                TileType.SHIP,      16,
                TileType.MINE,      10,
                TileType.ANIMAL,    20,
                TileType.BUILDING,  28,
                TileType.KNOWLEDGE, 18
        );

        // 3) 타일 공급 섹션에 맞게 채우기
        TileSupply supply = new TileSupply(counts, seed);
        SectionBoard sectionBoard = new SectionBoard(layout);

        // 4) 게임 시작: 섹션에 맞게 전부 채우기
        sectionBoard.fillAllFromSupply(supply);

        return new GameContext(seed, layout, sectionBoard, supply);
    }
}
