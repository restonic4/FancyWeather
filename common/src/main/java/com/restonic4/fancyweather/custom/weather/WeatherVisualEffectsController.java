package com.restonic4.fancyweather.custom.weather;

import net.minecraft.util.Mth;

public class WeatherVisualEffectsController {
    // 0.0 = Clear Sky, 1.0 = Fully Gray/Stormy appearance
    private static float targetCloudiness = 0.0f;
    private static float currentCloudiness = 0.0f;

    private static float targetFogEnd = 0.0f;
    private static float currentFogEnd = 0.0f;

    public static void setCloudiness(float value) {
        targetCloudiness = Mth.clamp(value, 0.0f, 1.0f);
    }

    public static float getCloudiness() {
        return currentCloudiness;
    }

    public static void setFogEnd(float value) {
        targetFogEnd = Mth.clamp(value, 0.0f, 1.0f);
    }

    public static float getFogEnd() {
        return currentFogEnd;
    }

    /**
     * Transitions the weather effects
     */
    public static void tick() {
        if (currentCloudiness < targetCloudiness) {
            currentCloudiness += 0.01f;
            if (currentCloudiness > targetCloudiness) currentCloudiness = targetCloudiness;
        } else if (currentCloudiness > targetCloudiness) {
            currentCloudiness -= 0.01f;
            if (currentCloudiness < targetCloudiness) currentCloudiness = targetCloudiness;
        }

        if (currentFogEnd < targetFogEnd) {
            currentFogEnd += 0.01f;
            if (currentFogEnd > targetFogEnd) currentFogEnd = targetFogEnd;
        } else if (currentFogEnd > targetFogEnd) {
            currentFogEnd -= 0.01f;
            if (currentFogEnd < targetFogEnd) currentFogEnd = targetFogEnd;
        }
    }
}
