package com.restonic4.fancyweather.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class FancyWeatherMidnightConfig extends MidnightConfig {
    public static final String VANILLA = "vanilla";
    public static final String CUSTOM = "custom";

    @Entry(category = VANILLA) public static boolean enableImprovedBedRainSkip = true;
    @Entry(category = VANILLA) public static boolean enableRainShouldExtinguishCampfires = true;

    @Entry(category = CUSTOM) public static boolean enableCustom = true;
}
