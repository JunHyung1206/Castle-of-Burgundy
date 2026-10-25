import castlesofburgundy.view.ConsoleGame;

import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        long seed = 42L;
        new ConsoleGame(new Scanner(System.in), System.out).run(seed);
    }
}
