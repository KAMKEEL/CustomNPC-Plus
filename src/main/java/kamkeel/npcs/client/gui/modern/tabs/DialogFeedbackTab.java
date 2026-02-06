package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernDropdown;
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
 * Feedback tab for the dialog editor.
 * Contains display options, sound settings, and color configuration.
 * Uses ModernFieldPanel for declarative field rendering.
 */
public class DialogFeedbackTab extends DialogEditorTab {

    private ModernFieldPanel fieldPanel;

    public DialogFeedbackTab(DialogEditorPanel parent) {
        super(parent);
        fieldPanel = new ModernFieldPanel();
        fieldPanel.setListener(new ModernFieldPanelListener() {
            @Override
            public void onSelectAction(String action, int slot) {
                IDialogEditorListener listener = parent.getListener();
                if (listener == null) return;
                if ("sound".equals(action)) {
                    listener.onSoundSelectRequested();
                }
            }

            @Override
            public void onColorSelect(int slot, int currentColor) {
                IDialogEditorListener listener = parent.getListener();
                if (listener != null) {
                    listener.onColorSelectRequested(slot, currentColor);
                }
            }

            @Override
            public void onFieldChanged() {
                markDirty();
            }
        });
    }

    private List<FieldDef> createFieldDefs() {
        return Arrays.asList(
            // Display section (expanded by default)
            FieldDef.section("Display").collapsed(true),
            FieldDef.boolField("Hide NPC", () -> dialog.hideNPC, v -> dialog.hideNPC = v),
            FieldDef.boolField("Show Wheel", () -> dialog.showWheel, v -> dialog.showWheel = v),
            FieldDef.boolField("Darken Screen", () -> dialog.darkenScreen, v -> dialog.darkenScreen = v),
            FieldDef.boolField("Disable ESC", () -> dialog.disableEsc, v -> dialog.disableEsc = v),
            FieldDef.stringEnumField("Render Type", new String[]{"Instant", "Gradual"},
                () -> dialog.renderGradual ? "Gradual" : "Instant",
                v -> dialog.renderGradual = "Gradual".equals(v)),
            FieldDef.boolField("Prev Blocks", () -> dialog.showPreviousBlocks, v -> dialog.showPreviousBlocks = v),
            FieldDef.boolField("Option Line", () -> dialog.showOptionLine, v -> dialog.showOptionLine = v),

            // Sound section
            FieldDef.section("Sound").collapsed(true),
            FieldDef.row(
                FieldDef.stringField("Sound", () -> dialog.sound, v -> dialog.sound = v).placeholder("Sound path..."),
                FieldDef.selectField("...", () -> "...").action("sound")
            ),
            FieldDef.stringField("Text Sound", () -> dialog.textSound, v -> dialog.textSound = v)
                .placeholder("Text sound...")
                .visibleWhen(() -> dialog.renderGradual),
            FieldDef.floatField("Pitch", () -> dialog.textPitch, v -> dialog.textPitch = v)
                .range(0.1f, 2.0f)
                .visibleWhen(() -> dialog.renderGradual),

            // Colors section
            FieldDef.section("Colors").collapsed(true),
            FieldDef.row(
                FieldDef.colorField("Text", () -> dialog.color, v -> dialog.color = v).slot(0),
                FieldDef.colorField("Title", () -> dialog.titleColor, v -> dialog.titleColor = v).slot(1)
            )
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

    public void onSoundSelected(String soundPath) {
        if (dialog != null) {
            dialog.sound = soundPath;
            fieldPanel.refresh();
            markDirty();
        }
    }

    public void onColorSelected(int slot, int color) {
        if (dialog == null) return;
        if (slot == 0) dialog.color = color;
        else if (slot == 1) dialog.titleColor = color;
        markDirty();
        // Color buttons sync from data on next draw, no refresh needed
    }
}
