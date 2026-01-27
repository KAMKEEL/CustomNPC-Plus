package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityVortex;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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
        GuiNpcButton aoeBtn = new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, vortex.isAoe() ? 1 : 0);
        aoeBtn.setHoverText("ability.hover.aoe");
        addButton(aoeBtn);

        addLabel(new GuiNpcLabel(105, "ability.maxTargets", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, vortex.getMaxTargets()));

        y += 24;

        // Row 4: Effects button
        addButton(new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects"));

        y += 24;

        // Row 5: Damage on Pull + Pull Damage
        addLabel(new GuiNpcLabel(108, "ability.dmgOnPull", labelX, y + 5));
        GuiNpcButton dmgOnPullBtn = new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, vortex.isDamageOnPull() ? 1 : 0);
        dmgOnPullBtn.setHoverText("ability.hover.dmgOnPull");
        addButton(dmgOnPullBtn);

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
            case 150:
                setSubGui(new SubGuiAbilityEffects(vortex.getEffects()));
                break;
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                vortex.setEffects(result);
            }
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
            case 109:
                vortex.setPullDamage(parseFloat(field, vortex.getPullDamage()));
                break;
        }
    }
}
