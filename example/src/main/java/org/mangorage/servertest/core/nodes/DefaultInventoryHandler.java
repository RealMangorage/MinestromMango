package org.mangorage.servertest.core.nodes;

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
        return EventNode.all("default-inventory-handler")
                .addListener(InventoryPreClickEvent.class, event -> {
                    if (event.getClickType() == ClickType.SHIFT_CLICK)
                        event.setCancelled(true);
                })
                .addListener(InventoryCloseEvent.class, event -> {
                    if (event.getInventory() instanceof Inventory inventory) {
                        for (@NotNull ItemStack stack : inventory.getItemStacks()) {
                            event.getPlayer().getInventory().addItemStack(stack);
                        }
                    }
                })
                .addListener(InventoryClickEvent.class, event -> {
                    // 37 -> 40

                    // 36 Output
                    if (event.getInventory() instanceof PlayerInventory playerInventory) {
                        if (event.getSlot() >= 37 && event.getSlot() <= 40) {
                            updateCraftingView(server.getCraftingRecipeManager(), playerInventory, false);
                        } else if (event.getSlot() == 36 && !event.getClickedItem().isAir()) {
                            var result = updateCraftingView(server.getCraftingRecipeManager(), playerInventory, false); // Verify
                            if (event.getCursorItem().material() != result.material() && !event.getCursorItem().isAir()) {
                                playerInventory.setItemStack(36, event.getClickedItem(), true);
                                playerInventory.setCursorItem(event.getCursorItem());
                                return;
                            } else if (!event.getCursorItem().isAir()) {
                                if ( (event.getCursorItem().amount() + result.amount()) > result.material().maxStackSize()) {
                                    playerInventory.setItemStack(36, event.getClickedItem(), true);
                                    playerInventory.setCursorItem(event.getCursorItem());
                                    return;
                                } else {
                                    playerInventory.setCursorItem(
                                            ItemStack.of(
                                                    result.material(),
                                                    event.getCursorItem().amount() + result.amount()
                                            )
                                    );
                                }
                            }
                            shrinkCrafting(playerInventory, false);
                            playerInventory.setItemStack(36, ItemStack.AIR, true);
                            updateCraftingView(server.getCraftingRecipeManager(), playerInventory, false);
                        }
                    } else if (event.getInventory() instanceof Inventory inventory) {
                        if (inventory.getInventoryType().equals(InventoryType.CRAFTING)) {
                            if (event.getSlot() != 0) {
                                updateCraftingView(server.getCraftingRecipeManager(), inventory, true);
                            } else if (event.getSlot() == 0 && !event.getClickedItem().isAir()) {
                                var result = updateCraftingView(server.getCraftingRecipeManager(), inventory, true); // Verify
                                if (event.getCursorItem().material() != result.material() && !event.getCursorItem().isAir()) {
                                    inventory.setItemStack(0, event.getClickedItem(), true);
                                    inventory.setCursorItem(event.getPlayer(), event.getCursorItem());
                                    return;
                                } else if (!event.getCursorItem().isAir()) {
                                    if ( (event.getCursorItem().amount() + result.amount()) > result.material().maxStackSize()) {
                                        inventory.setItemStack(0, event.getClickedItem(), true);
                                        inventory.setCursorItem(event.getPlayer(), event.getCursorItem());
                                        return;
                                    } else {
                                        inventory.setCursorItem(
                                                event.getPlayer(),
                                                ItemStack.of(
                                                        result.material(),
                                                        event.getCursorItem().amount() + result.amount()
                                                )
                                        );
                                    }
                                }
                                shrinkCrafting(inventory, true);
                                inventory.setItemStack(0, ItemStack.AIR, true);
                                updateCraftingView(server.getCraftingRecipeManager(), inventory, true);
                            }
                        }
                    }
                });
    }
}
