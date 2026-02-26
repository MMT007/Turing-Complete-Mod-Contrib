package name.turingcomplete.data.provider;

import name.turingcomplete.init.ItemTagsInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class TuringCompleteItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public TuringCompleteItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTagsInit.FITS_ON_UPGRADE_SLOT)
            .add(Items.QUARTZ);
    }
}
