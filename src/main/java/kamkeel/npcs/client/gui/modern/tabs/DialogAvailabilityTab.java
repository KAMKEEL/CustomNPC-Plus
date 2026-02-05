package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.*;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Availability tab for the dialog editor.
 * Contains quest, dialog, faction requirements and time/level settings.
 */
public class DialogAvailabilityTab extends DialogEditorTab {

    private int componentGap = 3;

    // Quest Requirements Section
    private CollapsibleSection questReqSection;
    private List<AvailabilityRow> questReqRows = new ArrayList<>();

    // Dialog Requirements Section
    private CollapsibleSection dialogReqSection;
    private List<AvailabilityRow> dialogReqRows = new ArrayList<>();

    // Faction Requirements Section
    private CollapsibleSection factionReqSection;
    private List<FactionRow> factionRows = new ArrayList<>();

    // Time & Level Section
    private CollapsibleSection timeSection;
    private ModernDropdown dayTimeDropdown;
    private ModernNumberField minLevelField;

    public DialogAvailabilityTab(DialogEditorPanel parent) {
        super(parent);
        initComponents();
    }

    private void initComponents() {
        // Quest Requirements
        questReqSection = new CollapsibleSection(500, "Quest Requirements");
        for (int i = 0; i < 4; i++) {
            questReqRows.add(new AvailabilityRow(510 + i * 10, i, "Quest",
                Arrays.asList("Always", "After", "Before", "Active", "Not Active", "Acceptable", "Not Acceptable")));
        }

        // Dialog Requirements
        dialogReqSection = new CollapsibleSection(550, "Dialog Requirements", false);
        for (int i = 0; i < 4; i++) {
            dialogReqRows.add(new AvailabilityRow(560 + i * 10, i, "Dialog",
                Arrays.asList("Always", "After", "Before")));
        }

        // Faction Requirements
        factionReqSection = new CollapsibleSection(600, "Faction Requirements", false);
        for (int i = 0; i < 2; i++) {
            factionRows.add(new FactionRow(610 + i * 10, i));
        }

        // Time & Level
        timeSection = new CollapsibleSection(650, "Time & Level", false);
        dayTimeDropdown = new ModernDropdown(651, 0, 0, 80, 16);
        dayTimeDropdown.setOptions(Arrays.asList("Always", "Night", "Day"));
        minLevelField = new ModernNumberField(652, 0, 0, 50, 16, 0);
        minLevelField.setIntegerBounds(0, 1000, 0);
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        if (dialog == null || dialog.availability == null) return;

        loadAvailability(dialog.availability);
    }

    private void loadAvailability(Availability avail) {
        // Quest requirements
        questReqRows.get(0).setData(avail.questAvailable.ordinal(), avail.questId);
        questReqRows.get(1).setData(avail.quest2Available.ordinal(), avail.quest2Id);
        questReqRows.get(2).setData(avail.quest3Available.ordinal(), avail.quest3Id);
        questReqRows.get(3).setData(avail.quest4Available.ordinal(), avail.quest4Id);

        // Dialog requirements
        dialogReqRows.get(0).setData(avail.dialogAvailable.ordinal(), avail.dialogId);
        dialogReqRows.get(1).setData(avail.dialog2Available.ordinal(), avail.dialog2Id);
        dialogReqRows.get(2).setData(avail.dialog3Available.ordinal(), avail.dialog3Id);
        dialogReqRows.get(3).setData(avail.dialog4Available.ordinal(), avail.dialog4Id);

        // Faction requirements
        factionRows.get(0).setData(avail.factionAvailable.ordinal(), avail.factionStance.ordinal(), avail.factionId);
        factionRows.get(1).setData(avail.faction2Available.ordinal(), avail.faction2Stance.ordinal(), avail.faction2Id);

        // Time & level
        dayTimeDropdown.setSelectedIndex(avail.daytime.ordinal());
        minLevelField.setInteger(avail.minPlayerLevel);
    }

