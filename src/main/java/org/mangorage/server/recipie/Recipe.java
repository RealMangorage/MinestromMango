package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

public interface Recipe {
    ItemStack getResult(ItemStack[] stacks);
}
