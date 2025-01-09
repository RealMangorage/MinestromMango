package org.mangorage.server.recipie;

import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Ingredient {
    public static final Ingredient EMPTY = of(ItemStack.AIR);

    public static Ingredient of(List<ItemStack> stacks) {
        return new Ingredient(List.copyOf(stacks));
    }

    public static Ingredient of(ItemStack... stacks) {
        return new Ingredient(List.of(stacks));
    }

    public static Ingredient of(Tag itemTag) {
        return new Ingredient(
                itemTag.getValues().stream()
                        .map(Material::fromNamespaceId)
                        .filter(Objects::nonNull)
                        .map(ItemStack::of)
                        .toList()
        );
    }


    private final List<ItemStack> stacks;

    private Ingredient(List<ItemStack> stacks) {
        this.stacks = List.copyOf(stacks);
    }

    public boolean is(ItemStack stack) {
        for (ItemStack itemStack : stacks) {
            if (itemStack.isSimilar(stack))
                return true;
        }
        return false;
    }

    public boolean hasAir() {
        for (ItemStack stack : stacks) {
            if (stack.isAir())
                return true;
        }

        return false;
    }

    public boolean equals(Object object) {
        if (object instanceof Ingredient ingredient) {
            return Arrays.equals(this.stacks.toArray(), ingredient.stacks.toArray());
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        return this == EMPTY || stacks.isEmpty();
    }
}
