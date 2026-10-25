package castlesofburgundy.view;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.core.GameUi;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;

import java.util.List;
import java.util.Scanner;

/**
 * GameUi의 콘솔 구현(어댑터). System.out 출력·Scanner 입력과 *ConsoleView 렌더러를 사용한다.
 */
public final class ConsoleUi implements GameUi {
    private final Scanner in;

    public ConsoleUi(Scanner in) {
        this.in = in;
    }

    @Override
    public void message(String text) {
        System.out.println(text);
    }

    @Override
    public int chooseIndex(List<String> options) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + ") " + options.get(i));
        }
        while (true) {
            System.out.print("번호 선택(0~" + (options.size() - 1) + "): ");
            String line = in.nextLine();
            try {
                int v = Integer.parseInt(line);
                if (v >= 0 && v < options.size()) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("잘못된 입력입니다.");
        }
    }

    @Override
    public String askLine(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }

    @Override
    public void showGameBoard(GameBoard board) {
        System.out.println(GameBoardConsoleView.render(board));
    }

    @Override
    public void showPersonalBoard(PersonalBoard board) {
        System.out.println(PlayerConsoleView.renderBoardOnly(board));
    }

    @Override
    public void showPlayer(Player player) {
        System.out.println(PlayerConsoleView.render(player));
    }
}
