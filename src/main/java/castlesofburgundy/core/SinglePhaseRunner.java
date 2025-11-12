package castlesofburgundy.core;

import castlesofburgundy.util.BoardRenderer;
import castlesofburgundy.section.SectionBoard;
import castlesofburgundy.board.GameBoardLayout;

/**
 * 한 페이즈만 진행하는 러너(테스트/데모용).
 * - startPhase(phase)로 보드 리셋/보급
 * - 5라운드만 순차 실행
 */
public final class SinglePhaseRunner {
    private final RoundManager rounds;
    private final GameBoardLayout layout;
    private final SectionBoard sectionBoard;

    public SinglePhaseRunner(RoundManager rounds, GameBoardLayout layout, SectionBoard sectionBoard) {
        this.rounds = rounds;
        this.layout = layout;
        this.sectionBoard = sectionBoard;
    }

    public void run(Phase phase) {
        rounds.startPhase(phase);
        System.out.println(" === PHASE " + phase + " START (single) ===");
                BoardRenderer.printBoard(layout, sectionBoard);

        while (rounds.hasMoreRounds()) {
            int r = rounds.nextRound();
            int d1 = Dice.roll();
            int d2 = Dice.roll();
            System.out.printf("-- Round %d: Player dice = [%d, %d]%n", r, d1, d2);
            // TODO: 여기에 실제 액션 처리 추가
        }
        System.out.println("=== PHASE " + phase + " END === ");
    }
}