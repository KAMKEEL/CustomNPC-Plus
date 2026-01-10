package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityHeavyHit;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Heavy Hit ability type-specific settings.
 */
public class SubGuiAbilityHeavyHit extends SubGuiAbilityConfig {

    private final AbilityHeavyHit heavyHit;

    public SubGuiAbilityHeavyHit(AbilityHeavyHit ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.heavyHit = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Stun Ticks
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, heavyHit.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.stunTicks", col2LabelX, y + 5));
        addTextField(createIntField(101, col2FieldX, y, 50, heavyHit.getStunTicks()));

        y += 24;

        // Row 2: Knockback + Knockback Up
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, heavyHit.getKnockback()));

        addLabel(new GuiNpcLabel(103, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, heavyHit.getKnockbackUp()));
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: heavyHit.setDamage(parseFloat(field, heavyHit.getDamage())); break;
            case 101: heavyHit.setStunTicks(field.getInteger()); break;
            case 102: heavyHit.setKnockback(parseFloat(field, heavyHit.getKnockback())); break;
            case 103: heavyHit.setKnockbackUp(parseFloat(field, heavyHit.getKnockbackUp())); break;
        }
    }
}
