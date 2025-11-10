package com.restonic4.fancyweather.gametests;

import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;

public class FancyGameTests {
    @GameTest(template = "minecraft:campfire_spread", timeoutTicks = 90*20, batch = "weather_clear")
    public void testCampfireSpread(GameTestHelper gameTestHelper) {
        gameTestHelper.pressButton(4, 2, 1);

        gameTestHelper.runAtTickTime(20, () -> {
            BlockPos litPosCheck = new BlockPos(3, 2, 2);
            gameTestHelper.assertBlockState(
                    litPosCheck,
                    state -> state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT),
                    () -> "Expected a lit campfire at " + litPosCheck + " but found: " + gameTestHelper.getBlockState(litPosCheck)
            );
        });

        gameTestHelper.runAtTickTime(90*20, () -> {
            BlockPos litPosCheck1 = new BlockPos(1, 2, 1);
            BlockPos litPosCheck2 = new BlockPos(1, 2, 3);
            BlockPos litPosCheck3 = new BlockPos(3, 2, 3);
            BlockPos litPosCheck4 = new BlockPos(3, 2, 1);

            gameTestHelper.assertBlockState(
                    litPosCheck1,
                    state -> state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT),
                    () -> "Expected a lit campfire at " + litPosCheck1 + " but found: " + gameTestHelper.getBlockState(litPosCheck1)
            );

            gameTestHelper.assertBlockState(
                    litPosCheck2,
                    state -> state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT),
                    () -> "Expected a lit campfire at " + litPosCheck2 + " but found: " + gameTestHelper.getBlockState(litPosCheck2)
            );

            gameTestHelper.assertBlockState(
                    litPosCheck3,
                    state -> state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT),
                    () -> "Expected a lit campfire at " + litPosCheck3 + " but found: " + gameTestHelper.getBlockState(litPosCheck3)
            );

            gameTestHelper.assertBlockState(
                    litPosCheck4,
                    state -> state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT),
                    () -> "Expected a lit campfire at " + litPosCheck4 + " but found: " + gameTestHelper.getBlockState(litPosCheck4)
            );

            gameTestHelper.succeed();
        });
    }

    @GameTest(template = "minecraft:rain_campfire", timeoutTicks = 60*20, skyAccess = true, batch = "weather_rain")
    public void testRainCampfire(GameTestHelper gameTestHelper) {
        ServerLevel severLevel = gameTestHelper.getLevel();

        gameTestHelper.runAtTickTime(FancyWeatherMidnightConfig.campfireRainLookupMaxTickRange + 80, () -> {
            BlockPos campfirePos = new BlockPos(0, 2, 0);
            gameTestHelper.assertBlockState(
                    campfirePos,
                    state -> state.getBlock() instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT),
                    () -> "Expected a unlit campfire at " + campfirePos + " but found: " + gameTestHelper.getBlockState(campfirePos)
            );

            gameTestHelper.succeed();
        });
    }

    @GameTest(template = "minecraft:rain_crop", timeoutTicks = 20*60*20 + 10*20, skyAccess = true, batch = "weather_rain")
    public void testRainCrop(GameTestHelper gameTestHelper) {
        ServerLevel severLevel = gameTestHelper.getLevel();

        int rainDuration = 20*60*20;
        severLevel.setWeatherParameters(0, rainDuration, true, false);

        gameTestHelper.runAtTickTime(rainDuration + 6*20, () -> {
            BlockPos wheatPos1 = new BlockPos(0, 3, 0);
            gameTestHelper.assertBlockState(
                    wheatPos1,
                    state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == CropBlock.MAX_AGE,
                    () -> "Expected fully-grown wheat at " + wheatPos1 + " but found: " + gameTestHelper.getBlockState(wheatPos1)
            );

            BlockPos wheatPos2 = new BlockPos(2, 3, 0);
            gameTestHelper.assertBlockState(
                    wheatPos2,
                    state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == CropBlock.MAX_AGE,
                    () -> "Expected fully-grown wheat at " + wheatPos2 + " but found: " + gameTestHelper.getBlockState(wheatPos2)
            );

            BlockPos wheatPos3 = new BlockPos(2, 3, 2);
            gameTestHelper.assertBlockState(
                    wheatPos3,
                    state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == CropBlock.MAX_AGE,
                    () -> "Expected fully-grown wheat at " + wheatPos3 + " but found: " + gameTestHelper.getBlockState(wheatPos3)
            );

            BlockPos wheatPos4 = new BlockPos(0, 3, 2);
            gameTestHelper.assertBlockState(
                    wheatPos4,
                    state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == CropBlock.MAX_AGE,
                    () -> "Expected fully-grown wheat at " + wheatPos4 + " but found: " + gameTestHelper.getBlockState(wheatPos4)
            );

            gameTestHelper.succeed();
        });
    }
}
