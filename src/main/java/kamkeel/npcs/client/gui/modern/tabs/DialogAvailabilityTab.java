package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.ModernFieldPanel;
import noppes.npcs.client.gui.builder.ModernFieldPanelListener;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Dialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Availability tab for the dialog editor.
 * Contains quest, dialog, faction requirements and time/level settings.
 * Uses ModernFieldPanel with custom AVAILABILITY_ROW and FACTION_ROW field types.
 */
public class DialogAvailabilityTab extends DialogEditorTab {

    private static final String[] QUEST_CONDITIONS = {"Always", "After", "Before", "Active", "Not Active", "Acceptable", "Not Acceptable"};
    private static final String[] DIALOG_CONDITIONS = {"Always", "After", "Before"};

    private ModernFieldPanel fieldPanel;

    public DialogAvailabilityTab(DialogEditorPanel parent) {
        super(parent);
        fieldPanel = new ModernFieldPanel();
        fieldPanel.setListener(new ModernFieldPanelListener() {
            @Override
            public void onSelectAction(String action, int slot) {
                IDialogEditorListener listener = parent.getListener();
                if (listener == null) return;
                switch (action) {
                    case "quest":
                        listener.onQuestSelectRequested(slot);
                        break;
                    case "dialog":
                        listener.onDialogSelectRequested(slot);
                        break;
                    case "faction":
                        listener.onFactionSelectRequested(slot);
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
        Availability a = dialog.availability;
        List<FieldDef> defs = new ArrayList<>();

        // Quest Requirements (expanded by default)
        defs.add(FieldDef.section("Quest Requirements").collapsed(true));
        defs.add(FieldDef.availabilityRow("", QUEST_CONDITIONS,
            () -> a.questAvailable.ordinal(), v -> a.questAvailable = EnumAvailabilityQuest.values()[v],
            () -> a.questId, v -> a.questId = v,
            () -> a.questId >= 0 ? "Quest #" + a.questId : "")
            .action("quest").slot(1).clearable(() -> a.questId = -1));
        defs.add(FieldDef.availabilityRow("", QUEST_CONDITIONS,
            () -> a.quest2Available.ordinal(), v -> a.quest2Available = EnumAvailabilityQuest.values()[v],
            () -> a.quest2Id, v -> a.quest2Id = v,
            () -> a.quest2Id >= 0 ? "Quest #" + a.quest2Id : "")
            .action("quest").slot(2).clearable(() -> a.quest2Id = -1));
        defs.add(FieldDef.availabilityRow("", QUEST_CONDITIONS,
            () -> a.quest3Available.ordinal(), v -> a.quest3Available = EnumAvailabilityQuest.values()[v],
            () -> a.quest3Id, v -> a.quest3Id = v,
            () -> a.quest3Id >= 0 ? "Quest #" + a.quest3Id : "")
            .action("quest").slot(3).clearable(() -> a.quest3Id = -1));
        defs.add(FieldDef.availabilityRow("", QUEST_CONDITIONS,
            () -> a.quest4Available.ordinal(), v -> a.quest4Available = EnumAvailabilityQuest.values()[v],
            () -> a.quest4Id, v -> a.quest4Id = v,
            () -> a.quest4Id >= 0 ? "Quest #" + a.quest4Id : "")
            .action("quest").slot(4).clearable(() -> a.quest4Id = -1));

        // Dialog Requirements
        defs.add(FieldDef.section("Dialog Requirements").collapsed(true));
        defs.add(FieldDef.availabilityRow("", DIALOG_CONDITIONS,
            () -> a.dialogAvailable.ordinal(), v -> a.dialogAvailable = EnumAvailabilityDialog.values()[v],
            () -> a.dialogId, v -> a.dialogId = v,
            () -> a.dialogId >= 0 ? "Dialog #" + a.dialogId : "")
            .action("dialog").slot(0).clearable(() -> a.dialogId = -1));
        defs.add(FieldDef.availabilityRow("", DIALOG_CONDITIONS,
            () -> a.dialog2Available.ordinal(), v -> a.dialog2Available = EnumAvailabilityDialog.values()[v],
            () -> a.dialog2Id, v -> a.dialog2Id = v,
            () -> a.dialog2Id >= 0 ? "Dialog #" + a.dialog2Id : "")
            .action("dialog").slot(1).clearable(() -> a.dialog2Id = -1));
        defs.add(FieldDef.availabilityRow("", DIALOG_CONDITIONS,
            () -> a.dialog3Available.ordinal(), v -> a.dialog3Available = EnumAvailabilityDialog.values()[v],
            () -> a.dialog3Id, v -> a.dialog3Id = v,
            () -> a.dialog3Id >= 0 ? "Dialog #" + a.dialog3Id : "")
            .action("dialog").slot(2).clearable(() -> a.dialog3Id = -1));
        defs.add(FieldDef.availabilityRow("", DIALOG_CONDITIONS,
            () -> a.dialog4Available.ordinal(), v -> a.dialog4Available = EnumAvailabilityDialog.values()[v],
            () -> a.dialog4Id, v -> a.dialog4Id = v,
            () -> a.dialog4Id >= 0 ? "Dialog #" + a.dialog4Id : "")
            .action("dialog").slot(3).clearable(() -> a.dialog4Id = -1));

        // Faction Requirements
        defs.add(FieldDef.section("Faction Requirements").collapsed(true));
        defs.add(FieldDef.factionRow("",
            () -> a.factionAvailable.ordinal(), v -> a.factionAvailable = EnumAvailabilityFactionType.values()[v],
            () -> a.factionStance.ordinal(), v -> a.factionStance = EnumAvailabilityFaction.values()[v],
            () -> a.factionId, v -> a.factionId = v,
            () -> a.factionId >= 0 ? "Faction #" + a.factionId : "")
            .action("faction").slot(0).clearable(() -> a.factionId = -1));
        defs.add(FieldDef.factionRow("",
            () -> a.faction2Available.ordinal(), v -> a.faction2Available = EnumAvailabilityFactionType.values()[v],
            () -> a.faction2Stance.ordinal(), v -> a.faction2Stance = EnumAvailabilityFaction.values()[v],
            () -> a.faction2Id, v -> a.faction2Id = v,
            () -> a.faction2Id >= 0 ? "Faction #" + a.faction2Id : "")
            .action("faction").slot(1).clearable(() -> a.faction2Id = -1));

        // Time & Level
        defs.add(FieldDef.section("Time & Level").collapsed(true));
        defs.add(FieldDef.row(
            FieldDef.stringEnumField("Time", new String[]{"Always", "Night", "Day"},
                () -> new String[]{"Always", "Night", "Day"}[a.daytime.ordinal()],
                v -> {
                    if ("Night".equals(v)) a.daytime = EnumDayTime.Night;
                    else if ("Day".equals(v)) a.daytime = EnumDayTime.Day;
                    else a.daytime = EnumDayTime.Always;
                }),
            FieldDef.intField("Min Lvl", () -> a.minPlayerLevel, v -> a.minPlayerLevel = v)
                .range(0, 1000)
        ));

        return defs;
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null || dialog.availability == null) return;
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

    public void onQuestSelected(int slot, int questId, String questName) {
        if (dialog == null || dialog.availability == null) return;
        Availability a = dialog.availability;
        // slot 1-4 are availability quest slots
        switch (slot) {
            case 1: a.questId = questId; break;
            case 2: a.quest2Id = questId; break;
            case 3: a.quest3Id = questId; break;
            case 4: a.quest4Id = questId; break;
        }
        markDirty();
        // Display syncs from getter on next draw
    }

    public void onDialogSelected(int slot, int dialogId, String dialogName) {
        if (dialog == null || dialog.availability == null) return;
        Availability a = dialog.availability;
        switch (slot) {
            case 0: a.dialogId = dialogId; break;
            case 1: a.dialog2Id = dialogId; break;
            case 2: a.dialog3Id = dialogId; break;
            case 3: a.dialog4Id = dialogId; break;
        }
        markDirty();
    }

    public void onFactionSelected(int slot, int factionId, String factionName) {
        if (dialog == null || dialog.availability == null) return;
        Availability a = dialog.availability;
        switch (slot) {
            case 0: a.factionId = factionId; break;
            case 1: a.faction2Id = factionId; break;
        }
        markDirty();
    }
}
