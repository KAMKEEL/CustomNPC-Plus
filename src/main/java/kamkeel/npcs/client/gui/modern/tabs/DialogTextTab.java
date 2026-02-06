package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.components.ModernTextArea;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.ModernFieldPanel;
import noppes.npcs.client.gui.builder.ModernFieldPanelListener;
import noppes.npcs.controllers.data.Dialog;

import java.util.Arrays;
import java.util.List;

/**
 * Text tab for the dialog editor.
 * Contains title and dialog text fields.
 * Uses ModernFieldPanel for declarative field rendering.
 */
public class DialogTextTab extends DialogEditorTab {

    private ModernFieldPanel fieldPanel;

    public DialogTextTab(DialogEditorPanel parent) {
        super(parent);
        fieldPanel = new ModernFieldPanel();
        fieldPanel.setListener(new ModernFieldPanelListener() {
            @Override
            public void onSelectAction(String action, int slot) {}

            @Override
            public void onColorSelect(int slot, int currentColor) {}

            @Override
            public void onFieldChanged() {
                markDirty();
            }
        });
    }

    private List<FieldDef> createFieldDefs() {
        return Arrays.asList(
            FieldDef.stringField("Title", () -> dialog.title, v -> dialog.title = v)
                .maxLength(64).placeholder("Dialog title..."),
            FieldDef.textAreaField("Dialog Text", () -> dialog.text, v -> dialog.text = v)
                .maxLength(2000).fillHeight().charCount()
        );
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null) return;
        fieldPanel.setFields(createFieldDefs());
    }

    @Override
    public void saveToDialog(Dialog dialog) {
        // No-op: immediate binding via FieldDef closures
    }

    @Override
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        fieldPanel.setAvailableHeight(parent.getAvailableContentHeight());
        return fieldPanel.draw(cx, cw, startY, mouseX, mouseY, fr);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return fieldPanel.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyTyped(char c, int keyCode) {
        return fieldPanel.keyTyped(c, keyCode);
    }

    @Override
    public void updateScreen() {
        fieldPanel.updateScreen();
    }

    @Override
    public List<ModernDropdown> getDropdowns() {
        return fieldPanel.getDropdowns();
    }

    @Override
    public boolean handleExpandedDropdownScreenClick(int mouseX, int mouseY, int button) {
        return fieldPanel.handleExpandedDropdownScreenClick(mouseX, mouseY, button);
    }

    // === Accessors for parent panel ===

    public ModernTextArea getTextArea() {
        List<ModernTextArea> areas = fieldPanel.getTextAreas();
        return areas.isEmpty() ? null : areas.get(0);
    }

    public String getTitle() {
        return dialog != null ? dialog.title : "";
    }
}