    @Override
    public void saveToDialog(Dialog dialog) {
        if (dialog == null || dialog.availability == null) return;

        saveAvailability(dialog.availability);
    }

    private void saveAvailability(Availability avail) {
        // Quest requirements
        avail.questAvailable = EnumAvailabilityQuest.values()[questReqRows.get(0).getCondition()];
        avail.questId = questReqRows.get(0).getSelectedId();
        avail.quest2Available = EnumAvailabilityQuest.values()[questReqRows.get(1).getCondition()];
        avail.quest2Id = questReqRows.get(1).getSelectedId();
        avail.quest3Available = EnumAvailabilityQuest.values()[questReqRows.get(2).getCondition()];
        avail.quest3Id = questReqRows.get(2).getSelectedId();
        avail.quest4Available = EnumAvailabilityQuest.values()[questReqRows.get(3).getCondition()];
        avail.quest4Id = questReqRows.get(3).getSelectedId();

        // Dialog requirements
        avail.dialogAvailable = EnumAvailabilityDialog.values()[dialogReqRows.get(0).getCondition()];
        avail.dialogId = dialogReqRows.get(0).getSelectedId();
        avail.dialog2Available = EnumAvailabilityDialog.values()[dialogReqRows.get(1).getCondition()];
        avail.dialog2Id = dialogReqRows.get(1).getSelectedId();
        avail.dialog3Available = EnumAvailabilityDialog.values()[dialogReqRows.get(2).getCondition()];
        avail.dialog3Id = dialogReqRows.get(2).getSelectedId();
        avail.dialog4Available = EnumAvailabilityDialog.values()[dialogReqRows.get(3).getCondition()];
        avail.dialog4Id = dialogReqRows.get(3).getSelectedId();

        // Faction requirements
        avail.factionAvailable = EnumAvailabilityFactionType.values()[factionRows.get(0).getCondition()];
        avail.factionStance = noppes.npcs.constants.EnumAvailabilityFaction.values()[factionRows.get(0).getStance()];
        avail.factionId = factionRows.get(0).getSelectedId();
        avail.faction2Available = EnumAvailabilityFactionType.values()[factionRows.get(1).getCondition()];
        avail.faction2Stance = noppes.npcs.constants.EnumAvailabilityFaction.values()[factionRows.get(1).getStance()];
        avail.faction2Id = factionRows.get(1).getSelectedId();

        // Time & level
        avail.daytime = EnumDayTime.values()[dayTimeDropdown.getSelectedIndex()];
        avail.minPlayerLevel = minLevelField.getInteger();
    }

    @Override
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        int y = startY;
        int rowH = 20;

        // Quest Requirements
        questReqSection.setPosition(cx, y, cw);
        questReqSection.setContentHeight(rowH * 4 + 8);
        if (questReqSection.draw(mouseX, mouseY)) {
            int dy = questReqSection.getContentY();
            for (AvailabilityRow row : questReqRows) {
                row.setBounds(cx + 4, dy, cw - 8);
                row.draw(mouseX, mouseY, fr);
                dy += rowH;
            }
        }
        y += questReqSection.getTotalHeight() + componentGap;

        // Dialog Requirements
        dialogReqSection.setPosition(cx, y, cw);
        dialogReqSection.setContentHeight(rowH * 4 + 8);
        if (dialogReqSection.draw(mouseX, mouseY)) {
            int dy = dialogReqSection.getContentY();
            for (AvailabilityRow row : dialogReqRows) {
                row.setBounds(cx + 4, dy, cw - 8);
                row.draw(mouseX, mouseY, fr);
                dy += rowH;
            }
        }
        y += dialogReqSection.getTotalHeight() + componentGap;

        // Faction Requirements
        factionReqSection.setPosition(cx, y, cw);
        factionReqSection.setContentHeight(rowH * 2 + 8);
        if (factionReqSection.draw(mouseX, mouseY)) {
            int dy = factionReqSection.getContentY();
            for (FactionRow row : factionRows) {
                row.setBounds(cx + 4, dy, cw - 8);
                row.draw(mouseX, mouseY, fr);
                dy += rowH;
            }
        }
        y += factionReqSection.getTotalHeight() + componentGap;

