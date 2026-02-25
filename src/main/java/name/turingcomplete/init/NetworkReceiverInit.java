package name.turingcomplete.init;

import name.turingcomplete.network.c2s.SelectTruthTableCraftC2SPacket;
import name.turingcomplete.screen.truthtable.TruthTableScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkReceiverInit {
    public static void loadServer(){
        ServerPlayNetworking.registerGlobalReceiver(SelectTruthTableCraftC2SPacket.ID, (payload, context) -> {

            var currentScreen = context.player().currentScreenHandler;
            if (currentScreen instanceof TruthTableScreenHandler truthTableScreenHandler) {
                var craftId = payload.craftId();
                var category = payload.category();

                truthTableScreenHandler.setSelectedCategory(category);
                truthTableScreenHandler.setCraftIndex(craftId);
                truthTableScreenHandler.updateInventory();
                truthTableScreenHandler.switchTo(category,craftId);
            }
        });
    }
}
