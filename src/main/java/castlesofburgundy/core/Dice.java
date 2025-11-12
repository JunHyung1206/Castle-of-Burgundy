package castlesofburgundy.core;

import camp.nextstep.edu.missionutils.Randoms;

public class Dice {
    public static int roll(){
        return Randoms.pickNumberInRange(1, 6);
    }
}
