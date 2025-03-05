package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

/**
 * EditorTextFieldComponent wraps a ScriptGuiTextField.
 * Since ScriptGuiTextField only supports width, height, and default text,
 * this editor only provides editing buttons for width, height, and default text.
 */
public class EditorTextFieldComponent extends AbstractEditorComponent {
    private ScriptGuiTextField textFieldComponent;

    public EditorTextFieldComponent(ScriptGuiTextField textField) {
        super(textField.getPosX(), textField.getPosY(), textField.getWidth(), textField.getHeight());
        this.textFieldComponent = textField;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        drawRect(posX, posY, posX + width, posY + height, 0xFFFFFFFF);
        String text = textFieldComponent.getText();
        if(text != null)
            fr.drawStringWithShadow(text, posX + 2, posY + (height - 8) / 2, textFieldComponent.getColor());
        renderSelectionOutline();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseX >= posX && mouseX <= posX + width &&
           mouseY >= posY && mouseY <= posY + height) {
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
        buttonList.add(new GuiButton(301, 0, 0, 0, 0, "W+"));
        buttonList.add(new GuiButton(302, 0, 0, 0, 0, "W-"));
        buttonList.add(new GuiButton(303, 0, 0, 0, 0, "H+"));
        buttonList.add(new GuiButton(304, 0, 0, 0, 0, "H-"));
        buttonList.add(new GuiButton(307, 0, 0, 0, 0, "Text"));
        buttonList.add(new GuiButton(310, 0, 0, 0, 0, "ID"));
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
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "Default Text", textFieldComponent.getText(), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        textFieldComponent.setText(newValue);
                    }
                }));
                break;
            case 310:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "ID", Integer.toString(textFieldComponent.getID()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newID = Integer.parseInt(newValue);
                            textFieldComponent.setID(newID);
                        } catch (NumberFormatException e) { }
                    }
                }));
                break;
        }
    }
}
