package com.restonic4.fancyweather.custom.sync.skippers;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.mixin.MobEffectInstanceAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class MobEffectsDurationSkipper {
    public static void apply(Entity entity, long ticksSkipped) {
        if (!FancyWeatherMidnightConfig.enablePotionEffectsDurationSync) return;

        if (entity instanceof LivingEntity living) {
            // Create a copy of the effects list to avoid ConcurrentModificationException
            // getActiveEffectsMap().values() gives us the raw instances
            List<MobEffectInstance> effectsToUpdate = new ArrayList<>(living.getActiveEffects());

            for (MobEffectInstance effect : effectsToUpdate) {
                int currentDuration = effect.getDuration();
                long newDurationLong = currentDuration - ticksSkipped;

                if (newDurationLong <= 0) {
                    // If the effect has expired, remove it properly
                    living.removeEffect(effect.getEffect());
                } else {
                    // Otherwise, update the duration using the Accessor
                    ((MobEffectInstanceAccessor) effect).setDuration((int) newDurationLong);
                }
            }
        }
    }
}
