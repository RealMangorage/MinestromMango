package org.mangorage.server.block.handlers;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class CraftingTableBlockHandler implements BlockHandler {
    private final NamespaceID ID = NamespaceID.from("minecraft:crafting_table");

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        interaction.getPlayer().openInventory(
                new Inventory(
                        InventoryType.CRAFTING,
                        Component.text("Crafting Table")
                )
        );
        return true;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }
}
