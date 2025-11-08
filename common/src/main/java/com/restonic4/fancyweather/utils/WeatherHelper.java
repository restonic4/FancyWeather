package com.restonic4.fancyweather.utils;

import com.restonic4.fancyweather.mixin.Vec3iAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;

public class WeatherHelper {
    public static boolean isRainingOrThundering(ServerLevel level) {
        return level.isRaining() || level.isThundering();
    }

    public static boolean canRainingAtPosition(ServerLevel level, BlockPos pos) {
        Biome biome = level.getBiome(pos).value();
        return biome.hasPrecipitation();
    }

    public static boolean hasSkyVisibility(ServerLevel level, BlockPos blockPos) {
        return hasSkyVisibility(level, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    // Use accessor to avoid creating a blockPos each call
    private static final BlockPos cacheBlockPos = new BlockPos(0, 0, 0);
    public static boolean hasSkyVisibility(ServerLevel level, int x, int y, int z) {
        Vec3iAccessor accessor = ((Vec3iAccessor) cacheBlockPos);
        accessor.invokeSetX(x);
        accessor.invokeSetY(y);
        accessor.invokeSetZ(z);

        int skyLight = level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(cacheBlockPos);
        return skyLight >= level.getMaxLightLevel();
    }

    public static boolean isCloseEnoughToRain(ServerLevel level, BlockPos blockPos) {
        final int x = blockPos.getX();
        final int y = blockPos.getY();
        final int z = blockPos.getZ();

        // early return if the center isn't open to sky
        if (!hasSkyVisibility(level, blockPos)) return false;

        int count = 0;
        if (hasSkyVisibility(level, x + 1, y, z) && ++count >= 2) return true;
        if (hasSkyVisibility(level, x - 1, y, z) && ++count >= 2) return true;
        if (hasSkyVisibility(level, x, y, z + 1) && ++count >= 2) return true;
        if (hasSkyVisibility(level, x, y, z - 1) && ++count >= 2) return true;

        return false;
    }
}
