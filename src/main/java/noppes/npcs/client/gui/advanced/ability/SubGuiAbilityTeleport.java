package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityTeleport;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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

        // Row 1: Mode
        addLabel(new GuiNpcLabel(100, "ability.mode", labelX, y + 5));
        String[] modes = {"Blink", "Behind", "Single"};
        addButton(new GuiNpcButton(100, fieldX, y, 80, 20, modes, teleport.getMode().ordinal()));

        y += 24;

        switch (teleport.getMode()) {
            case BLINK:
                // Row 2: Blink Radius + Blink Count
                addLabel(new GuiNpcLabel(101, "ability.blinkRadius", labelX, y + 5));
                addTextField(createFloatField(101, fieldX, y, 50, teleport.getBlinkRadius()));

                addLabel(new GuiNpcLabel(102, "ability.blinkCount", col2LabelX, y + 5));
                addTextField(createIntField(102, col2FieldX, y, 50, teleport.getBlinkCount()));

                y += 24;

                // Row 3: Blink Delay + Line of Sight
                addLabel(new GuiNpcLabel(103, "ability.blinkDelay", labelX, y + 5));
                addTextField(createIntField(103, fieldX, y, 50, teleport.getBlinkDelayTicks()));

                addLabel(new GuiNpcLabel(105, "ability.lineOfSight", col2LabelX, y + 5));
                GuiNpcButton losBtn1 = new GuiNpcButton(105, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, teleport.isRequireLineOfSight() ? 1 : 0);
                losBtn1.setHoverText("ability.hover.lineOfSight");
                addButton(losBtn1);
                y += 24;
                break;
            case SINGLE:
                // Row 2: Blink Radius + Line of Sight
                addLabel(new GuiNpcLabel(101, "ability.blinkRadius", labelX, y + 5));
                addTextField(createFloatField(101, fieldX, y, 50, teleport.getBlinkRadius()));

                addLabel(new GuiNpcLabel(105, "ability.lineOfSight", col2LabelX, y + 5));
                GuiNpcButton losBtn2 = new GuiNpcButton(105, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, teleport.isRequireLineOfSight() ? 1 : 0);
                losBtn2.setHoverText("ability.hover.lineOfSight");
                addButton(losBtn2);
                y += 24;
                break;
            case BEHIND:
                // Row 2: Behind Distance
                addLabel(new GuiNpcLabel(104, "ability.behindDistance", labelX, y + 5));
                addTextField(createFloatField(104, fieldX, y, 50, teleport.getBehindDistance()));
                y += 24;
                break;
        }

        // Damage + Damage Radius
        addLabel(new GuiNpcLabel(106, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(106, fieldX, y, 50, teleport.getDamage()));

        addLabel(new GuiNpcLabel(107, "ability.dmgRadius", col2LabelX, y + 5));
        addTextField(createFloatField(107, col2FieldX, y, 50, teleport.getDamageRadius()));

        y += 24;

        // Damage at Start/End
        addLabel(new GuiNpcLabel(108, "ability.dmgAtStart", labelX, y + 5));
        GuiNpcButton dmgStartBtn = new GuiNpcButton(108, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, teleport.isDamageAtStart() ? 1 : 0);
        dmgStartBtn.setHoverText("ability.hover.dmgAtStart");
        addButton(dmgStartBtn);

        addLabel(new GuiNpcLabel(109, "ability.dmgAtEnd", col2LabelX, y + 5));
        GuiNpcButton dmgEndBtn = new GuiNpcButton(109, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, teleport.isDamageAtEnd() ? 1 : 0);
        dmgEndBtn.setHoverText("ability.hover.dmgAtEnd");
        addButton(dmgEndBtn);

        y += 24;

        // Effects button
        addButton(new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 100:
                teleport.setMode(AbilityTeleport.TeleportMode.values()[value]);
                initGui();
                break;
            case 105:
                teleport.setRequireLineOfSight(value == 1);
                break;
            case 108:
                teleport.setDamageAtStart(value == 1);
                break;
            case 109:
                teleport.setDamageAtEnd(value == 1);
                break;
            case 150:
                setSubGui(new SubGuiAbilityEffects(teleport.getEffects()));
                break;
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                teleport.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 101:
                teleport.setBlinkRadius(parseFloat(field, teleport.getBlinkRadius()));
                break;
            case 102:
                teleport.setBlinkCount(field.getInteger());
                break;
            case 103:
                teleport.setBlinkDelayTicks(field.getInteger());
                break;
            case 104:
                teleport.setBehindDistance(parseFloat(field, teleport.getBehindDistance()));
                break;
            case 106:
                teleport.setDamage(parseFloat(field, teleport.getDamage()));
                break;
            case 107:
                teleport.setDamageRadius(parseFloat(field, teleport.getDamageRadius()));
                break;
        }
    }
}
