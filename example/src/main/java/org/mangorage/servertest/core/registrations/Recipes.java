package org.mangorage.servertest.core.registrations;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.mangorage.server.recipie.CraftingRecipe;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.server.recipie.Ingredient;
import org.mangorage.server.recipie.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public final class Recipes {
    public static void register(CraftingRecipeManager manager) {


        manager.register(
                new ShapedRecipe(
                        2, 2,
                        List.of(
                                Ingredient.of(ItemStack.of(Material.OAK_PLANKS)),            Ingredient.of(ItemStack.of(Material.OAK_PLANKS)),
                                Ingredient.of(ItemStack.of(Material.OAK_PLANKS)),            Ingredient.of(ItemStack.of(Material.OAK_PLANKS))
                        ),
                        () -> ItemStack.of(Material.CRAFTING_TABLE),
                        ShapedRecipe.Offset.DEFAULT
                )
        );

        manager.register(
                new ShapedRecipe(
                        3, 3,
                        List.of(
                                Ingredient.of(ItemStack.of(Material.OAK_PLANKS)),            Ingredient.of(ItemStack.of(Material.OAK_PLANKS)), Ingredient.EMPTY,
                                Ingredient.of(ItemStack.of(Material.OAK_PLANKS)),            Ingredient.of(ItemStack.of(Material.OAK_PLANKS)), Ingredient.EMPTY,
                                Ingredient.of(ItemStack.of(Material.OAK_PLANKS)),            Ingredient.of(ItemStack.of(Material.OAK_PLANKS)), Ingredient.EMPTY
                        ),
                        () -> ItemStack.of(Material.OAK_DOOR, 3),
                        List.of(
                                new ShapedRecipe.Offset(0, 0),
                                new ShapedRecipe.Offset(1, 0)
                        )
                )
        );

        manager.register(
                new ShapedRecipe(
                        3, 3,
                        List.of(
                                Ingredient.of(ItemStack.of(Material.OAK_PLANKS)), Ingredient.of(ItemStack.of(Material.OAK_PLANKS)), Ingredient.of(ItemStack.of(Material.OAK_PLANKS))
                        ),
                        () -> ItemStack.of(Material.OAK_SLAB, 6),
                        List.of(
                                new ShapedRecipe.Offset(0, 0),
                                new ShapedRecipe.Offset(0, 1),
                                new ShapedRecipe.Offset(0, 2)
                        )
                )
        );
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
