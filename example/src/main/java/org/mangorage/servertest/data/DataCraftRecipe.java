package org.mangorage.servertest.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.recipie.Ingredient;
import org.mangorage.server.recipie.ShapedRecipe;
import org.mangorage.server.recipie.ShapelessCraftingRecipe;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DataCraftRecipe {
    private static JsonElement getElement(Path path, Gson gson) {
        try {
            return gson.fromJson(
                    new JsonReader(
                            new FileReader(path.toFile())
                    ),
                    JsonElement.class
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getRecipes(Path path, MangoServer server) {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try {
            List<JsonElement> list = Files.list(path).toList()
                    .stream()
                    .map(p -> getElement(p, gson))
                    .toList();

            list.forEach(entry -> {
                if (entry.getAsJsonObject().get("type").getAsString().contains("minecraft:crafting_shapeless")) {
                    System.out.println(
                            gson.toJson(entry)
                    );
                    var shapeless = gson.fromJson(entry, Shapeless.class);
                    server.getCraftingRecipeManager().register(shapeless.create(server));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public record Shaped(
            String type,
            String category,
            String group,
            boolean show_notifications,
            String[] pattern,
            Char2ObjectOpenHashMap<String> key,
            Result result
    ) {
        public ShapedRecipe create() {
            return null;
        }
    }

    public record Shapeless(
            String type,
            String category,
            String group,
            ArrayList<Object> ingredients,
            Result result
    ) {
        public ShapelessCraftingRecipe create(MangoServer server) {
            List<Ingredient> ingredientList = new ArrayList<>();
            Material resultItem = Material.fromNamespaceId(result.id());

            for (Object object : ingredients) {
                if (object instanceof String s) {
                    if (s.startsWith("#")) {
                        ingredientList.add(
                                Ingredient.of(
                                        server.getServerProcess().tag().getTag(Tag.BasicType.ITEMS, s.substring(s.indexOf("#") + 1))
                                )
                        );
                    } else {
                        ingredientList.add(
                                Ingredient.of(
                                        ItemStack.of(Material.fromNamespaceId(s))
                                )
                        );
                    }
                } else if (object instanceof List list) {
                    List<String> stringList = list;

                    var items = stringList.stream()
                            .map(str -> ItemStack.of(Material.fromNamespaceId(str)))
                            .toList();

                    ingredientList.add(
                            Ingredient.of(items)
                    );
                }
            }

            return new ShapelessCraftingRecipe(
                    List.copyOf(ingredientList),
                    () -> ItemStack.of(resultItem, result.count()),
                    ingredients().size() <= 4 ? 2 : 3,
                    ingredients().size() <= 4 ? 2 : 3
            );
        }
    }
}
