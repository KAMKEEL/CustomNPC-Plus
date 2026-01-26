package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityOrb;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * GUI for configuring Orb ability type-specific settings.
 */
public class SubGuiAbilityOrb extends SubGuiAbilityConfig {

    private final AbilityOrb orb;
    private int editingVisualColorId = 0;

    public SubGuiAbilityOrb(AbilityOrb ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.orb = ability;
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
        addTextField(createFloatField(100, fieldX, y, 50, orb.getDamage()));

        addLabel(new GuiNpcLabel(101, "stats.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, orb.getOrbSpeed()));

        y += 24;

        // Row 2: Orb Size + Max Distance
        addLabel(new GuiNpcLabel(102, "stats.size", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, orb.getOrbSize()));

        addLabel(new GuiNpcLabel(103, "ability.maxDist", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, orb.getMaxDistance()));

        y += 24;

        // Row 3: Homing + Homing Strength
        addLabel(new GuiNpcLabel(104, "ability.homing", labelX, y + 5));
        addButton(new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isHoming() ? 1 : 0));

        addLabel(new GuiNpcLabel(105, "ability.homingStr", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, orb.getHomingStrength()));

        y += 24;

        // Row 4: Explosive + Explosion Radius
        addLabel(new GuiNpcLabel(106, "ability.explosive", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isExplosive() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.explosionRad", col2LabelX, y + 5));
        addTextField(createFloatField(107, col2FieldX, y, 50, orb.getExplosionRadius()));

        y += 24;

        // Row 5: Knockback + Max Lifetime
        addLabel(new GuiNpcLabel(108, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(108, fieldX, y, 50, orb.getKnockback()));

        addLabel(new GuiNpcLabel(109, "ability.lifetime", col2LabelX, y + 5));
        addTextField(createIntField(109, col2FieldX, y, 50, orb.getMaxLifetime()));
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
        String innerHex = String.format("%06X", orb.getInnerColor() & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(200, fieldX, y, 55, 20, innerHex);
        innerColorBtn.setTextColor(orb.getInnerColor() & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(201, "ability.outerColor", col2LabelX, y + 5));
        String outerHex = String.format("%06X", orb.getOuterColor() & 0xFFFFFF);
        GuiNpcButton outerColorBtn = new GuiNpcButton(201, col2FieldX, y, 55, 20, outerHex);
        outerColorBtn.setTextColor(orb.getOuterColor() & 0xFFFFFF);
        outerColorBtn.setEnabled(orb.isOuterColorEnabled());
        addButton(outerColorBtn);

        y += 24;

        // Row 2: Outer Color Enabled + Outer Color Width
        addLabel(new GuiNpcLabel(202, "ability.outerEnabled", labelX, y + 5));
        addButton(new GuiNpcButton(202, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isOuterColorEnabled() ? 1 : 0));

        addLabel(new GuiNpcLabel(203, "ability.outerWidth", col2LabelX, y + 5));
        GuiNpcTextField widthField = createFloatField(203, col2FieldX, y, 55, orb.getOuterColorWidth());
        widthField.setEnabled(orb.isOuterColorEnabled());
        addTextField(widthField);

        y += 24;

        // Row 3: Rotation Speed
        addLabel(new GuiNpcLabel(204, "ability.rotationSpeed", labelX, y + 5));
        addTextField(createFloatField(204, fieldX, y, 50, orb.getRotationSpeed()));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 104:
                orb.setHoming(value == 1);
                break;
            case 106:
                orb.setExplosive(value == 1);
                break;
        }
    }

    @Override
    protected void handleVisualButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 200:
                editingVisualColorId = 200;
                setSubGui(new SubGuiColorSelector(orb.getInnerColor()));
                break;
            case 201:
                editingVisualColorId = 201;
                setSubGui(new SubGuiColorSelector(orb.getOuterColor()));
                break;
            case 202:
                orb.setOuterColorEnabled(value == 1);
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
                orb.setInnerColor(rgb);
            } else if (editingVisualColorId == 201) {
                orb.setOuterColor(rgb);
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
                orb.setDamage(parseFloat(field, orb.getDamage()));
                break;
            case 101:
                orb.setOrbSpeed(parseFloat(field, orb.getOrbSpeed()));
                break;
            case 102:
                orb.setOrbSize(parseFloat(field, orb.getOrbSize()));
                break;
            case 103:
                orb.setMaxDistance(parseFloat(field, orb.getMaxDistance()));
                break;
            case 105:
                orb.setHomingStrength(parseFloat(field, orb.getHomingStrength()));
                break;
            case 107:
                orb.setExplosionRadius(parseFloat(field, orb.getExplosionRadius()));
                break;
            case 108:
                orb.setKnockback(parseFloat(field, orb.getKnockback()));
                break;
            case 109:
                orb.setMaxLifetime(field.getInteger());
                break;
        }
    }

    @Override
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 203:
                orb.setOuterColorWidth(parseFloat(field, orb.getOuterColorWidth()));
                break;
            case 204:
                orb.setRotationSpeed(parseFloat(field, orb.getRotationSpeed()));
                break;
        }
    }
}
