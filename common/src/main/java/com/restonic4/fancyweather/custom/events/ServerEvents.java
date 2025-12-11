package com.restonic4.fancyweather.custom.events;

import com.restonic4.fancyweather.custom.events.factory.Event;
import com.restonic4.fancyweather.custom.events.factory.EventFactory;
import net.minecraft.server.MinecraftServer;

public class ServerEvents {
    public static final Event<WorldLoaded> WORLD_LOADED = EventFactory.createArray(WorldLoaded.class, callbacks -> (server) -> {
        for (WorldLoaded callback : callbacks) {
            callback.onEvent(server);
        }
    });

    @FunctionalInterface
    public interface WorldLoaded {
        void onEvent(MinecraftServer server);
    }

    public static final Event<WorldUnloading> WORLD_UNLOADING = EventFactory.createArray(WorldUnloading.class, callbacks -> (server) -> {
        for (WorldUnloading callback : callbacks) {
            callback.onEvent(server);
        }
    });

    @FunctionalInterface
    public interface WorldUnloading {
        void onEvent(MinecraftServer server);
    }

    public static final Event<TickStarted> TICK_STARTED = EventFactory.createArray(TickStarted.class, callbacks -> (server) -> {
        for (TickStarted callback : callbacks) {
            callback.onEvent(server);
        }
    });

    @FunctionalInterface
    public interface TickStarted {
        void onEvent(MinecraftServer server);
    }

    public static final Event<TickEnded> TICK_ENDED = EventFactory.createArray(TickEnded.class, callbacks -> (server) -> {
        for (TickEnded callback : callbacks) {
            callback.onEvent(server);
        }
    });

    @FunctionalInterface
    public interface TickEnded {
        void onEvent(MinecraftServer server);
    }
}
