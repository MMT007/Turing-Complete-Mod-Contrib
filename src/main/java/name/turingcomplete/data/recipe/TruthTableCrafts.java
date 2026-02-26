package name.turingcomplete.data.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TruthTableCrafts extends HashMap<TruthTableCategory, ArrayList<TruthTableRecipe>> {
    private final HashMap<TruthTableRecipe, RecipeEntry<TruthTableRecipe>> entries = new HashMap<>();

    public TruthTableCrafts() {}

    @Nullable
    public TruthTableRecipe getValidCraft(
        World world,
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
                return craft.matches(world, logic_plate, redstone, redstone_torch, upgrade) ? craft : null;
            }

            for (var craft : craft_list)
                if (craft.matches(world, logic_plate, redstone, redstone_torch, upgrade))
                    return craft;
        }
        return null;
    }

    public RecipeEntry<TruthTableRecipe> entryOf(TruthTableRecipe recipe){
        return this.entries.get(recipe);
    }

    public static TruthTableCrafts fromRecipes(List<RecipeEntry<TruthTableRecipe>> recipeEntries) {
        var crafts = new TruthTableCrafts();

        for(var entry : recipeEntries){
            var recipe = entry.value();
            for (var category : recipe.getCategories()){
                var craft_list = crafts.computeIfAbsent(category, c -> new ArrayList<>());

                craft_list.add(recipe);
                crafts.entries.put(recipe, entry);
            }
        }

        for (var craft_lists : crafts.values())
            craft_lists.sort(Comparator.comparing(TruthTableRecipe::getOutputName));

        return crafts;
    }
}
