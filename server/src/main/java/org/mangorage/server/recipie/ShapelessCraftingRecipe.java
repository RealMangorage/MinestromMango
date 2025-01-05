package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ShapelessCraftingRecipe implements CraftingRecipe {
    private final List<Predicate<ItemStack>> predicates;
    private final Supplier<ItemStack> result;

    public ShapelessCraftingRecipe(List<Predicate<ItemStack>> predicates, Supplier<ItemStack> result) {
        this.predicates = List.copyOf(predicates);
        this.result = result;
    }

    @Override
    public ItemStack getResult(CraftingInventory inventory) {
        int matched = 0;
        var stacks = inventory.getStacks();
        total: for (Predicate<ItemStack> predicate : predicates) {
            for (ItemStack stack : stacks) {
                if (stack.isAir()) continue;
                if (predicate.test(stack))
                    matched++;
                if (matched >= predicates.size())
                    break total;
            }
        }
        return matched == predicates.size() ? result.get() : ItemStack.AIR;
    }
}
