package name.turingcomplete.network.c2s;

import name.turingcomplete.TuringComplete;
import name.turingcomplete.data.recipe.TruthTableCategory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SelectTruthTableCraftC2SPacket(TruthTableCategory category, int craftId) implements CustomPayload {
    public static final Id<SelectTruthTableCraftC2SPacket> ID = new Id<>(TuringComplete.id("select_truth_table_craft"));
    public static final PacketCodec<RegistryByteBuf, SelectTruthTableCraftC2SPacket> PACKET_CODEC = PacketCodec.tuple(
        TruthTableCategory.PACKET_CODEC, SelectTruthTableCraftC2SPacket::category,
        PacketCodecs.INTEGER, SelectTruthTableCraftC2SPacket::craftId,
        SelectTruthTableCraftC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {return ID;}
}
