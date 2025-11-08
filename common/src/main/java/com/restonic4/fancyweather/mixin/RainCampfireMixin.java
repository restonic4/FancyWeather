package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.utils.RandomHelper;
import com.restonic4.fancyweather.utils.WeatherHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    Port of my mod: https://modrinth.com/mod/rain-should-extinguish-campfires
 */
@Mixin(CampfireBlockEntity.class)
public class RainCampfireMixin {
    @Unique private static int fancyWeather$tickCountdown = 0;

    @Inject(method = "cookTick", at = @At("TAIL"))
    private static void cookTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity, CallbackInfo ci) {
        ServerLevel serverLevel = (ServerLevel) level;

        if (WeatherHelper.isRainingOrThundering(serverLevel) && WeatherHelper.canRainingAtPosition(serverLevel, blockPos)) {
            if (fancyWeather$tickCountdown <= 0) {
                fancyWeather$tickCountdown = RandomHelper.getRandomInt(40, 120);

                if (WeatherHelper.isCloseEnoughToRain(serverLevel, blockPos)) {
                    CampfireBlock.dowse(null, level, blockPos, blockState);
                    BlockState newState = blockState.setValue(CampfireBlock.LIT, false);
                    level.setBlock(blockPos, newState, 11);

                    level.playSound(null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.0f);
                }
            }

            fancyWeather$tickCountdown--;
        }
    }
}
