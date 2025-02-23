package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.util.List;
import java.util.ArrayList;

public class GuiHudEditor extends GuiScreen {
    private GuiScreen parent;
    // Currently selected HUD component for moving/resizing.
    private HudComponent selectedComponent;
    private int dragOffsetX, dragOffsetY;
    private boolean dragging = false;
    private boolean resizing = false;
    // For general scaling resize.
    private int initialScale, initialMouseX, initialMouseY;
    // For Compass width adjustment.
    private boolean resizingWidth = false;
    private int initialOverlayWidth, initialWidthMouseX;

    private final int HANDLE_SIZE = 10;
    private final int RESET_POSITION_ID = 100;

    public GuiHudEditor(GuiScreen parent) {
        this.parent = parent;
        // For example, default to QuestTracker if available.
        selectedComponent = ClientHudManager.getInstance().getHudComponents().get(EnumHudComponent.QuestTracker);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        // Global "Save and Close" button always visible.
        buttonList.add(new GuiButton(0, width / 2 - 60, height - 45, 120, 20, "Save and Close"));
        updateCustomButtons();
    }

    /**
     * Updates the custom buttons shown on screen.
     * Removes all buttons except the global Save button (id 0)
     * then adds the selected component's custom buttons plus a Reset Position button.
     * Buttons are now arranged in two columns.
     */
    private void updateCustomButtons() {
        // Remove any buttons that are not the global Save (id 0)
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            GuiButton btn = (GuiButton) buttonList.get(i);
            if (btn.id != 0) {
                buttonList.remove(i);
            }
        }
        if (selectedComponent != null) {
            List<GuiButton> customButtons = new ArrayList<>();
            selectedComponent.addEditorButtons(customButtons);
            // Arrange buttons in 2 columns.
            int spacingX = 10;
            int spacingY = 5;
            int buttonWidth = 120; // assuming uniform width
            int startXLeft = width / 2 - buttonWidth - spacingX/2;
            int startXRight = width / 2 + spacingX/2;
            int rowHeight = 0;
            int numButtons = customButtons.size();
            for (int i = 0; i < numButtons; i += 2) {
                // If two buttons in this row.
                if (i + 1 < numButtons) {
                    GuiButton btnLeft = customButtons.get(i);
                    GuiButton btnRight = customButtons.get(i+1);
                    btnLeft.xPosition = startXLeft;
                    btnRight.xPosition = startXRight;
                    // Determine row y based on button height.
                    int rowY = height - 70 - rowHeight;
                    btnLeft.yPosition = rowY;
                    btnRight.yPosition = rowY;
                    rowHeight += btnLeft.height + spacingY;
                    buttonList.add(btnLeft);
                    buttonList.add(btnRight);
                } else {
                    // One button: center it.
                    GuiButton btn = customButtons.get(i);
                    btn.xPosition = width / 2 - buttonWidth / 2;
                    btn.yPosition = height - 70 - rowHeight;
                    buttonList.add(btn);
                    rowHeight += btn.height + spacingY;
                }
            }
            // Always add the Reset Position button below the custom buttons.
            GuiButton resetPos = new GuiButton(RESET_POSITION_ID, width / 2 - buttonWidth / 2, height - 70 - rowHeight, buttonWidth, 20, "Reset Position");
            buttonList.add(resetPos);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        // Render each HUD in editing mode.
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            hud.renderEditing();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Iterate over HUD components to check for selection.
        // First, check for Compass width adjust hitbox if the mouse is near a CompassHudComponent.
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            if (!(hud instanceof CompassHudComponent))
                continue;

            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            float effectiveScale = hud.getEffectiveScale(res);
            int actualX = (int)(hud.posX / 100F * res.getScaledWidth());
            int actualY = (int)(hud.posY / 100F * res.getScaledHeight());

            // Even if the mouse is outside the hud bounds, we want to check for the floating adjust bar.
            int margin = 5;
            int barWidth = 6; // same as WIDTH_BAR_SIZE
            // The adjust bar is drawn to the right of the component.
            int barX = actualX + (int)(hud.overlayWidth * effectiveScale) + margin;
            int barY = actualY + (int)(((hud.overlayHeight - 20) / 2.0F) * effectiveScale); // 20 = BAR_HEIGHT
            int barHeight = (int)(20 * effectiveScale);
            if (mouseX >= barX && mouseX <= barX + barWidth &&
                mouseY >= barY && mouseY <= barY + barHeight) {
                resizingWidth = true;
                selectedComponent = hud;
                initialOverlayWidth = hud.overlayWidth;
                initialWidthMouseX = mouseX;
                updateCustomButtons();
                return;
            }
        }

