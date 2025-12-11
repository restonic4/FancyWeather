package com.restonic4.fancyweather.custom.sync.skippers;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class CropAgeSkipper {
    public static void apply(ServerLevel level, LevelChunk chunk, long ticksSkipped) {
        if (!FancyWeatherMidnightConfig.enableCropSync) return;

        int randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (randomTickSpeed <= 0) return;

        double chancePerTick = (double) randomTickSpeed / 4096.0;
        int estimatedUpdates = (int) (ticksSkipped * chancePerTick);
        int attemptsToSimulate = Math.min(estimatedUpdates, 64);

        LevelChunkSection[] sections = chunk.getSections();

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;

            if (!section.getStates().maybeHas(state -> state.getBlock() instanceof CropBlock)) {
                continue;
            }

            int sectionBottomY = chunk.getMinBuildHeight() + (i * 16);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);

                        // We already know this section contains crops, but we still check the specific block
                        if (state.getBlock() instanceof CropBlock && state.isRandomlyTicking()) {

                            BlockPos pos = new BlockPos(
                                    chunk.getPos().getMinBlockX() + x,
                                    sectionBottomY + y,
                                    chunk.getPos().getMinBlockZ() + z
                            );

                            for (int k = 0; k < attemptsToSimulate; k++) {
                                BlockState currentState = level.getBlockState(pos);
                                if (!(currentState.getBlock() instanceof CropBlock) || !currentState.isRandomlyTicking()) {
                                    break;
                                }
                                currentState.randomTick(level, pos, level.random);
                            }
                        }
                    }
                }
            }
        }
    }
}
