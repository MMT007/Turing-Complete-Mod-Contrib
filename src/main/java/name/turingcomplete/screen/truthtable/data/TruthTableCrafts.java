package name.turingcomplete.screen.truthtable.data;

import name.turingcomplete.data.recipe.TruthTableRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TruthTableCrafts extends HashMap<TruthTableCategory, ArrayList<TruthTableCraft>> {
    public TruthTableCrafts() {}

    @Nullable
    public TruthTableCraft getValidCraft(
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torch,
        ItemStack upgrade,
        TruthTableCategory category,
        int index
    ) {
        if (containsKey(category)) {
            var craft_list = get(category);
            if (index > 0 && index < craft_list.size()) {
                var craft = craft_list.get(index);
                return craft.matchesCraft(logic_plate, redstone, redstone_torch, upgrade) ? craft : null;
            }

            for (var craft : craft_list)
                if (craft.matchesCraft(logic_plate, redstone, redstone_torch, upgrade))
                    return craft;
        }
        return null;
    }

    public static TruthTableCrafts fromRecipes(List<RecipeEntry<TruthTableRecipe>> recipeEntries) {
        var crafts = new TruthTableCrafts();

        for(var entry : recipeEntries){
            var recipe = entry.value();
            for (var category : recipe.getCategories()){
                var craft_list = crafts.computeIfAbsent(category, c -> new ArrayList<>());
                craft_list.add(new TruthTableCraft(
                    recipe.getLogicPlateRequired(),
                    recipe.getRedstoneRequired(),
                    recipe.getRedstoneTorchesRequired(),
                    recipe.getUpgrade(),
                    recipe.getOutput()
                ));
            }
        }

        return crafts;
    }
}
