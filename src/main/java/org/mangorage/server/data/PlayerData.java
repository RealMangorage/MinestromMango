package org.mangorage.server.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.StorageNBTComponent;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public record PlayerData(
        Map<Integer, Map<String, Object>> map,
        Pos pos
) {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String readFileToString(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            return null; // Or handle the error as appropriate
        }
    }

    public static boolean writeStringToFile(String content, String filePath) {
        try {
            Path path = Path.of(filePath);
            // Ensure parent directories exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            // Write content to file
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Or handle the error as appropriate
        }
    }


    public static PlayerData create(Player player) {
        Map<Integer, Map<String, Object>> items = new HashMap<>();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            var item = player.getInventory().getItemStack(i);
            if (item.isAir()) continue;
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.material().name());
            itemMap.put("count", item.amount());
            // Handle nothing more!
            items.put(i, itemMap);
        }
        return new PlayerData(items, player.getPosition());
    }

    public static void serialize(Player player) {
        writeStringToFile(
                gson.toJson(create(player)),
                Path.of("data/server/MangoServer/players/" + player.getUuid()).toAbsolutePath().toString()
        );
    }

    public static PlayerData.Loaded deserialize(Player player) {
        String data = readFileToString("data/server/MangoServer/players/" + player.getUuid());
        if (data == null) return null;
        PlayerData plrDataUnloaded = gson.fromJson(data, PlayerData.class);
        HashMap<Integer, ItemStack> loaded = new LinkedHashMap<>();
        AtomicReference<PlayerData.Loaded> plrData = new AtomicReference<>();

       plrDataUnloaded.map().forEach((k, v) -> {
            if (v instanceof Map map) {
                Map<String, Object> a = (Map<String, Object>) map;
                loaded.put(
                        k,
                        ItemStack.of(
                                Material.fromNamespaceId((String) a.get("id")),
                                ((Double) a.get("count")).intValue()
                        )
                );
            }
        });

        return new Loaded(
                loaded,
                plrDataUnloaded.pos()
        );
    }


    public record Loaded(
            Map<Integer, ItemStack> itemStacks,
            Pos pos
    ) {}
}
