package com.restonic4.fancyweather;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.platform.Services;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

public class MainFancyWeather {
    public static void init() {
        MidnightConfig.init(Constants.MOD_ID, FancyWeatherMidnightConfig.class);
        Synchronizer.init();
    }

    public static String sign(String string) {return Constants.MOD_ID + "_" + string; }
}
