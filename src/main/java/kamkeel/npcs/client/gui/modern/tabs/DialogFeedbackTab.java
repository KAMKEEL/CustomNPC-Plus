package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.*;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.ModernColors;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.controllers.data.Dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Feedback tab for the dialog editor.
 * Contains display options, sound settings, and color configuration.
 */
public class DialogFeedbackTab extends DialogEditorTab {

    private int componentGap = 3;

    // Display Section
    private CollapsibleSection displaySection;
    private ModernCheckbox hideNpcToggle;
    private ModernCheckbox showWheelToggle;
    private ModernCheckbox darkenScreenToggle;
    private ModernCheckbox disableEscToggle;
    private ModernDropdown renderTypeDropdown;
    private ModernCheckbox showPrevBlocksToggle;
    private ModernCheckbox showOptionLineToggle;

    // Sound Section
    private CollapsibleSection soundSection;
    private ModernTextField soundField;
    private ModernSelectButton soundSelectBtn;
    private ModernTextField textSoundField;
    private ModernNumberField textPitchField;

    // Colors Section
    private CollapsibleSection colorsSection;
    private ModernColorButton textColorBtn;
    private ModernColorButton titleColorBtn;

    public DialogFeedbackTab(DialogEditorPanel parent) {
        super(parent);
        initComponents();
    }

    private void initComponents() {
        // Display Section
        displaySection = new CollapsibleSection(300, "Display");
        hideNpcToggle = new ModernCheckbox(301, 0, 0, false);
        showWheelToggle = new ModernCheckbox(302, 0, 0, false);
        darkenScreenToggle = new ModernCheckbox(303, 0, 0, true);
        disableEscToggle = new ModernCheckbox(304, 0, 0, false);
        renderTypeDropdown = new ModernDropdown(305, 0, 0, 80, 16);
        renderTypeDropdown.setOptions(Arrays.asList("Instant", "Gradual"));
        showPrevBlocksToggle = new ModernCheckbox(306, 0, 0, true);
        showOptionLineToggle = new ModernCheckbox(307, 0, 0, true);

        // Sound Section
        soundSection = new CollapsibleSection(310, "Sound", false);
        soundField = new ModernTextField(311, 0, 0, 100, 16);
        soundField.setPlaceholder("Sound path...");
        soundSelectBtn = new ModernSelectButton(312, 0, 0, 50, 16, "...");
        textSoundField = new ModernTextField(313, 0, 0, 100, 16);
        textSoundField.setPlaceholder("Text sound...");
        textPitchField = new ModernNumberField(314, 0, 0, 40, 16, 1.0f);
        textPitchField.setFloatBounds(0.1f, 2.0f, 1.0f);

        // Colors Section
        colorsSection = new CollapsibleSection(320, "Colors", false);
        textColorBtn = new ModernColorButton(321, 0, 0, 50, 16, 0xE0E0E0);
        titleColorBtn = new ModernColorButton(322, 0, 0, 50, 16, 0xE0E0E0);
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null) return;

