package name.turingcomplete.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import name.turingcomplete.TuringComplete;
import name.turingcomplete.init.RecipeTypesInit;
import name.turingcomplete.network.c2s.SelectTruthTableCraftC2SPacket;
import name.turingcomplete.screen.TruthTableCraftList;
import name.turingcomplete.screen.TruthTableScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
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
import java.util.List;

import static name.turingcomplete.screen.TruthTableScreenHandler.SLOT_POSITIONS;

@Environment(EnvType.CLIENT)
public class TruthTableScreen extends HandledScreen<TruthTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/truth_table.png");
    private static final Identifier CRAFT_ARROW_TEXTURE = Identifier.of(TuringComplete.MOD_ID, "truth_table/craft_arrow");
    private static final Identifier SCROLL_BAR = Identifier.ofVanilla("container/villager/scroller");
    private static final Identifier SCROLL_BAR_DISABLED = Identifier.ofVanilla("container/villager/scroller_disabled");
    private static final List<Identifier> SLOT_TEXTURES = List.of(
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_base_plate_icon.png"),
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_redstone_icon.png"),
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_torch_icon.png"),
            Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/tr_ta_upgrade_icon.png")
    );

    private static final int CRAFT_BUTTON_HEIGHT = 20;
    private static final int CRAFT_BUTTON_WIDTH = 98;
    private static final int CRAFT_AREA_X_OFFSET = 7;
    private static final int CRAFT_AREA_Y_OFFSET = 7;

    private static final int CRAFT_ITEM_SIZE = 16;
    private static final int CRAFT_ARROW_SIZE = 7;
    private static final int CRAFT_ITEM_SPACING = 3;
    private static final int CRAFT_ITEM_SEPARATION = 1;

    private static final int CRAFT_LOGIC_PLATE_X = CRAFT_AREA_X_OFFSET + CRAFT_ITEM_SPACING;
    private static final int CRAFT_REDSTONE_X = CRAFT_LOGIC_PLATE_X + CRAFT_ITEM_SIZE + CRAFT_ITEM_SEPARATION;
    private static final int CRAFT_REDSTONE_TORCH_X = CRAFT_REDSTONE_X + CRAFT_ITEM_SIZE + CRAFT_ITEM_SEPARATION;
    private static final int CRAFT_UPGRADE_X = CRAFT_REDSTONE_TORCH_X + CRAFT_ITEM_SIZE + CRAFT_ITEM_SEPARATION;
    private static final int CRAFT_ARROW_X = CRAFT_UPGRADE_X + CRAFT_ITEM_SIZE + CRAFT_ITEM_SEPARATION;
    private static final int CRAFT_OUTPUT_X = CRAFT_ARROW_X + CRAFT_ARROW_SIZE + CRAFT_ITEM_SEPARATION;

    private static final int SCROLLBAR_HEIGHT = 27;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_AREA_HEIGHT = 132;
    private static final int SCROLLBAR_OFFSET_Y = 7;
    private static final int SCROLLBAR_OFFSET_X = 106;

    private static final int MAX_CRAFTS_ON_SCREEN = 7;
    private static final TruthTableCraftList RECIPES = getRecipes();

    private final CraftButtonWidget[] craft_buttons = new CraftButtonWidget[MAX_CRAFTS_ON_SCREEN];
    private int selectedIndex;
    private boolean scrolling;
    int indexStartOffset;

    
    public TruthTableScreen(TruthTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 320;
        this.backgroundHeight = 192;
    }


    @Override
    protected void init(){
        super.init();

        this.titleX = (backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.playerInventoryTitleX = this.titleX;
        this.playerInventoryTitleY = (this.playerInventoryTitleY - 10);

        final int ui_x = (this.width - this.backgroundWidth) / 2;
        final int ui_y = (this.height - this.backgroundHeight) / 2;

        final int button_x = ui_x + CRAFT_AREA_X_OFFSET;
        int button_y = ui_y + CRAFT_AREA_Y_OFFSET;

        for(int l = 0; l < MAX_CRAFTS_ON_SCREEN; ++l) {
            this.craft_buttons[l] = this.addDrawableChild(new CraftButtonWidget(button_x, button_y, l, button -> {
                if (button instanceof CraftButtonWidget craftButtonWidget) {
                    this.selectedIndex = craftButtonWidget.getIndex() + this.indexStartOffset;
                    this.syncRecipeIndex();
                }
            }));

            button_y += CRAFT_BUTTON_HEIGHT;
        }
    }


    private void syncRecipeIndex() {
        this.handler.setCraftIndex(this.selectedIndex);
        this.handler.switchTo(this.selectedIndex);

        if (this.client != null && this.client.getNetworkHandler() != null)
            ClientPlayNetworking.send(new SelectTruthTableCraftC2SPacket(this.selectedIndex));
    }


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

    private void renderScrollbar (DrawContext context, int x, int y){
        int i = RECIPES.size() + 1 - MAX_CRAFTS_ON_SCREEN;

        if (i > 1){
            // Not yet fixed to match expected results. There are no recipes yet,
            // so this if statement is never entered until there are.
            int j = SCROLLBAR_AREA_HEIGHT - (SCROLLBAR_HEIGHT + (i - 1) * SCROLLBAR_AREA_HEIGHT / i);
            int k = 1 + j / i + SCROLLBAR_AREA_HEIGHT / i;
            int m = Math.min(113, this.indexStartOffset * k);
            if (this.indexStartOffset == i - 1) {
                m = 113;
            }

            // Does match proper values, ignoring m, that likely needs tweaking
            context.drawGuiTexture(
                SCROLL_BAR,
                x+SCROLLBAR_OFFSET_X, y+SCROLLBAR_OFFSET_Y+m, 0,
                SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT
            );
        } else {
            context.drawGuiTexture(
                SCROLL_BAR_DISABLED,
                x+SCROLLBAR_OFFSET_X, y+SCROLLBAR_OFFSET_Y, 0,
                SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT
            );
        }
    }

    private void renderCraftButton(DrawContext context, int mouseX, int mouseY){
        for(var craftButton : this.craft_buttons) {
            if (craftButton.isSelected())
                craftButton.renderTooltip(context, mouseX, mouseY);

            craftButton.visible = craftButton.index < RECIPES.size();
        }
    }

    private boolean canScroll(int listSize) {
        return listSize > MAX_CRAFTS_ON_SCREEN;
    }

    private void renderArrow(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(
            CRAFT_ARROW_TEXTURE,
            x + CRAFT_ARROW_X,
            y + CRAFT_AREA_Y_OFFSET-2,
            0, CRAFT_ARROW_SIZE, 5
        );
    }

    private void renderCraftItems(DrawContext context, int menu_x, int menu_y, int mouseX, int mouseY){
        if (RECIPES.isEmpty()) return;

        var craft_y_offset = menu_y + CRAFT_AREA_Y_OFFSET;
        var craft_index = 0;

        for (var craft : RECIPES) {
            var can_scroll = this.canScroll(RECIPES.size());
            var inside_menu = craft_index >= this.indexStartOffset && craft_index < MAX_CRAFTS_ON_SCREEN + this.indexStartOffset;

            if (!can_scroll || inside_menu) {
                var logic_plates = craft.getLogicPlate();
                var redstone_dusts = craft.getRedstone();
                var redstone_torches = craft.getRedstoneTorch();
                var upgrades = craft.getUpgrade();
                var output = craft.getOutput();

                context.getMatrices().push();
                context.getMatrices().translate(0.0F, 0.0F, 100.0F);

                int item_y = craft_y_offset + 2;

                if (!logic_plates.isEmpty()) {
                    context.drawItemWithoutEntity(logic_plates, menu_x + CRAFT_LOGIC_PLATE_X, item_y);
                    context.drawItemInSlot(this.textRenderer, logic_plates, menu_x + CRAFT_LOGIC_PLATE_X, item_y);
                }

                if (!redstone_dusts.isEmpty()) {
                    context.drawItemWithoutEntity(redstone_dusts, menu_x + CRAFT_REDSTONE_X, item_y);
                    context.drawItemInSlot(this.textRenderer, redstone_dusts, menu_x + CRAFT_REDSTONE_X, item_y);
                }

                if (!redstone_torches.isEmpty()) {
                    context.drawItemWithoutEntity(redstone_torches, menu_x + CRAFT_REDSTONE_TORCH_X, item_y);
                    context.drawItemInSlot(this.textRenderer, redstone_torches, menu_x + CRAFT_REDSTONE_TORCH_X, item_y);
                }

                if (!upgrades.isEmpty()) {
                    context.drawItemWithoutEntity(upgrades, menu_x + CRAFT_UPGRADE_X, item_y);
                    context.drawItemInSlot(this.textRenderer, upgrades, menu_x + CRAFT_UPGRADE_X, item_y);
                }

                this.renderArrow(context, menu_x, item_y);

                context.drawItemWithoutEntity(output, menu_x + CRAFT_OUTPUT_X, item_y);
                context.drawItemInSlot(this.textRenderer, output, menu_x + CRAFT_OUTPUT_X, item_y);

                context.getMatrices().pop();

                craft_y_offset += 20;
            }

            ++craft_index;
        }

        for (var craft_button : this.craft_buttons) {
            if (craft_button.isSelected())
                craft_button.renderTooltip(context, mouseX, mouseY);

            craft_button.visible = craft_button.index < RECIPES.size();
        }

        RenderSystem.enableDepthTest();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int menu_x = (this.width - this.backgroundWidth) / 2;
        int menu_y = (this.height - this.backgroundHeight) / 2;

        this.renderScrollbar(context, menu_x, menu_y);
        this.renderCraftItems(context, menu_x, menu_y, mouseX, mouseY);
        this.renderCraftButton(context, mouseX, mouseY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.canScroll(RECIPES.size())) {
            int max_value = RECIPES.size() - MAX_CRAFTS_ON_SCREEN;
            this.indexStartOffset = MathHelper.clamp((int)(this.indexStartOffset - verticalAmount), 0, max_value);
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int scroll_y = this.y + SCROLLBAR_OFFSET_Y;
            int scroll_max_y = scroll_y + SCROLLBAR_AREA_HEIGHT;
            int max_val = RECIPES.size() - MAX_CRAFTS_ON_SCREEN;

            var index = (mouseY - scroll_y - SCROLLBAR_HEIGHT * 0.5f) / (scroll_max_y - scroll_y - SCROLLBAR_HEIGHT);
            index *= max_val + 0.5F;

            this.indexStartOffset = MathHelper.clamp((int) index, 0, max_val);
            return true;

        } else return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;

        int menu_x = (this.width - this.backgroundWidth) / 2;
        int menu_y = (this.height - this.backgroundHeight) / 2;

        var can_scroll = this.canScroll(RECIPES.size());
        var inside_scroll_x = mouseX > menu_x + SCROLLBAR_OFFSET_X && mouseX < menu_x + SCROLLBAR_OFFSET_X + SCROLLBAR_WIDTH;
        var inside_scroll_y = mouseY > menu_y + SCROLLBAR_OFFSET_Y && mouseY <= menu_y + SCROLLBAR_OFFSET_Y + SCROLLBAR_AREA_HEIGHT + 1;

        if (can_scroll && inside_scroll_x && inside_scroll_y)
            this.scrolling = true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // TODO: Get Player's Unlocked Recipes
    private static TruthTableCraftList getRecipes(){
        var client = MinecraftClient.getInstance();
        var player = client.player;
        var world = client.world;

        if (world == null || player == null)
            return new TruthTableCraftList();

        var manager = world.getRecipeManager();
        return TruthTableCraftList.fromRecipes(manager.listAllOfType(RecipeTypesInit.TRUTH_TABLE_RECIPE_TYPE));
    }

    @Environment(EnvType.CLIENT)
    class CraftButtonWidget extends ButtonWidget {
        final int index;

        public CraftButtonWidget(final int x, final int y, final int index, final ButtonWidget.PressAction onPress) {
            super(x, y, CRAFT_BUTTON_WIDTH, CRAFT_BUTTON_HEIGHT, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderTooltip(DrawContext context, int x, int y) {
            if (this.hovered && RECIPES.size() > this.index + TruthTableScreen.this.indexStartOffset) {
                var logic_plates = RECIPES.get(this.index + TruthTableScreen.this.indexStartOffset).getLogicPlate();
                var redstone_dusts = RECIPES.get(this.index + TruthTableScreen.this.indexStartOffset).getRedstone();
                var redstone_torches = RECIPES.get(this.index + TruthTableScreen.this.indexStartOffset).getRedstoneTorch();
                var upgrades = RECIPES.get(this.index + TruthTableScreen.this.indexStartOffset).getUpgrade();
                var output = RECIPES.get(this.index + TruthTableScreen.this.indexStartOffset).getOutput();

                tryRenderTooltip(context, logic_plates, CRAFT_LOGIC_PLATE_X, x, y);
                tryRenderTooltip(context, redstone_dusts, CRAFT_REDSTONE_X, x, y);
                tryRenderTooltip(context, redstone_torches, CRAFT_REDSTONE_TORCH_X, x, y);
                tryRenderTooltip(context, upgrades, CRAFT_UPGRADE_X, x, y);
                tryRenderTooltip(context, output, CRAFT_OUTPUT_X, x, y);
            }
        }

        private void tryRenderTooltip(DrawContext context, ItemStack stack, int pos, int x, int y){
            if (stack.isEmpty()) return;

            if (x >= this.getX() + pos && x < this.getX() + pos + CRAFT_ITEM_SIZE){
                context.drawItemTooltip(TruthTableScreen.this.textRenderer, stack, x, y);
            }
        }
    }
}
