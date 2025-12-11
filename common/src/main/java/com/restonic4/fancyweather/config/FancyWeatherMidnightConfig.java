package com.restonic4.fancyweather.config;

import com.restonic4.fancyweather.Constants;
import eu.midnightdust.lib.config.MidnightConfig;

public class FancyWeatherMidnightConfig extends MidnightConfig {
    public static final String VANILLA = "vanilla";
    public static final String SYNC = "sync";

    /*
        VANILLA TAB
     */

    @Entry(category = VANILLA) public static boolean enableImprovedBedRainSkip = true;

    @Comment(category = VANILLA) public static Comment spacer1;

    @Entry(category = VANILLA) public static boolean enableRainShouldExtinguishCampfires = true;
    //@Condition(requiredOption = Constants.MOD_ID + ":enableRainShouldExtinguishCampfires", requiredValue = "true")
    @Entry(category = VANILLA, min = 20, max = 6000) public static int campfireRainLookupMinTickRange = 40;
    //@Condition(requiredOption = Constants.MOD_ID + ":enableRainShouldExtinguishCampfires", requiredValue = "true")
    @Entry(category = VANILLA, min = 20, max = 6000) public static int campfireRainLookupMaxTickRange = 120;

    @Comment(category = VANILLA) public static Comment spacer2;

    @Entry(category = VANILLA) public static boolean enableCampfireFireSpread = true;
    //@Condition(requiredOption = Constants.MOD_ID + ":enableCampfireFireSpread", requiredValue = "true")
    @Entry(category = VANILLA, min = 20, max = 6000) public static int campfireSpreadLookupMinTickRange = 160;
    //@Condition(requiredOption = Constants.MOD_ID + ":enableCampfireFireSpread", requiredValue = "true")
    @Entry(category = VANILLA, min = 20, max = 6000) public static int campfireSpreadLookupMaxTickRange = 600;

    @Comment(category = VANILLA) public static Comment spacer3;

    @Entry(category = VANILLA) public static boolean enableCropRainBoost = true;
    //@Condition(requiredOption = Constants.MOD_ID + ":enableCropRainBoost", requiredValue = "true")
    @Entry(category = VANILLA, min = 2, max = 64) public static int cropRainBoostMultiplier = 20;

    @Comment(category = VANILLA) public static Comment spacer4;

    @Entry(category = VANILLA) public static boolean enableFireSpreadingArrows = true;

    /*
        SYNC TAB
     */

    @Entry(category = SYNC) public static boolean enableSync = true;

    @Comment(category = SYNC) public static Comment spacer5;
}
