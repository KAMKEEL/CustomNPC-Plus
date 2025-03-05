package kamkeel.npcs.editorgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

/**
 * SubGuiEditProperty is a modal overlay for editing a single property.
 * It overlays on top of the current screen rather than switching screens.
 *
 * Usage: Pass the property name, the current value (as a string),
 * an IPropertyEditorCallback to receive the updated value,
 * and an ISubGuiCallback to notify when the overlay is closed.
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
     * @param subGuiCallback callback for when the overlay closes
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
        overlayWidth = 150;
        overlayHeight = 20;
        overlayX = (width - overlayWidth) / 2;
        overlayY = (height - overlayHeight) / 2;
        textField = new GuiTextField(fontRendererObj, overlayX, overlayY, overlayWidth, overlayHeight);
        textField.setText(initialValue);
        textField.setFocused(true);
        buttonList.clear();
        // "Done" button below the text field.
        buttonList.add(new GuiButton(0, overlayX, overlayY + overlayHeight + 5, overlayWidth, 20, "Done"));
    }

    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw a semi-transparent dark overlay over the whole screen.
        drawRect(0, 0, width, height, 0x88000000);
        drawCenteredString(fontRendererObj, "Edit " + propertyName, width / 2, overlayY - 30, 0xFFFFFF);
        textField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        textField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            closeOverlay();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
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

    private void closeOverlay() {
        Keyboard.enableRepeatEvents(false);
        if (subGuiCallback != null) {
            subGuiCallback.onSubGuiClosed();
        }
    }
}
