package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.goods.GoodsSupply;

/** 공용 보드가 비어 있는 게임과, 그 위에서 쓰는 PlayerActions를 만드는 테스트 도우미. */
final class TestGames {
    private TestGames() {
    }

    static GameContext emptyContext() {
        return new GameContext(1L, new GameBoard(), new TileSupplier(1L), new Dice(1L), new GoodsSupply(1L));
    }

    static PlayerActions actions(int playerCount) {
        return new PlayerActions(emptyContext(), new RecordingGameUi(), playerCount);
    }

    static PlayerActions actions(GameContext ctx) {
        return new PlayerActions(ctx, new RecordingGameUi(), 2);
    }
}
