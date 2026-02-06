package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.components.ModernTextArea;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.ModernFieldPanel;
import noppes.npcs.client.gui.builder.ModernFieldPanelListener;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import noppes.npcs.controllers.data.Dialog;

import java.util.Arrays;
import java.util.List;

/**
 * Settings tab for the dialog editor.
 * Contains quest, mail, and command configuration.
 * Uses ModernFieldPanel for declarative field rendering.
 */
public class DialogSettingsTab extends DialogEditorTab {

    private ModernFieldPanel fieldPanel;

    public DialogSettingsTab(DialogEditorPanel parent) {
        super(parent);
        fieldPanel = new ModernFieldPanel();
        fieldPanel.setListener(new ModernFieldPanelListener() {
            @Override
            public void onSelectAction(String action, int slot) {
                IDialogEditorListener listener = parent.getListener();
                if (listener == null) return;
                switch (action) {
                    case "quest":
                        listener.onQuestSelectRequested(0);
                        break;
                    case "mail":
                        listener.onMailSetupRequested();
                        break;
                }
            }

            @Override
            public void onColorSelect(int slot, int currentColor) {
                // No color fields in this tab
            }

            @Override
            public void onFieldChanged() {
                markDirty();
            }
        });
    }

    private List<FieldDef> createFieldDefs() {
        return Arrays.asList(
            // Quest section (expanded by default)
            FieldDef.section("Quest").collapsed(true),
            FieldDef.selectField("Quest", () -> dialog.quest >= 0 ? "Quest #" + dialog.quest : "Select...")
                .action("quest").slot(0)
                .clearable(() -> dialog.quest = -1),

            // Mail section
            FieldDef.section("Mail").collapsed(true),
            FieldDef.stringField("Subject", () -> dialog.mail != null ? dialog.mail.subject : "", v -> {
                if (dialog.mail != null) dialog.mail.subject = v;
            }).maxLength(64).placeholder("Subject..."),
            FieldDef.stringField("Sender", () -> dialog.mail != null ? dialog.mail.sender : "", v -> {
                if (dialog.mail != null) dialog.mail.sender = v;
            }).maxLength(32).placeholder("Sender..."),
            FieldDef.row(
                FieldDef.selectField("Edit...", () -> "Edit...").action("mail"),
                FieldDef.selectField("Clear", () -> "Clear").onAction(() -> {
                    if (dialog.mail != null) {
                        dialog.mail.subject = "";
                        dialog.mail.sender = "";
                    }
                })
            ),

            // Command section
            FieldDef.section("Command").collapsed(true),
            FieldDef.textAreaField("", () -> dialog.command, v -> dialog.command = v)
                .maxLength(32767).height(70)
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

    // === Selection Callbacks ===

    public void onQuestSelected(int questId, String questName) {
        if (dialog != null) {
            dialog.quest = questId;
            markDirty();
            // Display syncs from getter on next draw
        }
    }

    // === Accessors ===

    public ModernTextArea getCommandArea() {
        List<ModernTextArea> areas = fieldPanel.getTextAreas();
        return areas.isEmpty() ? null : areas.get(0);
    }
}
