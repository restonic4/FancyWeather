package com.restonic4.fancyweather.custom.sync;

import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.MainFancyWeather;
import com.restonic4.fancyweather.custom.events.ServerEvents;
import com.restonic4.fancyweather.custom.sync.skippers.CropAgeSkipper;
import com.restonic4.fancyweather.custom.sync.skippers.FurnaceCookingSkipper;
import com.restonic4.fancyweather.custom.sync.skippers.MobEffectsDurationSkipper;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

import static com.restonic4.fancyweather.Constants.MOD_ID;

public class TimeSkipper {
    public static final String WAS_LOADED_TAG = MOD_ID + ":seen_before";
    private static final int ASYNC_CHUNKS_PER_TICK = 2;
    private static final boolean DEBUG = true;

    // Cache
    private static long startUpSavedDiff;
    private static final Set<UUID> processed_entities = new HashSet<>();
    private static final Map<ResourceKey<Level>, LongSet> processed_chunks = new HashMap<>();
    private static final Queue<ChunkTask> chunkTaskQueue = new LinkedList<>();

    // Chunk async task
    private record ChunkTask(ServerLevel level, LevelChunk chunk, long ticksSkipped) {}
    public static void queueTimeSkip(ServerLevel level, LevelChunk chunk, long ticksSkipped) {
        if (ticksSkipped > 0) {
            chunkTaskQueue.add(new ChunkTask(level, chunk, ticksSkipped));
        }
    }

    public static void onWorldLoaded(MinecraftServer server) {
        long lastSavedTime = Synchronizer.getLoadedData().last_seen;
        long currentTime = System.currentTimeMillis();

        processed_entities.clear();
        processed_chunks.clear();

        if (lastSavedTime != -1) {
            long timeDiff = currentTime - lastSavedTime;
            if (timeDiff > 0) {
                Constants.LOG.info("Time skipped: {}ms", timeDiff);

                if (DEBUG) {
                    timeDiff = timeDiff * 100;
                    for (int i = 0; i < 25; i++) {
                        if (i % 2 == 0) {
                            Constants.LOG.error("DEBUG MODE ON, TIME SKIP");
                        } else {
                            Constants.LOG.warn("DEBUG MODE ON, TIME SKIP");
                        }
                    }
                }

                startUpSavedDiff = timeDiff;
                long ticksSkipped = timeDiff / 50L;
                processStartUp(server, ticksSkipped);
            }
        }
    }

    public static void onWorldUnLoaded(MinecraftServer server) {
        processed_entities.clear();
        processed_chunks.clear();
    }

    public static void init() {
        // Add chunks to async tasks
        ServerEvents.CHUNK_LOADED.register((ServerLevel serverLevel, LevelChunk levelChunk) -> {
            ResourceKey<Level> dimKey = serverLevel.dimension();
            long chunkPosLong = levelChunk.getPos().toLong();

            LongSet chunksInDimension = processed_chunks.computeIfAbsent(dimKey, k -> new LongOpenHashSet());

            if (chunksInDimension.add(chunkPosLong)) {
                queueTimeSkip(serverLevel, levelChunk, startUpSavedDiff);
            }
        });

        // Process only old entities, skip new spawns
        ServerEvents.ENTITY_LOADED.register((ServerLevel level, Entity entity) -> {
            UUID id = entity.getUUID();

            // Check if we have already processed this entity in this specific runtime session
            if (!processed_entities.contains(id)) {

                // Check if the entity has our persistent "signature" tag
                // If the tag is present, it means the entity was saved to disk and is now reloading (OLD)
                // If the tag is missing, it is a brand new spawn or a pre-mod entity (NEW)
                boolean isPreviouslyLoaded = entity.getTags().contains(WAS_LOADED_TAG);

                if (isPreviouslyLoaded) {
                    // It's an existing baby loaded from disk -> Apply the time skip
                    long ticksSkipped = startUpSavedDiff / 50L;
                    processEntity(level, entity, ticksSkipped);
                } else {
                    // It's a brand new spawn (or first time seeing it) -> Mark it for next time, but SKIP the effect now
                    entity.addTag(WAS_LOADED_TAG);
                }

                // Add to runtime set to prevent double processing in this session
                processed_entities.add(id);
            }
        });

        // Untrack dead entity
        ServerEvents.ENTITY_DIED.register((livingEntity, damageSource) -> {
            if (!livingEntity.level().isClientSide()) {
                processed_entities.remove(livingEntity.getUUID());
            }
        });
    }

    public static void tick() {
        if (chunkTaskQueue.isEmpty()) return;

        for (int i = 0; i < ASYNC_CHUNKS_PER_TICK; i++) {
            ChunkTask task = chunkTaskQueue.poll();
            if (task == null) break;

            // Verify chunk is still loaded before processing
            if (task.level.getChunkSource().hasChunk(task.chunk.getPos().x, task.chunk.getPos().z)) {
                processChunk(task.level, task.chunk, task.ticksSkipped);
            }
        }
    }

    private static void processStartUp(MinecraftServer server, long ticksSkipped) {

    }

    private static void processChunk(ServerLevel level, LevelChunk chunk, long ticksSkipped) {
        CropAgeSkipper.apply(level, chunk, ticksSkipped);
        FurnaceCookingSkipper.apply(level, chunk, ticksSkipped);
    }

    private static void processEntity(ServerLevel level, Entity entity, long ticksSkipped) {
        MobEffectsDurationSkipper.apply(entity, ticksSkipped);
    }
}
