package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityEnergyBeam;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * GUI for configuring Energy Beam ability type-specific settings.
 * Energy Beam is a homing head with trailing path (DBZ-style).
 */
public class SubGuiAbilityEnergyBeam extends SubGuiAbilityConfig {

    private final AbilityEnergyBeam beam;
    private int editingVisualColorId = 0;

    public SubGuiAbilityEnergyBeam(AbilityEnergyBeam ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.beam = ability;
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
        addTextField(createFloatField(100, fieldX, y, 50, beam.getDamage()));

        addLabel(new GuiNpcLabel(101, "stats.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, beam.getSpeed()));

        y += 24;

        // Row 2: Beam Width + Head Size
        addLabel(new GuiNpcLabel(102, "ability.beamWidth", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, beam.getBeamWidth()));

        addLabel(new GuiNpcLabel(103, "ability.headSize", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, beam.getHeadSize()));

        y += 24;

        // Row 3: Homing + Homing Strength
        addLabel(new GuiNpcLabel(104, "ability.homing", labelX, y + 5));
        addButton(new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isHoming() ? 1 : 0));

        addLabel(new GuiNpcLabel(105, "ability.homingStr", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, beam.getHomingStrength()));

        y += 24;

        // Row 4: Homing Range + Max Distance
        addLabel(new GuiNpcLabel(106, "ability.homingRange", labelX, y + 5));
        addTextField(createFloatField(106, fieldX, y, 50, beam.getHomingRange()));

        addLabel(new GuiNpcLabel(107, "ability.maxDist", col2LabelX, y + 5));
        addTextField(createFloatField(107, col2FieldX, y, 50, beam.getMaxDistance()));

        y += 24;

        // Row 5: Explosive + Explosion Radius
        addLabel(new GuiNpcLabel(108, "ability.explosive", labelX, y + 5));
        addButton(new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isExplosive() ? 1 : 0));

        addLabel(new GuiNpcLabel(109, "ability.explosionRad", col2LabelX, y + 5));
        addTextField(createFloatField(109, col2FieldX, y, 50, beam.getExplosionRadius()));

        y += 24;

        // Row 6: Knockback + Max Lifetime
        addLabel(new GuiNpcLabel(110, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(110, fieldX, y, 50, beam.getKnockback()));

        addLabel(new GuiNpcLabel(111, "ability.lifetime", col2LabelX, y + 5));
        addTextField(createIntField(111, col2FieldX, y, 50, beam.getMaxLifetime()));
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
        String innerHex = String.format("%06X", beam.getInnerColor() & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(200, fieldX, y, 55, 20, innerHex);
        innerColorBtn.setTextColor(beam.getInnerColor() & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(201, "ability.outerColor", col2LabelX, y + 5));
        String outerHex = String.format("%06X", beam.getOuterColor() & 0xFFFFFF);
        GuiNpcButton outerColorBtn = new GuiNpcButton(201, col2FieldX, y, 55, 20, outerHex);
        outerColorBtn.setTextColor(beam.getOuterColor() & 0xFFFFFF);
        outerColorBtn.setEnabled(beam.isOuterColorEnabled());
        addButton(outerColorBtn);

        y += 24;

        // Row 2: Outer Color Enabled + Outer Color Width
        addLabel(new GuiNpcLabel(202, "ability.outerEnabled", labelX, y + 5));
        addButton(new GuiNpcButton(202, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isOuterColorEnabled() ? 1 : 0));

        addLabel(new GuiNpcLabel(203, "ability.outerWidth", col2LabelX, y + 5));
        GuiNpcTextField widthField = createFloatField(203, col2FieldX, y, 55, beam.getOuterColorWidth());
        widthField.setEnabled(beam.isOuterColorEnabled());
        addTextField(widthField);

        y += 24;

        // Row 3: Rotation Speed
        addLabel(new GuiNpcLabel(204, "ability.rotationSpeed", labelX, y + 5));
        addTextField(createFloatField(204, fieldX, y, 50, beam.getRotationSpeed()));

        y += 24;

        // Row 4: Lightning Effect Enabled (only affects head)
        addLabel(new GuiNpcLabel(205, "ability.lightning", labelX, y + 5));
        addButton(new GuiNpcButton(205, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.hasLightningEffect() ? 1 : 0));

        // Only show Lightning settings if Lightning is enabled
        if (beam.hasLightningEffect()) {
            y += 24;

            // Row 5: Density + Radius
            addLabel(new GuiNpcLabel(206, "ability.lightningDensity", labelX, y + 5));
            GuiNpcTextField densityField = new GuiNpcTextField(206, this, fontRendererObj, fieldX, y, 55, 18, String.valueOf(beam.getLightningDensity()));
            densityField.setMinMaxDefaultFloat(0.01f, 5.0f, 0.15f);
            addTextField(densityField);

            addLabel(new GuiNpcLabel(207, "ability.lightningRadius", col2LabelX, y + 5));
            GuiNpcTextField radiusField = new GuiNpcTextField(207, this, fontRendererObj, col2FieldX, y, 55, 18, String.valueOf(beam.getLightningRadius()));
            radiusField.setMinMaxDefaultFloat(0.1f, 10.0f, 0.5f);
            addTextField(radiusField);
        }
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 104:
                beam.setHoming(value == 1);
                break;
            case 108:
                beam.setExplosive(value == 1);
                break;
        }
    }

    @Override
    protected void handleVisualButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 200:
                editingVisualColorId = 200;
                setSubGui(new SubGuiColorSelector(beam.getInnerColor()));
                break;
            case 201:
                editingVisualColorId = 201;
                setSubGui(new SubGuiColorSelector(beam.getOuterColor()));
                break;
            case 202:
                beam.setOuterColorEnabled(value == 1);
                initGui();
                break;
            case 205:
                beam.setLightningEffect(value == 1);
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
                beam.setInnerColor(rgb);
            } else if (editingVisualColorId == 201) {
                beam.setOuterColor(rgb);
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
                beam.setDamage(parseFloat(field, beam.getDamage()));
                break;
            case 101:
                beam.setSpeed(parseFloat(field, beam.getSpeed()));
                break;
            case 102:
                beam.setBeamWidth(parseFloat(field, beam.getBeamWidth()));
                break;
            case 103:
                beam.setHeadSize(parseFloat(field, beam.getHeadSize()));
                break;
            case 105:
                beam.setHomingStrength(parseFloat(field, beam.getHomingStrength()));
                break;
            case 106:
                beam.setHomingRange(parseFloat(field, beam.getHomingRange()));
                break;
            case 107:
                beam.setMaxDistance(parseFloat(field, beam.getMaxDistance()));
                break;
            case 109:
                beam.setExplosionRadius(parseFloat(field, beam.getExplosionRadius()));
                break;
            case 110:
                beam.setKnockback(parseFloat(field, beam.getKnockback()));
                break;
            case 111:
                beam.setMaxLifetime(field.getInteger());
                break;
        }
    }

    @Override
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 203:
                beam.setOuterColorWidth(parseFloat(field, beam.getOuterColorWidth()));
                break;
            case 204:
                beam.setRotationSpeed(parseFloat(field, beam.getRotationSpeed()));
                break;
            case 206:
                beam.setLightningDensity(parseFloat(field, beam.getLightningDensity()));
                break;
            case 207:
                beam.setLightningRadius(parseFloat(field, beam.getLightningRadius()));
                break;
        }
    }
}
