package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilitySweeper;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * GUI for configuring Sweeper ability type-specific settings.
 * Sweeper is a low rotating beam that players can jump over.
 */
public class SubGuiAbilitySweeper extends SubGuiAbilityConfig {

    private final AbilitySweeper sweeper;
    private int editingVisualColorId = 0;

    public SubGuiAbilitySweeper(AbilitySweeper ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.sweeper = ability;
    }

    @Override
    protected boolean hasVisualSettings() {
        return true;
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
        addTextField(createFloatField(100, fieldX, y, 50, sweeper.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.dmgInterval", col2LabelX, y + 5));
        addTextField(createIntField(101, col2FieldX, y, 50, sweeper.getDamageInterval()));

        y += 24;

        // Row 2: Beam Length + Beam Width
        addLabel(new GuiNpcLabel(102, "ability.beamLength", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, sweeper.getBeamLength()));

        addLabel(new GuiNpcLabel(103, "ability.beamWidth", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, sweeper.getBeamWidth()));

        y += 24;

        // Row 3: Beam Height + Sweep Speed
        addLabel(new GuiNpcLabel(104, "ability.beamHeight", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, sweeper.getBeamHeight()));

        addLabel(new GuiNpcLabel(105, "ability.sweepSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, sweeper.getSweepSpeed()));

        y += 24;

        // Row 4: Number of Rotations + Piercing
        addLabel(new GuiNpcLabel(106, "ability.rotations", labelX, y + 5));
        addTextField(createIntField(106, fieldX, y, 50, sweeper.getNumberOfRotations()));

        addLabel(new GuiNpcLabel(107, "ability.piercing", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, sweeper.isPiercing() ? 1 : 0));

        y += 24;

        // Row 5: Lock On Target
        addLabel(new GuiNpcLabel(108, "ability.lockTarget", labelX, y + 5));
        addButton(new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, sweeper.isLockOnTarget() ? 1 : 0));
    }

    @Override
    protected void initVisualTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 180;
        int col2FieldX = guiLeft + 260;

        // Row 1: Inner Color + Outer Color
        addLabel(new GuiNpcLabel(200, "ability.innerColor", labelX, y + 5));
        String innerHex = String.format("%06X", sweeper.getInnerColor() & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(200, fieldX, y, 55, 20, innerHex);
        innerColorBtn.setTextColor(sweeper.getInnerColor() & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(201, "ability.outerColor", col2LabelX, y + 5));
        String outerHex = String.format("%06X", sweeper.getOuterColor() & 0xFFFFFF);
        GuiNpcButton outerColorBtn = new GuiNpcButton(201, col2FieldX, y, 55, 20, outerHex);
        outerColorBtn.setTextColor(sweeper.getOuterColor() & 0xFFFFFF);
        outerColorBtn.setEnabled(sweeper.isOuterColorEnabled());
        addButton(outerColorBtn);

        y += 24;

        // Row 2: Outer Color Enabled + Outer Color Width
        addLabel(new GuiNpcLabel(202, "ability.outerEnabled", labelX, y + 5));
        addButton(new GuiNpcButton(202, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, sweeper.isOuterColorEnabled() ? 1 : 0));

        addLabel(new GuiNpcLabel(203, "ability.outerWidth", col2LabelX, y + 5));
        GuiNpcTextField widthField = createFloatField(203, col2FieldX, y, 55, sweeper.getOuterColorWidth());
        widthField.setEnabled(sweeper.isOuterColorEnabled());
        addTextField(widthField);
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 107:
                sweeper.setPiercing(value == 1);
                break;
            case 108:
                sweeper.setLockOnTarget(value == 1);
                break;
        }
    }

    @Override
    protected void handleVisualButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 200:
                editingVisualColorId = 200;
                setSubGui(new SubGuiColorSelector(sweeper.getInnerColor()));
                break;
            case 201:
                editingVisualColorId = 201;
                setSubGui(new SubGuiColorSelector(sweeper.getOuterColor()));
                break;
            case 202:
                sweeper.setOuterColorEnabled(value == 1);
                initGui();
                break;
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector && editingVisualColorId != 0) {
            SubGuiColorSelector colorSelector = (SubGuiColorSelector) subgui;
            int rgb = colorSelector.color & 0x00FFFFFF;
            if (editingVisualColorId == 200) {
                sweeper.setInnerColor(rgb);
            } else if (editingVisualColorId == 201) {
                sweeper.setOuterColor(rgb);
            }
            editingVisualColorId = 0;
            initGui();
        } else {
            super.subGuiClosed(subgui);
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                sweeper.setDamage(parseFloat(field, sweeper.getDamage()));
                break;
            case 101:
                sweeper.setDamageInterval(field.getInteger());
                break;
            case 102:
                sweeper.setBeamLength(parseFloat(field, sweeper.getBeamLength()));
                break;
            case 103:
                sweeper.setBeamWidth(parseFloat(field, sweeper.getBeamWidth()));
                break;
            case 104:
                sweeper.setBeamHeight(parseFloat(field, sweeper.getBeamHeight()));
                break;
            case 105:
                sweeper.setSweepSpeed(parseFloat(field, sweeper.getSweepSpeed()));
                break;
            case 106:
                sweeper.setNumberOfRotations(field.getInteger());
                break;
        }
    }

    @Override
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 203:
                sweeper.setOuterColorWidth(parseFloat(field, sweeper.getOuterColorWidth()));
                break;
        }
    }
}
