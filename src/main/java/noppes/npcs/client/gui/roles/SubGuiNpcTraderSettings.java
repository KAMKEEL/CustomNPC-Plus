package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.roles.RoleTrader;

public class SubGuiNpcTraderSettings extends SubGuiInterface {

    private final RoleTrader role;

    public SubGuiNpcTraderSettings(RoleTrader role) {
        this.role = role;
        setBackground("menubg.png");
        xSize = 176;
        ySize = 136;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 14;

        addLabel(new GuiNpcLabel(1, "gui.ignoreDamage", guiLeft + 10, y + 5));
        addButton(new GuiNpcButtonYesNo(1, guiLeft + 120, y, role.ignoreDamage));
        getButton(1).setHoverText("gui.ignoreDamage.hover");

        y += 28;

        addLabel(new GuiNpcLabel(2, "gui.ignoreNBT", guiLeft + 10, y + 5));
        addButton(new GuiNpcButtonYesNo(2, guiLeft + 120, y, role.ignoreNBT));
        getButton(2).setHoverText("gui.ignoreNBT.hover");

        y += 28;

        addLabel(new GuiNpcLabel(3, "gui.recordHistory", guiLeft + 10, y + 5));
        addButton(new GuiNpcButtonYesNo(3, guiLeft + 120, y, role.recordHistory));
        getButton(3).setHoverText("gui.recordHistory.hover");

        y += 34;

        addButton(new GuiNpcButton(0, guiLeft + (xSize - 60) / 2, y, 60, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton button) {
        switch (button.id) {
            case 0:
                close();
                break;
            case 1:
                role.ignoreDamage = ((GuiNpcButtonYesNo) button).getBoolean();
                break;
            case 2:
                role.ignoreNBT = ((GuiNpcButtonYesNo) button).getBoolean();
                break;
            case 3:
                role.recordHistory = ((GuiNpcButtonYesNo) button).getBoolean();
                break;
        }
    }
}
