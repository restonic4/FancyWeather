package com.restonic4.fancyweather.custom.sync;

import com.google.gson.Gson;
import com.restonic4.fancyweather.Constants;
import com.restonic4.fancyweather.MainFancyWeather;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.custom.events.ClientEvents;
import com.restonic4.fancyweather.custom.events.ServerEvents;
import com.restonic4.fancyweather.custom.weather.*;
import com.restonic4.fancyweather.utils.FileManager;
import com.restonic4.fancyweather.utils.GeolocationUtil;
import com.restonic4.fancyweather.utils.MathHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.restonic4.fancyweather.Constants.LOG;

/*
TODO:
    - Synchronization / Propper networking setup / Client & Server separation
 */
public class Synchronizer {
    public static final String WORLD_GLOBAL_DATA_FILE_NAME = MainFancyWeather.sign("world.json");
    private static final int AUTOSAVE_INTERVAL_TICKS = MathHelper.getTicksForMinutes(2);
    private static final int WEATHER_UPDATE_INTERVAL_TICKS = MathHelper.getTicksForMinutes(2);
    public static final ResourceLocation SYNC_PACKET_ID = new ResourceLocation(Constants.MOD_ID, "sync_weather");

    // Cache
    private static WeatherSave loadedData = null;
    private static int autosaveTickCounter = 0;
    private static int weatherUpdateTickCounter = 0;
    private static long forcedTicks = -1L;
    private static int forcedWMO = -1;

    public static void init() {
        ServerEvents.TICK_STARTED.register((server) -> {
            tick(server.overworld());
        });

        ClientEvents.TICK_STARTED.register((minecraft) -> {
            tick(minecraft.level);
            WeatherVisualEffectsController.tick();
        });

        ServerEvents.WORLD_LOADED.register((server) -> {
            WeatherAPI.updateWeatherData(server.overworld());
        });

        // Saving data
        ServerEvents.WORLD_UNLOADING.register((server) -> {
            save(server.overworld());

            // Reset cache
            loadedData = null;
            autosaveTickCounter = 0;
            weatherUpdateTickCounter = 0;
            forcedTicks = -1L;
            forcedWMO = -1;
        });

        ServerEvents.PLAYER_JOINED.register((server, serverPlayer) -> {
            sendSyncPacket(serverPlayer);
        });
    }

    /**
     * Sever and client side tick
     */
    public static void tick(Level level) {
        if (level == null) return;
        if (!FancyWeatherMidnightConfig.enableSync) return;

        int currentWMO = getCurrentWMOCode();
        WeatherState weatherState = WeatherState.fromCode(currentWMO);
        WeatherStateStrength weatherStateStrength = WeatherStateStrength.fromCode(currentWMO);

        if (level instanceof ServerLevel serverLevel) {
            ensureData(serverLevel);

            autosaveTickCounter++;
            if (autosaveTickCounter >= AUTOSAVE_INTERVAL_TICKS) {
                save(serverLevel);
                autosaveTickCounter = 0;
            }

            weatherUpdateTickCounter++;
            if (weatherUpdateTickCounter >= WEATHER_UPDATE_INTERVAL_TICKS) {
                WeatherAPI.updateWeatherData(serverLevel);
                weatherUpdateTickCounter = 0;
            }

            // Apply weather
            if (weatherState == WeatherState.THUNDERSTORM) {
                serverLevel.setWeatherParameters(0, 20, true, true);
            } else if (weatherState == WeatherState.RAINING || weatherState == WeatherState.SNOWING) {
                serverLevel.setWeatherParameters(0, 20, true, false);
            } else if (weatherState == WeatherState.CLEAR || weatherState == WeatherState.CLOUDY || weatherState == WeatherState.FOG ) {
                serverLevel.setWeatherParameters(20, 0, false, false);
            }
        } else if (level instanceof ClientLevel clientLevel) { // Client side effects, cloudy and fog
            if (weatherState == WeatherState.CLOUDY) {
                if (weatherStateStrength == WeatherStateStrength.SLIGHT) {
                    WeatherVisualEffectsController.setCloudiness(0.35f);
                } else if (weatherStateStrength == WeatherStateStrength.MODERATE) {
                    WeatherVisualEffectsController.setCloudiness(0.5f);
                } else if (weatherStateStrength == WeatherStateStrength.INTENSE) {
                    WeatherVisualEffectsController.setCloudiness(1f);
                }
            } else {
                WeatherVisualEffectsController.setCloudiness(0);
            }

            if (weatherState == WeatherState.FOG) {
                if (weatherStateStrength == WeatherStateStrength.SLIGHT) {
                    WeatherVisualEffectsController.setCurrentFogEnd(0.35f);
                } else if (weatherStateStrength == WeatherStateStrength.MODERATE) {
                    WeatherVisualEffectsController.setCurrentFogEnd(0.5f);
                } else if (weatherStateStrength == WeatherStateStrength.INTENSE) {
                    WeatherVisualEffectsController.setCurrentFogEnd(1f);
                }
            } else {
                WeatherVisualEffectsController.setCurrentFogEnd(0);
            }
        }
    }

