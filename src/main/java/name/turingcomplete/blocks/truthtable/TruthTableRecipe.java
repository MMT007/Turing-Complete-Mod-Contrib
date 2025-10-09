package name.turingcomplete.blocks.truthtable;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TruthTableRecipe implements Recipe<Inventory> {
    private final Identifier id;
    private final ItemStack output;
    private final ItemStack input1;
    private final ItemStack input2;
    private final ItemStack input3;
    private final ItemStack input4;

    public TruthTableRecipe(Identifier id, ItemStack output, ItemStack in1, ItemStack in2, ItemStack in3, ItemStack in4){
        this.id = id;
        this.output = output;
        this.input1 = in1;
        this.input2 = in2;
        this.input3 = in3;
        this.input4 = in4;
    }

    public ItemStack getInput1() { return input1; }
    public ItemStack getInput2() { return input2; }
    public ItemStack getInput3() { return input3; }
    public ItemStack getInput4() { return input4; }

    @Override
    public boolean matches(Inventory input, World world) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory input, RegistryWrapper.WrapperLookup lookup) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TruthTableSeralizer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return (RecipeType<?>) TruthTableRecipeType.INSTANCE;
    }
}
