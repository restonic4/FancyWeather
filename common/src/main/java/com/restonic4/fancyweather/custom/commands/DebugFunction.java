package com.restonic4.fancyweather.custom.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.custom.commands.core.CommandFunction;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.custom.sync.TimeSkipper;
import com.restonic4.fancyweather.custom.weather.WeatherState;
import com.restonic4.fancyweather.custom.weather.WeatherStateStrength;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public class DebugFunction implements CommandFunction {
    @Override
    public String getID() {
        return "debug";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                .then(Commands.literal("time_skipper")
                        .then(Commands.literal("reveal_processed_entities")
                                .executes(ctx -> {
                                    TimeSkipper.applyGlowToAllProcessedEntities(ctx.getSource().getServer(), true);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Done!"), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("reset_processed_entities_visuals")
                                .executes(ctx -> {
                                    TimeSkipper.applyGlowToAllProcessedEntities(ctx.getSource().getServer(), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Done!"), false);
                                    return 1;
                                })
                        )
                );
    }
}