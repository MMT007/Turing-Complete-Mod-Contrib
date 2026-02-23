package name.turingcomplete.truthtable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import name.turingcomplete.init.BlockInit;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Optional;

public class TruthTableCraft {
    public static final Codec<TruthTableCraft> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("logic_plate").forGetter(tradeOffer -> tradeOffer.logic_plate_required),
        Codec.INT.fieldOf("redstone").forGetter(tradeOffer -> tradeOffer.redstone_required),
        Codec.INT.fieldOf("redstone_torch").forGetter(tradeOffer -> tradeOffer.redstone_torch_required),
        ItemStack.OPTIONAL_CODEC.fieldOf("upgrade").forGetter(tradeOffer -> tradeOffer.upgrade),
        ItemStack.CODEC.fieldOf("sell").forGetter(tradeOffer -> tradeOffer.sellItem)
        ).apply(instance, TruthTableCraft::new)
    );

    public static final PacketCodec<RegistryByteBuf, TruthTableCraft> PACKET_CODEC = PacketCodec.ofStatic(
        TruthTableCraft::write, TruthTableCraft::read
    );

    private final Integer logic_plate_required;
    private final Integer redstone_required;
    private final Integer redstone_torch_required;
    private final ItemStack upgrade;
    private final ItemStack sellItem;

    private TruthTableCraft(
        Integer logic_plate_required,
        Integer redstone_required,
        Integer redstone_torch_required,
        Optional<ItemStack> upgrade,
        ItemStack sellItem)
    { this(logic_plate_required, redstone_required, redstone_torch_required,upgrade.orElse(ItemStack.EMPTY),sellItem); }

    private TruthTableCraft (TruthTableCraft original){
        this(
            original.logic_plate_required,
            original.redstone_required,
            original.redstone_torch_required,
            original.upgrade.copy(),
            original.sellItem.copy()
        );
    }

    public TruthTableCraft(
        Integer logic_plate_required,
        Integer redstone_required,
        Integer redstone_torch_required,
        ItemStack upgrade,
        ItemStack sellItem
    ) {
        this.logic_plate_required = logic_plate_required;
        this.redstone_required = redstone_required;
        this.redstone_torch_required = redstone_torch_required;
        this.upgrade = upgrade;
        this.sellItem = sellItem;
    }


    public TruthTableCraft copy() {return new TruthTableCraft(this);}

    public ItemStack getLogicPlate() {return new ItemStack(BlockInit.LOGIC_BASE_PLATE_BLOCK, logic_plate_required);}
    public ItemStack getRedstone() {return new ItemStack(Blocks.REDSTONE_WIRE, redstone_required);}
    public ItemStack getRedstoneTorch() {return new ItemStack(Blocks.REDSTONE_TORCH, redstone_torch_required);}
    public ItemStack getUpgrade() {return upgrade;}

    public ItemStack getOutput() {
        return this.sellItem;
    }
    public ItemStack copySellItem() {
        return this.sellItem.copy();
    }

    public boolean matchesCraft(
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torch,
        ItemStack upgrade
    ) {
        if (!getLogicPlate().itemMatches(logic_plate.getRegistryEntry()) || logic_plate.getCount() < this.logic_plate_required)
            return false;
        if (!getRedstone().itemMatches(redstone.getRegistryEntry()) || redstone.getCount() < this.redstone_required)
            return false;
        if (!getRedstoneTorch().itemMatches(redstone_torch.getRegistryEntry()) || redstone_torch.getCount() < this.redstone_torch_required)
            return false;

        if (!this.upgrade.isEmpty())
            return this.upgrade.itemMatches(upgrade.getRegistryEntry()) && upgrade.getCount() >= this.upgrade.getCount();

        return true;
    }

    public boolean depleteBuyItems(
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torch,
        ItemStack upgrade
    ) {
        if (!this.matchesCraft(logic_plate, redstone, redstone_torch, upgrade))
            return false;

        logic_plate.decrement(this.logic_plate_required);
        redstone.decrement(this.redstone_required);
        redstone_torch.decrement(this.redstone_torch_required);
        if (!this.upgrade.isEmpty()) upgrade.decrement(this.upgrade.getCount());

        return true;
    }


    private static void write(RegistryByteBuf buf, TruthTableCraft offer) {
        PacketCodecs.INTEGER.encode(buf, offer.logic_plate_required);
        PacketCodecs.INTEGER.encode(buf, offer.redstone_required);
        PacketCodecs.INTEGER.encode(buf, offer.redstone_torch_required);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, offer.getUpgrade());
        ItemStack.PACKET_CODEC.encode(buf, offer.getOutput());
    }

    public static TruthTableCraft read(RegistryByteBuf buf) {
        Integer logic_plate = PacketCodecs.INTEGER.decode(buf);
        Integer redstone = PacketCodecs.INTEGER.decode(buf);
        Integer redstone_torch = PacketCodecs.INTEGER.decode(buf);
        ItemStack upgrade = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
        ItemStack sell_item = ItemStack.PACKET_CODEC.decode(buf);

        return new TruthTableCraft(logic_plate,redstone,redstone_torch,upgrade,sell_item);
    }
}
