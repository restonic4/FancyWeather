package com.restonic4.fancyweather;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Fancyweather {
    public Fancyweather(IEventBus eventBus) {
        MainFancyWeather.init();
    }
}
