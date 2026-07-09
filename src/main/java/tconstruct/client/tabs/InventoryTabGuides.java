package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import somehussar.gui.guides.GuideController;

import static tconstruct.client.tabs.TabRegistry.getPotionOffset;

public class InventoryTabGuides extends AbstractTab {

    // TODO: Replace with a proper texture.
    private static final ResourceLocation HELP_TEXTURE = new ResourceLocation("customnpcs", "textures/marks/question.png");
    public InventoryTabGuides() {
        super(0, 0, 0, null);
    }

    @Override
    public void onTabClicked() {
        int xSize = 176;
        int ySize = 166;
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;

        if (!(gui instanceof GuiInventory))
            return;

        int guiLeft = (gui.width - xSize) / 2;
        int guiTop = (gui.height - ySize) / 2;
        guiLeft += getPotionOffset();
        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabGuides.class);
    }

    @Override
    public boolean shouldAddToList() {
        return GuideController.isGuideLoaded();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glDepthMask(true);
            this.zLevel = 100.0F;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            int yTexPos = this.enabled ? 3 : 32;
            int ySize = this.enabled ? 25 : 32;
            int xOffset = this.id == 2 ? 0 : 1;
            int yPos = this.yPosition + (this.enabled ? 3 : 0);
            mc.renderEngine.bindTexture(super.texture);
            this.drawTexturedModalRect(this.xPosition, yPos, xOffset * 28,
                yTexPos, 28, ySize);

            this.zLevel = 100.0f;
            mc.renderEngine.bindTexture(HELP_TEXTURE);

            RenderHelper.enableGUIStandardItemLighting();
            this.zLevel = 100.0F;
            this.itemRenderer.zLevel = 100.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            drawTextured(this.xPosition + 6, this.yPosition + 8, 0, 0, 16, 16, 16, 16);

            if (GuideController.glint()) {
                GL11.glDepthFunc(GL11.GL_EQUAL);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDepthMask(false);
                mc.renderEngine.bindTexture(RenderItem.RES_ITEM_GLINT);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);
                this.renderGlint(this.xPosition, yPos, 28, ySize);
                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
            }
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            this.zLevel = 0.0F;
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);

        }
    }

    private void renderGlint(int x, int y, int width, int height) {
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE, 0, 0);

        final float GLINT_TEX_SCALE = 0.00390625F; // 1 / 256
        long now = Minecraft.getSystemTime();
        double z = this.zLevel;
        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();

        for (int pass = 0; pass < 2; ++pass) {
            long cycleLengthMs = 3000 + pass * 1873;
            float scrollU = (now % cycleLengthMs) / (float) cycleLengthMs * 256.0F;
            float skew = (pass == 1) ? -1.0F : 4.0F;

            float uLeft = scrollU * GLINT_TEX_SCALE;
            float uRight = (scrollU + width) * GLINT_TEX_SCALE;
            float uBottomSkew = (scrollU + height * skew) * GLINT_TEX_SCALE;
            float uBottomRightSkew = (scrollU + width + height * skew) * GLINT_TEX_SCALE;
            float vTop = 0.0F;
            float vBottom = height * GLINT_TEX_SCALE;

            tessellator.addVertexWithUV(x, y + height, z, uBottomSkew, vBottom);
            tessellator.addVertexWithUV(x + width, y + height, z, uBottomRightSkew, vBottom);
            tessellator.addVertexWithUV(x + width, y, z, uRight, vTop);
            tessellator.addVertexWithUV(x, y, z, uLeft, vTop);
        }

        tessellator.draw();
    }

    private void drawTextured(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight)
    {
        float f4 = 1.0F / textureWidth;
        float f5 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)x, (double)(y + height), this.zLevel, (double)(u * f4), (double)((v + (float)height) * f5));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), this.zLevel, (double)((u + (float)width) * f4), (double)((v + (float)height) * f5));
        tessellator.addVertexWithUV((double)(x + width), (double)y, this.zLevel, (double)((u + (float)width) * f4), (double)(v * f5));
        tessellator.addVertexWithUV((double)x, (double)y, this.zLevel, (double)(u * f4), (double)(v * f5));
        tessellator.draw();
    }
}
