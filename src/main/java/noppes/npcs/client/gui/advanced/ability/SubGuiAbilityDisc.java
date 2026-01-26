package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityDisc;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * GUI for configuring Disc ability type-specific settings.
 */
public class SubGuiAbilityDisc extends SubGuiAbilityConfig {

    private final AbilityDisc disc;
    private int editingVisualColorId = 0;

    public SubGuiAbilityDisc(AbilityDisc ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.disc = ability;
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

        // Row 1: Damage + Speed
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, disc.getDamage()));

        addLabel(new GuiNpcLabel(101, "stats.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, disc.getSpeed()));

        y += 24;

        // Row 2: Disc Radius + Disc Thickness
        addLabel(new GuiNpcLabel(102, "ability.discRadius", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, disc.getDiscRadius()));

        addLabel(new GuiNpcLabel(103, "ability.discThickness", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, disc.getDiscThickness()));

        y += 24;

        // Row 3: Homing + Homing Strength
        addLabel(new GuiNpcLabel(104, "ability.homing", labelX, y + 5));
        addButton(new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, disc.isHoming() ? 1 : 0));

        addLabel(new GuiNpcLabel(105, "ability.homingStr", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, disc.getHomingStrength()));

        y += 24;

        // Row 4: Boomerang + Boomerang Delay
        addLabel(new GuiNpcLabel(106, "ability.boomerang", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, disc.isBoomerang() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.boomerangDelay", col2LabelX, y + 5));
        addTextField(createIntField(107, col2FieldX, y, 50, disc.getBoomerangDelay()));

        y += 24;

        // Row 5: Explosive + Explosion Radius
        addLabel(new GuiNpcLabel(108, "ability.explosive", labelX, y + 5));
        addButton(new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, disc.isExplosive() ? 1 : 0));

        addLabel(new GuiNpcLabel(109, "ability.explosionRad", col2LabelX, y + 5));
        addTextField(createFloatField(109, col2FieldX, y, 50, disc.getExplosionRadius()));

        y += 24;

        // Row 6: Knockback + Max Distance
        addLabel(new GuiNpcLabel(110, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(110, fieldX, y, 50, disc.getKnockback()));

        addLabel(new GuiNpcLabel(111, "ability.maxDist", col2LabelX, y + 5));
        addTextField(createFloatField(111, col2FieldX, y, 50, disc.getMaxDistance()));
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
        String innerHex = String.format("%06X", disc.getInnerColor() & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(200, fieldX, y, 55, 20, innerHex);
        innerColorBtn.setTextColor(disc.getInnerColor() & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(201, "ability.outerColor", col2LabelX, y + 5));
        String outerHex = String.format("%06X", disc.getOuterColor() & 0xFFFFFF);
        GuiNpcButton outerColorBtn = new GuiNpcButton(201, col2FieldX, y, 55, 20, outerHex);
        outerColorBtn.setTextColor(disc.getOuterColor() & 0xFFFFFF);
        outerColorBtn.setEnabled(disc.isOuterColorEnabled());
        addButton(outerColorBtn);

        y += 24;

        // Row 2: Outer Color Enabled + Outer Color Width
        addLabel(new GuiNpcLabel(202, "ability.outerEnabled", labelX, y + 5));
        addButton(new GuiNpcButton(202, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, disc.isOuterColorEnabled() ? 1 : 0));

        addLabel(new GuiNpcLabel(203, "ability.outerWidth", col2LabelX, y + 5));
        GuiNpcTextField widthField = createFloatField(203, col2FieldX, y, 55, disc.getOuterColorWidth());
        widthField.setEnabled(disc.isOuterColorEnabled());
        addTextField(widthField);

        y += 24;

        // Row 3: Rotation Speed
        addLabel(new GuiNpcLabel(204, "ability.rotationSpeed", labelX, y + 5));
        addTextField(createFloatField(204, fieldX, y, 50, disc.getRotationSpeed()));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 104:
                disc.setHoming(value == 1);
                break;
            case 106:
                disc.setBoomerang(value == 1);
                break;
            case 108:
                disc.setExplosive(value == 1);
                break;
        }
    }

    @Override
    protected void handleVisualButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 200:
                editingVisualColorId = 200;
                setSubGui(new SubGuiColorSelector(disc.getInnerColor()));
                break;
            case 201:
                editingVisualColorId = 201;
                setSubGui(new SubGuiColorSelector(disc.getOuterColor()));
                break;
            case 202:
                disc.setOuterColorEnabled(value == 1);
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
                disc.setInnerColor(rgb);
            } else if (editingVisualColorId == 201) {
                disc.setOuterColor(rgb);
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
                disc.setDamage(parseFloat(field, disc.getDamage()));
                break;
            case 101:
                disc.setSpeed(parseFloat(field, disc.getSpeed()));
                break;
            case 102:
                disc.setDiscRadius(parseFloat(field, disc.getDiscRadius()));
                break;
            case 103:
                disc.setDiscThickness(parseFloat(field, disc.getDiscThickness()));
                break;
            case 105:
                disc.setHomingStrength(parseFloat(field, disc.getHomingStrength()));
                break;
            case 107:
                disc.setBoomerangDelay(field.getInteger());
                break;
            case 109:
                disc.setExplosionRadius(parseFloat(field, disc.getExplosionRadius()));
                break;
            case 110:
                disc.setKnockback(parseFloat(field, disc.getKnockback()));
                break;
            case 111:
                disc.setMaxDistance(parseFloat(field, disc.getMaxDistance()));
                break;
        }
    }

    @Override
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 203:
                disc.setOuterColorWidth(parseFloat(field, disc.getOuterColorWidth()));
                break;
            case 204:
                disc.setRotationSpeed(parseFloat(field, disc.getRotationSpeed()));
                break;
        }
    }
}
