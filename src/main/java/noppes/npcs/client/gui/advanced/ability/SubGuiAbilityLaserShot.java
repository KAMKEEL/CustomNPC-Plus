package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityLaserShot;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

/**
 * GUI for configuring Laser Shot ability type-specific settings.
 */
public class SubGuiAbilityLaserShot extends SubGuiAbilityConfig {

    private final AbilityLaserShot laser;
    private int editingVisualColorId = 0;

    public SubGuiAbilityLaserShot(AbilityLaserShot ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.laser = ability;
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

        // Row 1: Damage + Laser Width
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, laser.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.laserWidth", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, laser.getLaserWidth()));

        y += 24;

        // Row 2: Expansion Speed + Linger Ticks
        addLabel(new GuiNpcLabel(102, "ability.expansionSpeed", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, laser.getExpansionSpeed()));

        addLabel(new GuiNpcLabel(103, "ability.lingerTicks", col2LabelX, y + 5));
        addTextField(createIntField(103, col2FieldX, y, 50, laser.getLingerTicks()));

        y += 24;

        // Row 3: Max Distance + Max Lifetime
        addLabel(new GuiNpcLabel(104, "ability.maxDist", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, laser.getMaxDistance()));

        addLabel(new GuiNpcLabel(105, "ability.lifetime", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, laser.getMaxLifetime()));

        y += 24;

        // Row 4: Explosive + Explosion Radius (only show radius if explosive)
        addLabel(new GuiNpcLabel(106, "ability.explosive", labelX, y + 5));
        GuiNpcButton explosiveBtn = new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, laser.isExplosive() ? 1 : 0);
        explosiveBtn.setHoverText("ability.hover.explosive");
        addButton(explosiveBtn);

        if (laser.isExplosive()) {
            addLabel(new GuiNpcLabel(107, "ability.explosionRad", col2LabelX, y + 5));
            addTextField(createFloatField(107, col2FieldX, y, 50, laser.getExplosionRadius()));
        }

        y += 24;

        // Row 5: Knockback + Knockback Up
        addLabel(new GuiNpcLabel(108, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(108, fieldX, y, 50, laser.getKnockback()));

        addLabel(new GuiNpcLabel(109, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(109, col2FieldX, y, 50, laser.getKnockbackUp()));

        y += 24;

        // Row 6: Effects button
        addButton(new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects"));
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
        String innerHex = String.format("%06X", laser.getInnerColor() & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(200, fieldX, y, 55, 20, innerHex);
        innerColorBtn.setTextColor(laser.getInnerColor() & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(201, "ability.outerColor", col2LabelX, y + 5));
        String outerHex = String.format("%06X", laser.getOuterColor() & 0xFFFFFF);
        GuiNpcButton outerColorBtn = new GuiNpcButton(201, col2FieldX, y, 55, 20, outerHex);
        outerColorBtn.setTextColor(laser.getOuterColor() & 0xFFFFFF);
        outerColorBtn.setEnabled(laser.isOuterColorEnabled());
        addButton(outerColorBtn);

        y += 24;

        // Row 2: Outer Color Enabled + Outer Color Width
        addLabel(new GuiNpcLabel(202, "ability.outerEnabled", labelX, y + 5));
        addButton(new GuiNpcButton(202, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, laser.isOuterColorEnabled() ? 1 : 0));

        addLabel(new GuiNpcLabel(203, "ability.outerWidth", col2LabelX, y + 5));
        GuiNpcTextField widthField = createFloatField(203, col2FieldX, y, 55, laser.getOuterColorWidth());
        widthField.setEnabled(laser.isOuterColorEnabled());
        addTextField(widthField);

        y += 24;

        // Row 3: Lightning Effect Enabled
        addLabel(new GuiNpcLabel(204, "ability.lightning", labelX, y + 5));
        GuiNpcButton lightningBtn = new GuiNpcButton(204, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, laser.hasLightningEffect() ? 1 : 0);
        lightningBtn.setHoverText("ability.hover.lightning");
        addButton(lightningBtn);

        // Only show Lightning settings if Lightning is enabled
        if (laser.hasLightningEffect()) {
            y += 24;

            // Row 4: Density + Radius
            addLabel(new GuiNpcLabel(205, "ability.lightningDensity", labelX, y + 5));
            GuiNpcTextField densityField = new GuiNpcTextField(205, this, fontRendererObj, fieldX, y, 55, 18, String.valueOf(laser.getLightningDensity()));
            densityField.setMinMaxDefaultFloat(0.01f, 5.0f, 0.15f);
            addTextField(densityField);

            addLabel(new GuiNpcLabel(206, "ability.lightningRadius", col2LabelX, y + 5));
            GuiNpcTextField radiusField = new GuiNpcTextField(206, this, fontRendererObj, col2FieldX, y, 55, 18, String.valueOf(laser.getLightningRadius()));
            radiusField.setMinMaxDefaultFloat(0.1f, 10.0f, 0.5f);
            addTextField(radiusField);
        }
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 106:
                laser.setExplosive(value == 1);
                initGui();
                break;
            case 150:
                setSubGui(new SubGuiAbilityEffects(laser.getEffects()));
                break;
        }
    }

    @Override
    protected void handleVisualButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 200:
                editingVisualColorId = 200;
                setSubGui(new SubGuiColorSelector(laser.getInnerColor()));
                break;
            case 201:
                editingVisualColorId = 201;
                setSubGui(new SubGuiColorSelector(laser.getOuterColor()));
                break;
            case 202:
                laser.setOuterColorEnabled(value == 1);
                initGui();
                break;
            case 204:
                laser.setLightningEffect(value == 1);
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
                laser.setInnerColor(rgb);
            } else if (editingVisualColorId == 201) {
                laser.setOuterColor(rgb);
            }
            editingVisualColorId = 0;
            initGui();
        } else if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                laser.setEffects(result);
            }
        } else {
            super.subGuiClosed(subgui);
        }
    }

    @Override
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 203:
                laser.setOuterColorWidth(parseFloat(field, laser.getOuterColorWidth()));
                break;
            case 205:
                laser.setLightningDensity(parseFloat(field, laser.getLightningDensity()));
                break;
            case 206:
                laser.setLightningRadius(parseFloat(field, laser.getLightningRadius()));
                break;
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100:
                laser.setDamage(parseFloat(field, laser.getDamage()));
                break;
            case 101:
                laser.setLaserWidth(parseFloat(field, laser.getLaserWidth()));
                break;
            case 102:
                laser.setExpansionSpeed(parseFloat(field, laser.getExpansionSpeed()));
                break;
            case 103:
                laser.setLingerTicks(field.getInteger());
                break;
            case 104:
                laser.setMaxDistance(parseFloat(field, laser.getMaxDistance()));
                break;
            case 105:
                laser.setMaxLifetime(field.getInteger());
                break;
            case 107:
                laser.setExplosionRadius(parseFloat(field, laser.getExplosionRadius()));
                break;
            case 108:
                laser.setKnockback(parseFloat(field, laser.getKnockback()));
                break;
            case 109:
                laser.setKnockbackUp(parseFloat(field, laser.getKnockbackUp()));
                break;
        }
    }
}
