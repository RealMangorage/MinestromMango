package org.mangorage.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.mangorage.server.recipie.RecipeManager;
import org.mangorage.server.recipie.ShapedRecipe;
import org.mangorage.server.recipie.ShapelessRecipe;

import java.util.List;


public final class Listeners {
    private final RecipeManager manager = new RecipeManager();


    public ItemStack[] getItems(PlayerInventory playerInventory) {
        ItemStack[] stacks = new ItemStack[4];
        stacks[0] = playerInventory.getItemStack(37);
        stacks[1] = playerInventory.getItemStack(38);
        stacks[2] = playerInventory.getItemStack(39);
        stacks[3] = playerInventory.getItemStack(40);
        return stacks;
    }

    public void shrinkCrafting(PlayerInventory inventory) {
        for (int i = 37; i <= 40; i++) {
            var item = inventory.getItemStack(i);
            if (item.isAir() || item.amount() == 1) {
                inventory.setItemStack(i, ItemStack.AIR, true);
            } else {
                inventory.setItemStack(
                        i,
                        item.withAmount(
                                item.amount() - 1
                        ),
                        true
                );
            }
        }
        inventory.update();
    }

    public ItemStack updateCraftingView(PlayerInventory inventory) {
        var result = manager.getResult(getItems(inventory));
        inventory.setItemStack(36, result, true);
        return result;
    }

    public Listeners() {
        manager.register(
                new ShapelessRecipe(
                        List.of(
                                Material.OAK_LOG
                        ),
                        () -> ItemStack.of(Material.OAK_PLANKS, 4)
                )
        );

        manager.register(
                new ShapedRecipe(
                        List.of(
                                Material.OAK_PLANKS,
                                Material.OAK_PLANKS,
                                Material.OAK_PLANKS,
                                Material.OAK_PLANKS
                        ).toArray(Material[]::new),
                        () -> ItemStack.of(Material.CRAFTING_TABLE, 1)
                )
        );

        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            event.getPlayer().getInventory().addItemStack(
                   ItemStack.of(
                           Material.fromNamespaceId(event.getBlock().namespace()),
                           1
                   )
           );
        });

        handler.addListener(InventoryPreClickEvent.class, event -> {
            if (event.getClickType() == ClickType.SHIFT_CLICK)
                event.setCancelled(true);
        });

        handler.addListener(InventoryClickEvent.class, event -> {
            // 37 -> 40

            // 36 Output
            if (event.getInventory() instanceof PlayerInventory playerInventory) {
                if (event.getSlot() >= 37 && event.getSlot() <= 40) {
                    updateCraftingView(playerInventory);
                } else if (event.getSlot() == 36) {
                    var result = updateCraftingView(playerInventory); // Verify
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
                    shrinkCrafting(playerInventory);
                    playerInventory.setItemStack(36, ItemStack.AIR, true);
                    updateCraftingView(playerInventory);
                }
            }
        });
    }




}
