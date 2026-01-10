package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityTeleport;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Teleport ability type-specific settings.
 */
public class SubGuiAbilityTeleport extends SubGuiAbilityConfig {

    private final AbilityTeleport teleport;

    public SubGuiAbilityTeleport(AbilityTeleport ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.teleport = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Pattern
        addLabel(new GuiNpcLabel(100, "ability.pattern", labelX, y + 5));
        String[] patterns = {"Random", "Toward", "Away", "Behind", "InFront", "Around", "ToTarget"};
        addButton(new GuiNpcButton(100, fieldX, y, 80, 20, patterns, teleport.getPattern().ordinal()));

        y += 24;

        // Row 2: Blink Radius + Min Blink Radius
        addLabel(new GuiNpcLabel(101, "ability.blinkRadius", labelX, y + 5));
        addTextField(createFloatField(101, fieldX, y, 50, teleport.getBlinkRadius()));

        addLabel(new GuiNpcLabel(102, "ability.minBlinkRad", col2LabelX, y + 5));
        addTextField(createFloatField(102, col2FieldX, y, 50, teleport.getMinBlinkRadius()));

        y += 24;

        // Row 3: Blink Count + Blink Delay
        addLabel(new GuiNpcLabel(103, "ability.blinkCount", labelX, y + 5));
        addTextField(createIntField(103, fieldX, y, 50, teleport.getBlinkCount()));

        addLabel(new GuiNpcLabel(104, "ability.blinkDelay", col2LabelX, y + 5));
        addTextField(createIntField(104, col2FieldX, y, 50, teleport.getBlinkDelayTicks()));

        y += 24;

        // Row 4: Damage + Damage Radius
        addLabel(new GuiNpcLabel(105, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(105, fieldX, y, 50, teleport.getDamage()));

        addLabel(new GuiNpcLabel(106, "ability.dmgRadius", col2LabelX, y + 5));
        addTextField(createFloatField(106, col2FieldX, y, 50, teleport.getDamageRadius()));

        y += 24;

        // Row 5: Damage at Start/End + Require LOS
        addLabel(new GuiNpcLabel(107, "ability.dmgAtStart", labelX, y + 5));
        addButton(new GuiNpcButton(107, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, teleport.isDamageAtStart() ? 1 : 0));

        addLabel(new GuiNpcLabel(108, "ability.dmgAtEnd", col2LabelX, y + 5));
        addButton(new GuiNpcButton(108, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, teleport.isDamageAtEnd() ? 1 : 0));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 100: teleport.setPattern(AbilityTeleport.TeleportPattern.values()[value]); break;
            case 107: teleport.setDamageAtStart(value == 1); break;
            case 108: teleport.setDamageAtEnd(value == 1); break;
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 101: teleport.setBlinkRadius(parseFloat(field, teleport.getBlinkRadius())); break;
            case 102: teleport.setMinBlinkRadius(parseFloat(field, teleport.getMinBlinkRadius())); break;
            case 103: teleport.setBlinkCount(field.getInteger()); break;
            case 104: teleport.setBlinkDelayTicks(field.getInteger()); break;
            case 105: teleport.setDamage(parseFloat(field, teleport.getDamage())); break;
            case 106: teleport.setDamageRadius(parseFloat(field, teleport.getDamageRadius())); break;
        }
    }
}
