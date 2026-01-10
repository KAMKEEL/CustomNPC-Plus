package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilitySlam;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Slam ability type-specific settings.
 */
public class SubGuiAbilitySlam extends SubGuiAbilityConfig {

    private final AbilitySlam slam;

    public SubGuiAbilitySlam(AbilitySlam ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.slam = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Radius
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, slam.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.radius", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, slam.getRadius()));

        y += 24;

        // Row 2: Knockback + Leap Speed
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, slam.getKnockbackStrength()));

        addLabel(new GuiNpcLabel(103, "ability.leapSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, slam.getLeapSpeed()));

        y += 24;

        // Row 3: Min/Max Leap Distance
        addLabel(new GuiNpcLabel(104, "ability.minDist", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, slam.getMinLeapDistance()));

        addLabel(new GuiNpcLabel(105, "ability.maxDist", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, slam.getMaxLeapDistance()));
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: slam.setDamage(parseFloat(field, slam.getDamage())); break;
            case 101: slam.setRadius(parseFloat(field, slam.getRadius())); break;
            case 102: slam.setKnockbackStrength(parseFloat(field, slam.getKnockbackStrength())); break;
            case 103: slam.setLeapSpeed(parseFloat(field, slam.getLeapSpeed())); break;
            case 104: slam.setMinLeapDistance(parseFloat(field, slam.getMinLeapDistance())); break;
            case 105: slam.setMaxLeapDistance(parseFloat(field, slam.getMaxLeapDistance())); break;
        }
    }
}
