package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

/**
 * EditorLabelComponent wraps a ScriptGuiLabel and adds editing buttons for width, height, scale, text, and ID.
 */
public class EditorLabelComponent extends AbstractEditorComponent {
    private ScriptGuiLabel labelComponent;

    public EditorLabelComponent(ScriptGuiLabel label) {
        super(label.getPosX(), label.getPosY(), label.getWidth(), label.getHeight());
        this.labelComponent = label;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        drawRect(posX, posY, posX + width, posY + height, 0xFFAAAAAA);
        String text = labelComponent.getText();
        if(text != null)
            fr.drawStringWithShadow(text, posX + 2, posY + (height - 8) / 2, labelComponent.getColor());
        renderSelectionOutline();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= posX && mouseX <= posX + width &&
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
            labelComponent.setPos(posX, posY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) { }

    @Override
    public Object toScriptComponent() {
        return labelComponent;
    }

    @Override
    public void addEditorButtons(List<GuiButton> buttonList) {
        buttonList.add(new GuiButton(201, 0, 0, 0, 0, "W+"));
        buttonList.add(new GuiButton(202, 0, 0, 0, 0, "W-"));
        buttonList.add(new GuiButton(203, 0, 0, 0, 0, "H+"));
        buttonList.add(new GuiButton(204, 0, 0, 0, 0, "H-"));
        buttonList.add(new GuiButton(205, 0, 0, 0, 0, "Sc"));
        buttonList.add(new GuiButton(207, 0, 0, 0, 0, "Txt"));
        buttonList.add(new GuiButton(210, 0, 0, 0, 0, "ID"));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
        switch(button.id) {
            case 201:
                width += 5;
                labelComponent.setSize(width, height);
                break;
            case 202:
                width = Math.max(10, width - 5);
                labelComponent.setSize(width, height);
                break;
            case 203:
                height += 5;
                labelComponent.setSize(width, height);
                break;
            case 204:
                height = Math.max(10, height - 5);
                labelComponent.setSize(width, height);
                break;
            case 205:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "Scale", Float.toString(labelComponent.getScale()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newScale = Float.parseFloat(newValue);
                            labelComponent.setScale(newScale);
                        } catch (NumberFormatException e) { }
                    }
                }));
                break;
            case 207:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "Text", labelComponent.getText(), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        labelComponent.setText(newValue);
                    }
                }));
                break;
            case 210:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "ID", Integer.toString(labelComponent.getID()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newID = Integer.parseInt(newValue);
                            labelComponent.setID(newID);
                        } catch (NumberFormatException e) { }
                    }
                }));
                break;
        }
    }
}
