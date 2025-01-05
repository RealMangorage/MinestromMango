package org.mangorage.servertest.block.handlers;

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
        if (interaction.getPlayer().isSneaking()) {
            return true;
        }

        interaction.getPlayer().openInventory(
                new Inventory(
                        InventoryType.CRAFTING,
                        Component.text("Crafting Table")
                )
        );
        return false;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }
}