        // Next, proceed with the normal hit detection for dragging/resizing of the HUD components.
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            float effectiveScale = hud.getEffectiveScale(res);
            int actualX = (int)(hud.posX / 100F * res.getScaledWidth());
            int actualY = (int)(hud.posY / 100F * res.getScaledHeight());
            int absWidth = (int)(hud.overlayWidth * effectiveScale);
            int absHeight = (int)(hud.overlayHeight * effectiveScale);
            if (mouseX >= actualX && mouseX <= actualX + absWidth &&
                mouseY >= actualY && mouseY <= actualY + absHeight) {
                // Check if clicking on the resize handle (bottom-right).
                if (mouseX >= actualX + absWidth - HANDLE_SIZE && mouseY >= actualY + absHeight - HANDLE_SIZE) {
                    resizing = true;
                    selectedComponent = hud;
                    initialScale = hud.scale;
                    initialMouseX = mouseX;
                    initialMouseY = mouseY;
                    updateCustomButtons();
                    return;
                } else {
                    dragging = true;
                    selectedComponent = hud;
                    dragOffsetX = mouseX - actualX;
                    dragOffsetY = mouseY - actualY;
                    updateCustomButtons();
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        dragging = false;
        resizing = false;
        resizingWidth = false;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        if (selectedComponent != null) {
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int scaledWidth = res.getScaledWidth();
            int scaledHeight = res.getScaledHeight();
            float effectiveScale = selectedComponent.getEffectiveScale(res);
            if (dragging) {
                int newX = Mouse.getX() * width / mc.displayWidth - dragOffsetX;
                int newY = scaledHeight - (Mouse.getY() * scaledHeight / mc.displayHeight) - dragOffsetY;
                if (newX < 0) newX = 0;
                if (newY < 0) newY = 0;
                if (newX + (int)(selectedComponent.overlayWidth * effectiveScale) > scaledWidth)
                    newX = scaledWidth - (int)(selectedComponent.overlayWidth * effectiveScale);
                if (newY + (int)(selectedComponent.overlayHeight * effectiveScale) > scaledHeight)
                    newY = scaledHeight - (int)(selectedComponent.overlayHeight * effectiveScale);
                selectedComponent.posX = (int)(100F * newX / scaledWidth);
                selectedComponent.posY = (int)(100F * newY / scaledHeight);
            }
            if (resizing) {
                int currentMouseX = Mouse.getX() * width / mc.displayWidth;
                int deltaX = currentMouseX - initialMouseX;
                selectedComponent.scale = initialScale + deltaX; // 1 pixel = 1% change.
                if (selectedComponent.scale < 50) selectedComponent.scale = 50;
                if (selectedComponent.scale > 300) selectedComponent.scale = 300;
            }
            if (resizingWidth && (selectedComponent instanceof CompassHudComponent)) {
                int currentMouseX = Mouse.getX() * width / mc.displayWidth;
                int delta = currentMouseX - initialWidthMouseX;
                selectedComponent.overlayWidth = initialOverlayWidth + delta;
                if (selectedComponent.overlayWidth < 50) selectedComponent.overlayWidth = 50;
                if (selectedComponent.overlayWidth > 500) selectedComponent.overlayWidth = 500;
            }
        }
        super.updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            close();
        } else if (button.id == RESET_POSITION_ID) {
            // Reset position for selected HUD.
            if (selectedComponent != null) {
                selectedComponent.posX = 50;
                selectedComponent.posY = 50;
                updateCustomButtons();
            }
        } else {
            // Dispatch other button actions to the selected component.
            if (selectedComponent != null) {
                selectedComponent.onEditorButtonPressed(button);
                updateCustomButtons();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            close();
        }
    }

    public void close() {
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            hud.isEditting = false;
            hud.save();
        }
        mc.displayGuiScreen(parent);
    }
}
