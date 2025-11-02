package name.turingcomplete.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import name.turingcomplete.init.blockInit;
import name.turingcomplete.init.recipeSerializersInit;
import name.turingcomplete.init.recipeTypesInit;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class TruthTableRecipe implements Recipe<TruthTableRecipe.TruthTableRecipeInput> {
    private final ItemStack output;
    private final Integer base_required;
    private final Integer redstone_required;
    private final Integer redstone_torches_required;
    private final Ingredient extender_module;
    private final Ingredient base_item = Ingredient.ofItems(blockInit.LOGIC_BASE_PLATE_BLOCK);

    public TruthTableRecipe(Integer base_required,Integer redstone_required, Integer redstone_torches_required, Ingredient extender_module, ItemStack output){
        this.output = output;
        this.base_required = base_required;
        this.redstone_required = redstone_required;
        this.redstone_torches_required = redstone_torches_required;
        this.extender_module = extender_module;
    }

    public ItemStack createIcon() {return new ItemStack(blockInit.TRUTH_TABLE);}
    public RecipeType<TruthTableRecipe> getType() {return recipeTypesInit.TRUTH_TABLE_RECIPE_TYPE;}
    public RecipeSerializer<?> getSerializer() {
        return recipeSerializersInit.TRUTH_TABLE_RECIPE_SERIALIZER;
    }

    public boolean fits(int width, int height) {return width >= 3 && height >= 2;}
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return output;
    }

    @Override
    public boolean matches(TruthTableRecipeInput input, World world) {
        return this.base_item.test(input.base_item) &&
              (this.redstone_required == 0 || (input.redstone_required.isOf(Items.REDSTONE)) && input.redstone_required.getCount() >= redstone_required) &&
              (this.redstone_torches_required == 0 || input.redstone_torches_required.isOf(Items.REDSTONE_TORCH) && input.redstone_torches_required.getCount() >= redstone_torches_required) &&
              (this.extender_module.isEmpty() || this.extender_module.test(input.extender_module));
    }

    @Override
    public ItemStack craft(TruthTableRecipeInput input, RegistryWrapper.WrapperLookup lookup) {return this.output.copy();}

    public record TruthTableRecipeInput(ItemStack base_item, ItemStack redstone_required, ItemStack redstone_torches_required, ItemStack extender_module) implements RecipeInput{
        public ItemStack getStackInSlot(int slot) {
            return switch (slot){
                case 0: yield base_item;
                case 1: yield redstone_required;
                case 2: yield redstone_torches_required;
                case 3: yield extender_module;
                default: throw new IllegalArgumentException("Recipe does not contain slot " + slot);
            };
        }

        public int getSize() {return 4;}
    }

    public static class Serializer implements RecipeSerializer<TruthTableRecipe>{
        public static MapCodec<TruthTableRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.INT.fieldOf("base_required").forGetter(recipe -> recipe.base_required),
                Codec.INT.lenientOptionalFieldOf("redstone_required",0).forGetter(recipe -> recipe.redstone_required),
                Codec.INT.lenientOptionalFieldOf("redstone_torches_required",0).forGetter(recipe -> recipe.redstone_torches_required),
                Ingredient.ALLOW_EMPTY_CODEC.fieldOf("extender_module").forGetter(recipe -> recipe.extender_module),
                ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.output)
            ).apply(instance, TruthTableRecipe::new)
        );
        public static final PacketCodec<RegistryByteBuf, TruthTableRecipe> PACKET_CODEC = PacketCodec.ofStatic(TruthTableRecipe.Serializer::write, TruthTableRecipe.Serializer::read);

        private static TruthTableRecipe read(RegistryByteBuf buf) {
            Integer base_required = PacketCodecs.INTEGER.decode(buf);
            Integer redstone_required = PacketCodecs.INTEGER.decode(buf);
            Integer torch_input = PacketCodecs.INTEGER.decode(buf);
            Ingredient extender_input = Ingredient.PACKET_CODEC.decode(buf);
            ItemStack output = ItemStack.PACKET_CODEC.decode(buf);

            return new TruthTableRecipe(base_required,redstone_required,torch_input,extender_input,output);
        }

        private static void write(RegistryByteBuf buf, TruthTableRecipe recipe){
            PacketCodecs.INTEGER.encode(buf,recipe.base_required);
            PacketCodecs.INTEGER.encode(buf,recipe.redstone_required);
            PacketCodecs.INTEGER.encode(buf,recipe.redstone_torches_required);
            Ingredient.PACKET_CODEC.encode(buf, recipe.extender_module);
            ItemStack.PACKET_CODEC.encode(buf,recipe.output);
        }

        public MapCodec<TruthTableRecipe> codec() {return CODEC;}
        public PacketCodec<RegistryByteBuf, TruthTableRecipe> packetCodec() {return PACKET_CODEC;}
    }
}
