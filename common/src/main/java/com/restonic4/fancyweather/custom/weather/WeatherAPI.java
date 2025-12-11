package com.restonic4.fancyweather.custom.weather;

import com.google.gson.Gson;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import com.restonic4.fancyweather.custom.sync.Synchronizer;
import com.restonic4.fancyweather.custom.sync.WeatherSave;
import com.restonic4.fancyweather.utils.GeolocationUtil;
import net.minecraft.server.level.ServerLevel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static com.restonic4.fancyweather.Constants.LOG;

public class WeatherAPI {
    private static final Gson GSON = new Gson();
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude={LATITUDE}&longitude={LONGITUDE}&current=weather_code&hourly=weather_code&forecast_days=16";

    public static void updateWeatherData(ServerLevel level) {
        GeolocationUtil.LatLng latLng = null;

        try {
            latLng = GeolocationUtil.getLatLng(level);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (latLng == null) {
            LOG.info("Geolocation failed.");
            return;
        }

        fetchFromApi(String.valueOf(latLng.latitude()), String.valueOf(latLng.longitude())).thenAccept(data -> {
            if (data != null) {
                WeatherSave save = Synchronizer.getLoadedData();

                save.weather_data.clear();
                save.weather_data.put("lastUpdateTimestamp", data.lastUpdateTimestamp);
                save.weather_data.put("current", data.current);
                save.weather_data.put("hourly", data.hourly);

               LOG.info("Weather synced with API.");
            } else {
                LOG.info("API failed.");
            }
        });
    }

    private static CompletableFuture<WeatherRequestData> fetchFromApi(String latitude, String longitude) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL.replaceAll("\\{LATITUDE}", latitude).replaceAll("\\{LONGITUDE}", longitude))).GET().build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        return GSON.fromJson(json, WeatherRequestData.class);
                    } catch (Exception e) {
                        LOG.error("Error: ", e);
                        return null;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch weather: " + e.getMessage());
                    return null;
                });
    }
}
