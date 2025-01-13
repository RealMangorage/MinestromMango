package org.mangorage.servertest.core.nodes;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.recipie.CraftingInput;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.servertest.inventory.MenuHandler;

public final class DefaultInventoryHandler {

    private static void shrinkCrafting(AbstractInventory inv, boolean is3x3) {
        int from = is3x3 ? 1 : 37;
        int to = is3x3 ? 9 : 40;
        for (int i = from; i <= to; i++) {
            var item = inv.getItemStack(i);
            if (item.isAir() || item.amount() == 1) {
                inv.setItemStack(i, ItemStack.AIR, true);
            } else {
                inv.setItemStack(
                        i,
                        item.withAmount(
                                item.amount() - 1
                        ),
                        true
                );
            }
        }
        inv.update();
    }

    private static ItemStack updateCraftingView(CraftingRecipeManager manager, AbstractInventory inventory, boolean is3x3) {
        var type = is3x3 ? CraftingInput.Type.CRAFTING_BENCH : CraftingInput.Type.PLAYER;
        var result = manager.getResult(new CraftingInput(type, inventory));
        inventory.setItemStack(type.getOutputSlot(), result, true);
        return result;
    }

    public static EventNode<Event> register(MangoServer server) {
        MenuHandler PLAYER_MENU_HANDLER = new MenuHandler()
                .putCancellableAction(36, (inventory, clickType, slot, player, cursor) -> {
                    if (clickType != ClickType.LEFT_CLICK) return true;
                    var stack = inventory.getItemStack(slot);
                    if (inventory.getItemStack(slot).material() == cursor.material()) {
                        if (stack.amount() + cursor.amount() > cursor.maxStackSize()) return true;
                        return false;
                    }
                    return !cursor.isAir();
                })
                .putAction(37, 40, (inventory, clickType, slot, player, cursor) -> {
                    updateCraftingView(server.getCraftingRecipeManager(), inventory, false);
                })
                .putAction(36, (inventory, clickType, slot, player, cursor) -> {
                    if (clickType == ClickType.LEFT_CLICK) {
                        var result = updateCraftingView(server.getCraftingRecipeManager(), inventory, false);
                        if (cursor.isAir() || cursor.material() == result.material()) {
                            if (cursor.isAir()) {
                                player.getInventory().setCursorItem(result);
                                inventory.setItemStack(slot, ItemStack.AIR, true);
                                shrinkCrafting(inventory, false);
                                updateCraftingView(server.getCraftingRecipeManager(), inventory, false);
                            } else if (!(cursor.amount() + result.amount() > result.maxStackSize())) {
                                player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() + result.amount()));
                                inventory.setItemStack(slot, ItemStack.AIR, true);
                                shrinkCrafting(inventory, false);
                                updateCraftingView(server.getCraftingRecipeManager(), inventory, false);
                            }
                        }
                    }
                });

        MenuHandler CRAFTING_MENU_HANDLER = new MenuHandler()
                .putCancellableAction(0, (inventory, clickType, slot, player, cursor) -> {
                    if (clickType != ClickType.LEFT_CLICK) return true;
                    var stack = inventory.getItemStack(slot);
                    if (inventory.getItemStack(slot).material() == cursor.material()) {
                        if (stack.amount() + cursor.amount() > cursor.maxStackSize()) return true;
                        return false;
                    }
                    return !cursor.isAir();
                })
                .putAction(1, 9, (inventory, clickType, slot, player, cursor) -> {
                    updateCraftingView(server.getCraftingRecipeManager(), inventory, true);
                })
                .putAction(0, (inventory, clickType, slot, player, cursor) -> {
                    if (clickType == ClickType.LEFT_CLICK) {
                        var result = updateCraftingView(server.getCraftingRecipeManager(), inventory, true);
                        if (cursor.isAir() || cursor.material() == result.material()) {
                            if (cursor.isAir()) {
                                player.getInventory().setCursorItem(result);
                                inventory.setItemStack(slot, ItemStack.AIR, true);
                                shrinkCrafting(inventory, true);
                                updateCraftingView(server.getCraftingRecipeManager(), inventory, true);
                            } else if (!(cursor.amount() + result.amount() > result.maxStackSize())) {
                                player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() + result.amount()));
                                inventory.setItemStack(slot, ItemStack.AIR, true);
                                shrinkCrafting(inventory, true);
                                updateCraftingView(server.getCraftingRecipeManager(), inventory, true);
                            }
                        }
                    }
                });

        return EventNode.all("default-inventory-handler")
                .addListener(InventoryPreClickEvent.class, event -> {
                    if (event.getClickType() == ClickType.SHIFT_CLICK)
                        event.setCancelled(true);

                    if (event.getInventory() instanceof PlayerInventory) {
                        event.setCancelled(
                                PLAYER_MENU_HANDLER.processPreClick(event)
                        );
                    } else if (event.getInventory() instanceof Inventory inventory) {
                        var type = inventory.getInventoryType();
                        if (type == InventoryType.CRAFTING) {
                            event.setCancelled(
                                    CRAFTING_MENU_HANDLER.processPreClick(event)
                            );
                        }
                    }
                })
                .addListener(InventoryCloseEvent.class, event -> {
                    if (event.getInventory() instanceof Inventory inventory) {
                        for (@NotNull ItemStack stack : inventory.getItemStacks()) {
                            event.getPlayer().getInventory().addItemStack(stack);
                        }
                    }
                })
                .addListener(InventoryClickEvent.class, event -> {
                    if (event.getInventory() instanceof PlayerInventory) {
                        PLAYER_MENU_HANDLER.processClick(event);
                    } else if (event.getInventory() instanceof Inventory inventory) {
                        var type = inventory.getInventoryType();
                        if (type == InventoryType.CRAFTING) {
                            CRAFTING_MENU_HANDLER.processClick(event);
                        }
                    }
                });
    }
}
