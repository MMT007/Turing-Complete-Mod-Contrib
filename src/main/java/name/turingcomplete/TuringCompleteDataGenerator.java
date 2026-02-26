package name.turingcomplete;

import name.turingcomplete.data.provider.TuringCompleteItemTagProvider;
import name.turingcomplete.data.provider.TuringCompleteRecipeProvider;
import name.turingcomplete.data.provider.TuringCompleteLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class TuringCompleteDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(TuringCompleteItemTagProvider::new);
		pack.addProvider(TuringCompleteRecipeProvider::new);
		pack.addProvider(TuringCompleteLootTableProvider::new);
	}
}
