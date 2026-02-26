package name.turingcomplete.data.recipe;

import name.turingcomplete.TuringComplete;
import name.turingcomplete.init.BlockInit;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public enum TruthTableCategory {
    GATES(
        TuringComplete.id("gates"),
        Text.translatable("craftGroup.turingcomplete.gates"),
        new ItemStack(BlockInit.AND_GATE)
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
    ICS(
        TuringComplete.id("ics"),
        Text.translatable("craftGroup.turingcomplete.ics"),
        new ItemStack(BlockInit.BI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK)
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
