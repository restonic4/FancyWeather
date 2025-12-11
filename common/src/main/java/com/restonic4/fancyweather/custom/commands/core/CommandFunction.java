package com.restonic4.fancyweather.custom.commands.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface CommandFunction {
    String getID();
    LiteralArgumentBuilder<CommandSourceStack> build();
}
