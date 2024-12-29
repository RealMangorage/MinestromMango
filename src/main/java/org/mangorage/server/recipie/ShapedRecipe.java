package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.function.Supplier;

public class ShapedRecipe implements Recipe {
    private final Material[] materials;
    private final Supplier<ItemStack> result;

    public ShapedRecipe(Material[] materials, Supplier<ItemStack> result) {
        this.materials = materials;
        this.result = result;
    }

    @Override
    public ItemStack getResult(ItemStack[] stacks) {

        if (materials[0] == stacks[0].material() && materials[1] == stacks[1].material() && materials[2] == stacks[2].material() && materials[3] == stacks[3].material()) {
            return result.get();
        }

        return ItemStack.AIR;
    }
}
