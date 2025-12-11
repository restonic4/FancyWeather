package com.restonic4.fancyweather.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.custom.weather.WeatherState;
import com.restonic4.fancyweather.custom.weather.WeatherStateStrength;
import com.restonic4.fancyweather.custom.weather.WeatherVisualEffectsController;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Redirect(
            method = "setupColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"
            )
    )
    private static float injectCustomCloudinessInFog(ClientLevel instance, float partialTick) {
        float vanillaRain = instance.getRainLevel(partialTick);
        float customClouds = WeatherVisualEffectsController.getCloudiness();

        return Math.max(vanillaRain, customClouds);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void modifyFogDensity(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
        // Only apply to Terrain fog (not lava, water, or blindness effect)
        if (fogMode != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }

        float desiredFogEndProgress = WeatherVisualEffectsController.getFogEnd();

        int wmo = Synchronizer.getCurrentWMOCode();
        WeatherState weatherState = WeatherState.fromCode(wmo);

        if (weatherState == WeatherState.FOG) {
            float targetEnd = 30.0f;
            float end = viewDistance + desiredFogEndProgress * (targetEnd - viewDistance);

            // Apply the override
            RenderSystem.setShaderFogStart(0);
            RenderSystem.setShaderFogEnd(end);
        }
    }
}