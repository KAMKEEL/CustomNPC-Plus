package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Confirmation dialog shown when saving an ability preset with a name that already exists.
 */
public class SubGuiDuplicateNameConfirm extends SubGuiInterface {

    public enum Result {
        CONTINUE,
        BACK,
        CANCEL
    }

    private Result result = Result.CANCEL;

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
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 65, 20, "gui.continue"));
        addButton(new GuiNpcButton(1, guiLeft + 78, y, 65, 20, "gui.back"));
        addButton(new GuiNpcButton(2, guiLeft + 146, y, 65, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0) {
            result = Result.CONTINUE;
            close();
        } else if (guibutton.id == 1) {
            result = Result.BACK;
            close();
        } else if (guibutton.id == 2) {
            result = Result.CANCEL;
            close();
        }
    }

    public Result getResult() {
        return result;
    }

    public boolean isConfirmed() {
        return result == Result.CONTINUE;
    }

    public boolean isBack() {
        return result == Result.BACK;
    }

    public boolean isCancelled() {
        return result == Result.CANCEL;
    }
}
