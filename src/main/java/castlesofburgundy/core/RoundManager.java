package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.TileSupplier;

import java.util.Objects;

/**
 * 페이즈/라운드 진행과 보드 리셋/보급만 담당하는 최소 골격.
 * - 페이즈 시작: 보드의 섹션 타일 전부 제거 → 공급원에서 다시 채움
 * - 라운드: 1..5 카운팅만 수행(현재는 흰 주사위/상품 무시)
 */
public class RoundManager {
    private final BoardState boardState;
    private final TileSupplier tileSupply;

    private Phase currentPhase = Phase.A;
    private int roundIndex = -1; // 0..4

    public RoundManager(BoardState sectionBoard, TileSupplier tileSupply) {
        this.boardState = Objects.requireNonNull(sectionBoard);
        this.tileSupply = Objects.requireNonNull(tileSupply);
    }

    public Phase phase() {
        return currentPhase;
    }

    public int roundIndex() {
        return roundIndex;
    }

    /**
     * 페이즈 시작: 섹션 타일 리셋 & 채우기
     */
    public void startPhase(Phase phase) {
        this.currentPhase = Objects.requireNonNull(phase);
        boardState.removeAll();
        boardState.fillAllFromSupply(tileSupply);
        this.roundIndex = -1;
    }

    /**
     * 다음 라운드로 이동. 1..5 반환
     */
    public int nextRound() {
        if (roundIndex >= 4) {
            throw new IllegalStateException("이미 5라운드가 끝났습니다.");
        }
        roundIndex++;
        return roundIndex + 1;
    }

    public boolean hasMoreRounds() {
        return roundIndex < 4;
    }
}
