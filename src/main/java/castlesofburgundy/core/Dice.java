package castlesofburgundy.core;


import java.util.Random;

public final class Dice {
    private final Random random;

    public Dice() {
        this.random = new Random();
    }

    public Dice(long seed) {
        this.random = new Random(seed);
    }

    public int roll() {
        return random.nextInt(1, 7);
    }
}
