package org.mangorage.servertest.core.registrations;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.utils.NamespaceID;
import org.mangorage.server.recipie.CraftingRecipe;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.server.recipie.ShapedCraftingRecipe;
import org.mangorage.server.recipie.ShapelessCraftingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Recipes {
    public static void register(CraftingRecipeManager manager) {
        manager.register(
                ShapedCraftingRecipe.create()
                        .setIngredient(Material.IRON_INGOT).setIngredient(Material.IRON_INGOT).setIngredient(Material.IRON_INGOT)
                        .skip().setIngredient(Material.STICK).skip()
                        .skip().setIngredient(Material.STICK).skip()
                        .build(() -> ItemStack.of(Material.IRON_PICKAXE))
        );

        manager.register(
                ShapedCraftingRecipe.create()
                        .setIngredient(Material.OAK_PLANKS).setIngredient(Material.OAK_PLANKS)
                        .setIngredient(Material.OAK_PLANKS).setIngredient(Material.OAK_PLANKS)
                        .build(() -> ItemStack.of(Material.CRAFTING_TABLE))
        );

        List<RecipeGenerator<? extends BasicGenerator>> generators = new ArrayList<>();

        generators.add(new RecipeGenerator<>(
                (material, id) -> manager.createShapeless(
                        Material.fromNamespaceId(
                                id.asString()
                                        .replace("_log", "_planks")
                                        .replace("stripped_", "")
                        ),
                        4,
                        material
                ),
                (material, id) -> id.path().contains("_log")
        ));

        Material.values().stream()
                .forEach(material -> {
                    generators.forEach(generator -> {
                        var recipe = generator.create(material, material.namespace());
                        if (recipe != null) {
                            manager.register(recipe);
                        }
                    });
                });
    }

    public static class RecipeGenerator<T extends BasicGenerator> {
        private final T generator;
        private final BiPredicate<Material, NamespaceID> predicate;

        public RecipeGenerator(T generator, BiPredicate<Material, NamespaceID> predicate) {
            this.generator = generator;
            this.predicate = predicate;
        }

        public CraftingRecipe create(Material material, NamespaceID id) {
            if (predicate.test(material, id)) {
                return generator.create(material, id);
            }
            return null;
        }
    }

    public interface BasicGenerator {
        CraftingRecipe create(Material input, NamespaceID id);
    }

}
