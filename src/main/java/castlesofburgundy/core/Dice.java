package castlesofburgundy.core;


import java.util.Random;
import java.util.function.IntSupplier;

public final class Dice {
    private final IntSupplier rolls;

    public Dice() {
        this(new Random());
    }

    public Dice(long seed) {
        this(new Random(seed));
    }

    private Dice(Random random) {
        this(() -> random.nextInt(1, 7));
    }

    /** 굴림 결과를 정해 두고 흐름을 검증하는 테스트용. */
    Dice(IntSupplier rolls) {
        this.rolls = rolls;
    }

    public int roll() {
        return rolls.getAsInt();
    }
}
