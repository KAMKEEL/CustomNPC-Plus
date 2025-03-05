package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiLabel;
import noppes.npcs.client.gui.custom.components.CustomGuiLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

/**
 * EditorLabelComponent wraps a ScriptGuiLabel.
 */
public class EditorLabelComponent extends AbstractEditorComponent {
    private ScriptGuiLabel labelComponent;

    public EditorLabelComponent(ScriptGuiLabel label) {
        super(label.getPosX(), label.getPosY(), label.getWidth(), label.getHeight());
        this.labelComponent = label;
        this.id = label.getID();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CustomGuiLabel renderLbl = CustomGuiLabel.fromComponent(labelComponent);
        renderLbl.x = this.posX;
        renderLbl.y = this.posY;
        renderLbl.setScale(labelComponent.getScale());
        renderLbl.alpha = labelComponent.getAlpha();
        renderLbl.rotation = labelComponent.getRotation();
        renderLbl.onRender(Minecraft.getMinecraft(), mouseX, mouseY, 0, partialTicks);
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
        buttonList.add(new GuiButton(201, 0, 0, 30, 20, "W+"));
        buttonList.add(new GuiButton(202, 0, 0, 30, 20, "W-"));
        buttonList.add(new GuiButton(203, 0, 0, 30, 20, "H+"));
        buttonList.add(new GuiButton(204, 0, 0, 30, 20, "H-"));
        buttonList.add(new GuiButton(205, 0, 0, 30, 20, "Sc"));
        buttonList.add(new GuiButton(206, 0, 0, 30, 20, "Al"));
        buttonList.add(new GuiButton(207, 0, 0, 30, 20, "Rt"));
        buttonList.add(new GuiButton(208, 0, 0, 30, 20, "Txt"));
        buttonList.add(new GuiButton(210, 0, 0, 30, 20, "ID"));
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
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Scale", Float.toString(labelComponent.getScale()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newScale = Float.parseFloat(newValue);
                            labelComponent.setScale(newScale);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 206:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Alpha", Float.toString(labelComponent.getAlpha()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newAlpha = Float.parseFloat(newValue);
                            labelComponent.setAlpha(newAlpha);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 207:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Rotation", Float.toString(labelComponent.getRotation()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newRot = Float.parseFloat(newValue);
                            labelComponent.setRotation(newRot);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 208:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Text", labelComponent.getText(), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        labelComponent.setText(newValue);
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 210:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("ID", Integer.toString(labelComponent.getID()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newID = Integer.parseInt(newValue);
                            labelComponent.setID(newID);
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
