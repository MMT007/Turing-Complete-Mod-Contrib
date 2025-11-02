package name.turingcomplete.screen.data;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class GateCraftingRecipeList extends ArrayList<GateCraftingRecipe> {
    public static final Codec<GateCraftingRecipeList> CODEC;
    public static final PacketCodec<RegistryByteBuf, GateCraftingRecipeList> PACKET_CODEC;

    public GateCraftingRecipeList() {}
    private GateCraftingRecipeList(int initialCapacity) {super(initialCapacity);}
    private GateCraftingRecipeList(Collection<GateCraftingRecipe> gateCraftingRecipes)
    {super(gateCraftingRecipes);}

    @Nullable
    public GateCraftingRecipe getValidOffer(int index, ItemStack base_input, ItemStack redstone_input, ItemStack redtorch_input, ItemStack extender_input) {
        if (index > 0 && index < this.size()) {
            GateCraftingRecipe gateCraftingRecipe = this.get(index);
            return gateCraftingRecipe.matchesRecipeInput(base_input, redstone_input, redtorch_input, extender_input) ? gateCraftingRecipe : null;
        }

        for (GateCraftingRecipe gateCraftingRecipe : this)
            if (gateCraftingRecipe.matchesRecipeInput(base_input, redstone_input, redtorch_input, extender_input))
                return gateCraftingRecipe;

        return null;

    }

    static {
        CODEC = GateCraftingRecipe.CODEC.listOf().fieldOf("Recipes").xmap(GateCraftingRecipeList::new, Function.identity()).codec();
        PACKET_CODEC = GateCraftingRecipe.PACKET_CODEC.collect(PacketCodecs.toCollection(GateCraftingRecipeList::new));
    }
}
