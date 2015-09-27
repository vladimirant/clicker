package com.clicker.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vladimir on 19.09.15.
 */
public class RandomizerTest {

    @Test
    public void testGetRandomTime() {
        int leftBound = 100;
        int rightBound = 200;
        int targetTime = Randomizer.getRandomTime(leftBound, rightBound);
        assertTrue(targetTime >= leftBound && targetTime <= rightBound);

        leftBound = 1000;
        rightBound = 2500;
        targetTime = Randomizer.getRandomTime(leftBound, rightBound);
        assertTrue(targetTime >= leftBound && targetTime <= rightBound);

        leftBound = 18000;
        rightBound = 22000;
        targetTime = Randomizer.getRandomTime(leftBound, rightBound);
        assertTrue(targetTime >= leftBound && targetTime <= rightBound);

        leftBound = 1;
        rightBound = 22000;
        targetTime = Randomizer.getRandomTime(leftBound, rightBound);
        assertTrue(targetTime >= leftBound && targetTime <= rightBound);

        leftBound = 0;
        rightBound = 1;
        targetTime = Randomizer.getRandomTime(leftBound, rightBound);
        assertTrue(targetTime >= leftBound && targetTime <= rightBound);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testException1GetRandomTime() {
        int leftBound = -1;
        int rightBound = -25;

        Randomizer.getRandomTime(leftBound, rightBound);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException2GetRandomTime() {
        int leftBound = 100;
        int rightBound = 99;

        Randomizer.getRandomTime(leftBound, rightBound);
    }
}