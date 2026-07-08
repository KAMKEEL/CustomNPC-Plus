package tconstruct.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import somehussar.gui.guides.GuideController;

public class InventoryTabGuides extends AbstractTab {
    private static final ResourceLocation HELP_TEXTURE = new ResourceLocation("customnpcs", "textures/marks/question.png");
    public InventoryTabGuides() {
        super(0, 0, 0, null);
    }

    @Override
    public void onTabClicked() {

    }

    @Override
    public boolean shouldAddToList() {
        return GuideController.isGuideLoaded();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            int yTexPos = this.enabled ? 3 : 32;
            int ySize = this.enabled ? 25 : 32;
            int xOffset = this.id == 2 ? 0 : 1;
            int yPos = this.yPosition + (this.enabled ? 3 : 0);
            mc.renderEngine.bindTexture(super.texture);
            this.drawTexturedModalRect(this.xPosition, yPos, xOffset * 28,
                yTexPos, 28, ySize);

            this.zLevel = 100.0F;

            mc.renderEngine.bindTexture(HELP_TEXTURE);
            func_146110_a(xPosition + 6, yPosition + 8, 0, 0, 16, 16, 16, 16);

            this.zLevel = 0.0F;

        }
    }
}
