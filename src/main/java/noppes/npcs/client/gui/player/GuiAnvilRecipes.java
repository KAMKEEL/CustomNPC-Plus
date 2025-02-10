package noppes.npcs.client.gui.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;

import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeAnvil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
        if(search.equals(getTextField(3).getText()))
            return;
        search = getTextField(3).getText().toLowerCase();
        recipes = getSearchList();
        page = 0;
        updateButton();
    }

    private List<RecipeAnvil> getSearchList() {
        if(search.isEmpty()) {
            return new ArrayList<RecipeAnvil>(RecipeController.Instance.anvilRecipes.values());
        }
        List<RecipeAnvil> list = new ArrayList<RecipeAnvil>();
        for(RecipeAnvil recipe : RecipeController.Instance.anvilRecipes.values()) {
            ItemStack repairItem = recipe.itemToRepair;
            if(repairItem == null)
                continue;
            if(repairItem.getDisplayName() == null)
                continue;
            if(repairItem.getDisplayName().trim().isEmpty())
                continue;
            if(repairItem.getDisplayName().toLowerCase().contains(search))
                list.add(recipe);
        }
        return list;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(!button.enabled)
            return;
        if(button == btnRight) {
            if(page > 0)
                page--;
        }
        if(button == btnLeft) {
            int maxPages = MathHelper.ceiling_float_int(recipes.size() / 8.0F);
            if(page < maxPages - 1)
                page++;
        }
        updateButton();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Update page label.
        lblPage.label = (page + 1) + "/" + MathHelper.ceiling_float_int(recipes.size() / 8.0F);
        lblPage.x = guiLeft + (xSize - Minecraft.getMinecraft().fontRenderer.getStringWidth(lblPage.label)) / 2;

        // Determine which recipes to show.
        int recipesPerPage = 8;
        int startIndex = page * recipesPerPage;
        int endIndex = Math.min(recipes.size(), startIndex + recipesPerPage);

        // Set base positions: first column X and first row Y.
        int baseX = guiLeft + 4;
        int baseY = guiTop + 28;   // first row Y = 28
        int colSpacing = 124;      // horizontal spacing between columns
        int rowSpacing = 35;       // vertical spacing so row1 becomes 28+35 = 63

        for (int i = startIndex; i < endIndex; i++) {
            RecipeAnvil recipe = recipes.get(i);
            int localIndex = i - startIndex; // 0 to 7
            int col = localIndex / 4; // 0 for left column, 1 for right column
            int row = localIndex % 4; // 0 to 3

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
            if (toRepair != null) {
                ItemStack halfItem = toRepair.copy();
                if (halfItem.isItemStackDamageable()) {
                    int halfDamage = halfItem.getMaxDamage() / 2;
                    halfItem.setItemDamage(halfDamage);
                }
                drawItem(halfItem, slotX + 1, slotY + 1, mouseX, mouseY);
            }
            // Draw the repair material.
            ItemStack material = recipe.repairMaterial;
            if (material != null) {
                drawItem(material, slotX + 23, slotY + 1, mouseX, mouseY);
            }

            // Draw the output item fully repaired (damage = 0).
            // For first column, output X = guiLeft+103; second column, output X = guiLeft+229.
            int outputX = (col == 0) ? (guiLeft + 103) : (guiLeft + 229);
            if (recipe.itemToRepair != null) {
                ItemStack fullOutput = recipe.itemToRepair.copy();
                if (fullOutput.isItemStackDamageable()) {
                    fullOutput.setItemDamage(0);
                }
                drawItem(fullOutput, outputX, slotY, mouseX, mouseY);
            }

            // Draw the percentage text.
            // We want the "middle right" for the first column to be at 68.
            // So for column 0, centerX is fixed at 68; for column 1, centerX is 68 + colSpacing.
            String percentText = Math.round(recipe.getRepairPercentage()) + "%";
            int textWidth = fontRendererObj.getStringWidth(percentText);
            int centerX = (col == 0) ? guiLeft + 57 : (guiLeft + 57 + colSpacing);
            int textX = centerX - (textWidth / 2);
            int textY = slotY + 4 + row;
            fontRendererObj.drawString(percentText, textX, textY, CustomNpcResourceListener.DefaultTextColor);

            // Draw XP cost with scaling.
            String xpCostStr = formatXpCost(recipe.getXpCost());
            int digitCount = xpCostStr.length();
            // Scale factor: for 1 digit = 1.0, for 2 digits = 0.9, for 3 digits = 0.8.
            float xpScale = 1.0F - 0.1F * (digitCount - 1);
            // Get raw text width.
            int rawTextWidth = fontRendererObj.getStringWidth(xpCostStr);
            int scaledTextWidth = (int)(rawTextWidth * xpScale);
            int xpCenterX = (col == 0) ? (guiLeft + 76) : (guiLeft + 78 + colSpacing);
            int xpTextX = xpCenterX - (scaledTextWidth / 2);
            int xpTextY = slotY - 4 + row;  // adjust as needed

            GL11.glPushMatrix();
            GL11.glTranslatef(xpTextX, xpTextY, 0);
            GL11.glScalef(xpScale, xpScale, 1.0F);
            fontRendererObj.drawString(xpCostStr, 0, 0, 0x4ac26a);
            GL11.glPopMatrix();
        }
    }

    private String formatXpCost(int xp) {
        if (xp < 1000) {
            return String.valueOf(xp);
        }
        double xpK = xp / 1000.0;
        if (xpK == (int)xpK) {
            return ((int)xpK) + "K";
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
    public void save() {}
}
