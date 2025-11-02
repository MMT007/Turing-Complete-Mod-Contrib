package name.turingcomplete.data.recipe;

import name.turingcomplete.recipe.TruthTableRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TruthTableRecipeJsonBuilder {
    private final Item item_output;
    private final int base_input_count;
    private final int redstone_input_count;
    private final int torch_input_count;
    private final Ingredient extender_input;

    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

    private TruthTableRecipeJsonBuilder(Item item_output, int base_input_count, Ingredient extender_input, int redstone_input_count, int torch_input_count) {
        this.base_input_count = base_input_count;
        this.item_output = item_output;
        this.redstone_input_count = redstone_input_count;
        this.torch_input_count = torch_input_count;
        this.extender_input = extender_input;
    }

    public static TruthTableRecipeJsonBuilder create(Item item_output, Ingredient extender_input, int redstone_input_count, int torch_input_count){
        return new TruthTableRecipeJsonBuilder(item_output, 1, extender_input, redstone_input_count, torch_input_count);
    }
    public static TruthTableRecipeJsonBuilder create(Item item_output, TagKey<Item> extender_input, int redstone_input_count, int torch_input_count){
        return create(item_output, Ingredient.fromTag(extender_input), redstone_input_count, torch_input_count);
    }
    public static TruthTableRecipeJsonBuilder create(Item item_output, ItemConvertible extender_input, int redstone_input_count, int torch_input_count){
        return create(item_output, Ingredient.ofItems(extender_input), redstone_input_count, torch_input_count);
    }
    public static TruthTableRecipeJsonBuilder create(Item item_output, int base_input_count, Ingredient extender_input, int redstone_input_count, int torch_input_count){
        return new TruthTableRecipeJsonBuilder(item_output, base_input_count, extender_input, redstone_input_count, torch_input_count);
    }
    public static TruthTableRecipeJsonBuilder create(Item item_output, int base_input_count, TagKey<Item> extender_input, int redstone_input_count, int torch_input_count){
        return create(item_output, base_input_count,  Ingredient.fromTag(extender_input), redstone_input_count, torch_input_count);
    }
    public static TruthTableRecipeJsonBuilder create(Item item_output, int base_input_count, ItemConvertible extender_input, int redstone_input_count, int torch_input_count){
        return create(item_output, base_input_count, Ingredient.ofItems(extender_input), redstone_input_count, torch_input_count);
    }


    public TruthTableRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    static Identifier getItemID(ItemConvertible item) {
        return Registries.ITEM.getId(item.asItem());
    }
    public void offerTo(RecipeExporter exporter) {
        Identifier recipeId = getItemID(this.item_output);
        this.validate(recipeId);
        Advancement.Builder builder = exporter.getAdvancementBuilder().criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::criterion);
        TruthTableRecipe truthTableRecipe = new TruthTableRecipe(this.base_input_count,this.redstone_input_count, this.torch_input_count, this.extender_input, new ItemStack(this.item_output,1));
        exporter.accept(recipeId, truthTableRecipe, builder.build(recipeId));
    }

    private void validate(Identifier recipeId) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
