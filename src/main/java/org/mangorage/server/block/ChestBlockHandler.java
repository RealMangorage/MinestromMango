package org.mangorage.server.block;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ChestBlockHandler implements BlockHandler {
    private final Map<Point, Inventory> inventoryMap = new HashMap<>();

    @Override
    public void onTouch(@NotNull Touch touch) {
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        var inv = inventoryMap.get(destroy.getBlockPosition());
        if (inv != null) {
            inventoryMap.remove(destroy.getBlockPosition());
            for (@NotNull ItemStack stack : inv.getItemStacks()) {
                ItemEntity item = new ItemEntity(stack);
                item.setInstance(destroy.getInstance(), destroy.getBlockPosition());
            }
        }
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        interaction.getPlayer().openInventory(
                inventoryMap.computeIfAbsent(
                        interaction.getBlockPosition(),
                        k -> new Inventory(InventoryType.CHEST_3_ROW, Component.text("Chest"))
                )
        );
        return false;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("mango:demo");
    }
}
