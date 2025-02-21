package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.PlayerEffect;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiEffectBar extends GuiScreen {
    public int x, y, width, height;
    public int scrollY = 0; // Tracks vertical scrolling
    public int entryHeight = 20; // Height for each effect slot
    public List<EffectEntry> entries = new ArrayList<>();
    public FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

    public GuiEffectBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void initGui() {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background of effect bar
        drawRect(x, y, x + width, y + height, 0x90000000);

        int totalHeight = entries.size() * (entryHeight + 2); // +2 for padding
        if (scrollY < 0) scrollY = 0;
        if (scrollY > totalHeight - height) scrollY = Math.max(totalHeight - height, 0);

        // Enable scissoring for vertical clipping
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        int scissorX = x * scaleFactor;
        int scissorY = (sr.getScaledHeight() - (y + height)) * scaleFactor;
        int scissorW = width * scaleFactor;
        int scissorH = height * scaleFactor;
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        int iconRenderSize = 16;
        int padding = 1;

        int startIndex = scrollY / (entryHeight + padding);
        int endIndex = Math.min(entries.size(), startIndex + height / (entryHeight + padding) + 1);

        List<String> hoveredTooltip = null;
        int tooltipX = 0, tooltipY = 0;
        CustomEffect toolTipEffect = null;

        for (int i = startIndex; i < endIndex; i++) {
            int drawY = y + (i - startIndex) * (entryHeight + padding) - (scrollY % (entryHeight + padding));
            EffectEntry entry = entries.get(i);
            ImageData imageData = ClientCacheHandler.getImageData(entry.effect.getIcon());

            if (imageData != null && imageData.imageLoaded()) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                imageData.bindTexture();
                int iconU = entry.effect.iconX;
                int iconV = entry.effect.iconY;
                int iconWidth = entry.effect.getWidth();
                int iconHeight = entry.effect.getHeight();
                int texWidth = imageData.getTotalWidth();

                func_152125_a(x + 2, drawY + 2, iconU, iconV, iconWidth, iconHeight, iconRenderSize, iconRenderSize, texWidth, texWidth);
            }

            // If mouse hovers over this effect, save tooltip info
            if (mouseX >= x && mouseX < x + width &&
                mouseY >= drawY && mouseY < drawY + entryHeight) {
                int seconds = entry.playerEffect.duration;
                hoveredTooltip = new ArrayList<>();
                hoveredTooltip.add(entry.effect.getMenuName());
                hoveredTooltip.add("Time: " + seconds + "s");

                tooltipX = mouseX;
                tooltipY = mouseY;
                toolTipEffect = entry.effect;
            }
        }

        // Disable scissoring for tooltip rendering
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw vertical scrollbar
        if (totalHeight > height) {
            int scrollBarWidth = 4;
            int scrollBarX = x + width - scrollBarWidth - 2 + 10    ;
            drawRect(scrollBarX, y, scrollBarX + scrollBarWidth, y + height, 0xFF333333);
            int thumbHeight = Math.max(20, height * height / totalHeight);
            int thumbY = y + (scrollY * (height - thumbHeight)) / (totalHeight - height);
            drawRect(scrollBarX, thumbY, scrollBarX + scrollBarWidth, thumbY + thumbHeight, 0xFFAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw tooltip after all icons
        if (hoveredTooltip != null && fontRenderer != null && toolTipEffect != null) {
            drawCustomHoveringText(hoveredTooltip, tooltipX, tooltipY, fontRenderer, toolTipEffect);
        }
    }

    /**
     * Adjusted scrolling to move up and down instead of left and right.
     */
    public void mouseScrolled(int delta) {
        int totalHeight = entries.size() * entryHeight;
        scrollY -= delta * 10; // Adjust scroll speed as needed.
        if (scrollY < 0) scrollY = 0;
        if (scrollY > totalHeight - height) scrollY = Math.max(totalHeight - height, 0);
    }

    /**
     * Custom tooltip with enlarged effect icon.
     */
    public void drawCustomHoveringText(List<String> textLines, int x, int y, FontRenderer font, CustomEffect hoveredEffect) {
        if (textLines == null || textLines.isEmpty()) return;

        int maxWidth = 0;
        for (String s : textLines) {
            int lineWidth = font.getStringWidth(s);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int iconSize = 32;

        int tooltipHeight = 8 + iconSize + 8; // Reserve space for enlarged icon
        if (textLines.size() > 1) {
            tooltipHeight += 2 + (textLines.size() - 1) * 10;
        }

        int screenWidth = Minecraft.getMinecraft().currentScreen.width;
        int screenHeight = Minecraft.getMinecraft().currentScreen.height;

        int tooltipX = x + 12;
        int tooltipY = y - 12;
        if (tooltipX + maxWidth > screenWidth) {
            tooltipX = x - 28 - maxWidth;
        }
        if (tooltipY + tooltipHeight + 6 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 6;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        this.zLevel = 1000.0F;

        // Draw tooltip background
        drawRect(tooltipX - 3, tooltipY - 4, tooltipX + maxWidth + 3, tooltipY + tooltipHeight, 0xF0100010);

        // Draw text
        int textY = tooltipY;
        for (String s : textLines) {
            font.drawStringWithShadow(s, tooltipX, textY, 0xFFFFFF);
            textY += 10;
        }

        // Draw enlarged icon
        ImageData imageData = ClientCacheHandler.getImageData(hoveredEffect.getIcon());
        if (imageData != null && imageData.imageLoaded()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            imageData.bindTexture();
            func_152125_a(tooltipX + (maxWidth - iconSize) / 2, textY + 4, hoveredEffect.iconX, hoveredEffect.iconY, hoveredEffect.getWidth(), hoveredEffect.getHeight(), iconSize, iconSize, imageData.getTotalWidth(), imageData.getTotalWidth());
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static class EffectEntry {
        public CustomEffect effect;
        public PlayerEffect playerEffect;
        public EffectEntry(CustomEffect effect, PlayerEffect playerEffect) {
            this.effect = effect;
            this.playerEffect = playerEffect;
        }
    }
}
