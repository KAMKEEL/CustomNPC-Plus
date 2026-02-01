package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.type.AbilityDualOrb;
import kamkeel.npcs.controllers.data.ability.type.AbilityOrb;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.*;

import java.util.List;

public class SubGuiAbilityDualOrb extends SubGuiAbilityConfig {
    private final AbilityDualOrb orb;
    private int editingVisualColorId = 0;
    private int editingOrb = 0;

    public SubGuiAbilityDualOrb(AbilityDualOrb ability, IAbilityConfigCallback callback) {
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

        // Row 3: Homing + Homing Strength (only show strength if homing)
        addLabel(new GuiNpcLabel(104, "ability.homing", labelX, y + 5));
        GuiNpcButton homingBtn = new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isHoming() ? 1 : 0);
        homingBtn.setHoverText("ability.hover.homing");
        addButton(homingBtn);

        if (orb.isHoming()) {
            addLabel(new GuiNpcLabel(105, "ability.homingStr", col2LabelX, y + 5));
            addTextField(createFloatField(105, col2FieldX, y, 50, orb.getHomingStrength()));
        }

        y += 24;

        // Row 4: Explosive + Explosion Radius (only show radius if explosive)
        addLabel(new GuiNpcLabel(106, "ability.explosive", labelX, y + 5));
        GuiNpcButton explosiveBtn = new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isExplosive() ? 1 : 0);
        explosiveBtn.setHoverText("ability.hover.explosive");
        addButton(explosiveBtn);

        if (orb.isExplosive()) {
            addLabel(new GuiNpcLabel(107, "ability.explosionRad", col2LabelX, y + 5));
            addTextField(createFloatField(107, col2FieldX, y, 50, orb.getExplosionRadius()));
        }

        y += 24;

        // Row 5: Knockback + Max Lifetime
        addLabel(new GuiNpcLabel(108, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(108, fieldX, y, 50, orb.getKnockback()));

        addLabel(new GuiNpcLabel(109, "ability.lifetime", col2LabelX, y + 5));
        addTextField(createIntField(109, col2FieldX, y, 50, orb.getMaxLifetime()));

        y += 24;

        addLabel(new GuiNpcLabel(110, "ability.dualFire", labelX, y + 5));
        addButton(new GuiNpcButtonYesNo(110, fieldX, y, orb.isDualFire()));

        if (orb.isDualFire()) {
            addLabel(new GuiNpcLabel(111, "ability.dualFireDelay", col2LabelX, y + 5));
            addTextField(createIntField(111, col2FieldX, y, 50, orb.getDualFireDelay()));
        }

        y += 24;

        // Row 7: Effects button
        addButton(new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void initVisualTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 180;
        int col2FieldX = guiLeft + 260;

        // Row 1: Anchor Point
        addLabel(new GuiNpcLabel(210, "ability.anchorPoint", labelX, y + 5));
        GuiNpcButton anchorBtn = new GuiNpcButton(210, fieldX, y, 80, 20, AnchorPoint.getDisplayNames(), orb.getAnchorPointEnum(editingOrb).ordinal());
        anchorBtn.setHoverText("ability.hover.anchorPoint");
        addButton(anchorBtn);

        y += 24;

        // Row 2: Inner Color + Rotation Speed
        addLabel(new GuiNpcLabel(200, "ability.innerColor", labelX, y + 5));
        String innerHex = String.format("%06X", orb.getInnerColor(editingOrb) & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(200, fieldX, y, 55, 20, innerHex);
        innerColorBtn.setTextColor(orb.getInnerColor(editingOrb) & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(205, "ability.rotationSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(205, col2FieldX, y, 55, orb.getRotationSpeed(editingOrb)));

        y += 24;

        // Row 3: Outer Glow Enabled
        addLabel(new GuiNpcLabel(202, "ability.outerEnabled", labelX, y + 5));
        addButton(new GuiNpcButton(202, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isOuterColorEnabled(editingOrb) ? 1 : 0));

        // Only show Outer Color and Width if Outer Glow is enabled
        if (orb.isOuterColorEnabled()) {
            addLabel(new GuiNpcLabel(204, "ability.outerAlpha", col2LabelX, y + 5));
            GuiNpcTextField alphaField = createFloatField(204, col2FieldX, y, 55, orb.getOuterColorAlpha(editingOrb));
            alphaField.setEnabled(orb.isOuterColorEnabled(editingOrb));
            alphaField.setMinMaxDefaultFloat(0, 1, 0.5f);
            addTextField(alphaField);

            y += 24;

            // Row 4: Outer Color + Outer Width
            addLabel(new GuiNpcLabel(201, "ability.outerColor", labelX, y + 5));
            String outerHex = String.format("%06X", orb.getOuterColor(editingOrb) & 0xFFFFFF);
            GuiNpcButton outerColorBtn = new GuiNpcButton(201, fieldX, y, 55, 20, outerHex);
            outerColorBtn.setTextColor(orb.getOuterColor(editingOrb) & 0xFFFFFF);
            addButton(outerColorBtn);

            addLabel(new GuiNpcLabel(203, "ability.outerWidth", col2LabelX, y + 5));
            addTextField(createFloatField(203, col2FieldX, y, 55, orb.getOuterColorWidth(editingOrb)));
        }

        y += 24;

        // Row 5: Lightning Effect Enabled
        addLabel(new GuiNpcLabel(206, "ability.lightning", labelX, y + 5));
        GuiNpcButton lightningBtn = new GuiNpcButton(206, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.hasLightningEffect(editingOrb) ? 1 : 0);
        lightningBtn.setHoverText("ability.hover.lightning");
        addButton(lightningBtn);

        // Only show Lightning settings if Lightning is enabled
        if (orb.hasLightningEffect(editingOrb)) {
            y += 24;

            // Row 61: Density + Radius
            addLabel(new GuiNpcLabel(207, "ability.lightningDensity", labelX, y + 5));
            GuiNpcTextField densityField = new GuiNpcTextField(207, this, fontRendererObj, fieldX, y, 55, 18, "" + orb.getLightningDensity(editingOrb));
            densityField.setMinMaxDefaultFloat(0.01f, 5.0f, 0.15f);
            addTextField(densityField);

            addLabel(new GuiNpcLabel(208, "ability.lightningRadius", col2LabelX, y + 5));
            GuiNpcTextField radiusField = new GuiNpcTextField(208, this, fontRendererObj, col2FieldX, y, 55, 18, "" + orb.getLightningRadius(editingOrb));
            radiusField.setMinMaxDefaultFloat(0.1f, 10.0f, 0.5f);
            addTextField(radiusField);
        }

        y += 24;

        addButton(new GuiNpcButton(400, col2FieldX, y, 55, 20, new String[]{"Orb 1", "Orb 2"}, editingOrb));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 104:
                orb.setHoming(value == 1);
                initGui();
                break;
            case 106:
                orb.setExplosive(value == 1);
                initGui();
                break;
            case 110:
                orb.setDualFire(value == 1);
                initGui();
                break;
            case 150:
                setSubGui(new SubGuiAbilityEffects(orb.getEffects()));
                break;
        }
    }

    @Override
    protected void handleVisualButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 200:
                editingVisualColorId = 200;
                setSubGui(new SubGuiColorSelector(orb.getInnerColor(editingOrb)));
                break;
            case 201:
                editingVisualColorId = 201;
                setSubGui(new SubGuiColorSelector(orb.getOuterColor(editingOrb)));
                break;
            case 202:
                orb.setOuterColorEnabled(editingOrb, value == 1);
                initGui();
                break;
            case 206:
                orb.setLightningEffect(editingOrb, value == 1);
                initGui();
                break;
            case 210:
                orb.setAnchorPointEnum(editingOrb, AnchorPoint.fromOrdinal(value));
                break;
            case 400:
                editingOrb = (editingOrb + 1) % 2;
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
        } else if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                orb.setEffects(result);
            }
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
            case 111:
                orb.setDualFireDelay(field.getInteger());
                break;
        }
    }

    @Override
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 203:
                orb.setOuterColorWidth(editingOrb, parseFloat(field, orb.getOuterColorWidth(editingOrb)));
                break;
            case 204:
                orb.setOuterColorAlpha(editingOrb, parseFloat(field, orb.getOuterColorAlpha(editingOrb)));
                break;
            case 205:
                orb.setRotationSpeed(editingOrb, parseFloat(field, orb.getRotationSpeed(editingOrb)));
                break;
            case 207:
                orb.setLightningDensity(editingOrb, parseFloat(field, orb.getLightningDensity(editingOrb)));
                break;
            case 208:
                orb.setLightningRadius(editingOrb, parseFloat(field, orb.getLightningRadius(editingOrb)));
                break;
        }
    }
}