        // Time & Level
        timeSection.setPosition(cx, y, cw);
        timeSection.setContentHeight(rowH + 8);
        if (timeSection.draw(mouseX, mouseY)) {
            int dy = timeSection.getContentY();
            fr.drawString("Time:", cx + 4, dy + 4, ModernColors.TEXT_LIGHT);
            dayTimeDropdown.setBounds(cx + 35, dy, 70, 16);
            dayTimeDropdown.drawBase(mouseX, mouseY);

            fr.drawString("Min Lvl:", cx + 115, dy + 4, ModernColors.TEXT_LIGHT);
            minLevelField.setBounds(cx + 160, dy, 40, 16);
            minLevelField.draw(mouseX, mouseY);
        }
        y += timeSection.getTotalHeight() + componentGap;

        return y - startY;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (questReqSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (questReqSection.isExpanded()) {
            for (AvailabilityRow row : questReqRows) {
                if (row.mouseClicked(mouseX, mouseY, button, parent, "quest")) {
                    markDirty();
                    return true;
                }
            }
        }

        if (dialogReqSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (dialogReqSection.isExpanded()) {
            for (AvailabilityRow row : dialogReqRows) {
                if (row.mouseClicked(mouseX, mouseY, button, parent, "dialog")) {
                    markDirty();
                    return true;
                }
            }
        }

        if (factionReqSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (factionReqSection.isExpanded()) {
            for (FactionRow row : factionRows) {
                if (row.mouseClicked(mouseX, mouseY, button, parent)) {
                    markDirty();
                    return true;
                }
            }
        }

        if (timeSection.mouseClicked(mouseX, mouseY, button)) return true;
        if (timeSection.isExpanded()) {
            // Only mark dirty if dropdown selection actually changes
            int prevIndex = dayTimeDropdown.getSelectedIndex();
            if (dayTimeDropdown.mouseClicked(mouseX, mouseY, button)) {
                if (dayTimeDropdown.getSelectedIndex() != prevIndex) {
                    markDirty();
                }
                return true;
            }
            // Don't mark dirty on field click - only on content change
            if (minLevelField.handleClick(mouseX, mouseY, button)) { return true; }
        }

        return false;
    }

    @Override
    public boolean keyTyped(char c, int keyCode) {
        if (minLevelField.keyTyped(c, keyCode)) { markDirty(); return true; }
        return false;
    }

    @Override
    public void updateScreen() {
        minLevelField.updateCursorCounter();
    }

    @Override
    public List<ModernDropdown> getDropdowns() {
        List<ModernDropdown> dropdowns = new ArrayList<>();
        dropdowns.add(dayTimeDropdown);
        for (AvailabilityRow row : questReqRows) dropdowns.add(row.getConditionDropdown());
        for (AvailabilityRow row : dialogReqRows) dropdowns.add(row.getConditionDropdown());
        for (FactionRow row : factionRows) {
            dropdowns.add(row.getConditionDropdown());
            dropdowns.add(row.getStanceDropdown());
        }
        return dropdowns;
    }

    // === Selection Callbacks ===

    public void onQuestSelected(int slot, int questId, String questName) {
        // slot 1-4 are availability quest slots
        int reqSlot = slot - 1;
        if (reqSlot >= 0 && reqSlot < questReqRows.size()) {
            questReqRows.get(reqSlot).setSelected(questId, questName);
        }
        markDirty();
    }

    public void onDialogSelected(int slot, int dialogId, String dialogName) {
        if (slot >= 0 && slot < dialogReqRows.size()) {
            dialogReqRows.get(slot).setSelected(dialogId, dialogName);
        }
        markDirty();
    }

    public void onFactionSelected(int slot, int factionId, String factionName) {
        if (slot >= 0 && slot < factionRows.size()) {
            factionRows.get(slot).setSelected(factionId, factionName);
        }
        markDirty();
    }
}
