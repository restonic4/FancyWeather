package com.restonic4.fancyweather.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

    // Use MutableBlockPos to avoid creating a blockPos each call
    private static final BlockPos.MutableBlockPos skyCacheBlockPos = new BlockPos.MutableBlockPos();
    public static boolean hasSkyVisibility(ServerLevel level, int x, int y, int z) {
        skyCacheBlockPos.set(x, y, z);

        int skyLight = level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(skyCacheBlockPos);
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

    // bit flags, instead of creating arrays each call and a bunch of blockpos
    public static final int CAMPFIRE_POS_PLUS_X  = 1 << 0;
    public static final int CAMPFIRE_POS_MINUS_X = 1 << 1;
    public static final int CAMPFIRE_POS_PLUS_Z  = 1 << 2;
    public static final int CAMPFIRE_POS_MINUS_Z = 1 << 3;

    private static final BlockPos.MutableBlockPos spreadCacheBlockPos = new BlockPos.MutableBlockPos();
    public static int getNearbyCampfiresMask(ServerLevel level, BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        BlockState state;

        int mask = 0;

        spreadCacheBlockPos.set(x + 1, y, z);
        state = level.getBlockState(spreadCacheBlockPos);
        if (state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT)) mask |= CAMPFIRE_POS_PLUS_X;

        spreadCacheBlockPos.set(x - 1, y, z);
        state = level.getBlockState(spreadCacheBlockPos);
        if (state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT)) mask |= CAMPFIRE_POS_MINUS_X;

        spreadCacheBlockPos.set(x, y, z + 1);
        state = level.getBlockState(spreadCacheBlockPos);
        if (state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT)) mask |= CAMPFIRE_POS_PLUS_Z;

        spreadCacheBlockPos.set(x, y, z - 1);
        state = level.getBlockState(spreadCacheBlockPos);
        if (state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT)) mask |= CAMPFIRE_POS_MINUS_Z;

        return mask;
    }

    public static BlockPos getRandomCampfirePos(int mask, BlockPos.MutableBlockPos target) {
        if (mask == 0) return null;

        // Count how many possible directions are set
        int count = Integer.bitCount(mask);
        if (count == 0) return null;

        // Pick a random index among the set bits
        int targetIndex = ThreadLocalRandom.current().nextInt(count);

        // Iterate through the bits until we reach the chosen one
        int index = 0;

        if ((mask & CAMPFIRE_POS_PLUS_X) != 0) {
            if (index++ == targetIndex) {
                target.set(target.getX() + 1, target.getY(), target.getZ());
                return target;
            }
        }
        if ((mask & CAMPFIRE_POS_MINUS_X) != 0) {
            if (index++ == targetIndex) {
                target.set(target.getX() - 1, target.getY(), target.getZ());
                return target;
            }
        }
        if ((mask & CAMPFIRE_POS_PLUS_Z) != 0) {
            if (index++ == targetIndex) {
                target.set(target.getX(), target.getY(), target.getZ() + 1);
                return target;
            }
        }
        if ((mask & CAMPFIRE_POS_MINUS_Z) != 0) {
            if (index++ == targetIndex) {
                target.set(target.getX(), target.getY(), target.getZ() - 1);
                return target;
            }
        }

        return null;
    }

    private static final Set<Class<?>> CROP_BLOCKS = Set.of(
            CropBlock.class,
            SaplingBlock.class,
            BambooStalkBlock.class,
            BambooSaplingBlock.class,
            SweetBerryBushBlock.class,
            StemBlock.class,
            CocoaBlock.class,
            SugarCaneBlock.class,
            MossBlock.class
    );
    public static boolean isCropBlock(Block block) {
        return CROP_BLOCKS.stream().anyMatch(clazz -> clazz.isInstance(block));
    }
}
