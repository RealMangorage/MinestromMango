package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

public interface CraftingRecipe {
    ItemStack getResult(CraftingInput inventory);
}
