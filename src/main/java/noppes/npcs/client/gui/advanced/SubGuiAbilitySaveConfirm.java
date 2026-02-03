package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitySavePacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Confirmation dialog for saving an ability preset.
 * Uses packet-based communication for proper client-server architecture.
 */
public class SubGuiAbilitySaveConfirm extends SubGuiInterface {

    private final Ability ability;
    private final IAbilityConfigCallback callback;

    public SubGuiAbilitySaveConfirm(Ability ability) {
        this(ability, null);
    }

    public SubGuiAbilitySaveConfirm(Ability ability, IAbilityConfigCallback callback) {
        this.ability = ability;
        this.callback = callback;

        setBackground("menubg.png");
        xSize = 200;
        ySize = 100;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        addLabel(new GuiNpcLabel(0, "ability.save.confirm", guiLeft + 10, y));
        y += 12;
        addLabel(new GuiNpcLabel(1, "'" + ability.getName() + "'?", guiLeft + 10, y));

        y += 30;

        addButton(new GuiNpcButton(0, guiLeft + 30, y, 60, 20, "gui.yes"));
        addButton(new GuiNpcButton(1, guiLeft + 110, y, 60, 20, "gui.no"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0) {
            // Save the ability via packet
            PacketClient.sendClient(new CustomAbilitySavePacket(ability.writeNBT()));
            if (callback != null) {
                callback.onAbilitySaved(ability);
            }
            close();
        } else if (id == 1) {
            close();
        }
    }
}
