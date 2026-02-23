package name.turingcomplete.init;

import name.turingcomplete.TuringComplete;
import name.turingcomplete.data.recipe.TruthTableRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class RecipeTypesInit {
    public static final RecipeType<TruthTableRecipe> TRUTH_TABLE_RECIPE_TYPE = register("truth_table");

    private static <T extends Recipe<?>> RecipeType<T> register(String id){
        return Registry.register(Registries.RECIPE_TYPE, TuringComplete.id(id), new RecipeType<T>() {
            public String toString() {return id;}
        });
    }

    public static void load(){
        TuringComplete.LOGGER.info("Recipe Types initialised...");
    }
}
