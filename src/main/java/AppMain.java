import castlesofburgundy.view.ConsoleGame;

import java.nio.file.Path;
import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        long seed = 42L;
        Path statusFile = Path.of(args.length > 0 ? args[0] : "status.txt");
        new ConsoleGame(new Scanner(System.in), System.out, statusFile).run(seed);
    }
}
