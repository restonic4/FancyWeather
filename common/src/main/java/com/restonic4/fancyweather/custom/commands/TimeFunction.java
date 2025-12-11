package com.restonic4.fancyweather.custom.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.custom.commands.core.CommandFunction;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TimeFunction implements CommandFunction {
    private void failFast(CommandContext<CommandSourceStack> ctx) {
        if (!FancyWeatherMidnightConfig.enableSync) {
            ctx.getSource().sendFailure(Component.literal("The server does not support real time synchronization!\nThis command is used to manage real time sync."));
        }
    }

    @Override
    public String getID() {
        return "time";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                // "get" subcommand
                .then(Commands.literal("get")
                        .then(Commands.literal("minecraft")
                                .executes(ctx -> {
                                    failFast(ctx);

                                    ctx.getSource().sendSuccess(() -> Component.literal("Minecraft ticks: " + Synchronizer.getSyncedTime(ctx.getSource().getLevel())), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("system")
                                .executes(ctx -> {
                                    failFast(ctx);

                                    ctx.getSource().sendSuccess(() -> Component.literal("System time: " + Synchronizer.getSystemTimeString()), false);
                                    return 1;
                                })
                        )
                )
                // "force TICKS" subcommand
                .then(Commands.literal("force")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    failFast(ctx);

                                    int ticks = getInteger(ctx, "ticks"); // get the number the player typed
                                    ctx.getSource().sendSuccess(() -> Component.literal("Forced " + ticks + " ticks!"), false);

                                    Synchronizer.setForcedTicks(ticks);

                                    return ticks;
                                })
                        )
                )
                // "reset" subcommand
                .then(Commands.literal("reset")
                        .executes(ctx -> {
                            failFast(ctx);

                            ctx.getSource().sendSuccess(() -> Component.literal("Forced time reset!"), false);
                            Synchronizer.setForcedTicks(-1);
                            return 1;
                        })
                );
    }
}