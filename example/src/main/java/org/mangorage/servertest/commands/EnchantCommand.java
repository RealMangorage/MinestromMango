package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.Range;
import org.mangorage.server.core.MangoServer;

import java.util.function.UnaryOperator;

public class EnchantCommand extends MangoServerCommand {

    public EnchantCommand(MangoServer server) {
        super(server, "enchant");

        var enchantment = ArgumentType.Resource("enchantments", "minecraft:enchantment");
        var level = ArgumentType.Integer("level")
                .min(1)
                .max(255)
                .setDefaultValue(1);

        addSyntax(
                (sender, context) -> {
                    if (sender instanceof Player player) {
                        DynamicRegistry.Key<Enchantment> key = DynamicRegistry.Key.of(context.get(enchantment));
                        if (server.getServerProcess().enchantment().get(key) != null) {
                            player.setItemInMainHand(
                                    player.getItemInMainHand()
                                            .with(ItemComponent.ENCHANTMENTS, (UnaryOperator<EnchantmentList>) enchantmentList -> enchantmentList.with(
                                                    key,
                                                    context.get(level)
                                            ))
                            );
                        }
                    }
                },
                enchantment, level
        );
    }
}
