package com.restonic4.fancyweather.custom.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.restonic4.fancyweather.custom.commands.core.CommandFunction;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.custom.weather.WeatherState;
import com.restonic4.fancyweather.custom.weather.WeatherStateStrength;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class WeatherFunction implements CommandFunction {
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
                                    int WMO = Synchronizer.getCurrentWMOCode();
                                    WeatherState weatherState = WeatherState.fromCode(WMO);
                                    WeatherStateStrength weatherStateStrength = WeatherStateStrength.fromCode(WMO);

                                    ctx.getSource().sendSuccess(() -> Component.literal("Status: " + weatherState + " -> " + weatherStateStrength), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("wmo")
                                .executes(ctx -> {
                                    ctx.getSource().sendSuccess(() -> Component.literal("WMO code: " + Synchronizer.getCurrentWMOCode()), false);
                                    return 1;
                                })
                        )
                )
                // "force TICKS" subcommand
                .then(Commands.literal("force")
                        .then(Commands.argument("wmo", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int WMO = getInteger(ctx, "wmo"); // get the number the player typed
                                    ctx.getSource().sendSuccess(() -> Component.literal("Forced " + WMO + " WMO code!"), false);

                                    Synchronizer.setForcedWMO(WMO);

                                    return WMO;
                                })
                        )
                )
                // "reset" subcommand
                .then(Commands.literal("reset")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("Forced WMO reset!"), false);
                            Synchronizer.setForcedWMO(-1);
                            return 1;
                        })
                );
    }
}