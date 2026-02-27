package name.turingcomplete.screen;

import name.turingcomplete.data.recipe.TruthTableRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TruthTableOutputSlot extends Slot {
    private final TruthTableInventory inventory;
    private final PlayerEntity player;
    private int amount;

    public TruthTableOutputSlot(PlayerEntity player, TruthTableInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.player = player;
        this.inventory = inventory;
    }

    public boolean canInsert(ItemStack stack) {
        return false;
    }

    public ItemStack takeStack(int amount) {
        if (this.hasStack())
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

        this.inventory.unlockLastRecipe(this.player, this.inventory.getStacks());
    }

    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);

        TruthTableRecipe craft = this.inventory.getSelectedCraft();

        if (craft != null) {
            var logic_plates = this.inventory.getStack(0);
            var redstone_dusts = this.inventory.getStack(1);
            var redstone_torches = this.inventory.getStack(2);
            var upgrades = this.inventory.getStack(3);

            if (craft.depleteBuyItems(player.getWorld(),logic_plates, redstone_dusts, redstone_torches, upgrades)) {
                this.inventory.setStack(0, logic_plates);
                this.inventory.setStack(1, redstone_dusts);
                this.inventory.setStack(2, redstone_torches);
                this.inventory.setStack(3, upgrades);
            }
        }

    }
}
