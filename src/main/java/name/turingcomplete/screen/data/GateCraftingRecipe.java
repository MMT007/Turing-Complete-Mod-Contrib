package name.turingcomplete.screen.data;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import name.turingcomplete.init.blockInit;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.village.TradedItem;

public class GateCraftingRecipe {
    public static final Codec<GateCraftingRecipe> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.INT.fieldOf("base_item_count").forGetter((gateCraftingOffer) -> gateCraftingOffer.base_item_count),
            Codec.INT.lenientOptionalFieldOf("redstone_item_count", 0).forGetter((gateCraftingOffer) -> gateCraftingOffer.redstone_item_count),
            Codec.INT.lenientOptionalFieldOf("redstone_torch_item_count", 0).forGetter((gateCraftingOffer) -> gateCraftingOffer.redstone_torch_item_count),
            TradedItem.CODEC.lenientOptionalFieldOf("extender_item").forGetter((gateCraftingOffer) -> gateCraftingOffer.extender_item),
            ItemStack.CODEC.fieldOf("output_item").forGetter(gateCraftingOffer -> gateCraftingOffer.output_item)
    ).apply(instance, GateCraftingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, GateCraftingRecipe> PACKET_CODEC = PacketCodec.ofStatic(GateCraftingRecipe::write, GateCraftingRecipe::read);

    public static final GateCraftingRecipe TEST_OFFER = new GateCraftingRecipe(1,1,2,Optional.empty(), new ItemStack(blockInit.NAND_GATE,1));

    private final int base_item_count;
    private final int redstone_item_count;
    private final int redstone_torch_item_count;
    private final Optional<TradedItem> extender_item;
    private final ItemStack output_item;

    public GateCraftingRecipe(int base_item_count, int redstone_item_count, int redstone_torch_item_count, Optional<TradedItem> extender_item, ItemStack output_item) {
        this.base_item_count = base_item_count;
        this.redstone_item_count = redstone_item_count;
        this.redstone_torch_item_count = redstone_torch_item_count;
        this.extender_item = extender_item;
        this.output_item = output_item;
    }

    private GateCraftingRecipe(GateCraftingRecipe original){
        this.base_item_count = original.base_item_count;
        this.redstone_item_count = original.redstone_item_count;
        this.redstone_torch_item_count = original.redstone_item_count;
        this.extender_item = original.extender_item;
        this.output_item = original.output_item;
    }

    private ItemStack getDisplayedItem(ItemConvertible item, int amount)
    {return amount == 0 ? ItemStack.EMPTY : new ItemStack(item,amount);}

    public ItemStack getDisplayedBaseItem()
    {return getDisplayedItem(blockInit.LOGIC_BASE_PLATE_BLOCK, this.base_item_count);}

    public ItemStack getDisplayedRedstoneItem()
    {return getDisplayedItem(Items.REDSTONE, this.redstone_item_count);}

    public ItemStack getDisplayedRedTorchItem()
    {return getDisplayedItem(Items.REDSTONE_TORCH, this.redstone_torch_item_count);}

    public ItemStack getDisplayedOutputItem()
    {return this.output_item;}

    // TODO: Implement Extender Item
    public ItemStack getDisplayedExtenderItem()
    {return ItemStack.EMPTY;}

    public ItemStack copyOutputItem() {
        return this.output_item.copy();
    }

    private boolean matches(ItemConvertible itemConvertible, ItemConvertible itemConvertible2){
        return Registries.ITEM.getId(itemConvertible.asItem()).equals(Registries.ITEM.getId(itemConvertible2.asItem()));
    }

    public boolean matchesRecipeInput(ItemStack base_input, ItemStack redstone_input, ItemStack redtorch_input, ItemStack extender_input) {
        boolean base_cond = matches(base_input.getItem(), blockInit.LOGIC_BASE_PLATE_BLOCK) && base_input.getCount() >= this.base_item_count;
        boolean redstone_cond = matches(redstone_input.getItem(), Items.REDSTONE) && base_input.getCount() >= this.redstone_item_count;
        boolean redtorch_cond = matches(redtorch_input.getItem(), Items.REDSTONE_TORCH) && base_input.getCount() >= this.redstone_torch_item_count;
        boolean extender_cond = this.extender_item.map(tradedItem -> tradedItem.matches(extender_input) && extender_input.getCount() >= tradedItem.count())
                .orElseGet(extender_input::isEmpty);

        return base_cond && redstone_cond && redtorch_cond && extender_cond;
    }

    public boolean depleteBuyItems(ItemStack base_input, ItemStack redstone_input, ItemStack redtorch_input, ItemStack extender_input) {
        if (!this.matchesRecipeInput(base_input, redstone_input, redtorch_input, extender_input))
            return false;

        base_input.decrement(this.base_item_count);
        redstone_input.decrement(this.redstone_item_count);
        redtorch_input.decrement(this.redstone_torch_item_count);

        if (!this.getDisplayedExtenderItem().isEmpty()) {
            extender_input.decrement(this.getDisplayedExtenderItem().getCount());
        }

        return true;
    }

    public GateCraftingRecipe copy() {
        return new GateCraftingRecipe(this);
    }

    private static void write(RegistryByteBuf buf, GateCraftingRecipe offer) {
        PacketCodecs.INTEGER.encode(buf,offer.base_item_count);
        PacketCodecs.INTEGER.encode(buf,offer.redstone_item_count);
        PacketCodecs.INTEGER.encode(buf,offer.redstone_torch_item_count);
        TradedItem.OPTIONAL_PACKET_CODEC.encode(buf,offer.extender_item);
        ItemStack.PACKET_CODEC.encode(buf,offer.output_item);
    }

    public static GateCraftingRecipe read(RegistryByteBuf buf) {
        Integer base_item_count = PacketCodecs.INTEGER.decode(buf);
        Integer redstone_item_count = PacketCodecs.INTEGER.decode(buf);
        Integer redstone_torch_item_count = PacketCodecs.INTEGER.decode(buf);
        Optional<TradedItem> extender_item = TradedItem.OPTIONAL_PACKET_CODEC.decode(buf);
        ItemStack output_item = ItemStack.PACKET_CODEC.decode(buf);

        return new GateCraftingRecipe(base_item_count, redstone_item_count, redstone_torch_item_count, extender_item, output_item);
    }
}
