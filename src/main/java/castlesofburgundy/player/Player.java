package castlesofburgundy.player;

import castlesofburgundy.tile.Tile;

public final class Player {
    private final PersonalBoard board;
    private final Storage storage;
    private final String name;

    private int workers;
    private int silver = 0;
    private int score = 0;

    public Player(PersonalBoard board, int storageCapacity, String name, int workers) {
        this.board = board;
        this.storage = new Storage(storageCapacity);
        this.name = name;
        this.workers = workers;
    }

    public Player(PersonalBoard board, int storageCapacity, String name) {
        this(board, storageCapacity, name, 2);
    }

    public void addToStorage(Tile tile) {
        storage.add(tile);
    }

    public int spendWorkers(int usedWorkers) {
        if (usedWorkers < 0) {
            throw new IllegalArgumentException("workersToSpend >= 0");
        }
        if (workers < usedWorkers) {
            throw new IllegalStateException("일꾼 부족");
        }
        workers -= usedWorkers;
        return workers;
    }

    public void placeTileFromStorage(int storageId, int cellId, int dieUsed) { // (원래 actionPlaceFirstFromStorage)
        Tile tile = storage.removeAt(storageId);
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


    public int getWorkers() {
        return workers;
    }

    public int getSilver() {
        return silver;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }
}
