package name.turingcomplete.truthtable;

import name.turingcomplete.init.RecipeTypesInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TruthTableInventory implements Inventory {
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(5,ItemStack.EMPTY);
    private final PlayerEntity player;
    @Nullable private TruthTableCraft selectedCraft;
    private int selectedCraftIndex;

    public TruthTableInventory(PlayerEntity player) {
        this.player = player;
    }

    public int size() {
        return 5;
    }

    public List<ItemStack> getHeldStacks() {
        return stacks;
    }
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    public void setSelectedCraftIndex(int index){
        this.selectedCraftIndex = index;
        updateCrafts();
    }
    public @Nullable TruthTableCraft getSelectedCraft(){return this.selectedCraft;}

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

        var craftList = getRecipes();

        if (!craftList.isEmpty()) {
            var craft = craftList.getValidCraft(logic_plate,redstone_dusts,redstone_torches,upgrades, selectedCraftIndex);

            if (craft != null) {
                this.selectedCraft = craft;
                this.setStack(4, craft.copySellItem());

            } else this.setStack(4, ItemStack.EMPTY);

        }
    }

    public void markDirty() {updateCrafts();}

    public boolean canPlayerUse(PlayerEntity player) {return true;}

    // TODO: Get Player's Unlocked Recipes
    public TruthTableCraftList getRecipes(){
        if (player instanceof ServerPlayerEntity serverPlayer){
            var book = serverPlayer.getRecipeBook();
            if (serverPlayer.getServer() == null) return new TruthTableCraftList();

            var manager = serverPlayer.getServer().getRecipeManager();
            return TruthTableCraftList.fromRecipes(manager.listAllOfType(RecipeTypesInit.TRUTH_TABLE_RECIPE_TYPE));
        }

        return new TruthTableCraftList();
    }

}
