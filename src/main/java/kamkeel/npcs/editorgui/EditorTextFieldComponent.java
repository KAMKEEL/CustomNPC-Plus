package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiTextField;
import noppes.npcs.client.gui.custom.components.CustomGuiTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

/**
 * EditorTextFieldComponent wraps a ScriptGuiTextField.
 * It renders the actual text field and provides editing buttons for width, height, default text, and ID.
 */
public class EditorTextFieldComponent extends AbstractEditorComponent {
    private ScriptGuiTextField textFieldComponent;

    public EditorTextFieldComponent(ScriptGuiTextField textField) {
        super(textField.getPosX(), textField.getPosY(), textField.getWidth(), textField.getHeight());
        this.textFieldComponent = textField;
        this.id = textField.getID();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CustomGuiTextField renderField = CustomGuiTextField.fromComponent(textFieldComponent);
        renderField.xPosition = this.posX;
        renderField.yPosition = this.posY;
        renderField.drawTextBox();
        renderSelectionOutline();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseX >= posX && mouseX < posX + width &&
           mouseY >= posY && mouseY < posY + height) {
            selected = true;
            return true;
        }
        selected = false;
        return false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int mouseButton) {
        if(selected) {
            textFieldComponent.setPos(posX, posY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) { }

    @Override
    public Object toScriptComponent() {
        return textFieldComponent;
    }

    @Override
    public void addEditorButtons(List<GuiButton> buttonList) {
        buttonList.add(new GuiButton(301, 0, 0, 30, 20, "W+"));
        buttonList.add(new GuiButton(302, 0, 0, 30, 20, "W-"));
        buttonList.add(new GuiButton(303, 0, 0, 30, 20, "H+"));
        buttonList.add(new GuiButton(304, 0, 0, 30, 20, "H-"));
        buttonList.add(new GuiButton(307, 0, 0, 30, 20, "Txt"));
        buttonList.add(new GuiButton(310, 0, 0, 30, 20, "ID"));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
        switch(button.id) {
            case 301:
                width += 5;
                textFieldComponent.setSize(width, height);
                break;
            case 302:
                width = Math.max(10, width - 5);
                textFieldComponent.setSize(width, height);
                break;
            case 303:
                height += 5;
                textFieldComponent.setSize(width, height);
                break;
            case 304:
                height = Math.max(10, height - 5);
                textFieldComponent.setSize(width, height);
                break;
            case 307:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Default Text", textFieldComponent.getText(), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        textFieldComponent.setText(newValue);
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 310:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("ID", Integer.toString(textFieldComponent.getID()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newID = Integer.parseInt(newValue);
                            textFieldComponent.setID(newID);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
        }
    }
}
