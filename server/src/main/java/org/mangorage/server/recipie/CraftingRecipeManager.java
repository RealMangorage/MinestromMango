package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CraftingRecipeManager implements CraftingRecipe {
    private final List<CraftingRecipe> recipes = new ArrayList<>();

    public void register(CraftingRecipe recipe) {
        this.recipes.add(recipe);
    }

    public void registerShapeless(Material result, int amount, Material... materials) {
        register(createShapeless(result, amount, materials));
    }

    public CraftingRecipe createShapeless(Material result, int amount, Material... materials) {
        return new ShapelessCraftingRecipe(
                Arrays.stream(materials)
                        .map(material -> Ingredient.of(ItemStack.of(material))).toList(),
                () -> ItemStack.of(result, amount),
                materials.length < 4 ? 2 : 3,
                materials.length < 4 ? 2 : 3
        );
    }

    @Override
    public ItemStack getResult(CraftingInput inventory) {
        for (CraftingRecipe recipe : recipes) {
            var result = recipe.getResult(inventory);
            if (!result.isAir()) {
                System.out.println(result);
                return result;
            }
        }
        return ItemStack.AIR;
    }
}
