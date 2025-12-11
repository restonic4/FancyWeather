package com.restonic4.fancyweather.custom.sync.skippers;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class FurnaceCookingSkipper {
    public static void apply(ServerLevel level, LevelChunk chunk, long ticksSkipped) {
        if (!FancyWeatherMidnightConfig.enableFurnaceCookSync) return;

        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof AbstractFurnaceBlockEntity furnace) {
                for (int i = 0; i < ticksSkipped; i++) {
                    AbstractFurnaceBlockEntity.serverTick(level, furnace.getBlockPos(), furnace.getBlockState(), furnace);
                }
            }
        }
    }
}
