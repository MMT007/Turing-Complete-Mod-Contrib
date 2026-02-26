package name.turingcomplete.screen;

import name.turingcomplete.data.recipe.TruthTableRecipe;
import name.turingcomplete.init.RecipeTypesInit;
import name.turingcomplete.data.recipe.TruthTableCategory;
import name.turingcomplete.data.recipe.TruthTableCrafts;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TruthTableInventory implements Inventory, RecipeUnlocker {
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(5,ItemStack.EMPTY);
    private final PlayerEntity player;
    public final TruthTableCrafts crafts;
    private RecipeEntry<?> lastRecipe;

    private TruthTableCategory selectedCategory;
    @Nullable private TruthTableRecipe selectedCraft;
    private int selectedCraftIndex;

    public TruthTableInventory(PlayerEntity player) {
        this.player = player;
        this.crafts = getRecipes();
    }

    public int size() {
        return 5;
    }

    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }
    public List<ItemStack> getStacks() {return this.stacks;}

    public void setLastRecipe(@Nullable RecipeEntry<?> recipe) {this.lastRecipe = recipe;}

    public @Nullable RecipeEntry<?> getLastRecipe() {return lastRecipe;}

    public void setSelectedCategory(TruthTableCategory category){
        this.selectedCategory = category;
    }

    public void setSelectedCraftIndex(int index){
        this.selectedCraftIndex = index;
    }

    public @Nullable TruthTableRecipe getSelectedCraft(){return this.selectedCraft;}

    public boolean isEmpty(){return this.isEmpty(true);}
    public boolean isEmpty(boolean with_output) {
        for (ItemStack stack : stacks) {
            if (stacks.indexOf(stack) == stacks.size() - 1 && !with_output) continue;
            if (!stack.isEmpty()) return false;
        }

        return true;
    }

    private boolean needsCraftsUpdate(int slot) {return slot >=0 && slot <= 3;}
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));

        if (this.needsCraftsUpdate(slot))
            this.updateCrafts();

    }

    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = getStack(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result;

        if (stack.getCount() <= amount){
            result = stack;
            stacks.set(slot, ItemStack.EMPTY);

        } else result = stack.split(amount);

        if (this.needsCraftsUpdate(slot))
            this.updateCrafts();

        return result;
    }

    public ItemStack removeStack(int slot) {
        ItemStack result = stacks.get(slot);
        stacks.set(slot, ItemStack.EMPTY);

        if (this.needsCraftsUpdate(slot))
            this.updateCrafts();

        return result;
    }

    public void clear() {stacks.clear();}


    public void updateCrafts() {
        this.selectedCraft = null;

        var logic_plate = stacks.get(0);
        var redstone_dusts = stacks.get(1);
        var redstone_torches = stacks.get(2);
        var upgrades = stacks.get(3);

        var craftList = crafts;

        if (!craftList.isEmpty()) {
            var craft = craftList.getValidCraft(
                this.player.getWorld(),
                logic_plate, redstone_dusts, redstone_torches, upgrades,
                selectedCategory, selectedCraftIndex
            );

            if (craft != null) {
                this.selectedCraft = craft;
                this.setStack(4, craft.getOutput().copy());

            } else this.setStack(4, ItemStack.EMPTY);

        }
    }

    public void markDirty() {updateCrafts();}

    public boolean canPlayerUse(PlayerEntity player) {return true;}

    private TruthTableCrafts getRecipes(){
        if (player instanceof ServerPlayerEntity serverPlayer){
            if (serverPlayer.getServer() == null) return new TruthTableCrafts();

            var manager = serverPlayer.getServer().getRecipeManager();
            return TruthTableCrafts.fromRecipes(manager.listAllOfType(RecipeTypesInit.TRUTH_TABLE_RECIPE_TYPE));
        }

        return new TruthTableCrafts();
    }
}
