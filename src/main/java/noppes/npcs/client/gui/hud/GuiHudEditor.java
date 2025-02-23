package noppes.npcs.client.gui.hud;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
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
    private int initialScale, initialMouseX, initialMouseY;
    private final int HANDLE_SIZE = 10;
    private final int RESET_POSITION_ID = 100;

    public GuiHudEditor(GuiScreen parent) {
        this.parent = parent;
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
            int baseX = width / 2 - 60;
            int baseY = height - 70; // Start above the Save button.
            List<GuiButton> customButtons = new ArrayList<>();
            selectedComponent.addEditorButtons(customButtons);
            int offsetY = 0;
            for (GuiButton btn : customButtons) {
                btn.xPosition = baseX;
                btn.yPosition = baseY - offsetY;
                offsetY += btn.height + 5;
                buttonList.add(btn);
            }
            // Always add the Reset Position button.
            GuiButton resetPos = new GuiButton(RESET_POSITION_ID, baseX, baseY - offsetY, 120, 20, "Reset Position");
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
        for (HudComponent hud : ClientHudManager.getInstance().getHudComponents().values()) {
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            // Use base scaling (scale/100.0F)
            float effectiveScale = (hud.scale / 100.0F);
            int actualX = (int)((float)hud.posX / 100F * res.getScaledWidth());
            int actualY = (int)((float)hud.posY / 100F * res.getScaledHeight());
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
                } else {
                    dragging = true;
                    selectedComponent = hud;
                    dragOffsetX = mouseX - actualX;
                    dragOffsetY = mouseY - actualY;
                }
                updateCustomButtons();
                break;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        dragging = false;
        resizing = false;
        // Optionally keep selectedComponent so its buttons remain visible.
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void updateScreen() {
        if (selectedComponent != null) {
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int scaledWidth = res.getScaledWidth();
            int scaledHeight = res.getScaledHeight();
            float effectiveScale = (selectedComponent.scale / 100.0F);
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
