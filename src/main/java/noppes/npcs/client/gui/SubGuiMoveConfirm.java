package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiMoveConfirm extends SubGuiInterface {
    public boolean confirmed = false;

    private final int count;
    public final int destTab;
    public final String destFolder;

    public SubGuiMoveConfirm(int count, int destTab, String destFolder) {
        this.count = count;
        this.destTab = destTab;
        this.destFolder = destFolder;
        xSize = 220;
        ySize = 90;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        String line1 = "Move " + count + " clone" + (count != 1 ? "s" : "") + " to:";
        addLabel(new GuiNpcLabel(0, line1, guiLeft + 10, guiTop + 10, 0xFFFFFF));

        String dest;
        if (destFolder != null) {
            dest = "Folder: \"" + destFolder + "\"";
        } else {
            dest = "Tab: " + destTab;
        }
        addLabel(new GuiNpcLabel(1, "  " + dest, guiLeft + 10, guiTop + 25, 0xFFFFFF));

        addButton(new GuiNpcButton(0, guiLeft + 10, guiTop + 55, 95, 20, "gui.yes"));
        addButton(new GuiNpcButton(1, guiLeft + 115, guiTop + 55, 95, 20, "gui.cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            confirmed = true;
            close();
        }
        if (guibutton.id == 1) {
            close();
        }
    }

    @Override
    public void save() {
    }
}
