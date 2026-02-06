package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.ModernFieldPanel;
import noppes.npcs.client.gui.builder.ModernFieldPanelListener;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;

import java.util.*;

/**
 * Options tab for the dialog editor.
 * Contains up to 6 dialog option editors rendered via ModernFieldPanel.
 * Uses FieldDef sections with dynamic titles, remove buttons, and visibility predicates.
 */
public class DialogOptionsTab extends DialogEditorTab {

    private static final String[] OPTION_TYPES = {"Quit", "Dialog", "Disabled", "Role", "Command"};

    private ModernFieldPanel fieldPanel;
    private Map<Integer, Boolean> sectionExpandState = new HashMap<>();

    public DialogOptionsTab(DialogEditorPanel parent) {
        super(parent);
        fieldPanel = new ModernFieldPanel();
        fieldPanel.setListener(new ModernFieldPanelListener() {
            @Override
            public void onSelectAction(String action, int slot) {
                IDialogEditorListener listener = parent.getListener();
                if (listener == null) return;
                if ("optionDialog".equals(action)) {
                    listener.onOptionDialogSelectRequested(slot);
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
        List<FieldDef> defs = new ArrayList<>();

        for (int slot = 0; slot < 6; slot++) {
            DialogOption opt = dialog.options.get(slot);
            if (opt == null || opt.optionType == EnumOptionType.Disabled) continue;

            final int s = slot;
            boolean expanded = sectionExpandState.getOrDefault(slot, true);

            defs.add(FieldDef.section("Option " + s)
                .collapsed(expanded)
                .titleSupplier(() -> "Option " + s + ": " + dialog.options.get(s).title)
                .removeAction(() -> removeOption(s)));

            defs.add(FieldDef.stringField("Title",
                () -> dialog.options.get(s).title, v -> dialog.options.get(s).title = v)
                .maxLength(64));

            defs.add(FieldDef.row(
                FieldDef.stringEnumField("Type", OPTION_TYPES,
                    () -> OPTION_TYPES[dialog.options.get(s).optionType.ordinal()],
                    v -> {
                        for (int i = 0; i < OPTION_TYPES.length; i++) {
                            if (OPTION_TYPES[i].equals(v)) {
                                dialog.options.get(s).optionType = EnumOptionType.values()[i];
                                break;
                            }
                        }
                    }),
                FieldDef.colorField("Color",
                    () -> dialog.options.get(s).optionColor, v -> dialog.options.get(s).optionColor = v)
                    .slot(100 + s)
            ));

            defs.add(FieldDef.selectField("Target",
                () -> {
                    int did = dialog.options.get(s).dialogId;
                    if (did < 0) return "";
                    Dialog d = DialogController.Instance.dialogs.get(did);
                    return d != null ? "(ID: " + did + ") " + d.title : "Dialog #" + did;
                })
                .action("optionDialog").slot(s)
                .visibleWhen(() -> dialog.options.get(s).optionType == EnumOptionType.DialogOption));

            defs.add(FieldDef.stringField("Cmd",
                () -> dialog.options.get(s).command, v -> dialog.options.get(s).command = v)
                .maxLength(32767)
                .visibleWhen(() -> dialog.options.get(s).optionType == EnumOptionType.CommandBlock));
        }

        if (countActiveOptions() < 6) {
            defs.add(FieldDef.actionButton("+ Add Option", this::addNewOption));
        }

        return defs;
    }

    private int countActiveOptions() {
        int count = 0;
        for (int i = 0; i < 6; i++) {
            DialogOption opt = dialog.options.get(i);
            if (opt != null && opt.optionType != EnumOptionType.Disabled) count++;
        }
        return count;
    }

    private List<Integer> getActiveSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            DialogOption opt = dialog.options.get(i);
            if (opt != null && opt.optionType != EnumOptionType.Disabled) slots.add(i);
        }
        return slots;
    }

    private void saveExpandStates() {
        List<Integer> activeSlots = getActiveSlots();
        List<ModernFieldPanel.SectionInfo> sections = fieldPanel.getSections();
        for (int i = 0; i < Math.min(activeSlots.size(), sections.size()); i++) {
            sectionExpandState.put(activeSlots.get(i), sections.get(i).section.isExpanded());
        }
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null) return;
        saveExpandStates();
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

    // === Option Management ===

    private void addNewOption() {
        if (dialog == null) return;

        for (int i = 0; i < 6; i++) {
            DialogOption opt = dialog.options.get(i);
            if (opt == null || opt.optionType == EnumOptionType.Disabled) {
                DialogOption newOpt = new DialogOption();
                newOpt.title = "New Option";
                newOpt.optionType = EnumOptionType.QuitOption;
                dialog.options.put(i, newOpt);
                saveExpandStates();
                fieldPanel.setFields(createFieldDefs());
                markDirty();
                return;
            }
        }
    }

    public void removeOption(int slot) {
        if (dialog == null) return;
        DialogOption opt = dialog.options.get(slot);
        if (opt != null) {
            opt.optionType = EnumOptionType.Disabled;
            saveExpandStates();
            sectionExpandState.remove(slot);
            fieldPanel.setFields(createFieldDefs());
            markDirty();
        }
    }

    // === Selection Callbacks ===

    public void onOptionDialogSelected(int optionSlot, int dialogId, String dialogName) {
        if (dialog == null) return;
        DialogOption opt = dialog.options.get(optionSlot);
        if (opt != null) {
            opt.dialogId = dialogId;
            fieldPanel.refresh();
            markDirty();
        }
    }

    public void onColorSelected(int slot, int color) {
        if (dialog == null) return;
        int optionSlot = slot - 100;
        DialogOption opt = dialog.options.get(optionSlot);
        if (opt != null) {
            opt.optionColor = color;
            markDirty();
            // Color buttons sync from data on next draw
        }
    }
}
