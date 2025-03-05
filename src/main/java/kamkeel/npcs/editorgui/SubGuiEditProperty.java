package kamkeel.npcs.editorgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * SubGuiEditProperty is a modal overlay for editing a single property.
 * It overlays on top of the current editor screen.
 * When "Done" is pressed, the provided property callback is invoked and the overlay is closed.
 * All overridden methods are declared public.
 */
public class SubGuiEditProperty extends GuiScreen {
    private ISubGuiCallback subGuiCallback;
    private IPropertyEditorCallback propCallback;
    private GuiTextField textField;
    private String propertyName;
    private String initialValue;

    // Overlay dimensions
    private int overlayX, overlayY, overlayWidth, overlayHeight;

    /**
     * Creates a new property editor overlay.
     * @param propertyName the name of the property being edited
     * @param initialValue the current value (as String)
     * @param propCallback callback for when the property is updated
     * @param subGuiCallback callback for when the overlay is closed
     */
    public SubGuiEditProperty(String propertyName, String initialValue, IPropertyEditorCallback propCallback, ISubGuiCallback subGuiCallback) {
        this.propertyName = propertyName;
        this.initialValue = (initialValue == null ? "" : initialValue);
        this.propCallback = propCallback;
        this.subGuiCallback = subGuiCallback;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        if (this.fontRendererObj == null) {
            this.fontRendererObj = Minecraft.getMinecraft().fontRenderer;
        }

        // Use the current width/height from this screen.
        this.width = res.getScaledWidth();
        this.height = res.getScaledHeight();
        overlayWidth = 150;
        overlayHeight = 20;
        overlayX = (this.width - overlayWidth) / 2;
        overlayY = (this.height - overlayHeight) / 2;
        textField = new GuiTextField(this.fontRendererObj, overlayX, overlayY, overlayWidth, overlayHeight);
        textField.setText(initialValue);
        textField.setFocused(true);
        buttonList.clear();
        // "Done" button positioned below the text field.
        buttonList.add(new GuiButton(1000000, overlayX, overlayY + overlayHeight + 5, overlayWidth, 20, "Done"));
    }

    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.fontRendererObj == null) {
            this.fontRendererObj = Minecraft.getMinecraft().fontRenderer;
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Draw semi-transparent background and overlay elements
        drawRect(0, 0, this.width, this.height, 0x88000000);
        drawCenteredString(this.fontRendererObj, "Edit " + propertyName, this.width / 2, overlayY - 30, 0xFFFFFF);
        textField.drawTextBox();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        textField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closeOverlay();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1000000) {
            if (propCallback != null) {
                propCallback.propertyUpdated(textField.getText());
            }
            closeOverlay();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    public void closeOverlay() {
        Keyboard.enableRepeatEvents(false);
        if (subGuiCallback != null) {
            subGuiCallback.onSubGuiClosed();
        }
    }
}
