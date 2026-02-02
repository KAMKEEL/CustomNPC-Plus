package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Dialog asking the user whether to Clone (inline copy) or Reference the loaded ability.
 */
public class SubGuiAbilityLoadMode extends SubGuiInterface {

    public static final int MODE_CLONE = 0;
    public static final int MODE_REFERENCE = 1;

    private int result = -1;

    public SubGuiAbilityLoadMode() {
        setBackground("menubg.png");
        xSize = 200;
        ySize = 90;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0, "ability.load.mode", guiLeft + 10, y));

        y += 25;
        addButton(new GuiNpcButton(0, guiLeft + 15, y, 80, 20, "ability.clone"));
        addButton(new GuiNpcButton(1, guiLeft + 105, y, 80, 20, "ability.reference"));

        y += 25;
        addButton(new GuiNpcButton(2, guiLeft + 55, y, 90, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0) {
            result = MODE_CLONE;
            close();
        } else if (guibutton.id == 1) {
            result = MODE_REFERENCE;
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
