package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.function.Supplier;

public final class ShapelessRecipe implements Recipe {
    private final List<Material> materials;
    private final Supplier<ItemStack> result;

    public ShapelessRecipe(List<Material> materials, Supplier<ItemStack> result) {
        this.materials = materials;
        this.result = result;
    }

    @Override
    public ItemStack getResult(ItemStack[] stacks) {
        int matched = 0;


        for (ItemStack stack : stacks) {
            if (materials.contains(stack.material()))
                matched++;
        }

        if (matched == materials.size())
            return result.get();

        return ItemStack.AIR;
    }
}
