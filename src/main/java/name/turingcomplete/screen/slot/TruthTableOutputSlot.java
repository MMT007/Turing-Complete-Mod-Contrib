package name.turingcomplete.screen.slot;

import name.turingcomplete.screen.TruthTableInventory;
import name.turingcomplete.screen.data.GateCraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TruthTableOutputSlot extends Slot {
    private final PlayerEntity player;
    private final TruthTableInventory truthTableInventory;
    private int amount;

    public TruthTableOutputSlot(PlayerEntity player, TruthTableInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
        this.truthTableInventory = inventory;
    }

    public boolean canInsert(ItemStack stack) {return false;}

    public ItemStack takeStack(int amount) {
        if ( this.hasStack() )
            this.amount += Math.min(amount, this.getStack().getCount());

        return super.takeStack(amount);
    }

    protected void onCrafted(ItemStack stack, int amount) {
        this.amount += amount;
        this.onCrafted(stack);
    }

    protected void onCrafted(ItemStack stack) {
        stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
        this.amount = 0;
    }

    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);

        GateCraftingRecipe gateCraftingRecipe = this.truthTableInventory.getGateCraftingOffer();
        if (gateCraftingRecipe == null) return;

        ItemStack base_item_input = this.truthTableInventory.getStack(0);
        ItemStack redstone_item_input = this.truthTableInventory.getStack(1);
        ItemStack redstone_torch_item_input = this.truthTableInventory.getStack(2);
        ItemStack extender_module_item_input = this.truthTableInventory.getStack(3);

        if (gateCraftingRecipe.depleteBuyItems(base_item_input,redstone_item_input,redstone_torch_item_input,extender_module_item_input)) {
            // NOTE: Triggers VillagerTradeCriterion.class On the ServerPlayerEntity

            this.truthTableInventory.setStack(0,base_item_input);
            this.truthTableInventory.setStack(1,redstone_item_input);
            this.truthTableInventory.setStack(2,redstone_torch_item_input);
            this.truthTableInventory.setStack(3,extender_module_item_input);
        }

    }
}
