package noppes.npcs.client.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.Comparator;
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

    // IDs for cycle buttons.
    private final int CYCLE_LEFT_ID = 101;
    private final int CYCLE_RIGHT_ID = 102;
    private final int COMPONENT_LABEL_ID = 103;

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
     * Removes all buttons except the global Save (id 0) and then adds:
     *   - The selected component's custom buttons plus its Reset Position button,
     *   - Followed by the cycle buttons at the very bottom.
     * Custom buttons are arranged in two columns.
     */
    private void updateCustomButtons() {
        // Remove all buttons except the global Save (id 0) and cycle buttons (101,102,103)
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            GuiButton btn = (GuiButton) buttonList.get(i);
            if (btn.id != 0 && btn.id != CYCLE_LEFT_ID && btn.id != CYCLE_RIGHT_ID && btn.id != COMPONENT_LABEL_ID) {
                buttonList.remove(i);
            }
        }

        if (selectedComponent != null) {
            List<GuiButton> customButtons = new ArrayList<>();
            selectedComponent.addEditorButtons(customButtons);
            // Arrange custom buttons in 2 columns.
            int spacingX = 10;
            int spacingY = 5;
            int buttonWidth = 120; // assuming uniform width
            int startXLeft = width / 2 - buttonWidth - spacingX / 2;
            int startXRight = width / 2 + spacingX / 2;
            int rowHeight = 0;
            int numButtons = customButtons.size();
            for (int i = 0; i < numButtons; i += 2) {
                if (i + 1 < numButtons) {
                    GuiButton btnLeft = customButtons.get(i);
                    GuiButton btnRight = customButtons.get(i + 1);
                    btnLeft.xPosition = startXLeft;
                    btnRight.xPosition = startXRight;
                    int rowY = height - 70 - rowHeight;
                    btnLeft.yPosition = rowY;
                    btnRight.yPosition = rowY;
                    rowHeight += btnLeft.height + spacingY;
                    buttonList.add(btnLeft);
                    buttonList.add(btnRight);
                } else {
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

        // Now update (or add if missing) the cycle buttons at the very bottom.
        // We define a group width of 160: left arrow (20), wide center label (120), right arrow (20)
        int groupWidth = 160;
        int groupX = (width - groupWidth) / 2;
        int cycleY = height - 20; // positioned near the very bottom

        boolean hasLeft = false, hasRight = false, hasLabel = false;
        for (Object button : buttonList) {
            GuiButton btn = (GuiButton) button;
            if (btn.id == CYCLE_LEFT_ID) {
                hasLeft = true;
            } else if (btn.id == CYCLE_RIGHT_ID) {
                hasRight = true;
            } else if (btn.id == COMPONENT_LABEL_ID) {
                // Update the label text.
                btn.displayString = selectedComponent != null ? selectedComponent.getClass().getSimpleName() : "None";
                hasLabel = true;
            }
        }
        if (!hasLeft) {
            buttonList.add(new GuiButton(CYCLE_LEFT_ID, groupX, cycleY, 20, 20, "<"));
        }
        if (!hasLabel) {
            GuiButton labelBtn = new GuiButton(COMPONENT_LABEL_ID, groupX + 20, cycleY, 120, 20, selectedComponent != null ? selectedComponent.getClass().getSimpleName() : "None");
            labelBtn.enabled = false;
            buttonList.add(labelBtn);
        }
        if (!hasRight) {
            buttonList.add(new GuiButton(CYCLE_RIGHT_ID, groupX + 140, cycleY, 20, 20, ">"));
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
        // Check for Compass width adjust hitbox.
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            if (!(hud instanceof CompassHudComponent))
                continue;
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            float effectiveScale = hud.getEffectiveScale(res);
            int actualX = (int)(hud.posX / 100F * res.getScaledWidth());
            int actualY = (int)(hud.posY / 100F * res.getScaledHeight());
            int margin = 5;
            int barWidth = 6; // same as WIDTH_BAR_SIZE
            int barX = actualX + (int)(hud.overlayWidth * effectiveScale) + margin;
            int barY = actualY + (int)(((hud.overlayHeight - 20) / 2.0F) * effectiveScale);
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

        // Normal hit detection for dragging/resizing.
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            float effectiveScale = hud.getEffectiveScale(res);
            int actualX = (int)(hud.posX / 100F * res.getScaledWidth());
            int actualY = (int)(hud.posY / 100F * res.getScaledHeight());
            int absWidth = (int)(hud.overlayWidth * effectiveScale);
            int absHeight = (int)(hud.overlayHeight * effectiveScale);
            if (mouseX >= actualX && mouseX <= actualX + absWidth &&
                mouseY >= actualY && mouseY <= actualY + absHeight) {
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
                selectedComponent.scale = initialScale + deltaX;
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
            // Global Reset Position: center the selected HUD component.
            if (selectedComponent != null) {
                ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                float effectiveScale = selectedComponent.getEffectiveScale(res);
                int compWidth, compHeight;
                if (selectedComponent instanceof CompassHudComponent) {
                    int margin = 5;
                    int widthBar = 6; // as used in CompassHudComponent.
                    compWidth = (int)((selectedComponent.overlayWidth + margin + widthBar) * effectiveScale);
                    compHeight = (int)(selectedComponent.overlayHeight * effectiveScale);
                } else {
                    compWidth = (int)(selectedComponent.overlayWidth * effectiveScale);
                    compHeight = (int)(selectedComponent.overlayHeight * effectiveScale);
                }
                int centerX = (res.getScaledWidth() - compWidth) / 2;
                int centerY = (res.getScaledHeight() - compHeight) / 2;
                selectedComponent.posX = (int)(100F * centerX / res.getScaledWidth());
                selectedComponent.posY = (int)(100F * centerY / res.getScaledHeight());
                updateCustomButtons();
            }
        } else if (button.id == CYCLE_LEFT_ID) {
            cycleComponent(-1);
        } else if (button.id == CYCLE_RIGHT_ID) {
            cycleComponent(1);
        } else {
            if (selectedComponent != null) {
                selectedComponent.onEditorButtonPressed(button);
                updateCustomButtons();
            }
        }
    }

    private void cycleComponent(int direction) {
        // Clear any dragging/resizing state.
        dragging = false;
        resizing = false;
        resizingWidth = false;

        List<HudComponent> components = new ArrayList<>(ClientHudManager.getInstance().getHudComponents().values());
        if (components.isEmpty()) return;
        int index = components.indexOf(selectedComponent);
        if (index < 0) index = 0;
        index = (index + direction) % components.size();
        if (index < 0) {
            index += components.size();
        }
        selectedComponent = components.get(index);
        updateCustomButtons(); // Refresh buttons, without resetting the entire GUI.
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
