package com.restonic4.fancyweather.custom.sync;

import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.MainFancyWeather;
import com.restonic4.fancyweather.custom.events.ServerEvents;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

import static com.restonic4.fancyweather.Constants.MOD_ID;

public class TimeSkipper {
    private static final String FILE_NAME = MainFancyWeather.sign("time_tracker.dat");
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

    public static void init() {
        ServerEvents.CHUNK_LOADED.register((serverLevel, levelChunk) -> {
            System.out.println("Chunk loaded");
        });

        ServerEvents.CHUNK_UNLOADED.register((serverLevel, levelChunk) -> {
            System.out.println("Chunk unloaded");
        });
        /*ServerEvents.TICK_ENDED.register(server -> {
           tick();
        });

        ServerEvents.WORLD_LOADED.register(server -> {
            long lastSavedTime = Synchronizer.getLoadedData().last_seen;
            long currentTime = System.currentTimeMillis();

            processed_entities.clear();
            processed_chunks.clear();

            if (lastSavedTime != -1) {
                long timeDiff = currentTime - lastSavedTime;

                // Only do logic if time actually passed and it's a positive value
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
                    applyStartUpTimeSkipEffects(server, timeDiff);
                }
            }
        });

        ServerEvents.WORLD_UNLOADING.register(server -> {
            processed_entities.clear();
            processed_chunks.clear();
        });

        ServerChunkEvents.CHUNK_LOAD.register((ServerLevel level, LevelChunk chunk) -> {
            ResourceKey<Level> dimKey = level.dimension();
            long chunkPosLong = chunk.getPos().toLong();

            LongSet chunksInDimension = PROCESSED_CHUNKS.computeIfAbsent(dimKey, k -> new LongOpenHashSet());

            if (chunksInDimension.add(chunkPosLong)) {
                applyChunkLoadedTimeSkipEffects(level, chunk, startUpSavedDiff);
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerLevel level) -> {
            if (level.isClientSide()) return;

            UUID id = entity.getUUID();

            // 1. Check if we have already processed this entity in this specific runtime session
            if (!PROCESSED_ENTITIES.contains(id)) {

                // 2. Check if the entity has our persistent "signature" tag
                // If the tag is present, it means the entity was saved to disk and is now reloading (OLD)
                // If the tag is missing, it is a brand new spawn or a pre-mod entity (NEW)
                boolean isPreviouslyLoaded = entity.getTags().contains(WAS_LOADED_TAG);

                if (isPreviouslyLoaded) {
                    // It's an existing baby loaded from disk -> Apply the time skip
                    applyEntityLoadedTimeSkipEffects(entity, level, startUpSavedDiff);
                } else {
                    // It's a brand new spawn (or first time seeing it) -> Mark it for next time, but SKIP the effect now
                    entity.addTag(WAS_LOADED_TAG);
                }

                // Add to runtime set to prevent double processing in this session
                PROCESSED_ENTITIES.add(id);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            if (!livingEntity.level().isClientSide()) {
                PROCESSED_ENTITIES.remove(livingEntity.getUUID());
            }
        });*/
    }

    public static void tick() {
        if (chunkTaskQueue.isEmpty()) return;

        for (int i = 0; i < ASYNC_CHUNKS_PER_TICK; i++) {
            ChunkTask task = chunkTaskQueue.poll();
            if (task == null) break;

            // Verify chunk is still loaded before processing
            if (task.level.getChunkSource().hasChunk(task.chunk.getPos().x, task.chunk.getPos().z)) {
                processChunk(task);
            }
        }
    }

    /**
     * Process a chunk when loaded, skipping chunks that we already have checked
     */
    private static void processChunk(ChunkTask task) {
        LevelChunk chunk = task.chunk;
        ServerLevel level = task.level;
        long ticksSkipped = task.ticksSkipped;


    }
}
