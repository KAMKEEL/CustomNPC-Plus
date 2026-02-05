package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.*;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.ModernColors;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.controllers.data.DialogOption;

import java.util.Arrays;

/**
 * Editor component for a single dialog option.
 * Contains title, type dropdown, color button, target dialog selector, and command field.
 */
public class OptionEditorV2 {
    private int baseId;
    private int slot;

    private CollapsibleSection section;
    private ModernTextField titleField;
    private ModernDropdown typeDropdown;
    private ModernColorButton colorBtn;
    private ModernSelectButton targetDialogBtn;
    private ModernTextField commandField;
    private ModernButton removeBtn;

    private int targetDialogId = -1;
    private int x, y, width;

    public OptionEditorV2(int baseId, int slot, DialogOption opt) {
        this.baseId = baseId;
        this.slot = slot;

        section = new CollapsibleSection(baseId, "Option " + slot + ": " + opt.title);

        titleField = new ModernTextField(baseId + 1, 0, 0, 100, 16);
        titleField.setText(opt.title);
        titleField.setMaxLength(64);

        typeDropdown = new ModernDropdown(baseId + 2, 0, 0, 80, 16);
        typeDropdown.setOptions(Arrays.asList("Quit", "Dialog", "Disabled", "Role", "Command"));
        typeDropdown.setSelectedIndex(opt.optionType.ordinal());

        colorBtn = new ModernColorButton(baseId + 3, 0, 0, 50, 16, opt.optionColor);

        targetDialogBtn = new ModernSelectButton(baseId + 4, 0, 0, 80, 16, "Select...");
        if (opt.dialogId >= 0) {
            targetDialogBtn.setSelected(opt.dialogId, "Dialog #" + opt.dialogId);
        }
        targetDialogId = opt.dialogId;

        commandField = new ModernTextField(baseId + 5, 0, 0, 100, 16);
        commandField.setText(opt.command);
        commandField.setMaxLength(32767);

        removeBtn = new ModernButton(baseId + 6, 0, 0, 16, 16, "X");
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        section.setPosition(x, y, width);
    }

    public int getHeight() {
        return section.getTotalHeight();
    }

    public void draw(int mouseX, int mouseY, FontRenderer fr) {
        int typeIdx = typeDropdown.getSelectedIndex();
        boolean isDialog = typeIdx == 1;
        boolean isCommand = typeIdx == 4;

        int contentH = 56;
        if (isDialog) contentH += 20;
        if (isCommand) contentH += 20;
        section.setContentHeight(contentH);

        // Update section title
        section.setTitle("Option " + slot + ": " + titleField.getText());

        if (section.draw(mouseX, mouseY)) {
            int cx = section.getContentX();
            int cy = section.getContentY();
            int cw = section.getContentWidth();

            // Row 1: Title + Remove
            fr.drawString("Title", cx, cy + 4, ModernColors.TEXT_GRAY);
            titleField.setBounds(cx + 30, cy, cw - 50, 16);
            titleField.draw(mouseX, mouseY);
            removeBtn.xPosition = cx + cw - 16;
            removeBtn.yPosition = cy;
            removeBtn.width = 16;
            removeBtn.height = 16;
            removeBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
            cy += 20;

            // Row 2: Type + Color
            fr.drawString("Type", cx, cy + 4, ModernColors.TEXT_GRAY);
            typeDropdown.setBounds(cx + 30, cy, 70, 16);
            typeDropdown.drawBase(mouseX, mouseY);

            fr.drawString("Color", cx + 110, cy + 4, ModernColors.TEXT_GRAY);
            colorBtn.setBounds(cx + 140, cy, 50, 16);
            colorBtn.draw(mouseX, mouseY);
            cy += 20;

            // Row 3: Target dialog (if type=Dialog)
            if (isDialog) {
                fr.drawString("Target", cx, cy + 4, ModernColors.TEXT_GRAY);
                targetDialogBtn.setBounds(cx + 40, cy, cw - 40, 16);
                targetDialogBtn.draw(mouseX, mouseY);
                cy += 20;
            }

            // Row 4: Command (if type=Command)
            if (isCommand) {
                fr.drawString("Cmd", cx, cy + 4, ModernColors.TEXT_GRAY);
                commandField.setBounds(cx + 30, cy, cw - 30, 16);
                commandField.draw(mouseX, mouseY);
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button, DialogEditorPanel panel) {
        if (section.mouseClicked(mouseX, mouseY, button)) return true;
        // Only handle content clicks if section is expanded
        if (section.isExpanded()) {
            if (titleField.handleClick(mouseX, mouseY, button)) return true;
            if (typeDropdown.mouseClicked(mouseX, mouseY, button)) return true;
            if (colorBtn.mouseClicked(mouseX, mouseY, button)) {
                IDialogEditorListener listener = panel.getListener();
                if (listener != null) listener.onColorSelectRequested(100 + slot, colorBtn.getColor());
                return true;
            }
            if (targetDialogBtn.mouseClicked(mouseX, mouseY, button)) {
                IDialogEditorListener listener = panel.getListener();
                if (listener != null) listener.onOptionDialogSelectRequested(slot);
                return true;
            }
            if (commandField.handleClick(mouseX, mouseY, button)) return true;
            if (removeBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                panel.removeOption(slot);
                return true;
            }
        }
        return false;
    }

    public boolean keyTyped(char c, int keyCode) {
        if (titleField.keyTyped(c, keyCode)) return true;
        if (commandField.keyTyped(c, keyCode)) return true;
        return false;
    }

    public void updateCursorCounter() {
        titleField.updateCursorCounter();
        commandField.updateCursorCounter();
    }

    public void setTargetDialog(int id, String name) {
        targetDialogId = id;
        targetDialogBtn.setSelected(id, name);
    }

    // === Getters ===

    public int getSlot() { return slot; }
    public ModernDropdown getTypeDropdown() { return typeDropdown; }
    public ModernTextField getTitleField() { return titleField; }
    public ModernColorButton getColorBtn() { return colorBtn; }
    public int getTargetDialogId() { return targetDialogId; }
    public String getCommand() { return commandField.getText(); }
    public int getTypeIndex() { return typeDropdown.getSelectedIndex(); }
}
