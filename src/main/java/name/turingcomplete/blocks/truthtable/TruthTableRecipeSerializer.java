package name.turingcomplete.blocks.truthtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TruthTableRecipeSerializer implements RecipeSerializer<TruthTableRecipe> {
    public static TruthTableRecipeSerializer INSTANCE = new TruthTableRecipeSerializer();
    public static final Identifier ID = Identifier.of("turingcomplete","truth_table");

    @Override
    public TruthTableRecipe read(Identifier recipeId, JsonObject json){
        List<ItemStack> inputs = new ArrayList<>();
        if (!json.has("inputs")){
            throw new JsonSyntaxException("Missing 'inputs' array for truth table recipe " + recipeId );
        }
        JsonArray inArray = json.getAsJsonArray("inputs");
        for (JsonElement elem : inArray){
            if (!elem.isJsonObject()){
                throw new JsonSyntaxException("Invalid element in 'inputs' array for " + recipeId);
            }
            JsonObject inObj = elem.getAsJsonObject();

            if (!inObj.has("item")){
                throw new JsonSyntaxException("Missing item in input for " + recipeId);
            }
        }

    }
}