package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityLaserShot;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * GUI for configuring Laser Shot ability type-specific settings.
 */
public class SubGuiAbilityLaserShot extends SubGuiAbilityConfig {

    private final AbilityLaserShot laser;
    private int editingTypeColorId = 0;

    public SubGuiAbilityLaserShot(AbilityLaserShot ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.laser = ability;
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

        // Row 4: Explosive + Explosion Radius
        addLabel(new GuiNpcLabel(106, "ability.explosive", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, laser.isExplosive() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.explosionRad", col2LabelX, y + 5));
        addTextField(createFloatField(107, col2FieldX, y, 50, laser.getExplosionRadius()));

        y += 24;

        // Row 5: Knockback + Knockback Up
        addLabel(new GuiNpcLabel(108, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(108, fieldX, y, 50, laser.getKnockback()));

        addLabel(new GuiNpcLabel(109, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(109, col2FieldX, y, 50, laser.getKnockbackUp()));

        y += 24;

        // Row 6: Inner Color + Outer Color
        addLabel(new GuiNpcLabel(120, "ability.innerColor", labelX, y + 5));
        String innerHex = String.format("%06X", laser.getInnerColor() & 0xFFFFFF);
        GuiNpcButton innerColorBtn = new GuiNpcButton(120, fieldX, y, 50, 20, innerHex);
        innerColorBtn.setTextColor(laser.getInnerColor() & 0xFFFFFF);
        addButton(innerColorBtn);

        addLabel(new GuiNpcLabel(121, "ability.outerColor", col2LabelX, y + 5));
        String outerHex = String.format("%06X", laser.getOuterColor() & 0xFFFFFF);
        GuiNpcButton outerColorBtn = new GuiNpcButton(121, col2FieldX, y, 50, 20, outerHex);
        outerColorBtn.setTextColor(laser.getOuterColor() & 0xFFFFFF);
        addButton(outerColorBtn);
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 106:
                laser.setExplosive(value == 1);
                break;
            case 120:
                editingTypeColorId = 120;
                setSubGui(new SubGuiColorSelector(laser.getInnerColor()));
                break;
            case 121:
                editingTypeColorId = 121;
                setSubGui(new SubGuiColorSelector(laser.getOuterColor()));
                break;
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector && editingTypeColorId != 0) {
            SubGuiColorSelector colorSelector = (SubGuiColorSelector) subgui;
            int rgb = colorSelector.color & 0x00FFFFFF;
            if (editingTypeColorId == 120) {
                laser.setInnerColor(rgb);
            } else if (editingTypeColorId == 121) {
                laser.setOuterColor(rgb);
            }
            editingTypeColorId = 0;
            initGui();
        } else {
            super.subGuiClosed(subgui);
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