        hideNpcToggle.setValue(dialog.hideNPC);
        showWheelToggle.setValue(dialog.showWheel);
        darkenScreenToggle.setValue(dialog.darkenScreen);
        disableEscToggle.setValue(dialog.disableEsc);
        renderTypeDropdown.setSelectedIndex(dialog.renderGradual ? 1 : 0);
        showPrevBlocksToggle.setValue(dialog.showPreviousBlocks);
        showOptionLineToggle.setValue(dialog.showOptionLine);
        soundField.setText(dialog.sound);
        textSoundField.setText(dialog.textSound);
        textPitchField.setFloat(dialog.textPitch);
        textColorBtn.setColor(dialog.color);
        titleColorBtn.setColor(dialog.titleColor);
    }

    @Override
    public void saveToDialog(Dialog dialog) {
        if (dialog == null) return;

        dialog.hideNPC = hideNpcToggle.getValue();
        dialog.showWheel = showWheelToggle.getValue();
        dialog.darkenScreen = darkenScreenToggle.getValue();
        dialog.disableEsc = disableEscToggle.getValue();
        dialog.renderGradual = renderTypeDropdown.getSelectedIndex() == 1;
        dialog.showPreviousBlocks = showPrevBlocksToggle.getValue();
        dialog.showOptionLine = showOptionLineToggle.getValue();
        dialog.sound = soundField.getText();
        dialog.textSound = textSoundField.getText();
        dialog.textPitch = textPitchField.getFloat();
        dialog.color = textColorBtn.getColor();
        dialog.titleColor = titleColorBtn.getColor();
    }

    @Override
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        int y = startY;
        int toggleX = cx + cw - 36;
        int rowH = 18;

        // Display Section
        displaySection.setPosition(cx, y, cw);
        displaySection.setContentHeight(rowH * 7 + 10);
        if (displaySection.draw(mouseX, mouseY)) {
            int dy = displaySection.getContentY();

            fr.drawString("Hide NPC", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            hideNpcToggle.setPosition(toggleX, dy);
            hideNpcToggle.draw(mouseX, mouseY);
            dy += rowH;

            fr.drawString("Show Wheel", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            showWheelToggle.setPosition(toggleX, dy);
            showWheelToggle.draw(mouseX, mouseY);
            dy += rowH;

            fr.drawString("Darken Screen", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            darkenScreenToggle.setPosition(toggleX, dy);
            darkenScreenToggle.draw(mouseX, mouseY);
            dy += rowH;

            fr.drawString("Disable ESC", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            disableEscToggle.setPosition(toggleX, dy);
            disableEscToggle.draw(mouseX, mouseY);
            dy += rowH;

            fr.drawString("Render Type", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            renderTypeDropdown.setBounds(cx + cw - 80, dy, 80, 16);
            renderTypeDropdown.drawBase(mouseX, mouseY);
            dy += rowH;

            fr.drawString("Prev Blocks", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            showPrevBlocksToggle.setPosition(toggleX, dy);
            showPrevBlocksToggle.draw(mouseX, mouseY);
            dy += rowH;

            fr.drawString("Option Line", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            showOptionLineToggle.setPosition(toggleX, dy);
            showOptionLineToggle.draw(mouseX, mouseY);
        }
        y += displaySection.getTotalHeight() + componentGap;

        // Sound Section
        soundSection.setPosition(cx, y, cw);
        boolean gradual = renderTypeDropdown.getSelectedIndex() == 1;
        soundSection.setContentHeight(gradual ? rowH * 3 + 6 : rowH + 4);
        if (soundSection.draw(mouseX, mouseY)) {
            int dy = soundSection.getContentY();

            fr.drawString("Sound", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            soundField.setBounds(cx + 45, dy, cw - 95, 16);
            soundField.draw(mouseX, mouseY);
            soundSelectBtn.setBounds(cx + cw - 45, dy, 45, 16);
            soundSelectBtn.draw(mouseX, mouseY);
            dy += rowH;

            if (gradual) {
                fr.drawString("Text Sound", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
                textSoundField.setBounds(cx + 65, dy, cw - 65, 16);
                textSoundField.draw(mouseX, mouseY);
                dy += rowH;

                fr.drawString("Pitch", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
                textPitchField.setBounds(cx + 35, dy, 50, 16);
                textPitchField.draw(mouseX, mouseY);
            }
        }
        y += soundSection.getTotalHeight() + componentGap;

        // Colors Section
        colorsSection.setPosition(cx, y, cw);
        colorsSection.setContentHeight(rowH + 4);
        if (colorsSection.draw(mouseX, mouseY)) {
            int dy = colorsSection.getContentY();

            fr.drawString("Text", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            textColorBtn.setBounds(cx + 30, dy, 50, 16);
            textColorBtn.draw(mouseX, mouseY);

            fr.drawString("Title", cx + 90, dy + 4, ModernColors.TEXT_LIGHT);
            titleColorBtn.setBounds(cx + 115, dy, 50, 16);
            titleColorBtn.draw(mouseX, mouseY);
        }
        y += colorsSection.getTotalHeight() + componentGap;

        return y - startY;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        IDialogEditorListener listener = parent.getListener();

        if (displaySection.mouseClicked(mouseX, mouseY, button)) return true;
        if (displaySection.isExpanded()) {
            // Toggles actually change state on click, so markDirty is correct for them
            if (hideNpcToggle.mouseClicked(mouseX, mouseY, button)) { markDirty(); return true; }
            if (showWheelToggle.mouseClicked(mouseX, mouseY, button)) { markDirty(); return true; }
            if (darkenScreenToggle.mouseClicked(mouseX, mouseY, button)) { markDirty(); return true; }
            if (disableEscToggle.mouseClicked(mouseX, mouseY, button)) { markDirty(); return true; }
            // Dropdown: only mark dirty if selection changes
            int prevIndex = renderTypeDropdown.getSelectedIndex();
            if (renderTypeDropdown.mouseClicked(mouseX, mouseY, button)) {
                if (renderTypeDropdown.getSelectedIndex() != prevIndex) {
                    markDirty();
                }
                return true;
            }
            if (showPrevBlocksToggle.mouseClicked(mouseX, mouseY, button)) { markDirty(); return true; }
            if (showOptionLineToggle.mouseClicked(mouseX, mouseY, button)) { markDirty(); return true; }
        }

        if (soundSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (soundSection.isExpanded()) {
            // Don't mark dirty on click - only mark dirty when content changes (in keyTyped)
            if (soundField.handleClick(mouseX, mouseY, button)) { return true; }
            if (soundSelectBtn.mouseClicked(mouseX, mouseY, button)) {
                if (listener != null) listener.onSoundSelectRequested();
                return true;
            }
            if (textSoundField.handleClick(mouseX, mouseY, button)) { return true; }
            if (textPitchField.handleClick(mouseX, mouseY, button)) { return true; }
        }

        if (colorsSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (colorsSection.isExpanded()) {
            if (textColorBtn.mouseClicked(mouseX, mouseY, button)) {
                if (listener != null) listener.onColorSelectRequested(0, textColorBtn.getColor());
                return true;
            }
            if (titleColorBtn.mouseClicked(mouseX, mouseY, button)) {
                if (listener != null) listener.onColorSelectRequested(1, titleColorBtn.getColor());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyTyped(char c, int keyCode) {
        if (soundField.keyTyped(c, keyCode)) { markDirty(); return true; }
        if (textSoundField.keyTyped(c, keyCode)) { markDirty(); return true; }
        if (textPitchField.keyTyped(c, keyCode)) { markDirty(); return true; }
        return false;
    }

    @Override
    public void updateScreen() {
        soundField.updateCursorCounter();
        textSoundField.updateCursorCounter();
        textPitchField.updateCursorCounter();
    }

    @Override
    public List<ModernDropdown> getDropdowns() {
        List<ModernDropdown> dropdowns = new ArrayList<>();
        dropdowns.add(renderTypeDropdown);
        return dropdowns;
    }

    // === Selection Callbacks ===

    public void onSoundSelected(String soundPath) {
        soundField.setText(soundPath);
        markDirty();
    }

    public void onColorSelected(int slot, int color) {
        if (slot == 0) textColorBtn.setColor(color);
        else if (slot == 1) titleColorBtn.setColor(color);
        markDirty();
    }
}
