package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.goods.GoodsSupply;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;

import java.util.List;
import java.util.Optional;

/** 테스트용 게임·플레이어·PlayerActions를 만드는 도우미. */
final class TestGames {
    private TestGames() {
    }

    static GameContext emptyContext() {
        return new GameContext(1L, new GameBoard(), new TileSupplier(1L), new Dice(1L), new GoodsSupply(1L));
    }

    static PlayerActions actions(int playerCount) {
        return new PlayerActions(emptyContext(), playerCount);
    }

    static PlayerActions actions(GameContext ctx) {
        return new PlayerActions(ctx, 2);
    }

    /** 성을 가운데(19)에 둔, 일꾼 2개·은화 0개 플레이어. */
    static Player player(String name) {
        return player(name, 19);
    }

    static Player player(String name, int castleCell) {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(castleCell);
        return new Player(board, name);
    }

    /**
     * 주사위를 정해 둔 게임을 페이즈 A 첫 턴까지 진행한다.
     * rolls는 굴림 순서대로 쓰고(라운드 시작의 흰 주사위 → 차례마다 주사위 2개), 다 쓰면 처음부터 반복한다.
     */
    static Game game(List<Player> players, int... rolls) {
        int[] next = {0};
        Dice dice = new Dice(() -> rolls[next[0]++ % rolls.length]);
        GameContext ctx = new GameContext(1L, new GameBoard(), new TileSupplier(1L), dice, new GoodsSupply(1L));
        Game game = new Game(ctx, players);
        game.begin();
        return game;
    }

    /** 번호 창고의 육각 타일을 모두 치운다(선택지를 단순하게 만들 때). */
    static void emptyDepots(Game game) {
        game.board().refillDepots(game.players().size(), type -> Optional.empty());
    }
}
