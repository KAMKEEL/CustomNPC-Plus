package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Confirmation dialog for saving an ability preset.
 */
public class SubGuiAbilitySaveConfirm extends SubGuiInterface {

    private final Ability ability;
    private final boolean exists;

    public SubGuiAbilitySaveConfirm(Ability ability) {
        this.ability = ability;
        this.exists = AbilityController.Instance.hasSavedAbility(ability.getName());

        setBackground("menubg.png");
        xSize = 200;
        ySize = 100;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        if (exists) {
            addLabel(new GuiNpcLabel(0, "ability.save.overwrite", guiLeft + 10, y));
            y += 12;
            addLabel(new GuiNpcLabel(1, "'" + ability.getName() + "'?", guiLeft + 10, y));
        } else {
            addLabel(new GuiNpcLabel(0, "ability.save.confirm", guiLeft + 10, y));
            y += 12;
            addLabel(new GuiNpcLabel(1, "'" + ability.getName() + "'?", guiLeft + 10, y));
        }

        y += 30;

        addButton(new GuiNpcButton(0, guiLeft + 30, y, 60, 20, "gui.yes"));
        addButton(new GuiNpcButton(1, guiLeft + 110, y, 60, 20, "gui.no"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0) {
            // Save the ability
            AbilityController.Instance.saveAbility(ability);
            close();
        } else if (id == 1) {
            close();
        }
    }
}
