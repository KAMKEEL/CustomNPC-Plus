package kamkeel.npcs.editorgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import java.util.ArrayList;
import java.util.List;

/**
 * EditorToolBar renders a toolbar at the bottom with buttons to add components or save the GUI.
 */
public class EditorToolBar extends Gui {
    private List<GuiButton> buttons = new ArrayList<>();
    private int x, y, width, height;

    public EditorToolBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        initButtons();
    }

    private void initButtons() {
        buttons.clear();
        // IDs: 1 = Add Button, 2 = Add Label, 3 = Add TextField, 4 = Add Rect, 8 = Save.
        buttons.add(new GuiButton(1, x + 10, y + 10, 80, 20, "Add Button"));
        buttons.add(new GuiButton(2, x + 100, y + 10, 80, 20, "Add Label"));
        buttons.add(new GuiButton(3, x + 190, y + 10, 80, 20, "Add TextField"));
        buttons.add(new GuiButton(4, x + 280, y + 10, 80, 20, "Add Rect"));
        buttons.add(new GuiButton(8, x + 370, y + 10, 80, 20, "Save"));
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        // Optionally draw a background (commented out if undesired).
        // drawRect(x, y, x + width, y + height, 0xAA333333);
        for (GuiButton btn : buttons) {
            btn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }
    }

    public List<GuiButton> getButtons() {
        return buttons;
    }

    public void updateDimensions(int newX, int newY, int newWidth, int newHeight) {
        this.x = newX;
        this.y = newY;
        this.width = newWidth;
        this.height = newHeight;
        initButtons();
    }

    private int getDefaultId() {
        return -1;
    }

    public void actionPerformed(GuiButton button, GuiCustomGuiEditor editor) {
        int centerX = editor.canvasX + editor.canvasWidth / 2;
        int centerY = editor.canvasY + editor.canvasHeight / 2;
        switch (button.id) {
            case 1:
                noppes.npcs.scripted.gui.ScriptGuiButton newButton =
                        new noppes.npcs.scripted.gui.ScriptGuiButton(getDefaultId(), "New Button", centerX - 40, centerY - 10, 80, 20);
                EditorButtonComponent eButton = new EditorButtonComponent(newButton);
                editor.addEditorComponent(eButton);
                break;
            case 2:
                noppes.npcs.scripted.gui.ScriptGuiLabel newLabel =
                        new noppes.npcs.scripted.gui.ScriptGuiLabel(getDefaultId(), "New Label", centerX - 40, centerY - 10, 80, 20);
                EditorLabelComponent eLabel = new EditorLabelComponent(newLabel);
                editor.addEditorComponent(eLabel);
                break;
            case 3:
                noppes.npcs.scripted.gui.ScriptGuiTextField newField =
                        new noppes.npcs.scripted.gui.ScriptGuiTextField(getDefaultId(), centerX - 50, centerY - 10, 100, 20);
                EditorTextFieldComponent eField = new EditorTextFieldComponent(newField);
                editor.addEditorComponent(eField);
                break;
            case 4:
                noppes.npcs.scripted.gui.ScriptGuiTexturedRect newRect =
                        new noppes.npcs.scripted.gui.ScriptGuiTexturedRect(getDefaultId(), "default:texture", centerX - 40, centerY - 10, 80, 20);
                EditorTexturedRectComponent eRect = new EditorTexturedRectComponent(newRect);
                editor.addEditorComponent(eRect);
                break;
            case 8:
                editor.saveAndClose();
                break;
        }
    }
}
