package name.turingcomplete.init;

import name.turingcomplete.network.c2s.SelectTruthTableCraftC2SPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PacketTypeInit {
    public static void register(){
        PayloadTypeRegistry.playC2S().register(
            SelectTruthTableCraftC2SPacket.ID,
            SelectTruthTableCraftC2SPacket.PACKET_CODEC
        );
    }
}
