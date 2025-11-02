package name.turingcomplete.screen;

import name.turingcomplete.init.blockInit;
import name.turingcomplete.screen.data.GateCraftingRecipe;
import name.turingcomplete.screen.data.GateCraftingRecipeList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.Optional;


public class TruthTableInventory implements Inventory {

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5,ItemStack.EMPTY);
    private GateCraftingRecipe gateCraftingRecipe;
    private int recipeIndex;

    //===============================================================================================

    public int size() {
        return 5;
    }
    public void markDirty() {this.updateOffers();}
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }
    public void clear() {
        items.clear();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items){
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = getStack(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result;
        if (stack.getCount() <= amount){
            result = stack;
            items.set(slot, ItemStack.EMPTY);
        }
        else{
            result = stack.split(amount);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));

        if (this.needsRecipeUpdate(slot)) this.updateOffers();
    }

    public void setRecipeIndex(int index) {
        this.recipeIndex = index;
        this.updateOffers();
    }

    private boolean needsRecipeUpdate(int slot) {return slot != 4;}

    //===============================================================================================

    public GateCraftingRecipe getGateCraftingOffer(){ return this.gateCraftingRecipe; }

    // TODO: Return The Recipes The Player Has Unlocked
    GateCraftingRecipeList getRecipes(){
        GateCraftingRecipeList recipes = new GateCraftingRecipeList();
        recipes.add(new GateCraftingRecipe(
                1,1,1, Optional.empty(),
                new ItemStack(blockInit.AND_GATE)
        ));
        return recipes;
    }

    //===============================================================================================

    public void updateOffers() {
        this.gateCraftingRecipe = null;

        // check if the all slots are empty
        // if it is, there are no offers selected
        // then set the output slot to empty and return
        if (this.isEmpty()) {
            this.setStack(4,ItemStack.EMPTY);
            return;
        }

        // check if there are offers
        // if there is, get the offer list and then get a valid offer
        GateCraftingRecipeList gateCraftingOffers = this.getRecipes();
        GateCraftingRecipe craftingOffer = gateCraftingOffers.getValidOffer(
                this.recipeIndex,
                this.getStack(0), this.getStack(1),
                this.getStack(2), this.getStack(3)
        );

        // if there is a valid offer, set as the current offer and set the output item
        // else set the output slot to empty
        if (craftingOffer != null){
            this.gateCraftingRecipe = craftingOffer;
            this.setStack(4, craftingOffer.copyOutputItem());
        } else this.setStack(4, ItemStack.EMPTY);

    }
}
