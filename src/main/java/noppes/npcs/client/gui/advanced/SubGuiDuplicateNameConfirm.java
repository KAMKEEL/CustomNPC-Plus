package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Confirmation dialog shown when saving an ability preset with a name that already exists.
 */
public class SubGuiDuplicateNameConfirm extends SubGuiInterface {

    private boolean confirmed = false;

    public SubGuiDuplicateNameConfirm() {
        setBackground("menubg.png");
        xSize = 220;
        ySize = 80;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0, "ability.duplicate.name", guiLeft + 10, y));

        y += 30;
        addButton(new GuiNpcButton(0, guiLeft + 20, y, 80, 20, "gui.continue"));
        addButton(new GuiNpcButton(1, guiLeft + 120, y, 80, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0) {
            confirmed = true;
            close();
        } else if (guibutton.id == 1) {
            confirmed = false;
            close();
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
