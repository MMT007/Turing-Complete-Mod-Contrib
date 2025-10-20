package name.turingcomplete.init;

import name.turingcomplete.TuringComplete;
import name.turingcomplete.recipe.TruthTableRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class recipeSerializersInit {
    public static final RecipeSerializer<TruthTableRecipe> TRUTH_TABLE_RECIPE_SERIALIZER = register("truth_table", new TruthTableRecipe.Serializer());

    private static <T extends Recipe<?>> RecipeSerializer<T> register(String id,RecipeSerializer<T> serializer){
        return Registry.register(Registries.RECIPE_SERIALIZER, TuringComplete.id(id), serializer);
    }

    public static void load(){
        TuringComplete.LOGGER.info("Recipe Serializers initialised...");
    }
}
