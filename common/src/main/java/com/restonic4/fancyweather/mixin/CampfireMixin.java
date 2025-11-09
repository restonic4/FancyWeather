package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
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
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
    Port of my mod: https://modrinth.com/mod/rain-should-extinguish-campfires
 */
@Mixin(CampfireBlockEntity.class)
public class CampfireMixin {
    @Unique private static int fancyWeather$tickRainCountdown = 0;
    @Unique private static int fancyWeather$tickSpreadCountdown = 0;

    /*
        RAIN SHOULD EXTINGUISH CAMPFIRES
     */
    @Inject(method = "cookTick", at = @At("TAIL"))
    private static void rainTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity, CallbackInfo ci) {
        if (!FancyWeatherMidnightConfig.enableRainShouldExtinguishCampfires) return;

        ServerLevel serverLevel = (ServerLevel) level;

        if (WeatherHelper.isRainingOrThundering(serverLevel) && WeatherHelper.canRainingAtPosition(serverLevel, blockPos)) {
            if (fancyWeather$tickRainCountdown <= 0) {
                fancyWeather$tickRainCountdown = RandomHelper.getRandomInt(FancyWeatherMidnightConfig.campfireRainLookupMinTickRange, FancyWeatherMidnightConfig.campfireRainLookupMaxTickRange);

                if (WeatherHelper.isCloseEnoughToRain(serverLevel, blockPos)) {
                    CampfireBlock.dowse(null, level, blockPos, blockState);
                    BlockState newState = blockState.setValue(CampfireBlock.LIT, false);
                    level.setBlock(blockPos, newState, 11);
                    //level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);

                    level.playSound(null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f);
                }
            }

            fancyWeather$tickRainCountdown--;
        }
    }

    /*
        CAMPFIRE FIRE SPREAD
     */
    @Unique private static BlockPos.MutableBlockPos fancyWeather$cacheSpreadBlockPos = new BlockPos.MutableBlockPos();
    @Inject(method = "cookTick", at = @At("TAIL"))
    private static void spreadTick(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity, CallbackInfo ci) {
        if (!FancyWeatherMidnightConfig.enableCampfireFireSpread) return;

        ServerLevel serverLevel = (ServerLevel) level;

        int neighborCampfiresMask = WeatherHelper.getNearbyCampfiresMask(serverLevel, blockPos);
        if (neighborCampfiresMask == 0) {
            return;
        }

        if (fancyWeather$tickSpreadCountdown <= 0) {
            fancyWeather$tickSpreadCountdown = RandomHelper.getRandomInt(FancyWeatherMidnightConfig.campfireSpreadLookupMinTickRange, FancyWeatherMidnightConfig.campfireSpreadLookupMaxTickRange);

            fancyWeather$cacheSpreadBlockPos.set(blockPos);

            BlockPos neighborCampfire = WeatherHelper.getRandomCampfirePos(neighborCampfiresMask, fancyWeather$cacheSpreadBlockPos);
            if (neighborCampfire == null) return;

            BlockState neighborBlockstate = serverLevel.getBlockState(neighborCampfire);

            BlockState newState = neighborBlockstate.setValue(CampfireBlock.LIT, true);
            serverLevel.setBlock(neighborCampfire, newState, 11);
            serverLevel.gameEvent(null, GameEvent.BLOCK_CHANGE, neighborCampfire);

            serverLevel.playSound(null, neighborCampfire, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f);
        }

        fancyWeather$tickSpreadCountdown--;
    }
}
