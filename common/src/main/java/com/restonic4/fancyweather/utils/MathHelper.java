package com.restonic4.fancyweather.utils;

public class MathHelper {
    public static int getTicksForSeconds(int seconds) {
        return seconds * 20;
    }

    public static int getTicksForMinutes(int minutes) {
        return minutes * 60 * 20;
    }
}
