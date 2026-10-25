package castlesofburgundy.player;

/**
 * 게임 참가자와 그가 판 밖에 두는 자원(일꾼, 은화, 점수).
 * 판 위의 타일·상품은 PersonalBoard가 맡는다.
 */
public final class Player {
    private final PersonalBoard board;
    private final String name;

    private int workers;
    private int silver = 0;
    private int score = 0;

    public Player(PersonalBoard board, String name, int workers) {
        this.board = board;
        this.name = name;
        this.workers = workers;
    }

    public Player(PersonalBoard board, String name) {
        this(board, name, 2);
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

    public void addScore(int value) {
        score += value;
    }

    public void addSilver(int value) {
        silver += value;
    }

    public int spendSilver(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("silverToSpend >= 0");
        }
        if (silver < value) {
            throw new IllegalStateException("은화 부족");
        }
        silver -= value;
        return silver;
    }

    public void gainWorkers(int value) {
        workers += value;
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
