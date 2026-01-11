package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityTrap;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Trap ability type-specific settings.
 */
public class SubGuiAbilityTrap extends SubGuiAbilityConfig {

    private final AbilityTrap trap;

    public SubGuiAbilityTrap(AbilityTrap ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.trap = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Placement + Trigger Radius
        addLabel(new GuiNpcLabel(100, "ability.placement", labelX, y + 5));
        String[] placements = {"Caster", "Target", "Ahead"};
        addButton(new GuiNpcButton(100, fieldX, y, 55, 20, placements, trap.getPlacement().ordinal()));

        addLabel(new GuiNpcLabel(101, "ability.triggerRad", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, trap.getTriggerRadius()));

        y += 24;

        // Row 2: Damage + Damage Radius
        addLabel(new GuiNpcLabel(102, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, trap.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.dmgRadius", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, trap.getDamageRadius()));

        y += 24;

        // Row 3: Arm Time + Max Triggers
        addLabel(new GuiNpcLabel(104, "ability.armTime", labelX, y + 5));
        addTextField(createIntField(104, fieldX, y, 50, trap.getArmTime()));

        addLabel(new GuiNpcLabel(105, "ability.maxTriggers", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, trap.getMaxTriggers()));

        y += 24;

        // Row 4: Root Duration + Stun Duration
        addLabel(new GuiNpcLabel(106, "ability.rootDuration", labelX, y + 5));
        addTextField(createIntField(106, fieldX, y, 50, trap.getRootDuration()));

        addLabel(new GuiNpcLabel(107, "ability.stunDuration", col2LabelX, y + 5));
        addTextField(createIntField(107, col2FieldX, y, 50, trap.getStunDuration()));

        y += 24;

        // Row 5: Knockback + Knockback Up
        addLabel(new GuiNpcLabel(108, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(108, fieldX, y, 50, trap.getKnockback()));

        addLabel(new GuiNpcLabel(109, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(109, col2FieldX, y, 50, trap.getKnockbackUp()));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 100) {
            trap.setPlacement(AbilityTrap.TrapPlacement.values()[button.getValue()]);
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 101: trap.setTriggerRadius(parseFloat(field, trap.getTriggerRadius())); break;
            case 102: trap.setDamage(parseFloat(field, trap.getDamage())); break;
            case 103: trap.setDamageRadius(parseFloat(field, trap.getDamageRadius())); break;
            case 104: trap.setArmTime(field.getInteger()); break;
            case 105: trap.setMaxTriggers(field.getInteger()); break;
            case 106: trap.setRootDuration(field.getInteger()); break;
            case 107: trap.setStunDuration(field.getInteger()); break;
            case 108: trap.setKnockback(parseFloat(field, trap.getKnockback())); break;
            case 109: trap.setKnockbackUp(parseFloat(field, trap.getKnockbackUp())); break;
        }
    }
}
