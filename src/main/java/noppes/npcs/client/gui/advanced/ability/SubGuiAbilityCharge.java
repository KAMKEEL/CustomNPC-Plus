package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityCharge;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Charge ability type-specific settings.
 */
public class SubGuiAbilityCharge extends SubGuiAbilityConfig {

    private final AbilityCharge charge;

    public SubGuiAbilityCharge(AbilityCharge ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.charge = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Speed
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, charge.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, charge.getChargeSpeed()));

        y += 24;

        // Row 2: Knockback + Knockback Up
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, charge.getKnockback()));

        addLabel(new GuiNpcLabel(103, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, charge.getKnockbackUp()));

        y += 24;

        // Row 3: Max Distance + Hit Radius
        addLabel(new GuiNpcLabel(104, "ability.maxDist", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, charge.getMaxDistance()));

        addLabel(new GuiNpcLabel(105, "ability.hitRadius", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, charge.getHitRadius()));
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: charge.setDamage(parseFloat(field, charge.getDamage())); break;
            case 101: charge.setChargeSpeed(parseFloat(field, charge.getChargeSpeed())); break;
            case 102: charge.setKnockback(parseFloat(field, charge.getKnockback())); break;
            case 103: charge.setKnockbackUp(parseFloat(field, charge.getKnockbackUp())); break;
            case 104: charge.setMaxDistance(parseFloat(field, charge.getMaxDistance())); break;
            case 105: charge.setHitRadius(parseFloat(field, charge.getHitRadius())); break;
        }
    }
}
