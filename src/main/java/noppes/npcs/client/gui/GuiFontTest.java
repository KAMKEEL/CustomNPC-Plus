package noppes.npcs.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.gui.font.ScalableFontRenderer;

@SideOnly(Side.CLIENT)
public class GuiFontTest extends GuiScreen {
    private static final int[] SIZES = new int[]{8, 12, 16, 24, 32, 48, 64};
    private static final String FONT_PATH = "assets/customnpcs/OpenSans.ttf";

    private ScalableFontRenderer font;

    @Override
    public void initGui() {
        if (font == null) {
            font = ScalableFontRenderer.create(FONT_PATH);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int left = 30;
        int right = width - 30;
        int baseline = 34;

        font.drawString("CustomNPC+ Font Test", left, baseline, 24, 0xFFFFFFFF);
        baseline += 16;

        for (int size : SIZES) {
            drawHorizontalLine(left - 8, right, baseline, 0x55FFFFFF);
            String text = "OpenSans " + size + "px  Sphinx of black quartz, judge my vow 0123456789";
            font.drawString(text, left, baseline, size, 0xFFE6F2FF);
            baseline += Math.max(size + 8, 18);
        }

        baseline += 8;
        drawHorizontalLine(left - 8, right, baseline, 0x66AAFFAA);
        baseline += 16;

        font.drawString("Renderer: " + font.getRendererPath(), left, baseline, 12, 0xFFB5FFC9);
        baseline += 16;
        font.drawString("Font resource: " + font.getSourcePath(), left, baseline, 12, 0xFFB5FFC9);
        baseline += 16;
        font.drawString("Atlas: " + font.getAtlasWidth() + "x" + font.getAtlasHeight(), left, baseline, 12, 0xFFB5FFC9);
        baseline += 16;
        font.drawString("GUI scale (settings): " + mc.gameSettings.guiScale + "  |  active scale factor: " + sr.getScaleFactor(), left, baseline, 12, 0xFFB5FFC9);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static void open() {
        Minecraft.getMinecraft().displayGuiScreen(new GuiFontTest());
    }
}
