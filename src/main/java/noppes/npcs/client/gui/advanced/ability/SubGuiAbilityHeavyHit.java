package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityHeavyHit;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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

        // Row 1: Damage + Knockback
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, heavyHit.getDamage()));

        addLabel(new GuiNpcLabel(102, "ability.knockback", col2LabelX, y + 5));
        addTextField(createFloatField(102, guiLeft + 205, y, 50, heavyHit.getKnockback()));

        y += 24;

        // Row 2: Effects button
        addButton(new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 150) {
            setSubGui(new SubGuiAbilityEffects(heavyHit.getEffects()));
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                heavyHit.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                heavyHit.setDamage(parseFloat(field, heavyHit.getDamage()));
                break;
            case 102:
                heavyHit.setKnockback(parseFloat(field, heavyHit.getKnockback()));
                break;
        }
    }
}
