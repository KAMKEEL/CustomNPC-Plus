package kamkeel.npcs.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.font.ScalableFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiFontTest extends GuiScreen {
    private static final int[] SIZES = new int[]{8, 12, 16, 24, 32, 48, 64};
    private final ScalableFontRenderer renderer = ScalableFontRenderer.get();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        ScaledResolution scaled = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int scaleFactor = scaled.getScaleFactor();

        float startX = 30f;
        float y = 16f;

        renderer.drawString("Kamkeel Font Test (OpenSans.ttf)", startX, y, 24f, 0xFFFFFFFF);
        y += 32f;

        renderer.drawString("renderer=" + renderer.getRendererPath() + " atlas=" + renderer.getAtlasWidth() + "x" + renderer.getAtlasHeight(), startX, y, 12f, 0xFFE0E0E0);
        y += 14f;
        renderer.drawString("guiScaleSetting=" + Minecraft.getMinecraft().gameSettings.guiScale + " scaleFactor=" + scaleFactor, startX, y, 12f, 0xFFE0E0E0);
        y += 20f;

        float maxX = this.width - 24f;
        for (int size : SIZES) {
            float baseline = y + size;
            drawBaseline(startX, maxX, baseline);
            String line = "size " + size + " -> The quick brown fox jumps over 1234567890";
            renderer.drawString(line, startX, y, size, 0xFFFFFFFF);
            y += Math.max(renderer.getLineHeight(size), size + 10f);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawBaseline(float x0, float x1, float y) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.2f, 0.8f, 1.0f, 0.45f);
        GL11.glLineWidth(1.0f);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x0, y);
        GL11.glVertex2f(x1, y);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
