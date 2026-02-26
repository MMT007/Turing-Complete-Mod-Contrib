package name.turingcomplete.screen;

import name.turingcomplete.init.BlockInit;
import name.turingcomplete.init.ItemTagsInit;
import name.turingcomplete.init.ScreenHandlerInit;
import name.turingcomplete.data.recipe.TruthTableCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class TruthTableScreenHandler extends ScreenHandler {
    private static final List<Predicate<ItemStack>> INPUT_RULES = List.of(
            stack -> stack.isOf(BlockInit.LOGIC_BASE_PLATE_BLOCK.asItem()),
            stack -> stack.isOf(Items.REDSTONE),
            stack -> stack.isOf(Items.REDSTONE_TORCH),
            stack -> stack.isIn(ItemTagsInit.FITS_ON_UPGRADE_SLOT)
    );
    public static final List<Point> SLOT_POSITIONS = List.of(
            new Point(144, 30),  // slot 0 - top-left
            new Point(162, 21),  // slot 1 - center
            new Point(180, 30),  // slot 2 - top-right
            new Point(162, 39)   // slot 3 - bottom-center
    );
    private static final int TABLE_INV_START = 0;
    private static final int TABLE_INV_END = 4;
    private static final int PLAYER_INV_START_INDEX = TABLE_INV_END+1;
    private static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36;

    private final TruthTableInventory inventory;

    public TruthTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new TruthTableInventory(playerInventory.player));
    }

    public TruthTableScreenHandler(int syncId, PlayerInventory playerInventory, TruthTableInventory inventory) {
        super(ScreenHandlerInit.TRUTH_TABLE, syncId);
        this.inventory = inventory;

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
        this.addSlot(new TruthTableOutputSlot(playerInventory.player, inventory, 4, 243, 30));

        // Player inventory
        int invOffsetX = 126;
        int invOffsetY = 74;

        for (int row = 0; row < 3; ++row){
            for (int col = 0; col < 9; ++col){
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invOffsetX + col * 18, invOffsetY + row * 18));
            }
        }

        // Hotbar
        int hotbarOffsetY = 132;
        for (int col = 0; col < 9; ++col){
            this.addSlot(new Slot(playerInventory, col, invOffsetX + col * 18, hotbarOffsetY));
        }
    }

    private void dropItem(ServerPlayerEntity player, int slot){
        var itemStack = this.inventory.removeStack(slot);

        if (!itemStack.isEmpty())
            player.dropItem(itemStack, false);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (!player.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer) {

            if (!player.isAlive() || serverPlayer.isDisconnected()) {
                dropItem(serverPlayer,0);
                dropItem(serverPlayer,1);
                dropItem(serverPlayer,2);
                dropItem(serverPlayer,3);
            } else {
                player.getInventory().offerOrDrop(this.inventory.removeStack(0));
                player.getInventory().offerOrDrop(this.inventory.removeStack(1));
                player.getInventory().offerOrDrop(this.inventory.removeStack(2));
                player.getInventory().offerOrDrop(this.inventory.removeStack(3));
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot_index) {
        var moved = ItemStack.EMPTY;
        var slot = this.slots.get(slot_index);

        if (slot.hasStack()) {
            var slot_stack = slot.getStack();
            moved = slot_stack.copy();

            // From Output
            if (slot_index == TABLE_INV_END) {
                var moved_tiem = this.insertItem(slot_stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, true) ;

                if (!moved_tiem)
                    return ItemStack.EMPTY;

                slot.onQuickTransfer(slot_stack, moved);

            // From Inputs
            } else if (slot_index >= PLAYER_INV_START_INDEX) {
                if (!this.insertItem(slot_stack, TABLE_INV_START, TABLE_INV_END, false))
                    return ItemStack.EMPTY;

            // From Inv
            } else if (!this.insertItem(slot_stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                return ItemStack.EMPTY;


            if (slot_stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();

            if (slot_stack.getCount() == moved.getCount())
                return ItemStack.EMPTY;


            slot.onTakeItem(player, slot_stack);
        }

        return moved;
    }

    public boolean canUse(PlayerEntity player) {return inventory.canPlayerUse(player);}

    public void setSelectedCategory(TruthTableCategory category) {this.inventory.setSelectedCategory(category);}
    public void setCraftIndex(int index) {this.inventory.setSelectedCraftIndex(index);}
    public void updateInventory(){this.inventory.updateCrafts();}

    public void switchTo(TruthTableCategory category, int craftIndex) {
        if (this.inventory.crafts.containsKey(category)) {
            var craft_list = this.inventory.crafts.get(category);
            if (craftIndex >= 0 && craft_list.size() > craftIndex) {
                insertItem(this.inventory.getStack(0), 0);
                insertItem(this.inventory.getStack(1), 1);
                insertItem(this.inventory.getStack(2), 2);
                insertItem(this.inventory.getStack(4), 4);

                if (this.inventory.isEmpty(false)) {
                    var craft = craft_list.get(craftIndex);

                    this.autofill(0, craft.getLogicPlate());
                    this.autofill(1, craft.getRedstone());
                    this.autofill(2, craft.getRedstoneTorch());
                    if (!craft.getUpgrade().isEmpty())
                        this.autofill(3, craft.getUpgrade());
                }

            }
        }
    }

    private void autofill(int slot, ItemStack stack) {
        for(int i = PLAYER_INV_START_INDEX; i < PLAYER_INV_END_INDEX; ++i) {
            var stack_in_slot = this.slots.get(i).getStack();

            if (!stack_in_slot.isEmpty() && stack.isOf(stack_in_slot.getItem())) {
                var stack_in_menu = this.inventory.getStack(slot);

                if (stack_in_menu.isEmpty() || ItemStack.areItemsAndComponentsEqual(stack_in_slot, stack_in_menu)) {
                    int SIS_max_count = stack_in_slot.getMaxCount();
                    int move_amount = Math.min(SIS_max_count - stack_in_menu.getCount(), stack_in_slot.getCount());

                    var new_stack_in_menu = stack_in_slot.copyWithCount(stack_in_menu.getCount() + move_amount);
                    stack_in_slot.decrement(move_amount);

                    this.inventory.setStack(slot, new_stack_in_menu);
                    if (new_stack_in_menu.getCount() >= SIS_max_count)
                        break;
                }
            }
        }

    }

    private void insertItem(ItemStack itemStack, int index){
        if (!itemStack.isEmpty()) {
            if (!this.insertItem(itemStack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, true))
                return;

            this.inventory.setStack(index, itemStack);
        }
    }
}
