package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.*;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.ModernColors;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.controllers.data.Dialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings tab for the dialog editor.
 * Contains quest, mail, and command configuration.
 */
public class DialogSettingsTab extends DialogEditorTab {

    private int componentGap = 3;

    // Quest Section
    private CollapsibleSection questSection;
    private ModernSelectButton questSelectBtn;
    private ModernButton clearQuestBtn;

    // Mail Section
    private CollapsibleSection mailSection;
    private ModernTextField mailSubjectField;
    private ModernTextField mailSenderField;
    private ModernButton mailEditBtn;
    private ModernButton clearMailBtn;

    // Command Section
    private CollapsibleSection commandSection;
    private ModernTextArea commandArea;

    public DialogSettingsTab(DialogEditorPanel parent) {
        super(parent);
        initComponents();
    }

    private void initComponents() {
        // Quest Section
        questSection = new CollapsibleSection(400, "Quest");
        questSelectBtn = new ModernSelectButton(401, 0, 0, 100, 16, "Select Quest...");
        clearQuestBtn = new ModernButton(402, 0, 0, 16, 16, "X");

        // Mail Section
        mailSection = new CollapsibleSection(410, "Mail", false);
        mailSubjectField = new ModernTextField(411, 0, 0, 100, 16);
        mailSubjectField.setMaxLength(64);
        mailSubjectField.setPlaceholder("Subject...");
        mailSenderField = new ModernTextField(412, 0, 0, 100, 16);
        mailSenderField.setMaxLength(32);
        mailSenderField.setPlaceholder("Sender...");
        mailEditBtn = new ModernButton(413, 0, 0, 80, 16, "Edit...");
        clearMailBtn = new ModernButton(414, 0, 0, 16, 16, "X");

        // Command Section
        commandSection = new CollapsibleSection(420, "Command", false);
        commandArea = new ModernTextArea(421, 0, 0, 100, 60);
        commandArea.setMaxLength(32767);
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null) return;

        if (dialog.quest >= 0) {
            questSelectBtn.setSelected(dialog.quest, "Quest #" + dialog.quest);
        } else {
            questSelectBtn.clearSelection();
        }

        // Mail inline fields
        if (dialog.mail != null) {
            mailSubjectField.setText(dialog.mail.subject);
            mailSenderField.setText(dialog.mail.sender);
        } else {
            mailSubjectField.setText("");
            mailSenderField.setText("");
        }

        commandArea.setText(dialog.command);
    }

    @Override
    public void saveToDialog(Dialog dialog) {
        if (dialog == null) return;

        dialog.quest = questSelectBtn.getSelectedId();

        // Mail inline fields
        if (dialog.mail != null) {
            dialog.mail.subject = mailSubjectField.getText();
            dialog.mail.sender = mailSenderField.getText();
        }

        dialog.command = commandArea.getText();
    }

    @Override
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        int y = startY;
        int rowH = 18;

        // Quest Section
        questSection.setPosition(cx, y, cw);
        questSection.setContentHeight(rowH + 4);
        if (questSection.draw(mouseX, mouseY)) {
            int dy = questSection.getContentY();
            questSelectBtn.setBounds(cx + 4, dy, cw - 28, 16);
            questSelectBtn.draw(mouseX, mouseY);
            clearQuestBtn.xPosition = cx + cw - 20;
            clearQuestBtn.yPosition = dy;
            clearQuestBtn.width = 16;
            clearQuestBtn.height = 16;
            clearQuestBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }
        y += questSection.getTotalHeight() + componentGap;

        // Mail Section (inline subject, sender, edit button)
        mailSection.setPosition(cx, y, cw);
        mailSection.setContentHeight(rowH * 3 + 8);
        if (mailSection.draw(mouseX, mouseY)) {
            int dy = mailSection.getContentY();

            // Subject row
            fr.drawString("Subject:", cx + 4, dy + 4, ModernColors.TEXT_GRAY);
            mailSubjectField.setBounds(cx + 55, dy, cw - 59, 16);
            mailSubjectField.draw(mouseX, mouseY);
            dy += rowH;

            // Sender row
            fr.drawString("Sender:", cx + 4, dy + 4, ModernColors.TEXT_GRAY);
            mailSenderField.setBounds(cx + 55, dy, cw - 59, 16);
            mailSenderField.draw(mouseX, mouseY);
            dy += rowH;

            // Edit and Clear buttons row
            mailEditBtn.xPosition = cx + 4;
            mailEditBtn.yPosition = dy;
            mailEditBtn.width = 80;
            mailEditBtn.height = 16;
            mailEditBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);

            clearMailBtn.xPosition = cx + 88;
            clearMailBtn.yPosition = dy;
            clearMailBtn.width = 50;
            clearMailBtn.height = 16;
            clearMailBtn.displayString = "Clear";
            clearMailBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }
        y += mailSection.getTotalHeight() + componentGap;

        // Command Section
        commandSection.setPosition(cx, y, cw);
        commandSection.setContentHeight(80);
        if (commandSection.draw(mouseX, mouseY)) {
            int dy = commandSection.getContentY();
            commandArea.setBounds(cx + 4, dy, cw - 8, 70);
            commandArea.draw(mouseX, mouseY);
        }
        y += commandSection.getTotalHeight() + componentGap;

        return y - startY;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        IDialogEditorListener listener = parent.getListener();

        if (questSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (questSection.isExpanded()) {
            if (questSelectBtn.mouseClicked(mouseX, mouseY, button)) {
                if (listener != null) listener.onQuestSelectRequested(0);
                return true;
            }
            if (clearQuestBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                questSelectBtn.clearSelection();
                markDirty();
                return true;
            }
        }

        if (mailSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (mailSection.isExpanded()) {
            // Don't mark dirty on click - only mark dirty when content changes (in keyTyped)
            if (mailSubjectField.handleClick(mouseX, mouseY, button)) { return true; }
            if (mailSenderField.handleClick(mouseX, mouseY, button)) { return true; }
            if (mailEditBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                if (listener != null) listener.onMailSetupRequested();
                return true;
            }
            if (clearMailBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                mailSubjectField.setText("");
                mailSenderField.setText("");
                markDirty();
                return true;
            }
        }

        if (commandSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (commandSection.isExpanded()) {
            // Don't mark dirty on click - only mark dirty when content changes (in keyTyped)
            if (commandArea.handleClick(mouseX, mouseY, button)) { return true; }
        }

        return false;
    }

    @Override
    public boolean keyTyped(char c, int keyCode) {
        if (mailSubjectField.keyTyped(c, keyCode)) { markDirty(); return true; }
        if (mailSenderField.keyTyped(c, keyCode)) { markDirty(); return true; }
        if (commandArea.keyTyped(c, keyCode)) { markDirty(); return true; }
        return false;
    }

    @Override
    public void updateScreen() {
        mailSubjectField.updateCursorCounter();
        mailSenderField.updateCursorCounter();
        commandArea.updateCursorCounter();
    }

    @Override
    public List<ModernDropdown> getDropdowns() {
        return new ArrayList<>(); // No dropdowns in this tab
    }

    // === Selection Callbacks ===

    public void onQuestSelected(int questId, String questName) {
        questSelectBtn.setSelected(questId, questName);
        markDirty();
    }

    // === Accessors ===

    public ModernTextArea getCommandArea() {
        return commandArea;
    }
}
