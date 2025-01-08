package org.mangorage.servertest.core.nodes;

import net.goldenstack.loot.LootContext;
import net.goldenstack.loot.util.VanillaInterface;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.time.TimeUnit;
import org.mangorage.server.core.MangoServer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultBlockBreakHandler {
    public static EventNode<Event> register(MangoServer server) {
        return EventNode.all("block-break-handler")
                .addListener(PickupItemEvent.class, event -> {
                    var entity = event.getEntity();
                    if (entity instanceof Player player) {
                        var ret = player.getInventory().addItemStacks(List.of(event.getItemStack()), TransactionOption.ALL);
                        if (ret.stream().anyMatch(i -> i != ItemStack.AIR))
                            event.setCancelled(true);
                    } else {
                        event.setCancelled(true);
                    }
                })
                .addListener(ItemDropEvent.class, event -> {
                    Player player = event.getPlayer();
                    ItemStack droppedItem = event.getItemStack();

                    ItemEntity itemEntity = new ItemEntity(droppedItem);
                    itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
                    itemEntity.setInstance(player.getInstance());
                    itemEntity.teleport(player.getPosition().add(0, 1.5f, 0));

                    Vec velocity = player.getPosition().direction().mul(6);
                    itemEntity.setVelocity(velocity);
                })
                .addListener(PlayerBlockBreakEvent.class, event -> {
                    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

                    var material = Material.fromNamespaceId(event.getBlock().namespace());
                    if (material != null) {
                        var held = event.getPlayer().getItemInMainHand();
                        var enchs = held.get(ItemComponent.ENCHANTMENTS);
                        var tool = held.get(ItemComponent.TOOL);

                        if (tool.isCorrectForDrops(event.getBlock())) {
                            Map<LootContext.Key<?>, Object> l = new HashMap<>();
                            l.put(LootContext.RANDOM, server.getRandom());

                            l.put(LootContext.TOOL, event.getPlayer().getItemInMainHand());
                            l.put(LootContext.BLOCK_STATE, event.getBlock());
                            l.put(LootContext.ORIGIN, event.getBlockPosition());

                            if (enchs.has(Enchantment.FORTUNE)) {
                                l.put(LootContext.ENCHANTMENT_ACTIVE, true);
                                l.put(LootContext.ENCHANTMENT_LEVEL, enchs.level(Enchantment.FORTUNE));
                            }

                            server.getLootTables().get(event.getBlock().namespace()).blockDrop(
                                    LootContext.from(
                                            VanillaInterface.defaults(),
                                            l
                                    ),
                                    event.getInstance(),
                                    event.getBlockPosition()
                            );
                        }

                        if (held.has(ItemComponent.DAMAGE)) {
                            var maxDmg = held.get(ItemComponent.MAX_DAMAGE);
                            var dmg = held.get(ItemComponent.DAMAGE) + tool.damagePerBlock();

                            if (dmg >= maxDmg) {
                                event.getPlayer().setItemInMainHand(ItemStack.AIR);
                                event.getPlayer().playSound(
                                        Sound.sound()
                                                .type(SoundEvent.ENTITY_ITEM_BREAK)
                                                .build()
                                );
                            } else {
                                event.getPlayer().setItemInMainHand(
                                        held.with(ItemComponent.DAMAGE, dmg)
                                );
                            }
                        }

                    }
                });
    }
}
