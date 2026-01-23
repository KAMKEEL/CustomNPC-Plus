package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityBeam;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;

/**
 * GUI for configuring Beam ability type-specific settings.
 */
public class SubGuiAbilityBeam extends SubGuiAbilityConfig {

    private final AbilityBeam beam;

    public SubGuiAbilityBeam(AbilityBeam ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.beam = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Damage Interval
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, beam.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.dmgInterval", col2LabelX, y + 5));
        addTextField(createIntField(101, col2FieldX, y, 50, beam.getDamageInterval()));

        y += 24;

        // Row 2: Beam Length + Beam Width
        addLabel(new GuiNpcLabel(102, "ability.beamLength", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, beam.getBeamLength()));

        addLabel(new GuiNpcLabel(103, "ability.beamWidth", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, beam.getBeamWidth()));

        y += 24;

        // Row 3: Sweep Angle + Sweep Speed
        addLabel(new GuiNpcLabel(104, "ability.sweepAngle", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, beam.getSweepAngle()));

        addLabel(new GuiNpcLabel(105, "ability.sweepSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, beam.getSweepSpeed()));

        y += 24;

        // Row 4: Piercing + Lock On Target
        addLabel(new GuiNpcLabel(106, "ability.piercing", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isPiercing() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.lockTarget", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isLockOnTarget() ? 1 : 0));

        y += 24;

        // Row 5: Sweep Back & Forth
        addLabel(new GuiNpcLabel(108, "ability.sweepBackForth", labelX, y + 5));
        addButton(new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isSweepBackAndForth() ? 1 : 0));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 106:
                beam.setPiercing(value == 1);
                break;
            case 107:
                beam.setLockOnTarget(value == 1);
                break;
            case 108:
                beam.setSweepBackAndForth(value == 1);
                break;
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                beam.setDamage(parseFloat(field, beam.getDamage()));
                break;
            case 101:
                beam.setDamageInterval(field.getInteger());
                break;
            case 102:
                beam.setBeamLength(parseFloat(field, beam.getBeamLength()));
                break;
            case 103:
                beam.setBeamWidth(parseFloat(field, beam.getBeamWidth()));
                break;
            case 104:
                beam.setSweepAngle(parseFloat(field, beam.getSweepAngle()));
                break;
            case 105:
                beam.setSweepSpeed(parseFloat(field, beam.getSweepSpeed()));
                break;
        }
    }
}
