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

        // Row 2: Knockback
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, charge.getKnockback()));

        y += 24;

        // Row 3: Hit Width
        addLabel(new GuiNpcLabel(104, "ability.hitWidth", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, charge.getHitWidth()));

    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: charge.setDamage(parseFloat(field, charge.getDamage())); break;
            case 101: charge.setChargeSpeed(parseFloat(field, charge.getChargeSpeed())); break;
            case 102: charge.setKnockback(parseFloat(field, charge.getKnockback())); break;
            case 104: charge.setHitWidth(parseFloat(field, charge.getHitWidth())); break;
        }
    }
}
