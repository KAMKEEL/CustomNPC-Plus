package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.components.ModernSelectButton;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;

/**
 * A row component for quest/dialog availability requirements.
 * Contains a condition dropdown, select button, and clear button.
 */
public class AvailabilityRow {
    private int baseId;
    private int slot;
    private String type;

    private ModernDropdown conditionDropdown;
    private ModernSelectButton selectBtn;
    private ModernButton clearBtn;

    private int x, y, width;

    public AvailabilityRow(int baseId, int slot, String type, List<String> conditions) {
        this.baseId = baseId;
        this.slot = slot;
        this.type = type;

        conditionDropdown = new ModernDropdown(baseId, 0, 0, 70, 16);
        conditionDropdown.setOptions(conditions);

        selectBtn = new ModernSelectButton(baseId + 1, 0, 0, 80, 16, "Select " + type + "...");
        clearBtn = new ModernButton(baseId + 2, 0, 0, 16, 16, "X");
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void draw(int mouseX, int mouseY, FontRenderer fr) {
        conditionDropdown.setBounds(x, y, 70, 16);
        conditionDropdown.drawBase(mouseX, mouseY);

        boolean enabled = conditionDropdown.getSelectedIndex() != 0;
        selectBtn.setEnabled(enabled);
        selectBtn.setBounds(x + 74, y, width - 94, 16);
        selectBtn.draw(mouseX, mouseY);

        clearBtn.xPosition = x + width - 16;
        clearBtn.yPosition = y;
        clearBtn.width = 16;
        clearBtn.height = 16;
        clearBtn.enabled = enabled;
        clearBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button, DialogEditorPanel panel, String selectType) {
        if (conditionDropdown.mouseClicked(mouseX, mouseY, button)) return true;
        if (selectBtn.mouseClicked(mouseX, mouseY, button)) {
            IDialogEditorListener listener = panel.getListener();
            if (listener != null) {
                if ("quest".equals(selectType)) {
                    listener.onQuestSelectRequested(slot + 1);
                } else {
                    listener.onDialogSelectRequested(slot);
                }
            }
            return true;
        }
        if (clearBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            selectBtn.clearSelection();
            return true;
        }
        return false;
    }

    public void setData(int condition, int selectedId) {
        conditionDropdown.setSelectedIndex(condition);
        if (selectedId >= 0) {
            selectBtn.setSelected(selectedId, type + " #" + selectedId);
        } else {
            selectBtn.clearSelection();
        }
    }

    public void setSelected(int id, String name) {
        selectBtn.setSelected(id, name);
    }

    public int getCondition() { return conditionDropdown.getSelectedIndex(); }
    public int getSelectedId() { return selectBtn.getSelectedId(); }
    public ModernDropdown getConditionDropdown() { return conditionDropdown; }
}
