package name.turingcomplete.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import name.turingcomplete.init.BlockInit;
import name.turingcomplete.init.RecipeSerializersInit;
import name.turingcomplete.init.RecipeTypesInit;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class TruthTableRecipe implements Recipe<TruthTableRecipe.TruthTableRecipeInput> {
    private final List<TruthTableCategory> categories;
    private final Integer logic_plate_required;
    private final Integer redstone_required;
    private final Integer redstone_torches_required;
    private final ItemStack upgrade;
    private final ItemStack output;

    public TruthTableRecipe(
        List<Identifier> categories,
        Integer logic_plate_required,
        Integer redstone_required,
        Integer redstone_torches_required,
        ItemStack upgrade,
        ItemStack output
    ){
        this.categories = categories.stream().map(TruthTableCategory::fromID).toList();
        this.logic_plate_required = Objects.requireNonNullElse(logic_plate_required,0);
        this.redstone_required = Objects.requireNonNullElse(redstone_required,0);
        this.redstone_torches_required = Objects.requireNonNullElse(redstone_torches_required,0);
        this.upgrade = upgrade;
        this.output = output;
    }

    public ItemStack createIcon() {return new ItemStack(BlockInit.TRUTH_TABLE);}
    public RecipeType<TruthTableRecipe> getType() {return RecipeTypesInit.TRUTH_TABLE_RECIPE_TYPE;}
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersInit.TRUTH_TABLE_RECIPE_SERIALIZER;
    }
    public String getOutputName(){return this.output.getName().getString();}

    public boolean fits(int width, int height) {return width >= 3 && height >= 2;}
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output;
    }

    public boolean matches(
        World world,
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torch,
        ItemStack upgrade
    ) {return matches(new TruthTableRecipeInput(logic_plate, redstone, redstone_torch, upgrade), world);}


    @Override
    public boolean matches(TruthTableRecipeInput input, World world) {
        return
          (this.logic_plate_required == 0 || (input.logic_plate.isOf(BlockInit.LOGIC_BASE_PLATE_BLOCK.asItem()) && input.logic_plate.getCount() >= logic_plate_required)) &&
          (this.redstone_required == 0 || (input.redstone.isOf(Items.REDSTONE)) && input.redstone.getCount() >= redstone_required) &&
          (this.redstone_torches_required == 0 || input.redstone_torches.isOf(Items.REDSTONE_TORCH) && input.redstone_torches.getCount() >= redstone_torches_required) &&
          (this.upgrade.isEmpty() || (this.upgrade.isOf(input.upgrade.getItem()) && input.upgrade.getCount() >= this.upgrade.getCount()));
    }

    @Override
    public ItemStack craft(TruthTableRecipeInput input, RegistryWrapper.WrapperLookup lookup) {return this.output.copy();}

    public ItemStack getLogicPlate() {return new ItemStack(BlockInit.LOGIC_BASE_PLATE_BLOCK, logic_plate_required);}
    public ItemStack getRedstone() {return new ItemStack(Items.REDSTONE, redstone_required);}
    public ItemStack getRedstoneTorch() {return new ItemStack(Items.REDSTONE_TORCH, redstone_torches_required);}
    public ItemStack getUpgrade() {return upgrade;}
    public ItemStack getOutput() {return output;}
    public List<TruthTableCategory> getCategories() {return categories;}

    public boolean depleteBuyItems(
        World world,
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torch,
        ItemStack upgrade
    ) {
        if (!this.matches(world,logic_plate, redstone, redstone_torch, upgrade))
            return false;

        logic_plate.decrement(this.logic_plate_required);
        redstone.decrement(this.redstone_required);
        redstone_torch.decrement(this.redstone_torches_required);
        if (!this.upgrade.isEmpty()) upgrade.decrement(this.upgrade.getCount());

        return true;
    }

    public record TruthTableRecipeInput(
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torches,
        ItemStack upgrade
    ) implements RecipeInput{
        public ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> logic_plate;
                case 1 -> redstone;
                case 2 -> redstone_torches;
                case 3 -> upgrade;
                default -> throw new IllegalArgumentException("Recipe does not contain slot " + slot);
            };
        }

        public int getSize() {return 4;}
    }

    public static class Serializer implements RecipeSerializer<TruthTableRecipe>{
        public final static MapCodec<TruthTableRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Identifier.CODEC.listOf().fieldOf("categories").forGetter(recipe -> recipe.categories.stream().map(TruthTableCategory::getId).toList()),
                Codec.INT.fieldOf("logic_plate_requeired").forGetter(recipe -> recipe.logic_plate_required),
                Codec.INT.fieldOf("redstone_required").forGetter(recipe -> recipe.redstone_required),
                Codec.INT.fieldOf("redstone_torches_required").forGetter(recipe -> recipe.redstone_torches_required),
                ItemStack.OPTIONAL_CODEC.fieldOf("upgrade").forGetter(recipe -> recipe.upgrade),
                ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
            ).apply(instance, TruthTableRecipe::new)
        );

        public static final PacketCodec<RegistryByteBuf, TruthTableRecipe> PACKET_CODEC = PacketCodec.ofStatic(
            TruthTableRecipe.Serializer::write, TruthTableRecipe.Serializer::read
        );

        private static TruthTableRecipe read(RegistryByteBuf buf) {
            List<Identifier> categories = IDENTIFIER_LIST_PACKET_CODEC.decode(buf);
            Integer logic_plate_required = PacketCodecs.INTEGER.decode(buf);
            Integer redstone_required = PacketCodecs.INTEGER.decode(buf);
            Integer torch_input = PacketCodecs.INTEGER.decode(buf);
            ItemStack upgrade = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
            ItemStack output = ItemStack.PACKET_CODEC.decode(buf);

            return new TruthTableRecipe(categories,logic_plate_required,redstone_required,torch_input, upgrade,output);
        }

        private static void write(RegistryByteBuf buf, TruthTableRecipe recipe){
            IDENTIFIER_LIST_PACKET_CODEC.encode(buf, recipe.categories.stream().map(TruthTableCategory::getId).toList());
            PacketCodecs.INTEGER.encode(buf,recipe.logic_plate_required);
            PacketCodecs.INTEGER.encode(buf,recipe.redstone_required);
            PacketCodecs.INTEGER.encode(buf,recipe.redstone_torches_required);
            ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, recipe.upgrade);
            ItemStack.PACKET_CODEC.encode(buf,recipe.output);
        }

        public MapCodec<TruthTableRecipe> codec() {return CODEC;}
        public PacketCodec<RegistryByteBuf, TruthTableRecipe> packetCodec() {return PACKET_CODEC;}
        private static final PacketCodec<ByteBuf, List<Identifier>> IDENTIFIER_LIST_PACKET_CODEC = Identifier.PACKET_CODEC.collect(PacketCodecs.toList());
    }
}
