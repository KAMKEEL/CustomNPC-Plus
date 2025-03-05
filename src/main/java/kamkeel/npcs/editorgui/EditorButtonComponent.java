package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiButton;
import noppes.npcs.client.gui.custom.components.CustomGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

/**
 * EditorButtonComponent wraps a ScriptGuiButton for editing.
 */
public class EditorButtonComponent extends AbstractEditorComponent {
    private ScriptGuiButton buttonComponent;

    public EditorButtonComponent(ScriptGuiButton button) {
        super(button.getPosX(), button.getPosY(), button.getWidth(), button.getHeight());
        this.buttonComponent = button;
        this.id = button.getID();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CustomGuiButton renderBtn = CustomGuiButton.fromComponent(buttonComponent);
        renderBtn.xPosition = this.posX;
        renderBtn.yPosition = this.posY;
        renderBtn.scale = buttonComponent.getScale();
        renderBtn.alpha = buttonComponent.getAlpha();
        renderBtn.onRender(Minecraft.getMinecraft(), mouseX, mouseY, 0, partialTicks);
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
        if (selected) {
            buttonComponent.setPos(posX, posY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) { }

    @Override
    public Object toScriptComponent() {
        return buttonComponent;
    }

    @Override
    public void addEditorButtons(List<GuiButton> buttonList) {
        buttonList.add(new GuiButton(101, 0, 0, 30, 20, "W+"));
        buttonList.add(new GuiButton(102, 0, 0, 30, 20, "W-"));
        buttonList.add(new GuiButton(103, 0, 0, 30, 20, "H+"));
        buttonList.add(new GuiButton(104, 0, 0, 30, 20, "H-"));
        buttonList.add(new GuiButton(105, 0, 0, 30, 20, "Sc"));
        buttonList.add(new GuiButton(106, 0, 0, 30, 20, "Al"));
        buttonList.add(new GuiButton(107, 0, 0, 30, 20, "Rt"));
        buttonList.add(new GuiButton(108, 0, 0, 30, 20, "Lbl"));
        buttonList.add(new GuiButton(110, 0, 0, 30, 20, "ID"));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
        GuiCustomGuiEditor editor = (GuiCustomGuiEditor) Minecraft.getMinecraft().currentScreen;
        switch (button.id) {
            case 101:
                width += 5;
                buttonComponent.setSize(width, height);
                break;
            case 102:
                width = Math.max(10, width - 5);
                buttonComponent.setSize(width, height);
                break;
            case 103:
                height += 5;
                buttonComponent.setSize(width, height);
                break;
            case 104:
                height = Math.max(10, height - 5);
                buttonComponent.setSize(width, height);
                break;
            case 105:
                editor.setSubGuiOverlay(new SubGuiEditProperty("Scale", Float.toString(buttonComponent.getScale()),
                    newValue -> {
                        try {
                            float newScale = Float.parseFloat(newValue);
                            buttonComponent.setScale(newScale);
                        } catch (NumberFormatException e) { }
                    }, () ->{
                    editor.clearSubGuiOverlay();
                    editor.setSelectedComponent(this);
                }));
                break;
            case 106:
                editor.setSubGuiOverlay(new SubGuiEditProperty("Alpha", Float.toString(buttonComponent.getAlpha()),
                    newValue -> {
                        try {
                            float newAlpha = Float.parseFloat(newValue);
                            buttonComponent.setAlpha(newAlpha);
                        } catch (NumberFormatException e) { }
                    }, () ->{
                    editor.clearSubGuiOverlay();
                    editor.setSelectedComponent(this);
                }));
                break;
            case 107:
                editor.setSubGuiOverlay(new SubGuiEditProperty("Rotation", Float.toString(buttonComponent.getRotation()),
                    newValue -> {
                        try {
                            float newRot = Float.parseFloat(newValue);
                            buttonComponent.setRotation(newRot);
                        } catch (NumberFormatException e) { }
                    }, () ->{
                    editor.clearSubGuiOverlay();
                    editor.setSelectedComponent(this);
                }));
                break;
            case 108:
                editor.setSubGuiOverlay(new SubGuiEditProperty("Label", buttonComponent.getLabel(),
                    newValue -> buttonComponent.setLabel(newValue), () ->{
                    editor.clearSubGuiOverlay();
                    editor.setSelectedComponent(this);
                }));
                break;
            case 110:
                editor.setSubGuiOverlay(new SubGuiEditProperty("ID", Integer.toString(buttonComponent.getID()),
                    newValue -> {
                        try {
                            int newID = Integer.parseInt(newValue);
                            buttonComponent.setID(newID);
                        } catch (NumberFormatException e) { }
                    }, () ->{
                    editor.clearSubGuiOverlay();
                    editor.setSelectedComponent(this);
                }));
                break;
        }
    }
}
