package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.util.*;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * SubGui for editing a single AbilityCondition.
 *
 * Type selection stays manual (we need to swap the condition object).
 * Field rendering is fully delegated to AbilityCondition#getAbilityDefinitions(),
 * mirroring the pattern used in SubGuiAbilityConfig.
 */
public class SubGuiConditionEdit extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {

    private static final int DECLARATIVE_ID_START = 1000;
    private static final int CLEAR_ID_START = 2000;
    private static final int LABEL_ID_START = 3000;

    private static final int BTN_TYPE = 1;
    private static final int BTN_CANCEL = 10;
    private static final int BTN_DONE = 11;

    private AbilityCondition condition;
    private AbilityCondition result;

    private final String[] conditionTypeIds;
    private final String[] conditionTypeNames;
    private int selectedTypeIndex = 0;

    private AbilityFieldBuilder builder;

    public SubGuiConditionEdit(AbilityCondition existing) {
        this.result = null;

        conditionTypeIds = AbilityController.Instance.getConditionTypes();
        conditionTypeNames = new String[conditionTypeIds.length];
        for (int i = 0; i < conditionTypeIds.length; i++) {
            Supplier<AbilityCondition> factory = AbilityController.Instance.getConditionType(conditionTypeIds[i]);
            conditionTypeNames[i] = factory != null ? factory.get().getName() : conditionTypeIds[i];
        }

        if (existing != null) {
            for (int i = 0; i < conditionTypeIds.length; i++) {
                if (conditionTypeIds[i].equals(existing.getTypeId())) {
                    selectedTypeIndex = i;
                    break;
                }
            }
            this.condition = existing;
        } else {
            this.condition = spawnCondition(selectedTypeIndex);
        }

        setBackground("menubg.png");
        xSize = 240;
        ySize = 180;
    }

    private AbilityCondition spawnCondition(int typeIndex) {
        if (typeIndex < 0 || typeIndex >= conditionTypeIds.length) return null;
        Supplier<AbilityCondition> factory = AbilityController.Instance.getConditionType(conditionTypeIds[typeIndex]);
        return factory != null ? factory.get() : null;
    }

    @Override
    public void initGui() {
        GuiNpcTextField.unfocus();
        super.initGui();

        int swX = guiLeft + 4;
        int swY = guiTop + 5;
        int swW = xSize - 8;
        int swH = ySize - 35;

        int labelY = guiTop + 8;
        addLabel(new GuiNpcLabel(0, "gui.type", guiLeft + 8, labelY + 5));
        GuiNpcButton typeBtn = new GuiNpcButton(BTN_TYPE, guiLeft + 80, labelY, 155, 20, conditionTypeNames, selectedTypeIndex);
        addButton(typeBtn);

        swY += 26;
        swH -= 26;

        List<FieldDef> fields = new ArrayList<>();
        if (condition != null) {
            fields = condition.getAllDefinitions();
        }

        builder = new AbilityFieldBuilder(this, fontRendererObj);
        builder.startIds(DECLARATIVE_ID_START, CLEAR_ID_START, LABEL_ID_START);
        builder.startY(5);

        builder.buildScrollWindow(fields, swX, swY, swW, swH);

        int btnY = guiTop + ySize - 26;
        addButton(new GuiNpcButton(BTN_CANCEL, guiLeft + 8,          btnY, 60, 20, "gui.cancel"));
        addButton(new GuiNpcButton(BTN_DONE,   guiLeft + xSize - 68, btnY, 60, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == BTN_TYPE) {
            int newIndex = ((GuiNpcButton) guibutton).getValue();
            if (newIndex != selectedTypeIndex) {
                selectedTypeIndex = newIndex;
                condition = spawnCondition(selectedTypeIndex);
            }
            initGui();
            return;
        }

        if (id == BTN_CANCEL) {
            result = null;
            close();
            return;
        }

        if (id == BTN_DONE) {
            result = condition;
            close();
            return;
        }

        if (builder != null && builder.handleButtonEvent(id, guibutton)) {
            if (!hasSubGui()) {
                initGui();
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id < DECLARATIVE_ID_START) return;
        if (builder != null && builder.handleTextFieldEvent(textField.id, textField)) {
            initGui();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (builder != null && builder.handleSubGuiClosed(subgui)) {
            initGui();
        }
    }

    public AbilityCondition getResult() {
        return result;
    }
}
