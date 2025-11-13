package castlesofburgundy.player;

import castlesofburgundy.core.Dice;
import castlesofburgundy.tile.*;

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
        if (workersToSpend < 0) throw new IllegalArgumentException("workersToSpend >= 0");
        if (workers < workersToSpend) throw new IllegalStateException("일꾼 부족");
        workers -= workersToSpend;
        int delta = (sign >= 0 ? +workersToSpend : -workersToSpend);
        return wrapAdd(die, delta);
    }

    private int wrapAdd(int face, int delta) {
        return ((face - 1 + delta) % 6 + 6) % 6 + 1;
    }

    // 액션: 일꾼 받기(주사위 값 무관) → 2개
    public void actionTakeWorkers() {
        workers += 2;
    }

    // 액션: (보드에서 이미 얻어온) 타일을 저장소에 넣기
    public void actionAddToStorage(Tile tile) {
        storage.add(tile);
    }

    // 액션: 저장소의 첫 타일을 꺼내 특정 칸에 배치(주사위 한 개 사용)
    public void actionPlaceFirstFromStorage(int cellId, int dieUsed) {
        Tile t = storage.removeFirst();
        board.place(cellId, t, dieUsed);
        // TODO: 배치 효과 및 점수/코인 반영은 차후 추가
    }

    // 점수/코인 유틸
    public void addScore(int v) {
        score += v;
    }

    public void addSilver(int v) {
        silver += v;
    }
}
