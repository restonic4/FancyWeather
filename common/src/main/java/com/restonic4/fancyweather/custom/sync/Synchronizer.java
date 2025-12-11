package com.restonic4.fancyweather.custom.sync;

import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.MainFancyWeather;
import com.restonic4.fancyweather.custom.events.ServerEvents;
import com.restonic4.fancyweather.utils.FileManager;
import com.restonic4.fancyweather.utils.MathHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static com.restonic4.fancyweather.Constants.LOG;

/*
TODO:

 */
public class Synchronizer {
    public static final String WORLD_GLOBAL_DATA_FILE_NAME = MainFancyWeather.sign("world.json");
    private static final int AUTOSAVE_INTERVAL_TICKS = MathHelper.getTicksForMinutes(2);

    // Cache
    private static WeatherSave loadedData = null;
    private static int autosaveTickCounter = 0;

    public static void init() {
        ServerEvents.TICK_STARTED.register((server) -> {
            tick(server.overworld());
        });

        // Saving data
        ServerEvents.WORLD_UNLOADING.register((server) -> {
            save(server.overworld());

            // Reset cache
            loadedData = null;
            autosaveTickCounter = 0;
        });
    }

    /**
     * Sever and client side tick
     */
    public static void tick(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            ensureData(serverLevel);

            autosaveTickCounter++;
            if (autosaveTickCounter >= AUTOSAVE_INTERVAL_TICKS) {
                save(serverLevel);
                autosaveTickCounter = 0;
            }
        }
    }

    /**
     * Loads or creates the save file
     */
    public static void ensureData(ServerLevel level) {
        if (loadedData != null) {
            return;
        }

        Optional<WeatherSave> loaded = FileManager.load(level, WORLD_GLOBAL_DATA_FILE_NAME, WeatherSave.class);
        loaded.ifPresentOrElse(
                (data) -> {
                    loadedData = data;
                    LOG.info("World data loaded!");
                },
                () -> {
                    WeatherSave save = new WeatherSave();
                    save.creation = System.currentTimeMillis();
                    save.last_seen = save.creation;

                    loadedData = save;

                    FileManager.save(level, WORLD_GLOBAL_DATA_FILE_NAME, save);
                    LOG.info("World data created!");
                }
        );
    }

    /**
     * Manually save current loadedData to file.
     */
    public static void save(ServerLevel level) {
        if (loadedData == null) return;

        loadedData.last_seen = System.currentTimeMillis();

        if (FileManager.save(level, WORLD_GLOBAL_DATA_FILE_NAME, loadedData)) {
            LOG.info("World data saved!");
        } else {
            LOG.warn("Failed to save world data!");
        }
    }
}
