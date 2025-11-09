package com.restonic4.fancyweather;

import net.fabricmc.api.ModInitializer;

public class Fancyweather implements ModInitializer {
    @Override
    public void onInitialize() {
        MainFancyWeather.init();
    }
}
