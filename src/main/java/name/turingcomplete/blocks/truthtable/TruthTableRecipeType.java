package name.turingcomplete.blocks.truthtable;

import net.minecraft.recipe.RecipeType;

public class TruthTableRecipeType implements RecipeType<TruthTableRecipe> {
    public static final TruthTableRecipe INSTANCE = new TruthTableRecipeType();
    public static final String ID = "truth_table";

    private TruthTableRecipeType() {}
}
