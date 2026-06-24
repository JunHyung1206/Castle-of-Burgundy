package castlesofburgundy.core;

/**
 * 한 페이즈 = 5라운드. 라운드 진행 카운팅만 담당한다.
 * (보드 리셋/보급은 페이즈 진행을 지휘하는 PhaseRunner의 책임)
 */
public class RoundManager {
    private static final int ROUNDS_PER_PHASE = 5;

    private int roundIndex = -1; // 0..4, -1 = 페이즈 시작 전

    /**
     * 새 페이즈를 위해 라운드 카운터를 초기화.
     */
    public void resetForNewPhase() {
        roundIndex = -1;
    }

    /**
     * 다음 라운드로 이동. 1..5 반환.
     */
    public int nextRound() {
        if (!hasMoreRounds()) {
            throw new IllegalStateException("이미 " + ROUNDS_PER_PHASE + "라운드가 끝났습니다.");
        }
        roundIndex++;
        return roundIndex + 1;
    }

    public boolean hasMoreRounds() {
        return roundIndex < ROUNDS_PER_PHASE - 1;
    }
}
