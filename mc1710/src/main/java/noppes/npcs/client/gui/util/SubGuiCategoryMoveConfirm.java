package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

public class SubGuiCategoryMoveConfirm extends SubGuiInterface {
    public boolean confirmed = false;

    private final int count;
    public final String destCategoryName;

    public SubGuiCategoryMoveConfirm(int count, String destCategoryName) {
        this.count = count;
        this.destCategoryName = destCategoryName;
        xSize = 220;
        ySize = 90;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        String line1 = String.format(StatCollector.translateToLocal("gui.move.confirmLine"), count);
        addLabel(new GuiNpcLabel(0, line1, guiLeft + 10, guiTop + 10, 0xFFFFFF));

        String dest = StatCollector.translateToLocal("gui.category") + ": \"" + destCategoryName + "\"";
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
