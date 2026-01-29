package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityCutter;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

/**
 * GUI for configuring Cutter ability type-specific settings.
 */
public class SubGuiAbilityCutter extends SubGuiAbilityConfig {

    private final AbilityCutter cutter;

    public SubGuiAbilityCutter(AbilityCutter ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.cutter = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Range
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, cutter.getDamage()));

        addLabel(new GuiNpcLabel(101, "gui.range", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, cutter.getRange()));

        y += 24;

        // Row 2: Arc Angle + Inner Radius
        addLabel(new GuiNpcLabel(102, "ability.arcAngle", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, cutter.getArcAngle()));

        addLabel(new GuiNpcLabel(103, "ability.innerRadius", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, cutter.getInnerRadius()));

        y += 24;

        // Row 3: Knockback + Sweep Speed
        addLabel(new GuiNpcLabel(104, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, cutter.getKnockback()));

        addLabel(new GuiNpcLabel(105, "ability.sweepSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, cutter.getSweepSpeed()));

        y += 24;

        // Row 4: Sweep Mode + Piercing
        addLabel(new GuiNpcLabel(106, "ability.sweepMode", labelX, y + 5));
        String[] sweepModes = {"Swipe", "Spin"};
        GuiNpcButton sweepModeBtn = new GuiNpcButton(106, fieldX, y, 70, 20, sweepModes, cutter.getSweepModeEnum().ordinal());
        sweepModeBtn.setHoverText("ability.hover.sweepMode");
        addButton(sweepModeBtn);

        addLabel(new GuiNpcLabel(107, "ability.piercing", col2LabelX, y + 5));
        GuiNpcButton piercingBtn = new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, cutter.isPiercing() ? 1 : 0);
        piercingBtn.setHoverText("ability.hover.piercing");
        addButton(piercingBtn);

        y += 24;

        // Row 5: Spin Duration (only for SPIN mode) + Effects button
        if (cutter.getSweepModeEnum() == AbilityCutter.SweepMode.SPIN) {
            addLabel(new GuiNpcLabel(108, "ability.duration", labelX, y + 5));
            GuiNpcTextField durationField = createIntField(108, fieldX, y, 50, cutter.getSpinDurationTicks());
            durationField.setMinMaxDefault(1, 1000, 60);
            addTextField(durationField);
        }

        addButton(new GuiNpcButton(150, col2LabelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 106:
                cutter.setSweepModeEnum(AbilityCutter.SweepMode.values()[value]);
                initGui(); // Refresh to show/hide duration field
                break;
            case 107:
                cutter.setPiercing(value == 1);
                break;
            case 150:
                setSubGui(new SubGuiAbilityEffects(cutter.getEffects()));
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
                cutter.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                cutter.setDamage(parseFloat(field, cutter.getDamage()));
                break;
            case 101:
                cutter.setRange(parseFloat(field, cutter.getRange()));
                break;
            case 102:
                cutter.setArcAngle(parseFloat(field, cutter.getArcAngle()));
                break;
            case 103:
                cutter.setInnerRadius(parseFloat(field, cutter.getInnerRadius()));
                break;
            case 104:
                cutter.setKnockback(parseFloat(field, cutter.getKnockback()));
                break;
            case 105:
                cutter.setSweepSpeed(parseFloat(field, cutter.getSweepSpeed()));
                break;
            case 108:
                cutter.setSpinDurationTicks(field.getInteger());
                break;
        }
    }
}
