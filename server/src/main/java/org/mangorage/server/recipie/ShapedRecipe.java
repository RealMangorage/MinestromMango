package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;
import org.mangorage.server.misc.NonNullList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class ShapedRecipe implements CraftingRecipe {
    private static final Logger log = LoggerFactory.getLogger(ShapedRecipe.class);
    private int width = 3;
    private int height = 3;
    private final List<Ingredient> ingredients;
    private final List<Offset> offsets;
    private final Supplier<ItemStack> result;
    private final int ingredientsCount;

    public ShapedRecipe(int width, int height, List<Ingredient> ingredients, Supplier<ItemStack> result, List<Offset> offsets) {
        this.width = width;
        this.height = height;
        this.ingredients = NonNullList.of(Ingredient.EMPTY, width * height, ingredients);
        this.result = result;
        this.offsets = offsets;
        this.ingredientsCount = (int) ingredients.stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .count();
    }


    @Override
    public ItemStack getResult(CraftingInput inventory) {
        return matches(inventory) ? result.get() : ItemStack.AIR;
    }

    public boolean matches(CraftingInput input) {
        if (input.getCount() != ingredientsCount) return false;

        for (Offset offset : offsets) {
            if (matchesAtOffset(input, offset.x(), offset.y())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAtOffset(CraftingInput input, int offsetX, int offsetY) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemStack stackInGrid = input.getItem(x + offsetX, y + offsetY);
                Ingredient requiredIngredient = ingredients.get(x + y * width);

                if (!requiredIngredient.is(stackInGrid) && requiredIngredient != Ingredient.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public record Offset(int x, int y) {
        public static Offset of(int x, int y) {
            return new Offset(x, y);
        }

        public static final List<Offset> DEFAULT = List.of(
                of(0, 0),
                of(1, 0),
                of(0, 1),
                of(1, 1)
        );
    }
}
