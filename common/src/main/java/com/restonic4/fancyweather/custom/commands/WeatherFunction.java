package com.restonic4.fancyweather.custom.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.custom.commands.core.CommandFunction;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.custom.weather.WeatherState;
import com.restonic4.fancyweather.custom.weather.WeatherStateStrength;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class WeatherFunction implements CommandFunction {
    private void failFast(CommandContext<CommandSourceStack> ctx) {
        if (!FancyWeatherMidnightConfig.enableSync) {
            ctx.getSource().sendFailure(Component.literal("The server does not support real time synchronization!\nThis command is used to manage real time sync."));
        }
    }

    @Override
    public String getID() {
        return "weather";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                // "get" subcommand
                .then(Commands.literal("get")
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    failFast(ctx);

                                    int WMO = Synchronizer.getCurrentWMOCode();
                                    WeatherState weatherState = WeatherState.fromCode(WMO);
                                    WeatherStateStrength weatherStateStrength = WeatherStateStrength.fromCode(WMO);

                                    ctx.getSource().sendSuccess(() -> Component.literal("Status: " + weatherState + " -> " + weatherStateStrength), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("wmo")
                                .executes(ctx -> {
                                    failFast(ctx);

                                    ctx.getSource().sendSuccess(() -> Component.literal("WMO code: " + Synchronizer.getCurrentWMOCode()), false);
                                    return 1;
                                })
                        )
                )
                // "force TICKS" subcommand
                .then(Commands.literal("force")
                        .then(weatherTypeArgument()
                                .then(weatherStrengthArgument()
                                        .executes(ctx -> {
                                            failFast(ctx);

                                            String typeName = StringArgumentType.getString(ctx, "type");
                                            String strengthName = StringArgumentType.getString(ctx, "intensity");

                                            WeatherState type = WeatherState.valueOf(typeName.toUpperCase());
                                            WeatherStateStrength strength = WeatherStateStrength.valueOf(strengthName.toUpperCase());

                                            int wmo = Synchronizer.getRepresentativeCode(type, strength);

                                            Synchronizer.setForcedWMO(wmo);

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Forced: "
                                                            + type.name() + " " + strength.name()
                                                            + " → WMO " + wmo),
                                                    false
                                            );

                                            return wmo;
                                        })
                                )
                        )
                )

                // "reset" subcommand
                .then(Commands.literal("reset")
                        .executes(ctx -> {
                            failFast(ctx);

                            ctx.getSource().sendSuccess(() -> Component.literal("Forced WMO reset!"), false);
                            Synchronizer.setForcedWMO(-1);
                            return 1;
                        })
                );
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> weatherTypeArgument() {
        return Commands.argument("type", StringArgumentType.string())
                .suggests((ctx, builder) -> {
                    return SharedSuggestionProvider.suggest(
                            Arrays.stream(WeatherState.values())
                                    .map(Enum::name)
                                    .toList(),
                            builder
                    );
                });
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> weatherStrengthArgument() {
        return Commands.argument("intensity", StringArgumentType.string())
                .suggests((ctx, builder) -> {
                    return SharedSuggestionProvider.suggest(
                            Arrays.stream(WeatherStateStrength.values())
                                    .map(Enum::name)
                                    .toList(),
                            builder
                    );
                });
    }

}