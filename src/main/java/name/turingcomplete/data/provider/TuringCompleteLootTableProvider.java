package name.turingcomplete.data.provider;

import name.turingcomplete.blocks.multiblock.Adder;
import name.turingcomplete.init.BlockInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;

import java.util.concurrent.CompletableFuture;

public class TuringCompleteLootTableProvider extends FabricBlockLootTableProvider {
    public TuringCompleteLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(dataOutput,completableFuture);
    }

    @Override
    public void generate() {
        addDrop(BlockInit.LOGIC_BASE_PLATE_BLOCK, drops(BlockInit.LOGIC_BASE_PLATE_BLOCK));

        addDrop(BlockInit.NAND_GATE, drops(BlockInit.NAND_GATE));
        addDrop(BlockInit.AND_GATE, drops(BlockInit.AND_GATE));
        addDrop(BlockInit.NOR_GATE, drops(BlockInit.NOR_GATE));
        addDrop(BlockInit.NOT_GATE, drops(BlockInit.NOT_GATE));
        addDrop(BlockInit.OR_GATE, drops(BlockInit.OR_GATE));
        addDrop(BlockInit.THREE_AND_GATE, drops(BlockInit.THREE_AND_GATE));
        addDrop(BlockInit.THREE_OR_GATE, drops(BlockInit.THREE_OR_GATE));
        addDrop(BlockInit.XNOR_GATE, drops(BlockInit.XNOR_GATE));
        addDrop(BlockInit.XOR_GATE, drops(BlockInit.XOR_GATE));

        addDrop(BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK, drops(BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK));
        addDrop(BlockInit.OMNI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK, drops(BlockInit.OMNI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK));

        addDrop(BlockInit.PULSE_EXTENDER_BLOCK, drops(BlockInit.PULSE_EXTENDER_BLOCK));

        addDrop(BlockInit.SWITCH_GATE, drops(BlockInit.SWITCH_GATE));
        addDrop(BlockInit.SR_LATCH_BLOCK, drops(BlockInit.SR_LATCH_BLOCK));
        addDrop(BlockInit.JK_LATCH_BLOCK, drops(BlockInit.JK_LATCH_BLOCK));
        addDrop(BlockInit.T_LATCH_BLOCK, drops(BlockInit.T_LATCH_BLOCK));
        addDrop(BlockInit.MEMORY_CELL, drops(BlockInit.MEMORY_CELL));

        addDrop(BlockInit.HALF_ADDER, drop_if_property(BlockInit.HALF_ADDER,Adder.PART,Adder.AdderPart.MIDDLE));
        addDrop(BlockInit.FULL_ADDER, drop_if_property(BlockInit.FULL_ADDER,Adder.PART,Adder.AdderPart.MIDDLE));
    }

    private <T extends Comparable<T> & StringIdentifiable> LootTable.Builder drop_if_property(Block block, Property<T> property, T value ){
         LootCondition.Builder condition = new BlockStatePropertyLootCondition.Builder(block)
                .properties(StatePredicate.Builder.create().exactMatch(property,value));
        LootPoolEntry.Builder<?> entry = ItemEntry.builder(block);

        return LootTable.builder().pool(
                addSurvivesExplosionCondition(block,LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)))
                        .conditionally(condition)
                        .with(entry)
        );
    }
}

