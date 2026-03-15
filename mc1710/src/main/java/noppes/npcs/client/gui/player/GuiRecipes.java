package noppes.npcs.client.gui.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiRecipes extends GuiNPCInterface {
    private static final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/slot.png");
    private int page = 0;
    private GuiNpcLabel label;
    private GuiNpcButton left, right;
    private List<IRecipe> recipes = new ArrayList<IRecipe>();
    private String search = "";

    public GuiRecipes() {
        this.ySize = 182;
        this.xSize = 256;
        setBackground("recipes.png");
        this.closeOnEsc = true;
        // TODO: Preloads every carpentry recipe from RecipeController (full dataset); this book is open to any player, so no
        //       explicit permission is checked when accessing it.
        recipes.addAll(RecipeController.Instance.carpentryRecipes.values());
    }

    @Override
    public void initGui() {
        super.initGui();
        guiTop += 10;

        addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 2, guiTop - 25, 250, 20, search));

        addLabel(new GuiNpcLabel(0, "Recipe List", guiLeft + 5, guiTop + 5));
        addLabel(label = new GuiNpcLabel(1, "", guiLeft + 5, guiTop + 168));

        addButton(this.left = new GuiButtonNextPage(1, guiLeft + 150, guiTop + 164, true));
        addButton(this.right = new GuiButtonNextPage(2, guiLeft + 80, guiTop + 164, false));

        updateButton();
    }

    private void updateButton() {
        right.visible = right.enabled = page > 0;
        left.visible = left.enabled = page + 1 < MathHelper.ceiling_float_int(recipes.size() / 4f);
    }

    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (search.equals(getTextField(3).getText()))
            return;
        search = getTextField(3).getText().toLowerCase();
        recipes = getSearchList();
        updateButton();
    }

    private List<IRecipe> getSearchList() {
        if (search.isEmpty()) {
            // TODO: Returns the entire carpentry recipe collection from RecipeController; again this is unrestricted player
            //       content with no CustomNpcsPermissions gating.
            return new ArrayList<IRecipe>(RecipeController.Instance.carpentryRecipes.values());
        }
        List<IRecipe> list = new ArrayList<IRecipe>();
        // TODO: Iterates over the RecipeController carpentry map to filter recipes; this viewing GUI is available to all
        //       players without special permissions.
        for (IRecipe recipe : RecipeController.Instance.carpentryRecipes.values()) {
            if (recipe.getRecipeOutput() == null)
                continue;

            if (recipe.getRecipeOutput().getDisplayName() == null)
                continue;

            if (recipe.getRecipeOutput().getDisplayName().trim().equals(""))
                continue;

            if (recipe.getRecipeOutput().getDisplayName().toLowerCase().contains(search.toLowerCase()))
                list.add(recipe);
        }
        return list;
    }

    protected void actionPerformed(GuiButton button) {
        if (!button.enabled)
            return;

        if (button == right)
            page--;
        if (button == left)
            page++;
        updateButton();
    }

    private static class ItemOverlayData {
        public final int x, y;
        public final ItemStack item;

        public ItemOverlayData(int x, int y, ItemStack item) {
            this.x = x;
            this.y = y;
            this.item = item;
        }
    }

    private static class TextOverlayData {
        public final int x, y, width, height;
        public final List<String> textLines;

        public TextOverlayData(int x, int y, int width, int height, String text) {
            this(x, y, width, height, Collections.singletonList(text));
        }

        public TextOverlayData(int x, int y, int width, int height, List<String> textLines) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textLines = textLines;
        }
    }

    @Override
    public void drawScreen(int xMouse, int yMouse, float f) {
        super.drawScreen(xMouse, yMouse, f);
        mc.renderEngine.bindTexture(resource);

        label.label = page + 1 + "/" + MathHelper.ceiling_float_int(recipes.size() / 4f);
        label.x = guiLeft + (256 - Minecraft.getMinecraft().fontRenderer.getStringWidth(label.label)) / 2;

        List<ItemOverlayData> itemOverlays = new ArrayList<ItemOverlayData>();
        List<TextOverlayData> textOverlays = new ArrayList<TextOverlayData>();

        // Loop through recipes.
        for (int i = 0; i < 4; i++) {
            int index = i + page * 4;
            if (index >= recipes.size())
                break;
            IRecipe irecipe = recipes.get(index);
            if (irecipe.getRecipeOutput() == null)
                continue;
            int x = guiLeft + 5 + (i / 2) * 126;
            int y = guiTop + 15 + (i % 2) * 76;

            drawItem(irecipe.getRecipeOutput(), x + 98, y + 28, xMouse, yMouse);
            if (func_146978_c((x + 98) - guiLeft, (y + 28) - guiTop, 16, 16, xMouse, yMouse)) {
                itemOverlays.add(new ItemOverlayData(x + 98, y + 28, irecipe.getRecipeOutput()));
            }

            if (irecipe instanceof RecipeCarpentry) {
                RecipeCarpentry recipe = (RecipeCarpentry) irecipe;
                if (!recipe.isGlobal && recipe.availability != null && !recipe.availability.isDefault()) {
                    int outputX = x + 98;
                    int outputY = y + 38;
                    float scale = 1.0F;
                    String iconStr;
                    int iconColor;
                    if (!recipe.availability.isAvailable(mc.thePlayer)) {
                        iconStr = "?";
                        iconColor = 0xbd3e35;
                    } else {
                        iconStr = "!";
                        iconColor = CustomNpcResourceListener.DefaultTextColor;
                    }
                    int iconWidth = (int) (fontRendererObj.getStringWidth(iconStr) * scale);
                    int iconHeight = (int) (fontRendererObj.FONT_HEIGHT * scale);
                    // Center the icon below the output slot (16x16 slot).
                    int iconX = outputX + (16 - iconWidth) / 2;
                    int iconY = outputY + 16;

                    // Render the icon.
                    GL11.glPushMatrix();
                    GL11.glTranslatef(iconX, iconY, 0);
                    GL11.glScalef(scale, scale, 1.0F);
                    fontRendererObj.drawString(iconStr, 0, 0, iconColor);
                    GL11.glPopMatrix();

                    // Instead of rendering the tooltip immediately, add it as a text overlay.
                    if (func_146978_c(iconX - guiLeft, iconY - guiTop, iconWidth, iconHeight, xMouse, yMouse)) {
                        List<String> tooltipLines = recipe.availability.isAvailableText(mc.thePlayer);
                        if (tooltipLines.isEmpty()) {
                            tooltipLines.add(StatCollector.translateToLocal("gui.available"));
                        }
                        textOverlays.add(new TextOverlayData(iconX, iconY, iconWidth, iconHeight, tooltipLines));
                    }
                }
            }

            // Draw the recipe grid (for RecipeCarpentry recipes).
            if (irecipe instanceof RecipeCarpentry) {
                RecipeCarpentry recipe = (RecipeCarpentry) irecipe;
                int gridX = x + (72 - recipe.recipeWidth * 18) / 2;
                int gridY = y + (72 - recipe.recipeHeight * 18) / 2;
                for (int j = 0; j < recipe.recipeWidth; j++) {
                    for (int k = 0; k < recipe.recipeHeight; k++) {
                        mc.renderEngine.bindTexture(resource);
                        GL11.glColor4f(1, 1, 1, 1);
                        drawTexturedModalRect(gridX + j * 18, gridY + k * 18, 0, 0, 18, 18);
                        ItemStack item = recipe.getCraftingItem(j + k * recipe.recipeWidth);
                        if (item == null)
                            continue;
                        drawItem(item, gridX + j * 18 + 1, gridY + k * 18 + 1, xMouse, yMouse);
                        if (func_146978_c((gridX + j * 18 + 1) - guiLeft, (gridY + k * 18 + 1) - guiTop, 16, 16, xMouse, yMouse)) {
                            itemOverlays.add(new ItemOverlayData(gridX + j * 18 + 1, gridY + k * 18 + 1, item));
                        }
                    }
                }
            }
        }

        // Render any item tooltips.
        for (ItemOverlayData iod : itemOverlays) {
            this.renderToolTip(iod.item, xMouse, yMouse);
        }
        // Render all text overlays (each line from the availability tooltip will be on its own line).
        for (TextOverlayData tod : textOverlays) {
            this.drawHoveringText(tod.textLines, xMouse, yMouse, this.fontRendererObj);
        }
    }

    private void drawItem(ItemStack item, int x, int y, int xMouse, int yMouse) {
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.zLevel = 100.0F;
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item, x, y);
        itemRender.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    private void drawOverlay(ItemStack item, int x, int y, int xMouse, int yMouse) {
        if (this.func_146978_c(x - guiLeft, y - guiTop, 16, 16, xMouse, yMouse)) {
            this.renderToolTip(item, xMouse, yMouse);
        }
    }

    protected boolean func_146978_c(int p_146978_1_, int p_146978_2_, int p_146978_3_, int p_146978_4_, int p_146978_5_, int p_146978_6_) {
        int k1 = this.guiLeft;
        int l1 = this.guiTop;
        p_146978_5_ -= k1;
        p_146978_6_ -= l1;
        return p_146978_5_ >= p_146978_1_ - 1 && p_146978_5_ < p_146978_1_ + p_146978_3_ + 1 && p_146978_6_ >= p_146978_2_ - 1 && p_146978_6_ < p_146978_2_ + p_146978_4_ + 1;
    }

    @Override
    public void save() {
    }
}
