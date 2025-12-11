package com.restonic4.fancyweather.custom.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.restonic4.fancyweather.custom.commands.core.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PrintFunction implements CommandFunction {
    @Override
    public String getID() {
        return "print";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getID())
                .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String msg = StringArgumentType.getString(ctx, "message");
                                    ctx.getSource().sendSuccess(() -> Component.literal("Echo: " + msg), false);
                                    return 1;
                                })
                );
    }
}