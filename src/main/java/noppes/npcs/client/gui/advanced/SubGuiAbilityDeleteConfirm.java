package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.CustomAbilityRemovePacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Confirmation dialog for deleting an ability preset.
 * Uses packet-based communication for proper client-server architecture.
 */
public class SubGuiAbilityDeleteConfirm extends SubGuiInterface {

    private final String abilityName;
    private final Runnable onDeleted;

    public SubGuiAbilityDeleteConfirm(String abilityName) {
        this(abilityName, null);
    }

    public SubGuiAbilityDeleteConfirm(String abilityName, Runnable onDeleted) {
        this.abilityName = abilityName;
        this.onDeleted = onDeleted;

        setBackground("menubg.png");
        xSize = 200;
        ySize = 100;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        addLabel(new GuiNpcLabel(0, "ability.delete.confirm", guiLeft + 10, y));
        y += 12;
        addLabel(new GuiNpcLabel(1, "'" + abilityName + "'?", guiLeft + 10, y));

        y += 30;

        addButton(new GuiNpcButton(0, guiLeft + 30, y, 60, 20, "gui.yes"));
        addButton(new GuiNpcButton(1, guiLeft + 110, y, 60, 20, "gui.no"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0) {
            // Delete the ability via packet
            PacketClient.sendClient(new CustomAbilityRemovePacket(abilityName));
            if (onDeleted != null) {
                onDeleted.run();
            }
            close();
        } else if (id == 1) {
            close();
        }
    }
}
