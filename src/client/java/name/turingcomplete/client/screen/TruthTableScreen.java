package name.turingcomplete.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import name.turingcomplete.TuringComplete;
import name.turingcomplete.init.RecipeTypesInit;
import name.turingcomplete.network.c2s.SelectTruthTableCraftC2SPacket;
import name.turingcomplete.screen.truthtable.data.TruthTableCategory;
import name.turingcomplete.screen.truthtable.data.TruthTableCrafts;
import name.turingcomplete.screen.truthtable.TruthTableScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

import static name.turingcomplete.screen.truthtable.TruthTableScreenHandler.SLOT_POSITIONS;

@Environment(EnvType.CLIENT)
public class TruthTableScreen extends HandledScreen<TruthTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(TuringComplete.MOD_ID, "textures/gui/container/truth_table.png");
    private static final Identifier CRAFT_ARROW_TEXTURE = Identifier.of(TuringComplete.MOD_ID, "truth_table/craft_arrow");
    private static final Identifier SCROLL_BAR = Identifier.ofVanilla("container/villager/scroller");
    private static final Identifier SCROLL_BAR_DISABLED = Identifier.ofVanilla("container/villager/scroller_disabled");
    private static final ButtonTextures CATEGORY_TABS_TEXTURES = new ButtonTextures(
        TuringComplete.id("truth_table/tab/tab_selected"), TuringComplete.id("truth_table/tab/tab_unselected"),
        TuringComplete.id("truth_table/tab/tab_selected"), TuringComplete.id("truth_table/tab/tab_unselected")
    );
    private static final ButtonTextures CATEGORY_PREVIOUS_TEXTURES = new ButtonTextures(
        TuringComplete.id("truth_table/tab/previous_tab_active_unselected"), TuringComplete.id("truth_table/tab/previous_tab_disabled"),
        TuringComplete.id("truth_table/tab/previous_tab_active_selected"), TuringComplete.id("truth_table/tab/previous_tab_disabled")
    );
    private static final ButtonTextures CATEGORY_NEXT_TEXTURES = new ButtonTextures(
        TuringComplete.id("truth_table/tab/next_tab_active_unselected"), TuringComplete.id("truth_table/tab/next_tab_disabled"),
        TuringComplete.id("truth_table/tab/next_tab_active_selected"), TuringComplete.id("truth_table/tab/next_tab_disabled")
    );
    private static final List<Identifier> SLOT_TEXTURES = List.of(
        Identifier.of(TuringComplete.MOD_ID, "truth_table/slots/base_plate_icon"),
        Identifier.of(TuringComplete.MOD_ID, "truth_table/slots/redstone_icon"),
        Identifier.of(TuringComplete.MOD_ID, "truth_table/slots/torch_icon"),
        Identifier.of(TuringComplete.MOD_ID, "truth_table/slots/upgrade_icon")
    );

    private static final int CATEGORY_TAB_HEIGHT = 20;
    private static final int CATEGORY_TAB_WIDTH = 20;
    private static final int CATEGORY_TAB_X_OFFSET = 17;
    private static final int CATEGORY_TAB_Y_OFFSET = -17;
    private static final int CATEGORY_TAB_SEPARATION = 1;

    private static final int CATEGORY_PREVIOUS_X_OFFSET = 4;
    private static final int CATEGORY_NEXT_X_OFFSET = 103;
    private static final int CATEGORY_CHANGE_Y_OFFSET = -13;
    private static final int CATEGORY_CHANGE_WIDTH = 12;
    private static final int CATEGORY_CHANGE_HEIGHT = 12;

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
    private static final int MAX_CATEGORIES_ON_SCREEN = 4;

    private final TruthTableCrafts CRAFTS = getCrafts();

    private final CraftButtonWidget[] craft_buttons = new CraftButtonWidget[MAX_CRAFTS_ON_SCREEN];
    private final CategoryTabWidget[] category_tabs = new CategoryTabWidget[MAX_CATEGORIES_ON_SCREEN];
    private final CategoryChangerButtonWidget[] category_change_buttons = new CategoryChangerButtonWidget[2];

    private TruthTableCategory selectedCategory = TruthTableCategory.AND_GATES;
    private boolean scrolling;
    private int selectedIndex;
    int craftIndexOffset;
    int categoryIndexOffset;

    // INIT
    //======================================================

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

        final int menu_x = (this.width - this.backgroundWidth) / 2;
        final int menu_y = (this.height - this.backgroundHeight) / 2;

        this.initCraftButtons(menu_x, menu_y);
        this.initCategoryTabs(menu_x,menu_y);
    }

    private void initCraftButtons(int menu_x, int menu_y){
        final int button_x = menu_x + CRAFT_AREA_X_OFFSET;
        int button_y = menu_y + CRAFT_AREA_Y_OFFSET;

        for(int l = 0; l < MAX_CRAFTS_ON_SCREEN; ++l) {
            this.craft_buttons[l] = this.addDrawableChild(new CraftButtonWidget(button_x, button_y, l, button -> {
                if (button instanceof CraftButtonWidget craftButtonWidget) {
                    this.selectedIndex = craftButtonWidget.getIndex() + this.craftIndexOffset;
                    this.syncRecipeIndex();
                }
            }));

            button_y += CRAFT_BUTTON_HEIGHT;
        }
    }

    private void initCategoryTabs(int menu_x, int menu_y){
        int button_x = menu_x + CATEGORY_TAB_X_OFFSET;
        final int button_y = menu_y + CATEGORY_TAB_Y_OFFSET;

        for(int l = 0; l < MAX_CATEGORIES_ON_SCREEN; ++l) {
            var tab = new CategoryTabWidget(button_x, button_y, l);
            tab.setToggled(l == 0);

            this.category_tabs[l] = this.addDrawableChild(tab);

            button_x += CATEGORY_TAB_WIDTH + CATEGORY_TAB_SEPARATION;
        }

        this.category_change_buttons[0] = this.addDrawableChild(new CategoryChangerButtonWidget(
            menu_x + CATEGORY_PREVIOUS_X_OFFSET, menu_y + CATEGORY_CHANGE_Y_OFFSET,
            -1, CATEGORY_PREVIOUS_TEXTURES,
            () -> this.categoryIndexOffset > 0
        ));

        this.category_change_buttons[1] = this.addDrawableChild(new CategoryChangerButtonWidget(
            menu_x + CATEGORY_NEXT_X_OFFSET, menu_y + CATEGORY_CHANGE_Y_OFFSET,
            1, CATEGORY_NEXT_TEXTURES,
            () -> this.categoryIndexOffset < Math.ceil(TruthTableCategory.values().length / (float)MAX_CATEGORIES_ON_SCREEN) - 1
        ));

        for (var button : category_change_buttons)
            button.updateState();
    }

    // HELPER METHODS
    //======================================================

    private void syncRecipeIndex() {
        this.handler.setCraftIndex(this.selectedIndex);
        this.handler.setSelectedCategory(this.selectedCategory);
        this.handler.updateInventory();
        this.handler.switchTo(this.selectedCategory, this.selectedIndex);

        if (this.client != null && this.client.getNetworkHandler() != null)
            ClientPlayNetworking.send(new SelectTruthTableCraftC2SPacket(this.selectedCategory,this.selectedIndex));
    }

    private boolean canScroll(int listSize) {
        return listSize > MAX_CRAFTS_ON_SCREEN;
    }

    // TODO: Get Player's Unlocked Recipes
    private static TruthTableCrafts getCrafts(){
        var client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) return new TruthTableCrafts();

        var manager = client.getNetworkHandler().getRecipeManager();
        return TruthTableCrafts.fromRecipes(manager.listAllOfType(RecipeTypesInit.TRUTH_TABLE_RECIPE_TYPE));
    }

    // RENDER METHODS
    //======================================================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int menu_x = (this.width - this.backgroundWidth) / 2;
        int menu_y = (this.height - this.backgroundHeight) / 2;

        this.renderScrollbar(context, menu_x, menu_y);
        this.renderCraftItems(context, menu_x, menu_y, mouseX, mouseY);
        this.renderCraftButton(context, mouseX, mouseY);
        this.renderCategoryTabs(context, mouseX, menu_y);
    }

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
                    context.drawGuiTexture(bgTex, m, n,1,16, 16);
                }
            }
        }
    }

    private void renderScrollbar (DrawContext context, int x, int y){
        if (!CRAFTS.containsKey(selectedCategory)) return;
        var craft_list = CRAFTS.get(selectedCategory);

        int i = craft_list.size() + 1 - MAX_CRAFTS_ON_SCREEN;

        if (i > 1){
            // Not yet fixed to match expected results. There are no recipes yet,
            // so this if statement is never entered until there are.
            int j = SCROLLBAR_AREA_HEIGHT - (SCROLLBAR_HEIGHT + (i - 1) * SCROLLBAR_AREA_HEIGHT / i);
            int k = 1 + j / i + SCROLLBAR_AREA_HEIGHT / i;
            int m = Math.min(113, this.craftIndexOffset * k);
            if (this.craftIndexOffset == i - 1) {
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
        if (!CRAFTS.containsKey(selectedCategory)) return;
        var craft_list = CRAFTS.get(selectedCategory);

        for(var craftButton : this.craft_buttons) {
            if (craftButton.isSelected())
                craftButton.renderTooltip(context, mouseX, mouseY);

            craftButton.visible = craftButton.index < craft_list.size();
        }
    }

    private void renderCategoryTabs(DrawContext context, int mouseX, int mouseY){
        for(var categoryTab : this.category_tabs) {
            if (categoryTab.isSelected())
                categoryTab.renderTooltip(context, mouseX, mouseY);

            categoryTab.updateState();
            categoryTab.visible = categoryTab.index + this.categoryIndexOffset < CRAFTS.size();
        }
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
        if (CRAFTS.isEmpty()) return;

        if (!CRAFTS.containsKey(selectedCategory)) return;
        var craft_list = CRAFTS.get(selectedCategory);

        if (craft_list.isEmpty()) return;

        var craft_y_offset = menu_y + CRAFT_AREA_Y_OFFSET;
        var craft_index = 0;

        for (var craft : craft_list) {
            var can_scroll = this.canScroll(craft_list.size());
            var inside_menu = craft_index >= this.craftIndexOffset && craft_index < MAX_CRAFTS_ON_SCREEN + this.craftIndexOffset;

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

        RenderSystem.enableDepthTest();
    }

    // MOUSE HANDLING
    //======================================================

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!CRAFTS.containsKey(selectedCategory)) return false;
        var craft_list = CRAFTS.get(selectedCategory);

        if (this.canScroll(craft_list.size())) {
            int max_value = craft_list.size() - MAX_CRAFTS_ON_SCREEN;
            this.craftIndexOffset = MathHelper.clamp((int)(this.craftIndexOffset - verticalAmount), 0, max_value);
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            if (!CRAFTS.containsKey(selectedCategory)) return super.mouseDragged(mouseX,mouseY, button, deltaX, deltaY);
            var craft_list = CRAFTS.get(selectedCategory);

            int scroll_y = this.y + SCROLLBAR_OFFSET_Y;
            int scroll_max_y = scroll_y + SCROLLBAR_AREA_HEIGHT;
            int max_val = craft_list.size() - MAX_CRAFTS_ON_SCREEN;

            var index = (mouseY - scroll_y - SCROLLBAR_HEIGHT * 0.5f) / (scroll_max_y - scroll_y - SCROLLBAR_HEIGHT);
            index *= max_val + 0.5F;

            this.craftIndexOffset = MathHelper.clamp((int) index, 0, max_val);
            return true;

        } else return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!CRAFTS.containsKey(selectedCategory)) return super.mouseClicked(mouseX,mouseY,button);
        var craft_list = CRAFTS.get(selectedCategory);

        this.scrolling = false;

        int menu_x = (this.width - this.backgroundWidth) / 2;
        int menu_y = (this.height - this.backgroundHeight) / 2;

        var can_scroll = this.canScroll(craft_list.size());
        var inside_scroll_x = mouseX > menu_x + SCROLLBAR_OFFSET_X && mouseX < menu_x + SCROLLBAR_OFFSET_X + SCROLLBAR_WIDTH;
        var inside_scroll_y = mouseY > menu_y + SCROLLBAR_OFFSET_Y && mouseY <= menu_y + SCROLLBAR_OFFSET_Y + SCROLLBAR_AREA_HEIGHT + 1;

        if (can_scroll && inside_scroll_x && inside_scroll_y)
            this.scrolling = true;

        return super.mouseClicked(mouseX, mouseY, button);
    }


    // WIDGET CLASSES
    //======================================================

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
            if (!CRAFTS.containsKey(selectedCategory)) return;
            var craft_list = CRAFTS.get(selectedCategory);

            if (this.hovered && craft_list.size() > this.index + craftIndexOffset) {
                var logic_plates = craft_list.get(this.index + craftIndexOffset).getLogicPlate();
                var redstone_dusts = craft_list.get(this.index + craftIndexOffset).getRedstone();
                var redstone_torches = craft_list.get(this.index + craftIndexOffset).getRedstoneTorch();
                var upgrades = craft_list.get(this.index + craftIndexOffset).getUpgrade();
                var output = craft_list.get(this.index + craftIndexOffset).getOutput();

                tryRenderTooltip(context, logic_plates, CRAFT_LOGIC_PLATE_X, x, y);
                tryRenderTooltip(context, redstone_dusts, CRAFT_REDSTONE_X, x, y);
                tryRenderTooltip(context, redstone_torches, CRAFT_REDSTONE_TORCH_X, x, y);
                tryRenderTooltip(context, upgrades, CRAFT_UPGRADE_X, x, y);
                tryRenderTooltip(context, output, CRAFT_OUTPUT_X, x, y);
            }
        }

        private void tryRenderTooltip(DrawContext context, ItemStack stack, int pos, int x, int y){
            if (stack.isEmpty()) return;

            var x_pos = this.getX() + pos - CRAFT_AREA_X_OFFSET;
            if (x >= x_pos && x < x_pos + CRAFT_ITEM_SIZE){
                context.drawItemTooltip(textRenderer, stack, x, y);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    class CategoryTabWidget extends ToggleButtonWidget {
        private final int index;

        public CategoryTabWidget(int x, int y, int index) {
            super(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT, false);
            setTextures(CATEGORY_TABS_TEXTURES);

            this.index = index;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (this.isToggled()) return;

            var category_index = this.index + categoryIndexOffset;
            if (category_index < 0 || category_index >= TruthTableCategory.values().length)
                return;

            selectedCategory = TruthTableCategory.values()[category_index];
            selectedIndex = 0;
            syncRecipeIndex();
        }

        public void updateState(){
            var category_index = this.index + categoryIndexOffset;
            if (category_index < 0 || category_index >= TruthTableCategory.values().length) {
                this.toggled = false;
                return;
            }

            this.toggled = selectedCategory == TruthTableCategory.values()[category_index];
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);

            var category_index = this.index + categoryIndexOffset;
            if (category_index < 0 || category_index >= TruthTableCategory.values().length)
                return;

            var category = TruthTableCategory.values()[category_index];
            context.drawItemWithoutEntity(category.getIcon(), this.getX() + 2, this.getY() + 2);
        }

        public void renderTooltip(DrawContext context, int x, int y) {
            if (this.hovered && CRAFTS.size() > this.index + categoryIndexOffset) {
                var category_index = this.index + categoryIndexOffset;
                if (category_index < 0 || category_index >= TruthTableCategory.values().length)
                    return;

                var category = TruthTableCategory.values()[category_index];

                context.drawTooltip(
                    textRenderer,
                    category.getDisplayName(),
                    x, y
                );
            }
        }
    }

    @Environment(EnvType.CLIENT)
    class CategoryChangerButtonWidget extends ClickableWidget {
        private final ButtonTextures TEXTURES;
        private final Supplier<Boolean> shouldBeActive;
        private final int changeAmount;

        protected CategoryChangerButtonWidget(int x, int y, int changeAmount, ButtonTextures textures, Supplier<Boolean> shouldBeActive) {
            super(x, y, CATEGORY_CHANGE_WIDTH, CATEGORY_CHANGE_HEIGHT, Text.empty());
            this.TEXTURES = textures;
            this.shouldBeActive = shouldBeActive;
            this.changeAmount = changeAmount;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (!shouldBeActive.get()) return;

            categoryIndexOffset += changeAmount;

            for(var button : category_change_buttons)
                button.updateState();
        }

        public void updateState(){
            this.active = shouldBeActive.get();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            context.drawGuiTexture(
                TEXTURES.get(this.active, this.isHovered()),
                this.getX(), this.getY(), this.getWidth(), this.getHeight()
            );
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }
}
