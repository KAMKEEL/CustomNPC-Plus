package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Mouse;
import java.util.List;

/**
 * EditorButtonComponent wraps a ScriptGuiButton.
 * It supports dragging and provides custom editing buttons to modify width, height,
 * scale, label text, and the component ID.
 */
public class EditorButtonComponent extends AbstractEditorComponent {
    private ScriptGuiButton buttonComponent;

    public EditorButtonComponent(ScriptGuiButton button) {
        super(button.getPosX(), button.getPosY(), button.getWidth(), button.getHeight());
        this.buttonComponent = button;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        drawRect(posX, posY, posX + width, posY + height, 0xFF888888);
        String label = buttonComponent.getLabel();
        if (label != null)
            fr.drawStringWithShadow(label, posX + (width - fr.getStringWidth(label)) / 2, posY + (height - 8) / 2, buttonComponent.getColor());
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
        // Create custom editing buttons with dummy positions.
        // The updateCustomButtons() in the editor repositions them to the top row.
        buttonList.add(new GuiButton(101, 0, 0, 0, 0, "W+"));
        buttonList.add(new GuiButton(102, 0, 0, 0, 0, "W-"));
        buttonList.add(new GuiButton(103, 0, 0, 0, 0, "H+"));
        buttonList.add(new GuiButton(104, 0, 0, 0, 0, "H-"));
        buttonList.add(new GuiButton(105, 0, 0, 0, 0, "Sc"));
        buttonList.add(new GuiButton(107, 0, 0, 0, 0, "Lbl"));
        buttonList.add(new GuiButton(110, 0, 0, 0, 0, "ID"));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
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
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "Scale", Float.toString(buttonComponent.getScale()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newScale = Float.parseFloat(newValue);
                            buttonComponent.setScale(newScale);
                        } catch (NumberFormatException e) { }
                    }
                }));
                break;
            case 107:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "Label", buttonComponent.getLabel(), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        buttonComponent.setLabel(newValue);
                    }
                }));
                break;
            case 110:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty(Minecraft.getMinecraft().currentScreen, "ID", Integer.toString(buttonComponent.getID()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newID = Integer.parseInt(newValue);
                            buttonComponent.setID(newID);
                        } catch (NumberFormatException e) { }
                    }
                }));
                break;
        }
    }
}
