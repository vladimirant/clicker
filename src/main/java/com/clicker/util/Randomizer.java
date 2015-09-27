package com.clicker.util;

import java.util.Random;

/**
 * Created by vladimir on 19.09.15.
 */
public class Randomizer {
    private static final Random random = new Random();

    public static Integer getRandomTime(int leftBound, int rightBound) throws IllegalArgumentException {
        if (leftBound < 0 || rightBound <= 0) {
            throw new IllegalArgumentException("Params must be more then 0");
        }
        if (leftBound >= rightBound) {
            throw new IllegalArgumentException("Left bound must be low right bound");
        }

        return leftBound + random.nextInt(rightBound - leftBound);
    }

}