    /**
     * Returns if the sync module is enabled on settings
     */
    public static boolean isEnabled() {
        return FancyWeatherMidnightConfig.enableSync;
    }

    /**
     * Gets the current tick time synced with real life, adding days as well
     * @return time in ticks
     */
    public static long getSyncedTime(Level level) {
        if (forcedTicks >= 0) return forcedTicks;

        // World creation timestamp
        long creationMillis = getWorldCreationTimestamp(level);
        if (creationMillis == -1L) return level.getDayTime(); // Not synced yet, return current level time

        // Current timestamp
        long nowMillis = System.currentTimeMillis();
        ZoneId zone = ZoneId.systemDefault();

        // Get the timestamps as ZonedDateTimes
        ZonedDateTime creationTime = Instant.ofEpochMilli(creationMillis).atZone(zone);
        ZonedDateTime nowTime = Instant.ofEpochMilli(nowMillis).atZone(zone);

        // Determine the "Current Solar Day"
        // If it is before 6:00 AM, we are still technically in the "previous" Minecraft day.
        ZonedDateTime currentSolarDay = nowTime;
        if (nowTime.getHour() < 6) {
            currentSolarDay = nowTime.minusDays(1);
        }

        // Calculate 6:00 AM for the Current Solar Day
        ZonedDateTime today6AM = currentSolarDay.toLocalDate().atTime(6, 0).atZone(zone);
        long today6AmMillis = today6AM.toInstant().toEpochMilli();

        // Calculate Time of Day (Ticks since 6 AM today)
        long elapsedSince6AM = nowMillis - today6AmMillis;
        if (elapsedSince6AM < 0) elapsedSince6AM = 0;
        long timeOfDayTicks = elapsedSince6AM / 3600L; // 3600 ms = 1 tick

        // Calculate Total Days Passed (from creation to the current solar day)
        // Using ChronoUnit.DAYS to count calendar days safely
        long daysPassed = java.time.temporal.ChronoUnit.DAYS.between(creationTime.toLocalDate(), currentSolarDay.toLocalDate());
        if (daysPassed < 0) daysPassed = 0;

        // Combine: Total Days * 24000 + Current Time Ticks
        return (daysPassed * 24000L) + timeOfDayTicks;
    }

    public static long getWorldCreationTimestamp(Level level) {
        if (loadedData == null) {
            return -1L;
        }

        return loadedData.creation;
    }

    public static String getSystemTimeString() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        return ZonedDateTime.now(ZoneId.systemDefault()).format(timeFmt);
    }

    public static String getSystemDateString() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return ZonedDateTime.now(ZoneId.systemDefault()).format(dateFmt);
    }

    public static void setForcedTicks(long forcedTicks) {
        Synchronizer.forcedTicks = forcedTicks;
    }

    public static WeatherSave getLoadedData() {
        return loadedData;
    }

    public static int getCurrentWMOCode() {
        if (forcedWMO >= 0) return forcedWMO;
        if (loadedData == null) return 0;

        Object entry = loadedData.weather_data.get("current");
        if (entry instanceof WeatherRequestData.Current currentWMOCode) {
            return currentWMOCode.weather_code;
        }

        return 0;
    }

    public static int getRepresentativeCode(WeatherState type, WeatherStateStrength strength) {
        // Try intersection between both enums (best choice)
        for (int codeType : type.getCodes()) {
            for (int codeStrength : strength.getCodes()) {
                if (codeType == codeStrength) return codeType;
            }
        }

        // Fallback: just return type’s first code
        return type.getCodes()[0];
    }


    public static void setForcedWMO(int wmo) {
        Synchronizer.forcedWMO = wmo;
    }

    /**
     * Loads or creates the save file on server side
     */
    public static void ensureData(ServerLevel serverLevel) {
        if (loadedData != null) {
            return;
        }

        Optional<WeatherSave> loaded = FileManager.load(serverLevel, WORLD_GLOBAL_DATA_FILE_NAME, WeatherSave.class);
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

                    FileManager.save(serverLevel, WORLD_GLOBAL_DATA_FILE_NAME, save);
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

    public static void setLoadedData(WeatherSave data) {
        loadedData = data;
    }

    public static void sendSyncPacket(ServerPlayer player) {
        if (loadedData == null) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        String json = FileManager.GSON.toJson(loadedData);
        buf.writeUtf(json);

        player.connection.send(new ClientboundCustomPayloadPacket(SYNC_PACKET_ID, buf));
    }

    public static void syncToAllPlayers(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            sendSyncPacket(player);
        }
    }
}
