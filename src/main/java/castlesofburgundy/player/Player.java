package castlesofburgundy.player;

import castlesofburgundy.tile.Tile;

public final class Player {
    private final PersonalBoard board;
    private final Storage storage;
    private int workers = 0;
    private int silver = 0;
    private int score = 0;

    public Player(PersonalBoard board, int storageCapacity) {
        this.board = board;
        this.storage = new Storage(storageCapacity);
    }

    // 규칙: 언제든지 일꾼 1개로 주사위 하나에 +/-1 (1↔6 랩). 여러 개 중첩 가능
    public int spendWorkersToAdjust(int die, int workersToSpend, int sign) {
        if (workersToSpend < 0) {
            throw new IllegalArgumentException("workersToSpend >= 0");
        }
        if (workers < workersToSpend) {
            throw new IllegalStateException("일꾼 부족");
        }
        workers -= workersToSpend;
        int delta = (sign >= 0 ? +workersToSpend : -workersToSpend);
        return wrapAdd(die, delta);
    }

    private int wrapAdd(int face, int delta) {
        return ((face - 1 + delta) % 6 + 6) % 6 + 1;
    }

    public void addToStorage(Tile tile) {
        storage.add(tile);
    }

    public void placeTileFromStorage(int storageId, int cellId, int dieUsed) { // (원래 actionPlaceFirstFromStorage)
        Tile tile = storage.remove(storageId);
        board.place(cellId, tile, dieUsed);
    }

    public void addScore(int value) {
        score += value;
    }

    public void addSilver(int value) {
        silver += value;
    }

    public void gainWorkers(int value) {
        workers += value;
    }

    public Storage getStorage() {
        return storage;
    }

    public PersonalBoard getBoard() {
        return board;
    }
}
