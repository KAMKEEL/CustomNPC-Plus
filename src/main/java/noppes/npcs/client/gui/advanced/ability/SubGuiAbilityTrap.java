package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityTrap;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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

        // Row 1: Duration + Placement
        addLabel(new GuiNpcLabel(99, "ability.duration", labelX, y + 5));
        GuiNpcTextField durationField = createIntField(99, fieldX, y, 50, trap.getDurationTicks());
        durationField.setMinMaxDefault(1, 2000, 200);
        addTextField(durationField);

        addLabel(new GuiNpcLabel(100, "ability.placement", col2LabelX, y + 5));
        String[] placements = {"Caster", "Target", "Ahead"};
        GuiNpcButton placementBtn = new GuiNpcButton(100, col2FieldX, y, 55, 20, placements, trap.getPlacement().ordinal());
        placementBtn.setHoverText("ability.hover.placement");
        addButton(placementBtn);

        y += 24;

        // Row 2: Trigger Radius
        addLabel(new GuiNpcLabel(101, "ability.triggerRad", labelX, y + 5));
        addTextField(createFloatField(101, fieldX, y, 50, trap.getTriggerRadius()));

        y += 24;

        // Row 2: Damage + Damage Radius
        addLabel(new GuiNpcLabel(102, "enchantment.damage", labelX, y + 5));
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

        // Row 4: Knockback + Effects button
        addLabel(new GuiNpcLabel(108, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(108, fieldX, y, 50, trap.getKnockback()));

        addButton(new GuiNpcButton(150, col2LabelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 100) {
            trap.setPlacement(AbilityTrap.TrapPlacement.values()[button.getValue()]);
        } else if (id == 150) {
            setSubGui(new SubGuiAbilityEffects(trap.getEffects()));
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                trap.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 99:
                trap.setDurationTicks(field.getInteger());
                break;
            case 101:
                trap.setTriggerRadius(parseFloat(field, trap.getTriggerRadius()));
                break;
            case 102:
                trap.setDamage(parseFloat(field, trap.getDamage()));
                break;
            case 103:
                trap.setDamageRadius(parseFloat(field, trap.getDamageRadius()));
                break;
            case 104:
                trap.setArmTime(field.getInteger());
                break;
            case 105:
                trap.setMaxTriggers(field.getInteger());
                break;
            case 108:
                trap.setKnockback(parseFloat(field, trap.getKnockback()));
                break;
        }
    }
}
