package com.restonic4.fancyweather.custom.events;

import com.restonic4.fancyweather.custom.events.factory.Event;
import com.restonic4.fancyweather.custom.events.factory.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

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

    public static final Event<PlayerJoined> PLAYER_JOINED = EventFactory.createArray(PlayerJoined.class, callbacks -> (server, serverPlayer) -> {
        for (PlayerJoined callback : callbacks) {
            callback.onEvent(server, serverPlayer);
        }
    });

    @FunctionalInterface
    public interface PlayerJoined {
        void onEvent(MinecraftServer server, ServerPlayer serverPlayer);
    }

    public static final Event<ChunkLoaded> CHUNK_LOADED = EventFactory.createArray(ChunkLoaded.class, callbacks -> (serverLevel, levelChunk) -> {
        for (ChunkLoaded callback : callbacks) {
            callback.onEvent(serverLevel, levelChunk);
        }
    });

    @FunctionalInterface
    public interface ChunkLoaded {
        void onEvent(ServerLevel serverLevel, LevelChunk levelChunk);
    }

    public static final Event<ChunkUnloaded> CHUNK_UNLOADED = EventFactory.createArray(ChunkUnloaded.class, callbacks -> (serverLevel, levelChunk) -> {
        for (ChunkUnloaded callback : callbacks) {
            callback.onEvent(serverLevel, levelChunk);
        }
    });

    @FunctionalInterface
    public interface ChunkUnloaded {
        void onEvent(ServerLevel serverLevel, LevelChunk levelChunk);
    }
}
