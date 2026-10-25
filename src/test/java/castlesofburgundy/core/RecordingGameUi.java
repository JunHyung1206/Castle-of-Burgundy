package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
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
    private final Deque<String> scriptedOptionTexts = new ArrayDeque<>();
    final List<List<String>> menus = new ArrayList<>();
    int chooseCalls = 0;

    void scriptChoices(int... choices) {
        for (int c : choices) {
            scriptedChoices.add(c);
        }
    }

    /** 보기 중 이 문자열을 포함한 첫 항목을 차례로 고른다. (scriptChoices보다 먼저 소비) */
    void scriptOptionsContaining(String... texts) {
        for (String t : texts) {
            scriptedOptionTexts.add(t);
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
        menus.add(List.copyOf(options));
        if (!scriptedOptionTexts.isEmpty()) {
            String text = scriptedOptionTexts.poll();
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).contains(text)) {
                    return i;
                }
            }
            throw new AssertionError("'" + text + "'를 포함한 보기가 없습니다: " + options);
        }
        return scriptedChoices.isEmpty() ? 0 : scriptedChoices.poll();
    }

    @Override
    public String askLine(String prompt) {
        return scriptedLines.isEmpty() ? "" : scriptedLines.poll();
    }

    @Override
    public void showGameBoard(GameBoard board) {
    }

    @Override
    public void showPersonalBoard(PersonalBoard board) {
    }

    @Override
    public void showPlayer(Player player) {
    }
}
