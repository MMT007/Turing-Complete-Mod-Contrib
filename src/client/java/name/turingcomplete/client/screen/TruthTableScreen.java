package name.turingcomplete.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import name.turingcomplete.TuringComplete;
import name.turingcomplete.screen.TruthTableScreenHandler;
import name.turingcomplete.screen.data.GateCraftingRecipe;
import name.turingcomplete.screen.data.GateCraftingRecipeList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static name.turingcomplete.screen.TruthTableScreenHandler.SLOT_POSITIONS;

@Environment(EnvType.CLIENT)
public class TruthTableScreen extends HandledScreen<TruthTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/truth_table.png");
    private static final List<Identifier> SLOT_TEXTURES = List.of(
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_base_plate_icon.png"),
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_redstone_icon.png"),
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_torch_icon.png"),
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_upgrade_icon.png")
    );
    private static final Identifier SCROLL_BAR = Identifier.ofVanilla("container/villager/scroller");
    private static final Identifier SCROLL_BAR_DISABLED = Identifier.ofVanilla("container/villager/scroller_disabled");
    private static final int[] RECIPE_SLOT_X_POSITIONS = new int[]{18,38,58,78,94};

    private final List<RecipeButtonWidget> recipes = new ArrayList<>();
    private int indexStartOffset;
    private boolean scrolling;
    private int selectedIndex;


    public TruthTableScreen(TruthTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 312;
        this.backgroundHeight = 156;
    }

    @Override
    protected void init(){
        super.init();
        this.titleX = (backgroundWidth - this.textRenderer.getWidth(this.title)) / 2 + 20;
        this.playerInventoryTitleX = this.titleX;
        this.playerInventoryTitleY = (this.playerInventoryTitleY - 10);
    }

    /*===============================================================================================
    Hey!! this is MMT007_backUP, Sorry If I Have Left A Mess In This Class, But I Hope You Guys Will
    be Able To Help Me Figure This GUI Thing Out!

    Useful Classes To Get Reference Code From:
    --CLIENT:
        * net.minecraft.client.gui.screen.ingame.MerchantScreen
    --COMMON:
        * net.minecraft.screen.MerchantScreenHandler
        * net.minecraft.village.MerchantInventory
        O net.minecraft.village.TradeOffer
        O net.minecraft.village.TradeOfferList

        * - Not Fully Implemented
        O - Most Likely Fully Implemented (Maybe Subject To Change)

    Another Important Notice, The Background Texture Is Bigger Than The Default Screen, So Some Values
    Like ``` (this.width - this.backgroundWidth) / 2 ``` May Not be Precise And May Need An Offset Value
    ===============================================================================================*/

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight);

        for (int i = 0; i < SLOT_POSITIONS.size(); i++){
            Slot slot = this.handler.slots.get(i);

            Point p = SLOT_POSITIONS.get(i);
            int m = x + p.x;
            int n = y + p.y;

            if (!slot.hasStack()){
                Identifier bgTex = SLOT_TEXTURES.get(i);
                if (bgTex != null){
                    context.drawTexture(bgTex, m, n, 0, 0, 16, 16, 16, 16);
                }
            }
        }
    }


    private void renderScrollbar (DrawContext context, int x, int y, GateCraftingRecipeList recipeList){
        int i = recipeList.size() + 1 - 7;
        if (i > 1){
            // Not yet fixed to match expected results. There are no recipes yet,
            // so this if statement is never entered until there are.
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }
            // Does match proper values, ignoring m, that likely needs tweaking
            context.drawGuiTexture(SCROLL_BAR,x+106, y+7+m, 0, 6, 27);
        } else {
            context.drawGuiTexture(SCROLL_BAR_DISABLED,x+106, y+7, 0, 6, 27);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int container_x = (this.width - this.backgroundWidth) / 2;
        int container_y = (this.height - this.backgroundHeight) / 2;

        GateCraftingRecipeList recipes = this.handler.getRecipes();
        if (!recipes.isEmpty()){
            this.renderScrollbar(context, container_x, container_y, recipes);

            int k = container_y + 16 + 1;

            for (int i = 0; i < recipes.size(); i++){
                GateCraftingRecipe recipe = recipes.get(i);
                if (!this.canScroll(recipes.size()) || i >= this.indexStartOffset && i < recipes.size() + this.indexStartOffset){
                    context.getMatrices().push();
                    context.getMatrices().translate(0.0F, 0.0F, 100.0F);

                    ItemStack[] items = new ItemStack[]{
                        recipe.getDisplayedBaseItem(),recipe.getDisplayedRedstoneItem(),
                        recipe.getDisplayedRedTorchItem(), recipe.getDisplayedExtenderItem(),
                        recipe.getDisplayedOutputItem()
                    };

                    int n = k + 2;
                    for (int item_i = 0; item_i < 5; item_i++){
                        ItemStack item = items[item_i];
                        context.drawItemWithoutEntity(item, 5 + RECIPE_SLOT_X_POSITIONS[item_i], n);
                        context.drawItemInSlot(this.textRenderer, item, 5 + RECIPE_SLOT_X_POSITIONS[item_i], n);
                    }

                    context.getMatrices().pop();

                    k += 20;
                }
            }

            for (RecipeButtonWidget recipeButtonWidget : this.recipes) {
                if (recipeButtonWidget.isSelected())
                    recipeButtonWidget.renderTooltip(context, mouseX, mouseY);

                recipeButtonWidget.visible = recipeButtonWidget.index < this.handler.getRecipes().size();
            }

            RenderSystem.enableDepthTest();
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private boolean canScroll(int listSize) {
        return listSize > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int i = this.handler.getRecipes().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.indexStartOffset = MathHelper.clamp((int)((double)this.indexStartOffset - verticalAmount), 0, j);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int i = this.handler.getRecipes().size();
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float)mouseY - (float)j - 13.5F) / ((float)(k - j) - 27.0F);
            f = f * (float)l + 0.5F;
            this.indexStartOffset = MathHelper.clamp((int)f, 0, l);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        if (this.canScroll(this.handler.getRecipes().size())
                && mouseX > (double)(i + 94)
                && mouseX < (double)(i + 94 + 6)
                && mouseY > (double)(j + 18)
                && mouseY <= (double)(j + 18 + 139 + 1)) {
            this.scrolling = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    //===============================================================================================

    @Environment(EnvType.CLIENT)
    class RecipeButtonWidget extends ButtonWidget {
        final int index;

        public RecipeButtonWidget(final int x, final int y, final int index, final ButtonWidget.PressAction onPress) {
            super(x, y, 88, 20, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderTooltip(DrawContext context, int x, int y) {
            TruthTableScreen screen = TruthTableScreen.this;

            if (!this.hovered || screen.handler.getRecipes().size() <= this.index + screen.indexStartOffset)
                return;

            GateCraftingRecipe recipe = screen.handler.getRecipes().get(this.index + screen.indexStartOffset);

            if (x < this.getX() + 18 && x >= this.getX() + 2)
                if (recipe.getDisplayedBaseItem() != ItemStack.EMPTY)
                    context.drawItemTooltip(screen.textRenderer, recipe.getDisplayedBaseItem(), x, y);
            else if (x < this.getX() + 38 && x >= this.getX() + 20)
                if (recipe.getDisplayedRedstoneItem() != ItemStack.EMPTY)
                    context.drawItemTooltip(screen.textRenderer, recipe.getDisplayedRedstoneItem(), x, y);
            else if (x < this.getX() + 58 && x >= this.getX() + 40)
                if (recipe.getDisplayedRedTorchItem() != ItemStack.EMPTY)
                    context.drawItemTooltip(screen.textRenderer, recipe.getDisplayedRedTorchItem(), x, y);
            else if (x < this.getX() + 78 && x >= this.getX() + 60)
                if (recipe.getDisplayedExtenderItem() != ItemStack.EMPTY)
                    context.drawItemTooltip(screen.textRenderer, recipe.getDisplayedExtenderItem(), x, y);
            else if (x < this.getX() + 112 && x >= this.getX() + 94)
                if (recipe.getDisplayedOutputItem() != ItemStack.EMPTY)
                    context.drawItemTooltip(screen.textRenderer, recipe.getDisplayedOutputItem(), x, y);
        }
    }
}
