package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityShockwave;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Shockwave ability type-specific settings.
 */
public class SubGuiAbilityShockwave extends SubGuiAbilityConfig {

    private final AbilityShockwave shockwave;

    public SubGuiAbilityShockwave(AbilityShockwave ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.shockwave = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Push Radius
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, shockwave.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.pushRadius", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, shockwave.getPushRadius()));

        y += 24;

        // Row 2: Push Strength + Push Up
        addLabel(new GuiNpcLabel(102, "ability.pushStrength", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, shockwave.getPushStrength()));

        addLabel(new GuiNpcLabel(103, "ability.pushUp", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, shockwave.getPushUp()));

        y += 24;

        // Row 3: Stun Duration + Max Targets
        addLabel(new GuiNpcLabel(104, "ability.stunDuration", labelX, y + 5));
        addTextField(createIntField(104, fieldX, y, 50, shockwave.getStunDuration()));

        addLabel(new GuiNpcLabel(105, "ability.maxTargets", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, shockwave.getMaxTargets()));
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: shockwave.setDamage(parseFloat(field, shockwave.getDamage())); break;
            case 101: shockwave.setPushRadius(parseFloat(field, shockwave.getPushRadius())); break;
            case 102: shockwave.setPushStrength(parseFloat(field, shockwave.getPushStrength())); break;
            case 103: shockwave.setPushUp(parseFloat(field, shockwave.getPushUp())); break;
            case 104: shockwave.setStunDuration(field.getInteger()); break;
            case 105: shockwave.setMaxTargets(field.getInteger()); break;
        }
    }
}
