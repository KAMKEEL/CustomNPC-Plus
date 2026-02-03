package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Dialog asking the user whether to clone-and-modify (convert to inline) or modify the parent ability.
 */
public class SubGuiAbilityEditMode extends SubGuiInterface {

    public static final int MODE_CLONE_MODIFY = 0;
    public static final int MODE_MODIFY_PARENT = 1;

    private int result = -1;

    public SubGuiAbilityEditMode() {
        setBackground("menubg.png");
        xSize = 220;
        ySize = 90;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0, "ability.edit.mode", guiLeft + 10, y));

        y += 25;
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 95, 20, "ability.cloneModify"));
        addButton(new GuiNpcButton(1, guiLeft + 115, y, 95, 20, "ability.modifyParent"));

        y += 25;
        addButton(new GuiNpcButton(2, guiLeft + 65, y, 90, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0) {
            result = MODE_CLONE_MODIFY;
            close();
        } else if (guibutton.id == 1) {
            result = MODE_MODIFY_PARENT;
            close();
        } else if (guibutton.id == 2) {
            result = -1;
            close();
        }
    }

    public int getResult() {
        return result;
    }
}
