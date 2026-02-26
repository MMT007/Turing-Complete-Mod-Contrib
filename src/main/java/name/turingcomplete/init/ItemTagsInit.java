package name.turingcomplete.init;

import name.turingcomplete.TuringComplete;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ItemTagsInit {
    public static final TagKey<Item> FITS_ON_UPGRADE_SLOT = register("fits_on_upgrade_slot");

    private static TagKey<Item> register(String key){
        return TagKey.of(RegistryKeys.ITEM, TuringComplete.id(key));
    }
}
