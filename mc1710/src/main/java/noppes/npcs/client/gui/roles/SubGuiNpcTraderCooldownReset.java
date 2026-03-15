package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.roles.RoleTrader;

/**
 * Confirmation dialog for resetting trader stock cooldown.
 */
public class SubGuiNpcTraderCooldownReset extends SubGuiInterface {

    private final RoleTrader role;
    public boolean confirmed = false;

    public SubGuiNpcTraderCooldownReset(RoleTrader role) {
        this.role = role;
        setBackground("menubg.png");
        xSize = 200;
        ySize = 100;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 15;

        addLabel(new GuiNpcLabel(0, "stock.cooldown.confirm", guiLeft + 10, y));
        y += 12;
        addLabel(new GuiNpcLabel(1, "stock.cooldown.warning", guiLeft + 10, y));

        y += 35;

        addButton(new GuiNpcButton(0, guiLeft + 30, y, 60, 20, "gui.yes"));
        addButton(new GuiNpcButton(1, guiLeft + 110, y, 60, 20, "gui.no"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0) {
            // Reset the cooldown
            role.resetCooldown();
            confirmed = true;
            close();
        } else if (guibutton.id == 1) {
            close();
        }
    }
}
