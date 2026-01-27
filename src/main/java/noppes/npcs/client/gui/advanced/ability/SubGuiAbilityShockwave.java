package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityShockwave;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, shockwave.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.pushRadius", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, shockwave.getPushRadius()));

        y += 24;

        // Row 2: Push Strength
        addLabel(new GuiNpcLabel(102, "ability.pushStrength", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, shockwave.getPushStrength()));

        y += 24;

        // Row 3: Max Targets + Effects button
        addLabel(new GuiNpcLabel(104, "ability.maxTargets", labelX, y + 5));
        addTextField(createIntField(104, fieldX, y, 50, shockwave.getMaxTargets()));

        addButton(new GuiNpcButton(150, col2LabelX, y, 80, 20, "ability.effects"));

        y += 24;
        addLabel(new GuiNpcLabel(105, "ability.shockwaveFalloff", labelX, y + 5));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 150) {
            setSubGui(new SubGuiAbilityEffects(shockwave.getEffects()));
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                shockwave.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                shockwave.setDamage(parseFloat(field, shockwave.getDamage()));
                break;
            case 101:
                shockwave.setPushRadius(parseFloat(field, shockwave.getPushRadius()));
                break;
            case 102:
                shockwave.setPushStrength(parseFloat(field, shockwave.getPushStrength()));
                break;
            case 104:
                shockwave.setMaxTargets(field.getInteger());
                break;
        }
    }
}
