package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

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

        // Row 1: Duration + Damage Reduction
        addLabel(new GuiNpcLabel(99, "ability.duration", labelX, y + 5));
        GuiNpcTextField durationField = createIntField(99, fieldX, y, 50, guard.getDurationTicks());
        durationField.setMinMaxDefault(1, 1000, 60);
        addTextField(durationField);

        addLabel(new GuiNpcLabel(100, "ability.dmgReduce", col2LabelX, y + 5));
        addTextField(createFloatField(100, col2FieldX, y, 50, guard.getDamageReduction()));

        y += 24;

        // Row 2: Can Counter + Counter Type
        addLabel(new GuiNpcLabel(101, "ability.canCounter", labelX, y + 5));
        GuiNpcButton canCounterBtn = new GuiNpcButton(101, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, guard.isCanCounter() ? 1 : 0);
        canCounterBtn.setHoverText("ability.hover.canCounter");
        addButton(canCounterBtn);

        addLabel(new GuiNpcLabel(102, "ability.counterType", col2LabelX, y + 5));
        String[] types = {"Flat", "Percent"};
        GuiNpcButton counterTypeButton = new GuiNpcButton(102, col2FieldX, y, 60, 20, types, guard.getCounterType().ordinal());
        counterTypeButton.setHoverText("ability.hover.counterType");
        counterTypeButton.setEnabled(guard.isCanCounter());
        addButton(counterTypeButton);

        y += 24;

        // Row 3: Counter Value + Counter Chance
        addLabel(new GuiNpcLabel(103, "ability.counterValue", labelX, y + 5));
        GuiNpcTextField counterValueField = createFloatField(103, fieldX, y, 50, guard.getCounterValue());
        counterValueField.setEnabled(guard.isCanCounter());
        addTextField(counterValueField);

        addLabel(new GuiNpcLabel(104, "ability.counterChance", col2LabelX, y + 5));
        GuiNpcTextField counterChanceField = createFloatField(104, col2FieldX, y, 50, guard.getCounterChance());
        counterChanceField.setEnabled(guard.isCanCounter());
        addTextField(counterChanceField);

        y += 24;

        // Row 4: Counter Sound + Counter Animation
        addLabel(new GuiNpcLabel(105, "ability.counterSound", labelX, y + 5));
        GuiNpcTextField counterSoundField = new GuiNpcTextField(105, this, fontRendererObj, fieldX, y, 100, 20, guard.getCounterSound());
        counterSoundField.setEnabled(guard.isCanCounter());
        addTextField(counterSoundField);

        addLabel(new GuiNpcLabel(106, "ability.counterAnim", col2LabelX, y + 5));
        GuiNpcTextField counterAnimField = createIntField(106, col2FieldX, y, 50, guard.getCounterAnimationId());
        counterAnimField.setEnabled(guard.isCanCounter());
        addTextField(counterAnimField);

        y += 24;

        // Row 5: Effects button (applies on counter)
        GuiNpcButton effectsBtn = new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects");
        effectsBtn.setEnabled(guard.isCanCounter());
        addButton(effectsBtn);
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        switch (id) {
            case 101:
                guard.setCanCounter(button.getValue() == 1);
                initGui();
                break;
            case 102:
                guard.setCounterType(AbilityGuard.CounterType.values()[button.getValue()]);
                break;
            case 150:
                setSubGui(new SubGuiAbilityEffects(guard.getEffects()));
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
                guard.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 99:
                guard.setDurationTicks(field.getInteger());
                break;
            case 100:
                guard.setDamageReduction(parseFloat(field, guard.getDamageReduction()));
                break;
            case 103:
                guard.setCounterValue(parseFloat(field, guard.getCounterValue()));
                break;
            case 104:
                guard.setCounterChance(parseFloat(field, guard.getCounterChance()));
                break;
            case 105:
                guard.setCounterSound(field.getText());
                break;
            case 106:
                guard.setCounterAnimationId(field.getInteger());
                break;
        }
    }
}
