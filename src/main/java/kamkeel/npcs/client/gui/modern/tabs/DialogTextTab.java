package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.components.ModernTextArea;
import kamkeel.npcs.client.gui.components.ModernTextField;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.controllers.data.Dialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Text tab for the dialog editor.
 * Contains title and dialog text fields.
 */
public class DialogTextTab extends DialogEditorTab {

    private ModernTextField titleField;
    private ModernTextArea textArea;

    public DialogTextTab(DialogEditorPanel parent) {
        super(parent);
        initComponents();
    }

    private void initComponents() {
        titleField = new ModernTextField(100, 0, 0, 100, 16);
        titleField.setMaxLength(64);
        titleField.setPlaceholder("Dialog title...");

        textArea = new ModernTextArea(101, 0, 0, 100, 80);
        textArea.setMaxLength(2000);
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null) return;

        titleField.setText(dialog.title);
        textArea.setText(dialog.text);
    }

    @Override
    public void saveToDialog(Dialog dialog) {
        if (dialog == null) return;

        dialog.title = titleField.getText();
        dialog.text = textArea.getText();
    }

    @Override
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        int y = startY;

        fr.drawString("Title:", cx, y, ModernColors.TEXT_LIGHT);
        y += 10;

        titleField.setBounds(cx, y, cw, 16);
        titleField.draw(mouseX, mouseY);
        y += 20;

        fr.drawString("Dialog Text:", cx, y, ModernColors.TEXT_LIGHT);
        y += 10;

        // Calculate available height for text area
        int areaH = Math.max(80, parent.getAvailableContentHeight() - 60);
        textArea.setBounds(cx, y, cw, areaH);
        textArea.draw(mouseX, mouseY);
        y += areaH + 4;

        // Character count
        String charCount = textArea.getCharacterCount() + " / " + textArea.getMaxLength();
        int countW = fr.getStringWidth(charCount);
        int countColor = textArea.getCharacterCount() > 1800 ? ModernColors.ACCENT_RED :
                        textArea.getCharacterCount() > 1500 ? ModernColors.ACCENT_ORANGE : ModernColors.TEXT_GRAY;
        fr.drawString(charCount, cx + cw - countW, y, countColor);
        y += 12;

        return y - startY;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // Don't mark dirty on click - only mark dirty when content changes (in keyTyped)
        if (titleField.handleClick(mouseX, mouseY, button)) return true;
        if (textArea.handleClick(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean keyTyped(char c, int keyCode) {
        if (titleField.keyTyped(c, keyCode)) { markDirty(); return true; }
        if (textArea.keyTyped(c, keyCode)) { markDirty(); return true; }
        return false;
    }

    @Override
    public void updateScreen() {
        titleField.updateCursorCounter();
        textArea.updateCursorCounter();
    }

    @Override
    public List<ModernDropdown> getDropdowns() {
        return new ArrayList<>(); // No dropdowns in this tab
    }

    // === Accessors for parent panel ===

    public ModernTextArea getTextArea() {
        return textArea;
    }

    public String getTitle() {
        return titleField.getText();
    }
}
