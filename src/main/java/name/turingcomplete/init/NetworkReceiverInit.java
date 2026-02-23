package name.turingcomplete.init;

import name.turingcomplete.network.c2s.SelectTruthTableCraftC2SPacket;
import name.turingcomplete.truthtable.TruthTableScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkReceiverInit {
    public static void registerServer(){
        ServerPlayNetworking.registerGlobalReceiver(SelectTruthTableCraftC2SPacket.ID, (payload, context) -> {

            var currentScreen = context.player().currentScreenHandler;
            if (currentScreen instanceof TruthTableScreenHandler truthTableScreenHandler) {
                var craftId = payload.craftId();

                truthTableScreenHandler.setCraftIndex(craftId);
                truthTableScreenHandler.switchTo(craftId);
            }
        });
    }
}
