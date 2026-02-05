package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.components.ModernSelectButton;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;

/**
 * A row component for faction availability requirements.
 * Contains condition dropdown, stance dropdown, select button, and clear button.
 */
public class FactionRow {
    private int baseId;
    private int slot;

    private ModernDropdown conditionDropdown;
    private ModernDropdown stanceDropdown;
    private ModernSelectButton selectBtn;
    private ModernButton clearBtn;

    private int x, y, width;

    public FactionRow(int baseId, int slot) {
        this.baseId = baseId;
        this.slot = slot;

        conditionDropdown = new ModernDropdown(baseId, 0, 0, 55, 16);
        conditionDropdown.setOptions(Arrays.asList("Always", "Is", "Is Not"));

        stanceDropdown = new ModernDropdown(baseId + 1, 0, 0, 60, 16);
        stanceDropdown.setOptions(Arrays.asList("Friendly", "Neutral", "Unfriendly"));

        selectBtn = new ModernSelectButton(baseId + 2, 0, 0, 60, 16, "Faction...");
        clearBtn = new ModernButton(baseId + 3, 0, 0, 16, 16, "X");
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void draw(int mouseX, int mouseY, FontRenderer fr) {
        conditionDropdown.setBounds(x, y, 55, 16);
        conditionDropdown.drawBase(mouseX, mouseY);

        boolean enabled = conditionDropdown.getSelectedIndex() != 0;

        stanceDropdown.setEnabled(enabled);
        stanceDropdown.setBounds(x + 58, y, 60, 16);
        stanceDropdown.drawBase(mouseX, mouseY);

        selectBtn.setEnabled(enabled);
        selectBtn.setBounds(x + 122, y, width - 142, 16);
        selectBtn.draw(mouseX, mouseY);

        clearBtn.xPosition = x + width - 16;
        clearBtn.yPosition = y;
        clearBtn.width = 16;
        clearBtn.height = 16;
        clearBtn.enabled = enabled;
        clearBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button, DialogEditorPanel panel) {
        if (conditionDropdown.mouseClicked(mouseX, mouseY, button)) return true;
        if (stanceDropdown.mouseClicked(mouseX, mouseY, button)) return true;
        if (selectBtn.mouseClicked(mouseX, mouseY, button)) {
            IDialogEditorListener listener = panel.getListener();
            if (listener != null) {
                listener.onFactionSelectRequested(slot);
            }
            return true;
        }
        if (clearBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            selectBtn.clearSelection();
            return true;
        }
        return false;
    }

    public void setData(int condition, int stance, int selectedId) {
        conditionDropdown.setSelectedIndex(condition);
        stanceDropdown.setSelectedIndex(stance);
        if (selectedId >= 0) {
            selectBtn.setSelected(selectedId, "Faction #" + selectedId);
        } else {
            selectBtn.clearSelection();
        }
    }

    public void setSelected(int id, String name) {
        selectBtn.setSelected(id, name);
    }

    public int getCondition() { return conditionDropdown.getSelectedIndex(); }
    public int getStance() { return stanceDropdown.getSelectedIndex(); }
    public int getSelectedId() { return selectBtn.getSelectedId(); }
    public ModernDropdown getConditionDropdown() { return conditionDropdown; }
    public ModernDropdown getStanceDropdown() { return stanceDropdown; }
}
