package com.restonic4.fancyweather.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {
    public static int getRandomInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Min value should be smaller than Max value!");
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
