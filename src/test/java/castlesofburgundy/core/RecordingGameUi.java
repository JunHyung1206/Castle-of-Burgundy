package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 테스트용 GameUi. 출력 메시지를 기록하고, 선택/입력은 미리 대본으로 넣어둔다.
 * 콘솔 없이 핵심 흐름(성 추가 행동, 구역 완성 서술 등)을 검증하기 위한 더블.
 */
final class RecordingGameUi implements GameUi {
    final List<String> messages = new ArrayList<>();
    private final Deque<Integer> scriptedChoices = new ArrayDeque<>();
    private final Deque<String> scriptedLines = new ArrayDeque<>();
    int chooseCalls = 0;

    void scriptChoices(int... choices) {
        for (int c : choices) {
            scriptedChoices.add(c);
        }
    }

    void scriptLines(String... lines) {
        for (String l : lines) {
            scriptedLines.add(l);
        }
    }

    boolean messaged(String substring) {
        return messages.stream().anyMatch(m -> m.contains(substring));
    }

    @Override
    public void message(String text) {
        messages.add(text);
    }

    @Override
    public int chooseIndex(List<String> options) {
        chooseCalls++;
        return scriptedChoices.isEmpty() ? 0 : scriptedChoices.poll();
    }

    @Override
    public String askLine(String prompt) {
        return scriptedLines.isEmpty() ? "" : scriptedLines.poll();
    }

    @Override
    public void showGameBoard(GameBoardLayout layout, BoardState boardState) {
    }

    @Override
    public void showPersonalBoard(PersonalBoard board) {
    }

    @Override
    public void showPlayer(Player player) {
    }
}
