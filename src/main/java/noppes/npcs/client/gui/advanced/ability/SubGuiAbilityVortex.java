package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityVortex;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;

/**
 * GUI for configuring Vortex ability type-specific settings.
 */
public class SubGuiAbilityVortex extends SubGuiAbilityConfig {

    private final AbilityVortex vortex;

    public SubGuiAbilityVortex(AbilityVortex ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.vortex = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Pull Radius + Pull Strength
        addLabel(new GuiNpcLabel(100, "ability.pullRadius", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, vortex.getPullRadius()));

        addLabel(new GuiNpcLabel(101, "ability.pullStrength", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, vortex.getPullStrength()));

        y += 24;

        // Row 2: Damage + Knockback
        addLabel(new GuiNpcLabel(102, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, vortex.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.knockback", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, vortex.getKnockback()));

        y += 24;

        // Row 3: AOE + Max Targets
        addLabel(new GuiNpcLabel(104, "ability.aoe", labelX, y + 5));
        addButton(new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, vortex.isAoe() ? 1 : 0));

        addLabel(new GuiNpcLabel(105, "ability.maxTargets", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, vortex.getMaxTargets()));

        y += 24;

        // Row 4: Stun Duration + Root Duration
        addLabel(new GuiNpcLabel(106, "ability.stunDuration", labelX, y + 5));
        addTextField(createIntField(106, fieldX, y, 50, vortex.getStunDuration()));

        addLabel(new GuiNpcLabel(107, "ability.rootDuration", col2LabelX, y + 5));
        addTextField(createIntField(107, col2FieldX, y, 50, vortex.getRootDuration()));

        y += 24;

        // Row 5: Damage on Pull + Pull Damage
        addLabel(new GuiNpcLabel(108, "ability.dmgOnPull", labelX, y + 5));
        addButton(new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, vortex.isDamageOnPull() ? 1 : 0));

        addLabel(new GuiNpcLabel(109, "ability.pullDamage", col2LabelX, y + 5));
        addTextField(createFloatField(109, col2FieldX, y, 50, vortex.getPullDamage()));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 104:
                vortex.setAoe(value == 1);
                break;
            case 108:
                vortex.setDamageOnPull(value == 1);
                break;
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                vortex.setPullRadius(parseFloat(field, vortex.getPullRadius()));
                break;
            case 101:
                vortex.setPullStrength(parseFloat(field, vortex.getPullStrength()));
                break;
            case 102:
                vortex.setDamage(parseFloat(field, vortex.getDamage()));
                break;
            case 103:
                vortex.setKnockback(parseFloat(field, vortex.getKnockback()));
                break;
            case 105:
                vortex.setMaxTargets(field.getInteger());
                break;
            case 106:
                vortex.setStunDuration(field.getInteger());
                break;
            case 107:
                vortex.setRootDuration(field.getInteger());
                break;
            case 109:
                vortex.setPullDamage(parseFloat(field, vortex.getPullDamage()));
                break;
        }
    }
}
