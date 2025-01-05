package org.mangorage.server.misc;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.mangorage.server.core.MangoServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public final class PlayerUtil {

    public enum DeserializationResult {
        SUCCESS,
        NO_DATA,
        FAILED
    }

    private static final Logger log = LoggerFactory.getLogger(PlayerUtil.class);

    public static void serialize(MangoServer server, String path, Player player) {
        Path playerSavePath = server.getServerPath().resolve(path).resolve(player.getUuid() + ".nbt");
        try {
            if (!Files.exists(playerSavePath)) {
                Files.createDirectories(playerSavePath.getParent());
            }

            var root = CompoundBinaryTag.builder();
            var inventory = ListBinaryTag.builder();
            var location = CompoundBinaryTag.builder();

            location.putDouble("x", player.getPosition().x());
            location.putDouble("y", player.getPosition().y());
            location.putDouble("z", player.getPosition().z());
            location.putFloat("yaw", player.getPosition().yaw());
            location.putFloat("pitch", player.getPosition().pitch());

            root.put("location", location.build());

            for (int i = 0; i < player.getInventory().getSize(); i++) {
                inventory.add(player.getInventory().getItemStack(i).toItemNBT());
            }

            root.put("inventory", inventory.build());


            BinaryTagIO.writer()
                    .write(root.build(), playerSavePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deserialize(MangoServer server, String path, Player player, BiConsumer<Player, DeserializationResult> playerConsumer) {
        Path playerSavePath = server.getServerPath().resolve(path).resolve(player.getUuid() + ".nbt");
        if (!Files.exists(playerSavePath)) {
            playerConsumer.accept(player, DeserializationResult.NO_DATA);
            return;
        }
        try {
            CompoundBinaryTag root = BinaryTagIO.reader().read(playerSavePath);
            var inventory = root.getList("inventory", CompoundBinaryTag.empty().type());
            var location = root.getCompound("location");
            var size = inventory.size();

            for (int i = 0; i < size; i++) {
                player.getInventory()
                        .setItemStack(
                                i,
                                ItemStack.fromItemNBT(
                                        inventory.getCompound(i)
                                )
                        );
            }

            player.setRespawnPoint(
                    new Pos(
                            location.getDouble("x"),
                            location.getDouble("y"),
                            location.getDouble("z"),
                            location.getFloat("yaw"),
                            location.getFloat("pitch")
                    )
            );
            playerConsumer.accept(player, DeserializationResult.SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
            playerConsumer.accept(player, DeserializationResult.FAILED);
        }
    }
}
