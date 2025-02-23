package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.config.ConfigClient;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiOverlayQuestEditor extends GuiScreen {
    private GuiScreen parent;
    // posX and posY are stored as percentages (0-100) of the scaled resolution.
    private int posX, posY, scale, textAlign;
    private final int overlayWidth = 200, overlayHeight = 120;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private boolean resizing = false;
    private int initialScale, initialMouseX, initialMouseY;
    private final int HANDLE_SIZE = 10;

    private GuiButton saveButton, alignButton, resetButton;

    public GuiOverlayQuestEditor(GuiScreen parent) {
        this.parent = parent;
        // Load config values (stored as percentages)
        posX = ConfigClient.QuestOverlayX;
        posY = ConfigClient.QuestOverlayY;
        scale = ConfigClient.QuestOverlayScale;
        textAlign = ConfigClient.QuestOverlayTextAlign;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        saveButton = new GuiButton(0, width / 2 - 60, height - 45, 120, 20, "Save and Close");
        alignButton = new GuiButton(1, width / 2 - 60, height - 70, 120, 20, getAlignText());
        resetButton = new GuiButton(3, width / 2 - 60, height - 95, 120, 20, "Reset to Center");
        buttonList.add(saveButton);
        buttonList.add(alignButton);
        buttonList.add(resetButton);
    }

    private String getAlignText() {
        switch (textAlign) {
            case 0: return "Align: Left";
            case 1: return "Align: Center";
            case 2: return "Align: Right";
            default: return "Align: Unknown";
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int) ((float) posX / 100F * res.getScaledWidth());
        int actualY = (int) ((float) posY / 100F * res.getScaledHeight());

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        float s = scale / 100.0F;
        GL11.glScalef(s, s, s);

        // Draw preview background
        drawRect(0, 0, overlayWidth, overlayHeight, 0x40FFFFFF);

        int currentY = 5;
        currentY = renderDemoTextBlock(new String[] { "Dummy Quest Title" }, currentY, textAlign, 0xFFFFFF);
        drawDecorativeLine(currentY - 1);
        currentY += 8;

        currentY = renderDemoTextBlock(new String[] { "Dummy Category" }, currentY, textAlign, 0xCCCCCC);
        drawDecorativeLine(currentY - 1);
        currentY += 8;

        currentY = renderDemoTextBlock(new String[] { "Objective: Kill 10 mobs", "Objective: Collect 5 items" }, currentY, textAlign, 0xAAAAAA);
        drawDecorativeLine(currentY - 1);
        currentY += 8;

        renderDemoTextBlock(new String[] { "Turn in with NPC" }, currentY, textAlign, 0xAAAAAA);

        // Draw resize handle at bottom-right
        drawRect(overlayWidth - HANDLE_SIZE, overlayHeight - HANDLE_SIZE, overlayWidth, overlayHeight, 0xFFCCCCCC);
        GL11.glPopMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int renderDemoTextBlock(String[] lines, int startY, int align, int color) {
        int y = startY;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        for (String line : lines) {
            int strWidth = font.getStringWidth(line);
            int xOffset;
            if (align == 1)
                xOffset = (overlayWidth - strWidth) / 2;
            else if (align == 2)
                xOffset = overlayWidth - strWidth - 5;
            else
                xOffset = 5;
            font.drawStringWithShadow(line, xOffset, y, color);
            y += font.FONT_HEIGHT + 4;
        }
        return y;
    }

    private void drawDecorativeLine(int y) {
        drawHorizontalLine(5, overlayWidth - 5, y, 0xFF777777);
        drawHorizontalLine(5, overlayWidth - 5, y + 1, 0xFFA8A8A8);
        drawHorizontalLine(5, overlayWidth - 5, y + 2, 0xFFFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int actualX = (int) ((float) posX / 100F * res.getScaledWidth());
        int actualY = (int) ((float) posY / 100F * res.getScaledHeight());
        float s = scale / 100.0F;
        int absWidth = (int) (overlayWidth * s);
        int absHeight = (int) (overlayHeight * s);
        if (mouseX >= actualX && mouseX <= actualX + absWidth &&
            mouseY >= actualY && mouseY <= actualY + absHeight) {
            // Check if within resize handle
            if (mouseX >= actualX + absWidth - HANDLE_SIZE && mouseY >= actualY + absHeight - HANDLE_SIZE) {
                resizing = true;
                initialScale = scale;
                initialMouseX = mouseX;
                initialMouseY = mouseY;
            } else {
                dragging = true;
                dragOffsetX = mouseX - actualX;
                dragOffsetY = mouseY - actualY;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // For Minecraft 1.7.10, use mouseMovedOrUp for button release.
    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        dragging = false;
        resizing = false;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaledWidth = res.getScaledWidth();
        int scaledHeight = res.getScaledHeight();
        // Calculate absolute dimensions for the overlay preview
        int absWidth = (int) (overlayWidth * (scale / 100.0F));
        int absHeight = (int) (overlayHeight * (scale / 100.0F));

        if (dragging) {
            // Compute new absolute position in pixels
            int newX = Mouse.getX() * width / mc.displayWidth - dragOffsetX;
            int newY = scaledHeight - (Mouse.getY() * scaledHeight / mc.displayHeight) - dragOffsetY;
            // Clamp the absolute position so that the overlay stays fully on screen
            if (newX < 0) newX = 0;
            if (newY < 0) newY = 0;
            if (newX + absWidth > scaledWidth) newX = scaledWidth - absWidth;
            if (newY + absHeight > scaledHeight) newY = scaledHeight - absHeight;
            // Convert back to percentage values
            posX = (int) (100F * newX / scaledWidth);
            posY = (int) (100F * newY / scaledHeight);
        }
        if (resizing) {
            int currentMouseX = Mouse.getX() * width / mc.displayWidth;
            int deltaX = currentMouseX - initialMouseX;
            scale = initialScale + deltaX; // 1 pixel = 1% change
            if (scale < 50) scale = 50;
            if (scale > 300) scale = 300;
        }
        super.updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) { // Save and close
            ConfigClient.QuestOverlayX = posX;
            ConfigClient.QuestOverlayY = posY;
            ConfigClient.QuestOverlayScale = scale;
            ConfigClient.QuestOverlayTextAlign = textAlign;
            if (ConfigClient.config.hasChanged())
                ConfigClient.config.save();
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) { // Cycle alignment
            textAlign = (textAlign + 1) % 3;
            button.displayString = getAlignText();
        } else if (button.id == 3) { // Reset to center
            posX = 50;
            posY = 50;
        }
    }
}
