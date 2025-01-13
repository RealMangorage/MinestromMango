package org.mangorage.servertest.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

public interface CancellableMenuAction {
    boolean accept(AbstractInventory inventory, ClickType clickType, int slot, Player player, ItemStack cursor);
}
