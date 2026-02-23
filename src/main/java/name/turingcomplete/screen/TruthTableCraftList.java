package name.turingcomplete.screen;

import com.mojang.serialization.Codec;
import name.turingcomplete.data.recipe.TruthTableRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class TruthTableCraftList extends ArrayList<TruthTableCraft> {
    public TruthTableCraftList() {}

    private TruthTableCraftList(int size) {
        super(size);
    }

    @Nullable
    public TruthTableCraft getValidCraft(
        ItemStack logic_plate,
        ItemStack redstone,
        ItemStack redstone_torch,
        ItemStack upgrade,
        int index
    ) {
        if (index > 0 && index < this.size()) {
            var tradeOffer = this.get(index);
            return tradeOffer.matchesCraft(logic_plate,redstone,redstone_torch,upgrade) ? tradeOffer : null;
        }

        for (var craft : this)
            if (craft.matchesCraft(logic_plate,redstone,redstone_torch,upgrade))
                return craft;

        return null;
    }

    public static TruthTableCraftList fromRecipes(List<RecipeEntry<TruthTableRecipe>> recipeEntries) {
        var craftList = new TruthTableCraftList();

        for(var entry : recipeEntries){
            var recipe = entry.value();
            craftList.add(new TruthTableCraft(
                recipe.getLogicPlateRequired(),
                recipe.getRedstoneRequired(),
                recipe.getRedstoneTorchesRequired(),
                recipe.getUpgrade(),
                recipe.getOutput()
            ));
        }

        return craftList;
    }
}
