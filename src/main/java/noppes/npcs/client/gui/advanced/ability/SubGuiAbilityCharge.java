package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityCharge;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, charge.getDamage()));

        addLabel(new GuiNpcLabel(101, "stats.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, charge.getChargeSpeed()));

        y += 24;

        // Row 2: Knockback
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, charge.getKnockback()));

        y += 24;

        // Row 3: Hit Width + Effects
        addLabel(new GuiNpcLabel(104, "ability.hitWidth", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, charge.getHitWidth()));

        addButton(new GuiNpcButton(150, col2LabelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 150) {
            setSubGui(new SubGuiAbilityEffects(charge.getEffects()));
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                charge.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                charge.setDamage(parseFloat(field, charge.getDamage()));
                break;
            case 101:
                charge.setChargeSpeed(parseFloat(field, charge.getChargeSpeed()));
                break;
            case 102:
                charge.setKnockback(parseFloat(field, charge.getKnockback()));
                break;
            case 104:
                charge.setHitWidth(parseFloat(field, charge.getHitWidth()));
                break;
        }
    }
}
