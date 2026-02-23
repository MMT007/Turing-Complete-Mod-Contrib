package name.turingcomplete.data.provider;

import name.turingcomplete.data.recipe.TruthTableRecipeBuilder;
import name.turingcomplete.init.BlockInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class TuringCompleteRecipeProvider extends FabricRecipeProvider {
    public TuringCompleteRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, BlockInit.LOGIC_BASE_PLATE_BLOCK, 12)
                .input('E', Blocks.SMOOTH_STONE_SLAB)
                .pattern("   ")
                .pattern("   ")
                .pattern("EEE")
                .criterion(hasItem(Blocks.SMOOTH_STONE_SLAB),conditionsFromItem(Blocks.SMOOTH_STONE_SLAB))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.NAND_GATE, 3)
                .input('E', BlockInit.LOGIC_BASE_PLATE_BLOCK)
                .input('T', Blocks.REDSTONE_TORCH)
                .input('R', Blocks.REDSTONE_WIRE)
                .pattern(" R ")
                .pattern("TRT")
                .pattern("EEE")
                .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.NOT_GATE, 2)
                .input('N', BlockInit.NAND_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .pattern("RR ")
                .pattern("RN ")
                .pattern("RR ")
                .criterion(hasItem(BlockInit.NAND_GATE),conditionsFromItem(BlockInit.NAND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.AND_GATE, 1)
                .input('N', BlockInit.NAND_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .input('G', BlockInit.NOT_GATE)
                .pattern(" R ")
                .pattern(" NG")
                .pattern(" R ")
                .criterion(hasItem(BlockInit.NAND_GATE),conditionsFromItem(BlockInit.NAND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.OR_GATE, 1)
                .input('N', BlockInit.NAND_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .input('G', BlockInit.NOT_GATE)
                .pattern("GR ")
                .pattern(" N ")
                .pattern("GR ")
                .criterion(hasItem(BlockInit.NAND_GATE),conditionsFromItem(BlockInit.NAND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.NOR_GATE, 1)
                .input('N', BlockInit.NAND_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .input('G', BlockInit.NOT_GATE)
                .pattern("GR ")
                .pattern(" NG")
                .pattern("GR ")
                .criterion(hasItem(BlockInit.NAND_GATE),conditionsFromItem(BlockInit.NAND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.XOR_GATE, 1)
                .input('N', BlockInit.NAND_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .pattern(" N ")
                .pattern("NRN")
                .pattern(" N ")
                .criterion(hasItem(BlockInit.NAND_GATE),conditionsFromItem(BlockInit.NAND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.XNOR_GATE, 1)
                .input('X', BlockInit.XOR_GATE)
                .input('N', BlockInit.NOT_GATE)
                .pattern("   ")
                .pattern(" XN")
                .pattern("   ")
                .criterion(hasItem(BlockInit.XOR_GATE),conditionsFromItem(BlockInit.XOR_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.THREE_AND_GATE, 1)
                .input('A', BlockInit.AND_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .pattern("RRA")
                .pattern(" AR")
                .pattern("   ")
                .criterion(hasItem(BlockInit.AND_GATE),conditionsFromItem(BlockInit.AND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.THREE_OR_GATE, 1)
                .input('O', BlockInit.OR_GATE)
                .input('R', Blocks.REDSTONE_WIRE)
                .pattern("RRO")
                .pattern(" OR")
                .pattern("   ")
                .criterion(hasItem(BlockInit.OR_GATE),conditionsFromItem(BlockInit.OR_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.SWITCH_GATE, 1)
                .input('C', Blocks.COMPARATOR)
                .input('R', Blocks.REDSTONE_WIRE)
                .input('T', Blocks.REDSTONE_TORCH)
                .pattern("TR ")
                .pattern("RCR")
                .pattern("   ")
                .criterion(hasItem(Blocks.COMPARATOR),conditionsFromItem(Blocks.COMPARATOR))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.MEMORY_CELL, 1)
                .input('R', Blocks.REPEATER)
                .input('W', Blocks.REDSTONE_WIRE)
                .pattern(" W ")
                .pattern(" R ")
                .pattern("WRW")
                .criterion(hasItem(Blocks.REPEATER),conditionsFromItem(Blocks.REPEATER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.HALF_ADDER, 1)
                .input('X', BlockInit.XOR_GATE)
                .input('A', BlockInit.AND_GATE)
                .input('W', Blocks.REDSTONE_WIRE)
                .pattern("WW ")
                .pattern(" WX")
                .pattern("WA ")
                .criterion(hasItem(BlockInit.XOR_GATE),conditionsFromItem(BlockInit.XOR_GATE))
                .criterion(hasItem(BlockInit.AND_GATE),conditionsFromItem(BlockInit.AND_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.FULL_ADDER, 1)
                .input('H', BlockInit.HALF_ADDER)
                .input('O', BlockInit.OR_GATE)
                .input('W', Blocks.REDSTONE_WIRE)
                .pattern("HW ")
                .pattern(" HW")
                .pattern(" WO")
                .criterion(hasItem(BlockInit.HALF_ADDER),conditionsFromItem(BlockInit.HALF_ADDER))
                .criterion(hasItem(BlockInit.OR_GATE),conditionsFromItem(BlockInit.OR_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK, 1)
                .input('R', Blocks.REPEATER)
                .input('W', Blocks.REDSTONE_WIRE)
                .pattern(" W ")
                .pattern("WRW")
                .pattern(" W ")
                .criterion(hasItem(Blocks.REPEATER),conditionsFromItem(Blocks.REPEATER))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.OMNI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK, 1)
                .input('R', BlockInit.LOGIC_BASE_PLATE_BLOCK)
                .input('W', Blocks.REDSTONE_WIRE)
                .pattern(" W ")
                .pattern("WRW")
                .pattern(" W ")
                .criterion(hasItem(BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK),conditionsFromItem(BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.PULSE_EXTENDER_BLOCK, 1)
                .input('R', BlockInit.LOGIC_BASE_PLATE_BLOCK)
                .input('W', Blocks.REDSTONE_WIRE)
                .input('C', Blocks.COMPARATOR)
                .pattern(" W ")
                .pattern("CRC")
                .pattern(" W ")
                .criterion(hasItem(Blocks.COMPARATOR),conditionsFromItem(Blocks.COMPARATOR))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.SR_LATCH_BLOCK, 1)
                .input('R', Blocks.REDSTONE_WIRE)
                .input('N', BlockInit.NOR_GATE)
                .pattern("RNR")
                .pattern(" R ")
                .pattern("RNR")
                .criterion(hasItem(BlockInit.NOR_GATE),conditionsFromItem(BlockInit.NOR_GATE))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.JK_LATCH_BLOCK, 1)
                .input('S', BlockInit.SR_LATCH_BLOCK)
                .input('W', Blocks.REDSTONE_WIRE)
                .input('A', BlockInit.AND_GATE)
                .pattern("AW ")
                .pattern(" S ")
                .pattern("AW ")
                .criterion(hasItem(BlockInit.SR_LATCH_BLOCK),conditionsFromItem(BlockInit.SR_LATCH_BLOCK))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, BlockInit.T_LATCH_BLOCK, 1)
                .input('W', Blocks.REDSTONE_WIRE)
                .input('J', BlockInit.JK_LATCH_BLOCK)
                .pattern("WW ")
                .pattern("WJ ")
                .pattern("WW ")
                .criterion(hasItem(BlockInit.JK_LATCH_BLOCK),conditionsFromItem(BlockInit.JK_LATCH_BLOCK))
                .offerTo(exporter);





        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.AND_GATE), 1, 1, 3, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.NAND_GATE), 1, 1, 2, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.OR_GATE), 1, 1, 2, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.NOR_GATE), 1, 1, 1, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.XOR_GATE), 1, 5, 7, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.XNOR_GATE), 1, 5, 8, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.NOT_GATE), 1, 1, 1, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.THREE_AND_GATE), 1, 1, 4, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.THREE_OR_GATE), 1, 2, 2, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);

        TruthTableRecipeBuilder.create(new ItemStack(BlockInit.SWITCH_GATE), 1, 1, 3, ItemStack.EMPTY)
            .criterion(hasItem(BlockInit.LOGIC_BASE_PLATE_BLOCK),conditionsFromItem(BlockInit.LOGIC_BASE_PLATE_BLOCK))
            .offerTo(exporter);
    }

}
