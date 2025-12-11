package com.restonic4.fancyweather.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.restonic4.fancyweather.config.FancyWeatherMidnightConfig;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

/**
 * Used to grab your latitude and longitude to sync the minecraft world weather with the real life weather.
 * THIS DATA IS NOT BEING SENT TO ANYONE, it gets stored on the world.
 * You can disable this feature by disabling the real time sync config.
 */
public class GeolocationUtil {
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_FILENAME = "DONT_OPEN_ON_PUBLIC.json";

    public record LatLng(double latitude, double longitude) {
        @Override
        public @NotNull String toString() {
            return "LatLng{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LatLng latLng)) return false;
            return Double.compare(latLng.latitude, latitude) == 0 && Double.compare(latLng.longitude, longitude) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude);
        }
    }

    /**
     * Convenience: use default file under user home directory.
     * Default file: {user.home}/.location.json
     */
    public static LatLng getLatLng(ServerLevel serverLevel) throws IOException {
        Path defaultPath = FileManager.getFilePath(serverLevel, DEFAULT_FILENAME);
        return getLatLng(defaultPath);
    }

    /**
     * Main entry: check the given filePath for existing JSON; if missing or invalid, fetch, save, return.
     *
     * @param filePath path to JSON file to read/write
     * @return LatLng object with latitude and longitude
     * @throws IOException on IO / network errors
     */
    public static LatLng getLatLng(Path filePath) throws IOException {
        // try read from file
        if (Files.exists(filePath)) {
            try (Reader r = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(r);
                if (parsed != null && parsed.isJsonObject()) {
                    LatLng fromFile = readLatLngFromJsonObject(parsed.getAsJsonObject());
                    if (fromFile != null) return fromFile;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // fetch from network
        LatLng fetched = fetchLatLngFromNetwork();

        // save to file
        saveLatLngAtomic(filePath, fetched);

        return fetched;
    }

    /* ---------- JSON helpers ---------- */

    private static LatLng readLatLngFromJsonObject(JsonObject obj) {
        Double lat = getAsDoubleIfPresent(obj, "latitude");
        Double lon = getAsDoubleIfPresent(obj, "longitude");
        if (lat == null || lon == null) {
            lat = lat == null ? getAsDoubleIfPresent(obj, "lat") : lat;
            lon = lon == null ? getAsDoubleIfPresent(obj, "lon") : lon;
        }
        if (lat == null || lon == null) {
            lon = lon == null ? getAsDoubleIfPresent(obj, "lng") : lon;
        }
        if (lat != null && lon != null) {
            return new LatLng(lat, lon);
        }
        return null;
    }

    private static Double getAsDoubleIfPresent(JsonObject obj, String key) {
        try {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsDouble();
            }
        } catch (Exception ignored) { }
        return null;
    }

    /* ---------- Network fetch with fallbacks ---------- */

    private static LatLng fetchLatLngFromNetwork() throws IOException {
        if (!FancyWeatherMidnightConfig.enableSync) {
            throw new IOException("Unable to fetch geolocation, not allowed by user!");
        }

        // Try a sequence of public IP geolocation endpoints. Some return fields "latitude"/"longitude",
        // others "lat"/"lon". We handle both. These are tried in order until one succeeds.
        String[] urls = {
                "https://ipapi.co/json/",       // returns "latitude"/"longitude"
                "https://ipwhois.app/json/",    // returns "latitude"/"longitude"
                "http://ip-api.com/json/"       // returns "lat"/"lon" and "status"
        };

        IOException lastEx = null;
        for (String sUrl : urls) {
            try {
                JsonObject json = httpGetJson(sUrl);
                if (json != null) {
                    LatLng parsed = readLatLngFromJsonObject(json);
                    // ip-api.com returns "status": "success" or "fail" - check it if present
                    if (parsed != null) {
                        if (json.has("status")) {
                            try {
                                String status = json.get("status").getAsString();
                                if ("success".equalsIgnoreCase(status)) {
                                    return parsed;
                                } else {
                                    // skip this result (failed)
                                    continue;
                                }
                            } catch (Exception ignored) { /* ignore and return parsed if fields present */ }
                        } else {
                            return parsed;
                        }
                    }
                }
            } catch (IOException e) {
                lastEx = e;
                // try next URL
            }
        }
        throw new IOException("Unable to fetch geolocation from network", lastEx);
    }

    private static JsonObject httpGetJson(String sUrl) throws IOException {
        URL url = new URL(sUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6_000);
        conn.setReadTimeout(6_000);
        conn.setRequestProperty("User-Agent", "JavaGeolocationUtil/1.0");
        int code = conn.getResponseCode();
        Reader reader = null;
        if (code >= 200 && code < 300) {
            reader = new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
        } else {
            // attempt to read error stream for diagnostics but still treat as non-success
            try {
                if (conn.getErrorStream() != null) {
                    reader = new java.io.InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) { }
        }
        if (reader == null) {
            throw new IOException("HTTP " + code + " from " + sUrl);
        }
        try (Reader r = reader) {
            JsonElement el = JsonParser.parseReader(r);
            if (el != null && el.isJsonObject()) return el.getAsJsonObject();
            return null;
        } finally {
            conn.disconnect();
        }
    }

    /* ---------- Atomic save ---------- */

    private static void saveLatLngAtomic(Path path, LatLng latLng) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(latLng, "latLng");

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        // prepare JSON to write
        JsonObject toWrite = new JsonObject();
        toWrite.addProperty("latitude", latLng.latitude);
        toWrite.addProperty("longitude", latLng.longitude);

        Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(toWrite, w);
        }

        // Try atomic move where supported; fall back to replace.
        try {
            Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
