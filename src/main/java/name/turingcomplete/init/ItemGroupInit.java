package name.turingcomplete.init;

import name.turingcomplete.TuringComplete;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ItemGroupInit {

    private static final Text TITLE = Text.translatable("itemGroup." + TuringComplete.MOD_ID + ".group");

    public static final ItemGroup TURING_GROUP = register("turing_group", FabricItemGroup.builder()
            .icon(() -> new ItemStack(BlockInit.AND_GATE))
            .displayName(TITLE)
            .entries((context, entries) -> {
                entries.add(BlockInit.LOGIC_BASE_PLATE_BLOCK);
                entries.add(BlockInit.NAND_GATE);
                entries.add(BlockInit.NOT_GATE);
                entries.add(BlockInit.OR_GATE);
                entries.add(BlockInit.AND_GATE);
                entries.add(BlockInit.NOR_GATE);
                entries.add(BlockInit.XOR_GATE);
                entries.add(BlockInit.XNOR_GATE);
                entries.add(BlockInit.THREE_AND_GATE);
                entries.add(BlockInit.THREE_OR_GATE);
                entries.add(BlockInit.SWITCH_GATE);
                entries.add(BlockInit.MEMORY_CELL);
                entries.add(BlockInit.HALF_ADDER);
                entries.add(BlockInit.FULL_ADDER);
                entries.add(BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK);
                entries.add(BlockInit.OMNI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK);
                entries.add(BlockInit.PULSE_EXTENDER_BLOCK);
                entries.add(BlockInit.SR_LATCH_BLOCK);
                entries.add(BlockInit.JK_LATCH_BLOCK);
                entries.add(BlockInit.T_LATCH_BLOCK);
                entries.add(BlockInit.TRUTH_TABLE);
            })
            .build());


    public static <T extends ItemGroup> T register(String name, T itemGroup){
        return  Registry.register(Registries.ITEM_GROUP, TuringComplete.id(name), itemGroup);
    }

    public static void load(){
        TuringComplete.LOGGER.info("Item Group initialised...");
    }
}
