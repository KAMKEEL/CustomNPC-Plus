package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Guard ability type-specific settings.
 */
public class SubGuiAbilityGuard extends SubGuiAbilityConfig {

    private final AbilityGuard guard;

    public SubGuiAbilityGuard(AbilityGuard ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.guard = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 105;
        int col2LabelX = guiLeft + 165;
        int col2FieldX = guiLeft + 225;

        // Row 1: Damage Reduction
        addLabel(new GuiNpcLabel(100, "ability.dmgReduction", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, guard.getDamageReduction()));

        y += 24;

        // Row 2: Can Counter + Counter Damage
        addLabel(new GuiNpcLabel(101, "ability.canCounter", labelX, y + 5));
        addButton(new GuiNpcButton(101, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, guard.isCanCounter() ? 1 : 0));

        addLabel(new GuiNpcLabel(102, "ability.counterDmg", col2LabelX, y + 5));
        GuiNpcTextField counterDmgField = createFloatField(102, col2FieldX, y, 40, guard.getCounterDamage());
        counterDmgField.setEnabled(guard.isCanCounter());
        addTextField(counterDmgField);

        y += 24;

        // Row 3: Counter Chance
        addLabel(new GuiNpcLabel(103, "ability.counterChance", labelX, y + 5));
        GuiNpcTextField counterChanceField = createFloatField(103, fieldX, y, 50, guard.getCounterChance());
        counterChanceField.setEnabled(guard.isCanCounter());
        addTextField(counterChanceField);
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        if (id == 101) {
            guard.setCanCounter(button.getValue() == 1);
            initGui();
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: guard.setDamageReduction(parseFloat(field, guard.getDamageReduction())); break;
            case 102: guard.setCounterDamage(parseFloat(field, guard.getCounterDamage())); break;
            case 103: guard.setCounterChance(parseFloat(field, guard.getCounterChance())); break;
        }
    }
}
