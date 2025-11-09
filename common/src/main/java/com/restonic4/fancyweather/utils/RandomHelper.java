package com.restonic4.fancyweather.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {
    public static int getRandomInt(int min, int max) {
        if (min == max) {
            return min;
        }

        // Swap if min is greater than max
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
