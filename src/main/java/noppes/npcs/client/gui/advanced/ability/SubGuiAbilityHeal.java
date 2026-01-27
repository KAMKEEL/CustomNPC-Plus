package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityHeal;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;

/**
 * GUI for configuring Heal ability type-specific settings.
 */
public class SubGuiAbilityHeal extends SubGuiAbilityConfig {

    private final AbilityHeal heal;

    public SubGuiAbilityHeal(AbilityHeal ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.heal = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Instant Heal + Duration (only shown if not instant)
        addLabel(new GuiNpcLabel(103, "dialog.instant", labelX, y + 5));
        GuiNpcButton instantBtn = new GuiNpcButton(103, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, heal.isInstantHeal() ? 1 : 0);
        instantBtn.setHoverText("ability.hover.instant");
        addButton(instantBtn);

        if (!heal.isInstantHeal()) {
            addLabel(new GuiNpcLabel(99, "ability.duration", col2LabelX, y + 5));
            GuiNpcTextField durationField = createIntField(99, col2FieldX, y, 50, heal.getDurationTicks());
            durationField.setMinMaxDefault(1, 1000, 60);
            addTextField(durationField);
        }

        y += 24;

        // Row 2: Heal Amount + Heal Percent
        addLabel(new GuiNpcLabel(100, "ability.healAmount", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, heal.getHealAmount()));

        addLabel(new GuiNpcLabel(101, "ability.healPercent", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, heal.getHealPercent()));

        y += 24;

        // Row 3: Heal Radius
        addLabel(new GuiNpcLabel(102, "ability.healRadius", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, heal.getHealRadius()));

        y += 24;

        // Row 4: Heal Self + Heal Allies
        addLabel(new GuiNpcLabel(104, "ability.healSelf", labelX, y + 5));
        GuiNpcButton healSelfBtn = new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, heal.isHealSelf() ? 1 : 0);
        healSelfBtn.setHoverText("ability.hover.healSelf");
        addButton(healSelfBtn);

        addLabel(new GuiNpcLabel(105, "ability.healAllies", col2LabelX, y + 5));
        GuiNpcButton healAlliesBtn = new GuiNpcButton(105, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, heal.isHealAllies() ? 1 : 0);
        healAlliesBtn.setHoverText("ability.hover.healAllies");
        addButton(healAlliesBtn);
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 103:
                heal.setInstantHeal(value == 1);
                initGui(); // Refresh to show/hide duration field
                break;
            case 104:
                heal.setHealSelf(value == 1);
                break;
            case 105:
                heal.setHealAllies(value == 1);
                break;
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 99:
                heal.setDurationTicks(field.getInteger());
                break;
            case 100:
                heal.setHealAmount(parseFloat(field, heal.getHealAmount()));
                break;
            case 101:
                heal.setHealPercent(parseFloat(field, heal.getHealPercent()));
                break;
            case 102:
                heal.setHealRadius(parseFloat(field, heal.getHealRadius()));
                break;
        }
    }
}
