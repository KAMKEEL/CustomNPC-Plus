package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityDash;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Dash ability type-specific settings.
 */
public class SubGuiAbilityDash extends SubGuiAbilityConfig {

    private final AbilityDash dash;

    public SubGuiAbilityDash(AbilityDash ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.dash = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Dash Mode
        addLabel(new GuiNpcLabel(100, "ability.mode", labelX, y + 5));
        String[] dashModes = {"Aggressive", "Defensive"};
        addButton(new GuiNpcButton(100, fieldX, y, 80, 20, dashModes, dash.getDashMode().ordinal()));

        y += 24;

        // Row 2: Dash Distance + Dash Speed
        addLabel(new GuiNpcLabel(101, "ability.dashDist", labelX, y + 5));
        addTextField(createFloatField(101, fieldX, y, 50, dash.getDashDistance()));

        addLabel(new GuiNpcLabel(102, "ability.dashSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(102, col2FieldX, y, 50, dash.getDashSpeed()));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 100) {
            dash.setDashMode(AbilityDash.DashMode.values()[button.getValue()]);
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 101: dash.setDashDistance(parseFloat(field, dash.getDashDistance())); break;
            case 102: dash.setDashSpeed(parseFloat(field, dash.getDashSpeed())); break;
        }
    }
}
