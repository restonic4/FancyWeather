package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.custom.weather.WeatherVisualEffectsController;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Redirect(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"
            )
    )
    private float injectCustomCloudinessForCelestialBodies(ClientLevel instance, float partialTick) {
        float vanillaRain = instance.getRainLevel(partialTick);
        float customClouds = WeatherVisualEffectsController.getCloudiness();

        // Return the maximum value.
        // If clouds = 1.0, this returns 1.0.
        // The game calculates alpha as (1.0 - returnedValue), so alpha becomes 0.0 (Invisible/Transparent).
        return Math.max(vanillaRain, customClouds);
    }
}