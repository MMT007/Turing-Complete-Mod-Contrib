package name.turingcomplete.screen;

import name.turingcomplete.init.blockInit;
import name.turingcomplete.init.screenHandlerInit;
import name.turingcomplete.screen.data.GateCraftingRecipeList;
import name.turingcomplete.screen.slot.TruthTableOutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class TruthTableScreenHandler extends ScreenHandler {
    private final TruthTableInventory inventory;

    public static ScreenHandlerType<TruthTableScreenHandler> TYPE;
    // List of rules for each input slot for the GUI. Items listed here are allowed in the slots.
    // Example to add multiple items to one slot: stack -> stack.isOf(Items.item1) || stack.isOf(Items.item2)
    private static final List<Predicate<ItemStack>> INPUT_RULES = List.of(
            stack -> stack.isOf(blockInit.LOGIC_BASE_PLATE_BLOCK.asItem()),
            stack -> stack.isOf(Items.REDSTONE),
            stack -> stack.isOf(Items.REDSTONE_TORCH),
            stack -> false
    );
    public static final List<Point> SLOT_POSITIONS = List.of(
            new Point(162, 30),  // slot 0 - top-left
            new Point(180, 21),  // slot 1 - center
            new Point(198, 30),  // slot 2 - top-right
            new Point(180, 39)   // slot 3 - bottom-center
    );

    //===============================================================================================

    public TruthTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1));
    }

    public TruthTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory result) {
        super(screenHandlerInit.TRUTH_TABLE, syncId);
        this.inventory = new TruthTableInventory();

        // Input slots for crafting
        for (int i = 0; i < INPUT_RULES.size(); i++){
            Predicate<ItemStack> rule = INPUT_RULES.get(i);
            Point pos = SLOT_POSITIONS.get(i);
            int startX = pos.x;
            int startY = pos.y;

            this.addSlot(new Slot(inventory, i, startX, startY){
                @Override
                public boolean canInsert(ItemStack stack){
                    return rule.test(stack);
                }
            });
        }
        
        // Output slot
        this.addSlot(new TruthTableOutputSlot(playerInventory.player, inventory, 4, 261, 30));

        // Player inventory
        int invOffsetX = 144, invOffsetY = 74;
        for (int row = 0; row < 3; ++row){
            for (int col = 0; col < 9; ++col){
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invOffsetX + col * 18, invOffsetY + row * 18));
            }
        }

        // Hotbar
        int hotbarOffsetX = 144, hotbarOffsetY = 132;
        for (int col = 0; col < 9; ++col){
            this.addSlot(new Slot(playerInventory, col, hotbarOffsetX + col * 18, hotbarOffsetY));
        }
    }

    //===============================================================================================

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public Inventory getInventory(){
        return inventory;
    }

    public void setOfferIndex(int index) {
        this.inventory.setRecipeIndex(index);
    }

    public GateCraftingRecipeList getRecipes() {return this.inventory.getRecipes();}

    //===============================================================================================

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        // Only do this on the server side
        if (!player.getWorld().isClient) {
            // Loop through your input slots
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.removeStack(i);
                if (!stack.isEmpty()) {
                    // Drop it at the player's feet
                    player.dropItem(stack, false);
                }
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack moved = ItemStack.EMPTY;
        Slot selectedSlot = this.slots.get(slot);
        if (!selectedSlot.hasStack()) return moved;

        ItemStack original = selectedSlot.getStack();
        moved = original.copy();

        final int INPUT_START = 0;
        final int INPUT_END   = 4;   // exclusive: slots 0–3 are inputs
        final int RESULT_SLOT = 4;   // slot 4 is the crafting result
        final int PLAYER_START= 5;   // slot 5 is first player-inventory slot
        final int PLAYER_END  = PLAYER_START + 27 + 9; // 27 inv + 9 hotbar = 42 total

        if (slot == RESULT_SLOT) {
            // shift-clicking the result: move into player inventory
            if (!insertItem(original, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
            selectedSlot.onQuickTransfer(original, moved);
        }
        else if (slot >= PLAYER_START) {
            // shift-clicking from the player inv/hotbar: try to put into the 5 input slots
            if (!insertItem(original, INPUT_START, INPUT_END, false)) {
                return ItemStack.EMPTY;
            }
        }
        else {
            // shift-clicking from one of the 5 inputs: move back to player inv
            if (!insertItem(original, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        // cleanup empty slots
        if (original.isEmpty()) {
            selectedSlot.setStack(ItemStack.EMPTY);
        } else {
            selectedSlot.markDirty();
        }

        return moved;
    }

    //===============================================================================================

    public void onContentChanged(Inventory inventory) {
        this.inventory.updateOffers();
        super.onContentChanged(inventory);
    }
}
