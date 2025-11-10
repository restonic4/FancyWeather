package com.restonic4.fancyweather.mixin;

import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.utils.WeatherHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public class CropsRainGrowMixin {

    /*

        THIS MIXIN OVERRIDES A LITHIUM'S MIXIN
        mixin.alloc.chunk_random
        URL: https://github.com/CaffeineMC/lithium/blob/1.21.1/common/src/main/java/net/caffeinemc/mods/lithium/mixin/alloc/chunk_random/ServerLevelMixin.java

     */
    @Redirect(
            method = "tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
            )
    )
    private void onBlockRandomTickRedirect(BlockState instance, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        // Call vanilla behavior first
        instance.randomTick(serverLevel, blockPos.immutable(), randomSource);

        if (!FancyWeatherMidnightConfig.enableCropRainBoost) return;

        if (!WeatherHelper.isRainingOrThundering(serverLevel)) return;
        if (!WeatherHelper.canRainingAtPosition(serverLevel, blockPos)) return;

        Block block = serverLevel.getBlockState(blockPos).getBlock();
        if (!WeatherHelper.isCropBlock(block)) return;

        // Extra calls
        for (int i = 0; i < FancyWeatherMidnightConfig.cropRainBoostMultiplier; i ++) {
            instance.randomTick(serverLevel, blockPos.immutable(), randomSource);
        }
    }
}
