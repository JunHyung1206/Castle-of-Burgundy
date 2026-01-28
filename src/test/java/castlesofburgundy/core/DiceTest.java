package castlesofburgundy.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiceTest {

    @Test
    void test() {
        Dice dice = new Dice();
        List<Integer> results = new ArrayList<>(List.of(0, 0, 0, 0, 0, 0, 0));
        for (int i = 0; i < 100000; i++) {
            int roll = dice.roll();
            results.set(roll, results.get(roll) + 1);
        }
        System.out.println("results = " + results);
    }

}