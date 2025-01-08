package org.mangorage.server.recipie;

import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import java.util.List;
import java.util.Objects;

public final class Ingredient {

    public static Ingredient of(List<ItemStack> stacks) {
        return new Ingredient(stacks);
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
}
