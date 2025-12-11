package com.restonic4.fancyweather.custom.sync;

import java.util.HashMap;
import java.util.Map;

public class WeatherSave {
    public long creation;
    public long last_seen;
    public Map<String, Object> weather_data = new HashMap<>();
}
