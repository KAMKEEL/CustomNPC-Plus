package kamkeel.npcs.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.client.renderer.font.ScalableSdfFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

@SideOnly(Side.CLIENT)
public class GuiFontTest extends GuiScreen {
    private static final int[] SAMPLE_SIZES = new int[]{8, 12, 16, 24, 32, 48, 64};
    private static final String SAMPLE_TEXT = "The quick brown fox jumps over 1234567890";

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        ScalableSdfFontRenderer renderer = ScalableSdfFontRenderer.INSTANCE;
        renderer.ensureInitialized();

        int left = 24;
        int y = 20;
        renderer.drawString("GuiFontTest - OpenSans.ttf scaling demo", left, y, 20, 0xFFFFFFFF);
        y += 28;

        for (int size : SAMPLE_SIZES) {
            int baseline = y + renderer.getLineHeight(size) - 4;
            drawHorizontalLine(left - 6, this.width - 24, baseline, 0x66FFFFFF);
            renderer.drawString(size + "px | " + SAMPLE_TEXT, left, y, size, 0xFFFFFFFF);
            y += renderer.getLineHeight(size) + 10;
        }

        ScaledResolution scaled = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int debugY = this.height - 58;
        renderer.drawString("Renderer: " + renderer.getRendererPath(), left, debugY, 12, 0xFF88FF88);
        renderer.drawString("Atlas: " + renderer.getAtlasDimensions() + " | guiScale=" + this.mc.gameSettings.guiScale + " | scaleFactor=" + scaled.getScaleFactor(), left, debugY + 14, 12, 0xFF88FF88);
        renderer.drawString("ESC to close", left, debugY + 28, 12, 0xFFBBBBBB);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
