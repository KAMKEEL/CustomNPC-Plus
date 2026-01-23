package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Condition;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * SubGui for editing a single ability condition.
 * Allows selecting condition type and configuring its parameters.
 */
public class SubGuiConditionEdit extends SubGuiInterface implements ITextfieldListener {

    // Condition types
    private static final String[] CONDITION_TYPES = {
        "condition.hp_above",
        "condition.hp_below",
        "condition.target_hp_above",
        "condition.target_hp_below",
        "condition.hit_count"
    };

    private static final String[] CONDITION_TYPE_IDS = {
        "hp_above",
        "hp_below",
        "target_hp_above",
        "target_hp_below",
        "hit_count"
    };

    // Current condition being edited
    private Condition condition;
    private Condition result;

    // State
    private int selectedTypeIndex = 0;
    private float threshold = 0.5f;  // For HP conditions (0.0-1.0)
    private int requiredHits = 3;    // For hit_count
    private int withinTicks = 60;    // For hit_count

    public SubGuiConditionEdit(Condition existing) {
        this.condition = existing;
        this.result = null;

        if (existing != null) {
            String typeId = existing.getTypeId();
            for (int i = 0; i < CONDITION_TYPE_IDS.length; i++) {
                if (CONDITION_TYPE_IDS[i].equals(typeId)) {
                    selectedTypeIndex = i;
                    break;
                }
            }
            loadFromCondition(existing);
        }

        setBackground("menubg.png");
        xSize = 220;
        ySize = 140;
    }

    private void loadFromCondition(Condition cond) {
        String typeId = cond.getTypeId();
        if (typeId.equals("hit_count")) {
            Condition.ConditionHitCount hitCount = (Condition.ConditionHitCount) cond;
            requiredHits = hitCount.getRequiredHits();
            withinTicks = hitCount.getWithinTicks();
        } else {
            // HP-based conditions
            net.minecraft.nbt.NBTTagCompound nbt = cond.writeNBT();
            threshold = nbt.hasKey("threshold") ? nbt.getFloat("threshold") : 0.5f;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 8;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 90;

        // Title
        addLabel(new GuiNpcLabel(0, "condition.edit", labelX, y));
        y += 18;

        // Condition Type selector
        addLabel(new GuiNpcLabel(1, "gui.type", labelX, y + 5));
        addButton(new GuiNpcButton(1, fieldX, y, 115, 20, CONDITION_TYPES, selectedTypeIndex));
        y += 26;

        // Type-specific fields
        String typeId = CONDITION_TYPE_IDS[selectedTypeIndex];
        if (typeId.equals("hit_count")) {
            // Hit count fields
            addLabel(new GuiNpcLabel(2, "condition.hits", labelX, y + 5));
            GuiNpcTextField hitsField = new GuiNpcTextField(2, this, fontRendererObj, fieldX, y, 40, 20, String.valueOf(requiredHits));
            hitsField.setIntegersOnly();
            hitsField.setMinMaxDefault(1, 100, 3);
            addTextField(hitsField);
            y += 24;

            addLabel(new GuiNpcLabel(3, "condition.within", labelX, y + 5));
            GuiNpcTextField ticksField = new GuiNpcTextField(3, this, fontRendererObj, fieldX, y, 40, 20, String.valueOf(withinTicks));
            ticksField.setIntegersOnly();
            ticksField.setMinMaxDefault(1, 1200, 60);
            addTextField(ticksField);
        } else {
            // HP threshold field (as percentage)
            addLabel(new GuiNpcLabel(2, "condition.percent", labelX, y + 5));
            int percent = (int) (threshold * 100);
            GuiNpcTextField thresholdField = new GuiNpcTextField(4, this, fontRendererObj, fieldX, y, 40, 20, String.valueOf(percent));
            thresholdField.setIntegersOnly();
            thresholdField.setMinMaxDefault(1, 99, 50);
            addTextField(thresholdField);
            addLabel(new GuiNpcLabel(4, "%", fieldX + 45, y + 5));
        }

        // Bottom buttons
        addButton(new GuiNpcButton(10, guiLeft + 8, guiTop + ySize - 28, 60, 20, "gui.cancel"));
        addButton(new GuiNpcButton(11, guiLeft + xSize - 68, guiTop + ySize - 28, 60, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 1) {
            // Type changed
            selectedTypeIndex = ((GuiNpcButton) guibutton).getValue();
            initGui();
        } else if (id == 10) {
            // Cancel - close without saving
            result = null;
            close();
        } else if (id == 11) {
            // Done - create condition and close
            result = createCondition();
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;

        if (id == 2) {
            requiredHits = textField.getInteger();
        } else if (id == 3) {
            withinTicks = textField.getInteger();
        } else if (id == 4) {
            int percent = textField.getInteger();
            threshold = percent / 100.0f;
        }
    }

    private Condition createCondition() {
        String typeId = CONDITION_TYPE_IDS[selectedTypeIndex];

        switch (typeId) {
            case "hp_above":
                return new Condition.ConditionHPAbove(threshold);
            case "hp_below":
                return new Condition.ConditionHPBelow(threshold);
            case "target_hp_above":
                return new Condition.ConditionTargetHPAbove(threshold);
            case "target_hp_below":
                return new Condition.ConditionTargetHPBelow(threshold);
            case "hit_count":
                return new Condition.ConditionHitCount(requiredHits, withinTicks);
            default:
                return null;
        }
    }

    /**
     * Get the resulting condition after dialog closes.
     * Returns null if cancelled or no valid condition.
     */
    public Condition getResult() {
        return result;
    }
}
