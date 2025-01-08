package org.mangorage.servertest.block.handlers;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.type.FurnaceInventory;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class FurnaceBlockHandler implements BlockHandler {
    private final NamespaceID ID = NamespaceID.from("minecraft:furnace");
    private final Map<Point, Inventory> maps = new HashMap<>();

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        if (interaction.getPlayer().isSneaking()) return true;
        interaction.getPlayer().openInventory(
                maps.computeIfAbsent(interaction.getBlockPosition(), id -> new FurnaceInventory(Component.text("Furnace")))
        );
        return false;
    }

    @Override
    public boolean isTickable() {
        return true;
    }

    @Override
    public void tick(@NotNull Tick tick) {
        var a = 1;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }
}
