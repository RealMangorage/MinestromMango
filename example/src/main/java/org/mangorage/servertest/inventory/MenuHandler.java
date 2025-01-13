package org.mangorage.servertest.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;

public final class MenuHandler {
    private final Int2ObjectOpenHashMap<MenuAction> actions = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<CancellableMenuAction> cancellableMenuActions = new Int2ObjectOpenHashMap<>();



    public MenuHandler putCancellableAction(int slot, CancellableMenuAction cancellableMenuAction) {
        cancellableMenuActions.put(slot, cancellableMenuAction);
        return this;
    }

    public MenuHandler putAction(int fromSlot, int toSlot, MenuAction action) {
        for (int i = fromSlot; i <= toSlot; i++) {
            actions.put(i, action);
        }
        return this;
    }

    public MenuHandler putAction(int slot, MenuAction action) {
        actions.put(slot, action);
        return this;
    }

    public void processClick(InventoryClickEvent event) {
        var action = actions.get(event.getSlot());
        if (action != null)
            action.accept(
                    event.getInventory(),
                    event.getClickType(),
                    event.getSlot(),
                    event.getPlayer(),
                    event.getCursorItem()
            );
    }

    public boolean processPreClick(InventoryPreClickEvent event) {
        var action = cancellableMenuActions.get(event.getSlot());
        if (action != null) {
            return action.accept(
                    event.getInventory(),
                    event.getClickType(),
                    event.getSlot(),
                    event.getPlayer(),
                    event.getCursorItem()
            );
        }
        return false;
    }
}
