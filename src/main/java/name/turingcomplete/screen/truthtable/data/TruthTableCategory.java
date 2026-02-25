package name.turingcomplete.screen.truthtable.data;

import name.turingcomplete.TuringComplete;
import name.turingcomplete.init.BlockInit;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public enum TruthTableCategory {
    AND_GATES(
        TuringComplete.id("and_gates"),
        Text.translatable("craftGroup.turingcomplete.and_gates"),
        new ItemStack(BlockInit.AND_GATE)
    ),
    OR_GATES(
        TuringComplete.id("or_gates"),
        Text.translatable("craftGroup.turingcomplete.or_gates"),
        new ItemStack(BlockInit.OR_GATE)
    ),
    XOR_GATES(
        TuringComplete.id("xor_gates"),
        Text.translatable("craftGroup.turingcomplete.xor_gates"),
        new ItemStack(BlockInit.XOR_GATE)
    ),
    NOT_GATES(
        TuringComplete.id("not_gates"),
        Text.translatable("craftGroup.turingcomplete.not_gates"),
        new ItemStack(BlockInit.NOT_GATE)
    ),
    LATCHES(
        TuringComplete.id("latches"),
        Text.translatable("craftGroup.turingcomplete.lathes"),
        new ItemStack(BlockInit.SR_LATCH_BLOCK)
    ),
    ADDERS(
        TuringComplete.id("adders"),
        Text.translatable("craftGroup.turingcomplete.adders"),
        new ItemStack(BlockInit.FULL_ADDER)
    );

    private final Identifier id;
    private final Text display_name;
    private final ItemStack icon;

    TruthTableCategory(Identifier id, Text display_name, ItemStack icon) {
        this.id = id;
        this.display_name = display_name;
        this.icon = icon;
    }

    public Identifier getId() {return id;}
    public Text getDisplayName() {return display_name;}
    public ItemStack getIcon() {return icon;}

    public static TruthTableCategory fromID(Identifier id){
        for (var category : values()){
            if (category.id.equals(id))
                return category;
        }

        return null;
    }

    public static final PacketCodec<RegistryByteBuf, TruthTableCategory> PACKET_CODEC = PacketCodec.tuple(
        Identifier.PACKET_CODEC, TruthTableCategory::getId, TruthTableCategory::fromID
    );
}
