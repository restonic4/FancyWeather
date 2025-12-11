package com.restonic4.fancyweather.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import static com.restonic4.fancyweather.Constants.*;

public class FileManager {
    private static final String JSON_KEY = "json";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path getFilePath(ServerLevel level, String fileName) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve(level.dimension().location().getPath()).resolve(fileName);
    }

    public static boolean save(ServerLevel level, String fileName, Object data) {
        Path path = getFilePath(level, fileName);

        try {
            // Ensure parent directories
            if (Files.notExists(path.getParent()))
                Files.createDirectories(path.getParent());

            // Write to temp file first (atomic)
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");

            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(data, writer);
            }

            Files.move(tmp, path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> Optional<T> load(ServerLevel level, String fileName, Class<T> clazz) {
        Path path = getFilePath(level, fileName);

        if (!Files.exists(path)) return Optional.empty();

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            T obj = GSON.fromJson(reader, clazz);
            return Optional.ofNullable(obj);

        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static boolean delete(ServerLevel level, String fileName) {
        Path path = getFilePath(level, fileName);
        try {
            if (Files.notExists(path)) return true;
            Files.delete(path);
            return true;
        } catch (IOException e) {
            LOG.error("Failed to delete file {}", path, e);
            return false;
        }
    }

    public static boolean exists(ServerLevel level, String fileName) {
        return Files.exists(getFilePath(level, fileName));
    }
}
