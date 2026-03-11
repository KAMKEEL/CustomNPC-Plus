package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;

/**
 * Choice dialog for the Load button: Load Ability or Load Chain.
 */
public class SubGuiLoadTypeChoice extends SubGuiSimpleChoice {

    public static final int RESULT_NONE = -1;
    public static final int RESULT_ABILITY = 0;
    public static final int RESULT_CHAIN = 1;

    public SubGuiLoadTypeChoice() {
        setBackground("menubg.png");
        xSize = 200;
        ySize = 80;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0, "ability.loadType", guiLeft + 10, y));

        y += 20;
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 85, 20, "ability.loadAbility"));
        addButton(new GuiNpcButton(1, guiLeft + 105, y, 85, 20, "ability.loadChain"));

        y += 24;
        addButton(new GuiNpcButton(2, guiLeft + 55, y, 90, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 0 || id == 1) {
            setResult(id);
            close();
        } else if (id == 2) {
            close();
        }
    }
}
