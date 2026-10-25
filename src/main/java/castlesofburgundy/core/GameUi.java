package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;

import java.util.List;

/**
 * 핵심 로직과 입출력(콘솔/GUI/테스트) 사이의 경계(포트).
 * 핵심 로직은 이 인터페이스에만 의존하고, System.out/Scanner를 직접 알지 못한다.
 */
public interface GameUi {
    void message(String text);

    /** 보기 목록을 보여주고, 유효한 선택의 인덱스(0-base)를 받는다. */
    int chooseIndex(List<String> options);

    /** 프롬프트를 보여주고 한 줄 입력을 받는다. */
    String askLine(String prompt);

    void showGameBoard(GameBoard board);

    void showPersonalBoard(PersonalBoard board);

    void showPlayer(Player player);
}
