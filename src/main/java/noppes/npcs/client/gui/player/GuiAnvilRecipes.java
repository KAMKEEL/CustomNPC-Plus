package noppes.npcs.client.gui.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
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
import noppes.npcs.controllers.data.RecipeAnvil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiAnvilRecipes extends GuiNPCInterface {

    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/slot.png");

    private int page = 0;
    private GuiNpcLabel lblPage;
    private GuiNpcButton btnLeft, btnRight;
    private List<RecipeAnvil> recipes = new ArrayList<RecipeAnvil>();
    private String search = "";

    public GuiAnvilRecipes() {
        this.xSize = 256;
        this.ySize = 182;
        setBackground("recipesAnvil.png");
        this.closeOnEsc = true;

        recipes.addAll(RecipeController.Instance.anvilRecipes.values());
    }

    @Override
    public void initGui() {
        super.initGui();
        guiTop += 10;
        // Add search text field (ID 3) at top.
        addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 2, guiTop - 25, 250, 20, search));

        addLabel(new GuiNpcLabel(0, "Anvil Recipe List", guiLeft + 5, guiTop + 5));
        addLabel(lblPage = new GuiNpcLabel(1, "", guiLeft + 5, guiTop + 168));

        addButton(this.btnLeft = new GuiButtonNextPage(1, guiLeft + 150, guiTop + 164, true));
        addButton(this.btnRight = new GuiButtonNextPage(2, guiLeft + 80, guiTop + 164, false));

        updateButton();
    }

    private void updateButton() {
        // We want 8 recipes per page.
        int maxPages = MathHelper.ceiling_float_int(recipes.size() / 8.0F);
        btnRight.visible = btnRight.enabled = page > 0;
        btnLeft.visible = btnLeft.enabled = page < maxPages - 1;
        lblPage.label = (page + 1) + "/" + maxPages;
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (search.equals(getTextField(3).getText()))
            return;
        search = getTextField(3).getText().toLowerCase();
        recipes = getSearchList();
        page = 0;
        updateButton();
    }

    private List<RecipeAnvil> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<RecipeAnvil>(RecipeController.Instance.anvilRecipes.values());
        }
        List<RecipeAnvil> list = new ArrayList<RecipeAnvil>();
        for (RecipeAnvil recipe : RecipeController.Instance.anvilRecipes.values()) {
            ItemStack repairItem = recipe.itemToRepair;
            if (repairItem == null)
                continue;
            if (repairItem.getDisplayName() == null)
                continue;
            if (repairItem.getDisplayName().trim().isEmpty())
                continue;
            if (repairItem.getDisplayName().toLowerCase().contains(search))
                list.add(recipe);
        }
        return list;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled)
            return;
        if (button == btnRight) {
            if (page > 0)
                page--;
        }
        if (button == btnLeft) {
            int maxPages = MathHelper.ceiling_float_int(recipes.size() / 8.0F);
            if (page < maxPages - 1)
                page++;
        }
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Update page label.
        lblPage.label = (page + 1) + "/" + MathHelper.ceiling_float_int(recipes.size() / 8.0F);
        lblPage.x = guiLeft + (xSize - Minecraft.getMinecraft().fontRenderer.getStringWidth(lblPage.label)) / 2;

        int recipesPerPage = 8;
        int startIndex = page * recipesPerPage;
        int endIndex = Math.min(recipes.size(), startIndex + recipesPerPage);

        // Base positions relative to guiLeft and guiTop.
        int baseX = guiLeft + 4;
        int baseY = guiTop + 28;   // first row = guiTop + 28
        int colSpacing = 124;      // horizontal spacing between columns
        int rowSpacing = 35;       // vertical spacing so row1 = guiTop + 28 + 35

        // Create lists to collect overlay data.
        List<ItemOverlayData> itemOverlays = new ArrayList<ItemOverlayData>();
        List<TextOverlayData> textOverlays = new ArrayList<TextOverlayData>();

        for (int i = startIndex; i < endIndex; i++) {
            RecipeAnvil recipe = recipes.get(i);
            if (!recipe.isValid())
                continue;

            int localIndex = i - startIndex; // 0 to 7
            int col = localIndex / 4;         // 0 for left column, 1 for right column
            int row = localIndex % 4;         // rows 0..3

            int slotX = baseX + col * colSpacing;
            int slotY = baseY + row * rowSpacing;

            // Draw slot backgrounds for two input slots.
            mc.renderEngine.bindTexture(SLOT_TEXTURE);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            // Left slot (item to repair)
            drawTexturedModalRect(slotX, slotY, 0, 0, 18, 18);
            // Middle slot (repair material)
            drawTexturedModalRect(slotX + 22, slotY, 0, 0, 18, 18);

            // Draw the item-to-repair at 50% durability.
            ItemStack toRepair = recipe.itemToRepair;
            ItemStack halfItem = null;
            if (toRepair != null) {
                halfItem = toRepair.copy();
                if (halfItem.isItemStackDamageable()) {
                    int halfDamage = halfItem.getMaxDamage() / 2;
                    halfItem.setItemDamage(halfDamage);
                }
                drawItem(halfItem, slotX + 1, slotY + 1, mouseX, mouseY);
                // Instead of drawing overlay immediately, add to list.
                if (func_146978_c((slotX + 1) - guiLeft, (slotY + 1) - guiTop, 16, 16, mouseX, mouseY)) {
                    itemOverlays.add(new ItemOverlayData(slotX + 1, slotY + 1, halfItem));
                }
            }

            // Draw the repair material.
            ItemStack material = recipe.repairMaterial;
            if (material != null) {
                drawItem(material, slotX + 23, slotY + 1, mouseX, mouseY);
                if (func_146978_c((slotX + 23) - guiLeft, (slotY + 1) - guiTop, 16, 16, mouseX, mouseY)) {
                    itemOverlays.add(new ItemOverlayData(slotX + 23, slotY + 1, material));
                }
            }

            // Draw the output item fully repaired (damage = 0).
            int outputX = (col == 0) ? (guiLeft + 103) : (guiLeft + 229);
            ItemStack fullOutput = null;
            if (recipe.itemToRepair != null) {
                fullOutput = recipe.itemToRepair.copy();
                if (fullOutput.isItemStackDamageable()) {
                    fullOutput.setItemDamage(0);
                }
                drawItem(fullOutput, outputX, slotY, mouseX, mouseY);
                if (func_146978_c(outputX - guiLeft, slotY - guiTop, 16, 16, mouseX, mouseY)) {
                    itemOverlays.add(new ItemOverlayData(outputX, slotY, fullOutput));
                }
            }

            // Draw the percentage text.
            int percentCenterX = (col == 0) ? (guiLeft + 57) : (guiLeft + 57 + colSpacing);
            String percentText = Math.round(recipe.getRepairPercentage()) + "%";
            int percentWidth = fontRendererObj.getStringWidth(percentText);
            int percentTextX = percentCenterX - (percentWidth / 2);
            int percentTextY = slotY + 4 + row;
            fontRendererObj.drawString(percentText, percentTextX, percentTextY, CustomNpcResourceListener.DefaultTextColor);
            // Add text overlay for percentage with the full unrounded value.
            if (func_146978_c(percentTextX - guiLeft, percentTextY - guiTop, percentWidth, fontRendererObj.FONT_HEIGHT, mouseX, mouseY)) {
                textOverlays.add(new TextOverlayData(percentTextX, percentTextY, percentWidth, fontRendererObj.FONT_HEIGHT, String.valueOf(recipe.getRepairPercentage())));
            }

            if (!recipe.availability.isDefault()) {
                if (!recipe.availability.isAvailable(this.player)) {
                    String helpChar = "?";
                    float scale = 1F;  // Adjust for a very small size
                    int questionWidth = (int) (fontRendererObj.getStringWidth(helpChar) * scale);
                    int questionHeight = (int) (fontRendererObj.FONT_HEIGHT * scale);
                    // Center the "?" under the percentage text.
                    int questionX = percentCenterX - (questionWidth / 2) - 1 - row;
                    int questionY = percentTextY + fontRendererObj.FONT_HEIGHT; // You can tweak the vertical offset if needed

                    GL11.glPushMatrix();
                    GL11.glTranslatef(questionX, questionY, 0);
                    GL11.glScalef(scale, scale, 1.0F);

                    fontRendererObj.drawString(helpChar, 0, 0, 0xbd3e35);
                    GL11.glPopMatrix();

                    if (func_146978_c(questionX - guiLeft, questionY - guiTop, questionWidth, questionHeight, mouseX, mouseY)) {
                        List<String> availableText = recipe.availability.isAvailableText(this.player);
                        textOverlays.add(new TextOverlayData(questionX, questionY, questionWidth, questionHeight, availableText));
                    }
                } else {
                    // Draw a very small "?" under the percentage text.
                    String helpChar = "!";
                    float scale = 1F;  // Adjust for a very small size
                    int questionWidth = (int) (fontRendererObj.getStringWidth(helpChar) * scale);
                    int questionHeight = (int) (fontRendererObj.FONT_HEIGHT * scale);
                    // Center the "?" under the percentage text.
                    int questionX = percentCenterX - (questionWidth / 2) - 1 - row;
                    int questionY = percentTextY + fontRendererObj.FONT_HEIGHT; // You can tweak the vertical offset if needed

                    GL11.glPushMatrix();
                    GL11.glTranslatef(questionX, questionY, 0);
                    GL11.glScalef(scale, scale, 1.0F);

                    fontRendererObj.drawString(helpChar, 0, 0, CustomNpcResourceListener.DefaultTextColor);
                    GL11.glPopMatrix();

                    // When the "!" is hovered, add its tooltip overlay.
                    if (func_146978_c(questionX - guiLeft, questionY - guiTop, questionWidth, questionHeight, mouseX, mouseY)) {
                        textOverlays.add(new TextOverlayData(questionX, questionY, questionWidth, questionHeight, StatCollector.translateToLocal("gui.available")));
                    }
                }
            }

            // Draw XP cost with scaling.
            String xpCostStr = formatXpCost(recipe.getXpCost());
            int digitCount = xpCostStr.length();
            float xpScale = 1.0F - 0.1F * (digitCount - 1);
            int rawTextWidth = fontRendererObj.getStringWidth(xpCostStr);
            int scaledTextWidth = (int) (rawTextWidth * xpScale);
            int xpCenterX = (col == 0) ? (guiLeft + 76) : (guiLeft + 76 + colSpacing);
            int xpTextX = xpCenterX - (scaledTextWidth / 2) + col;
            int xpTextY = slotY - 4 + row;
            GL11.glPushMatrix();
            GL11.glTranslatef(xpTextX, xpTextY, 0);
            GL11.glScalef(xpScale, xpScale, 1.0F);
            fontRendererObj.drawString(xpCostStr, 0, 0, 0x4ac26a);
            GL11.glPopMatrix();
            // Add text overlay for XP cost with the full (unformatted) number.
            int xpBoxWidth = scaledTextWidth;
            int xpBoxHeight = fontRendererObj.FONT_HEIGHT;
            if (func_146978_c(xpTextX - guiLeft, xpTextY - guiTop, xpBoxWidth, xpBoxHeight, mouseX, mouseY)) {
                textOverlays.add(new TextOverlayData(xpTextX, xpTextY, xpBoxWidth, xpBoxHeight, String.valueOf(recipe.getXpCost())));
            }
        }

        for (ItemOverlayData iod : itemOverlays) {
            this.renderToolTip(iod.item, mouseX, mouseY);
        }

        for (TextOverlayData tod : textOverlays) {
            this.drawHoveringText(tod.textLines, mouseX, mouseY, this.fontRendererObj);
        }
    }

    protected boolean func_146978_c(int p_146978_1_, int p_146978_2_, int p_146978_3_, int p_146978_4_, int p_146978_5_, int p_146978_6_) {
        int k1 = this.guiLeft;
        int l1 = this.guiTop;
        p_146978_5_ -= k1;
        p_146978_6_ -= l1;
        return p_146978_5_ >= p_146978_1_ - 1 && p_146978_5_ < p_146978_1_ + p_146978_3_ + 1 && p_146978_6_ >= p_146978_2_ - 1 && p_146978_6_ < p_146978_2_ + p_146978_4_ + 1;
    }

    private String formatXpCost(int xp) {
        if (xp < 1000) {
            return String.valueOf(xp);
        }
        double xpK = xp / 1000.0;
        if (xpK == (int) xpK) {
            return ((int) xpK) + "K";
        } else {
            return String.format("%.1fK", xpK);
        }
    }

    private void drawItem(ItemStack item, int x, int y, int mouseX, int mouseY) {
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

    @Override
    public void save() {
    }
}
