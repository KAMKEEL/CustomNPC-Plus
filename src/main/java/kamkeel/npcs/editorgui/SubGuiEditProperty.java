package kamkeel.npcs.editorgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

/**
 * SubGuiEditProperty is a pop-up for editing a property.
 * It accepts a parent screen and returns to that parent when “Done” is pressed.
 */
public class SubGuiEditProperty extends GuiScreen {
    private GuiScreen parent;
    private IPropertyEditorCallback callback;
    private GuiTextField textField;
    private String propertyName;
    private String initialValue;

    public SubGuiEditProperty(GuiScreen parent, String propertyName, String initialValue, IPropertyEditorCallback callback) {
        this.parent = parent;
        this.propertyName = propertyName;
        // Ensure initialValue is not null
        this.initialValue = (initialValue == null ? "" : initialValue);
        this.callback = callback;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int w = 150;
        int h = 20;
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        textField = new GuiTextField(fontRendererObj, x, y, w, h);
        // Ensure the text field is set with a non-null string.
        textField.setText(initialValue);
        textField.setFocused(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, x, y + h + 5, w, 20, "Done"));
    }

    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Edit " + propertyName, width / 2, height / 2 - 40, 0xFFFFFF);
        textField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        textField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            if (callback != null) {
                callback.propertyUpdated(textField.getText());
            }
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
