package name.turingcomplete.data.recipe;

import name.turingcomplete.screen.truthtable.data.TruthTableCategory;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class TruthTableRecipeBuilder {
    private final int logic_plate_count;
    private final int redstone_count;
    private final int redstone_torch_count;
    private final ItemStack upgrade;
    private final List<TruthTableCategory> categories = new ArrayList<>();

    private final ItemStack output;

    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

    private TruthTableRecipeBuilder(
        ItemStack output,
        int logic_plate_count,
        int redstone_count,
        int redstone_torch_count,
        ItemStack upgrade
    ) {
        this.output = output;
        this.logic_plate_count = logic_plate_count;
        this.redstone_count = redstone_count;
        this.redstone_torch_count = redstone_torch_count;
        this.upgrade = upgrade;
    }

    public static TruthTableRecipeBuilder create(
        ItemStack output,
        int logic_plate_count,
        int redstone_count,
        int redstone_torch_count,
        ItemStack upgrade
    ){ return new TruthTableRecipeBuilder(output, logic_plate_count, redstone_count, redstone_torch_count, upgrade); }

    public static TruthTableRecipeBuilder create(
        ItemStack output,
        int logic_plate_count,
        int redstone_count,
        int redstone_torch_count,
        ItemConvertible upgrade
    ){ return create(output, logic_plate_count, redstone_count, redstone_torch_count, new ItemStack(upgrade)); }

    public static TruthTableRecipeBuilder create(
        ItemStack output,
        int logic_plate_count,
        int redstone_count,
        int redstone_torch_count
    ){ return new TruthTableRecipeBuilder(output, logic_plate_count, redstone_count, redstone_torch_count, ItemStack.EMPTY); }

    public TruthTableRecipeBuilder onCategory(TruthTableCategory category){
        this.categories.add(category);
        return this;
    }

    public TruthTableRecipeBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    static Identifier getItemID(ItemConvertible item) {
        return Registries.ITEM.getId(item.asItem()).withPrefixedPath("truth_table_");
    }

    public void offerTo(RecipeExporter exporter) {
        Identifier recipeId = getItemID(this.output.getItem());
        this.validate(recipeId);

        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        Objects.requireNonNull(builder);

        this.criteria.forEach(builder::criterion);

        TruthTableRecipe truthTableRecipe = new TruthTableRecipe(
            this.categories.stream().map(TruthTableCategory::getId).toList(),
            this.logic_plate_count, this.redstone_count,
            this.redstone_torch_count, this.upgrade,
            this.output
        );

        exporter.accept(recipeId, truthTableRecipe, builder.build(recipeId));
    }

    private void validate(Identifier recipeId) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }
}
