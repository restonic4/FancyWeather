package com.restonic4.fancyweather.custom.commands.core;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.custom.commands.PrintFunction;
import com.restonic4.fancyweather.custom.commands.TimeFunction;
import com.restonic4.fancyweather.custom.commands.WeatherFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.LinkedHashMap;
import java.util.Map;

public class FancyWeatherCommands {
    private static final Map<String, CommandFunction> FUNCTIONS = new LinkedHashMap<>();

    static {
        registerFunction(new PrintFunction());
        registerFunction(new TimeFunction());
        registerFunction(new WeatherFunction());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(Constants.MOD_ID);

        for (Map.Entry<String, CommandFunction> e : FUNCTIONS.entrySet()) {
            root.then(e.getValue().build());
        }

        dispatcher.register(root);
    }

    public static void registerFunction(CommandFunction function) {
        FUNCTIONS.put(function.getID(), function);
    }
}
