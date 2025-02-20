package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataGetPacket;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataRemovePacket;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SubGuiPlayerData extends SubGuiInterface implements GuiYesNoCallback, ICustomScrollListener, IScrollData {

    private String playerName;
    private int currentTab = 10; // 10: Quests, 11: Dialog, 12: Transport, 13: Bank, 14: Faction.
    private int viewMode = 0; // 0: Categorical, 1: Compact.
    private int lastDataType = 0;

    // ----- Quest Tab (10) -----
    private GuiCustomScroll questCatScroll, questFinishedScroll, questActiveScroll;
    private String questCatSearch = "", questFinishedSearch = "", questActiveSearch = "";
    // Maps for category display (questCatData) and for quest entries:
    private HashMap<String, Integer> questCatData = new HashMap<>();
    private HashMap<String, Integer> questFinishedData = new HashMap<>();
    private HashMap<String, Integer> questActiveData = new HashMap<>();
    // New: maps storing the extracted category for each quest entry (keyed by the displayed quest title)
    private HashMap<String, String> questFinishedCategory = new HashMap<>();
    private HashMap<String, String> questActiveCategory = new HashMap<>();
    private String selectedQuestCategory = "";

    // ----- Dialog Tab (11) -----
    private GuiCustomScroll dialogCatScroll, dialogReadScroll;
    private String dialogCatSearch = "", dialogReadSearch = "";
    private HashMap<String, Integer> dialogCatData = new HashMap<>();
    private HashMap<String, Integer> dialogReadData = new HashMap<>();
    // For dialog entries, we split off the category from the key ("Category: Title")
    private HashMap<String, String> dialogCategory = new HashMap<>();
    private GuiCustomScroll dialogCompactScroll;
    private String dialogCompactSearch = "";
    private HashMap<String, Integer> dialogCompactData = new HashMap<>();
    private String selectedDialogCategory = "";

    // ----- Transport Tab (12) -----
    private GuiCustomScroll transCatScroll, transLocScroll;
    private String transCatSearch = "", transLocSearch = "";
    private HashMap<String, Integer> transCatData = new HashMap<>();
    private HashMap<String, Integer> transLocData = new HashMap<>();
    // For transport locations, also store their category:
    private HashMap<String, String> transLocationCategory = new HashMap<>();
    private GuiCustomScroll transCompactScroll;
    private String transCompactSearch = "";
    private HashMap<String, Integer> transCompactData = new HashMap<>();
    private String selectedTransCategory = "";

    // ----- Bank & Faction Tabs (13 & 14) -----
    private GuiCustomScroll singleScroll;
    private String singleSearch = "";
    private HashMap<String, Integer> singleData = new HashMap<>();

    // ----- Divider/resizing variables -----
// We use margins so that scrolls have room on the right for buttons.
    private final int leftMargin = guiLeft + 4;
    private final int rightMargin = guiLeft + xSize - 80; // leave 80px for the buttons on the right
    private int dividerOffset1 = 120;
    private int dividerOffset2 = 240;
    private final int dividerWidth = 5;
    private final int minScrollWidth = 50;
    private int draggingDivider = 0;
    private int initialDragX = 0;
    private boolean isResizing = false;

    public SubGuiPlayerData(String playerName) {
        this.playerName = playerName;
        xSize = 420;
        ySize = 216;
        setBackground("menubg.png");
        lastDataType = 11;
        PacketClient.sendClient(new PlayerDataGetPacket(EnumPlayerData.Quest, playerName));
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        // --- Top Buttons ---
        GuiMenuTopButton close = new GuiMenuTopButton(-5, guiLeft + xSize - 22, guiTop - 10, "X");
        GuiMenuTopButton quests = new GuiMenuTopButton(10, guiLeft + 4, guiTop - 10, StatCollector.translateToLocal("tab.quests"));
        GuiMenuTopButton dialog = new GuiMenuTopButton(11, quests.xPosition + quests.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.dialog"));
        GuiMenuTopButton transport = new GuiMenuTopButton(12, dialog.xPosition + dialog.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.transport"));
        GuiMenuTopButton bank = new GuiMenuTopButton(13, transport.xPosition + transport.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.bank"));
        GuiMenuTopButton faction = new GuiMenuTopButton(14, bank.xPosition + bank.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.faction"));

        quests.active = (currentTab == 10);
        dialog.active = (currentTab == 11);
        transport.active = (currentTab == 12);
        bank.active = (currentTab == 13);
        faction.active = (currentTab == 14);
        close.active = false;

        addTopButton(close);
        addTopButton(quests);
        addTopButton(dialog);
        addTopButton(transport);
        addTopButton(bank);
        addTopButton(faction);

        if (currentTab == 10 || currentTab == 11 || currentTab == 12) {
            addButton(new GuiNpcButton(20, guiLeft + xSize - 80, guiTop - 10, 75, 20,
                viewMode == 0 ? StatCollector.translateToLocal("view.categorical") : StatCollector.translateToLocal("view.compact")));
        }

        guiTop += 7;

        if (currentTab == 10)
            lastDataType = 11;
        else if (currentTab == 11)
            lastDataType = (viewMode == 0) ? 21 : 22;
        else if (currentTab == 12)
            lastDataType = (viewMode == 0) ? 31 : 32;
        else if (currentTab == 13 || currentTab == 14)
            lastDataType = 40;

        switch (currentTab) {
            case 10:
                initQuestTab();
                break;
            case 11:
                initDialogTab();
                break;
            case 12:
                initTransportTab();
                break;
            case 13:
            case 14:
                initSingleScroll();
                break;
        }

        addButton(new GuiNpcButton(30, guiLeft + xSize - 80, guiTop + ySize - 30, 75, 20, StatCollector.translateToLocal("button.delete")));
    }

    private void initQuestTab() {
        int regionLeft = leftMargin;
        int regionRight = rightMargin;
        int dividerX1 = regionLeft + dividerOffset1;
        int dividerX2 = regionLeft + dividerOffset2;

        if (viewMode == 0) {
            if (questCatScroll == null) {
                questCatScroll = new GuiCustomScroll(this, 0, 0);
            }
            questCatScroll.guiLeft = regionLeft;
            questCatScroll.guiTop = guiTop + 30;
            questCatScroll.setSize(dividerX1 - regionLeft, 150);
            addScroll(questCatScroll);
            addLabel(new GuiNpcLabel(1000, "Categories", regionLeft, guiTop + 15, 0xFFFFFF));

            if (questFinishedScroll == null) {
                questFinishedScroll = new GuiCustomScroll(this, 1, 0);
            }
            questFinishedScroll.guiLeft = dividerX1 + dividerWidth;
            questFinishedScroll.guiTop = guiTop + 30;
            questFinishedScroll.setSize(dividerX2 - dividerX1 - dividerWidth, 150);
            addScroll(questFinishedScroll);
            addLabel(new GuiNpcLabel(1001, "Finished", dividerX1 + dividerWidth, guiTop + 15, 0xFFFFFF));

            if (questActiveScroll == null) {
                questActiveScroll = new GuiCustomScroll(this, 2, 0);
            }
            questActiveScroll.guiLeft = dividerX2 + dividerWidth;
            questActiveScroll.guiTop = guiTop + 30;
            questActiveScroll.setSize(regionRight - dividerX2 - dividerWidth, 150);
            addScroll(questActiveScroll);
            addLabel(new GuiNpcLabel(1002, "Active", dividerX2 + dividerWidth, guiTop + 15, 0xFFFFFF));

            // When a category is selected, filter using our extra maps.
            if (!selectedQuestCategory.isEmpty()) {
                questFinishedScroll.setList(filterQuestsByCategory(questFinishedData, questFinishedCategory, selectedQuestCategory));
                questActiveScroll.setList(filterQuestsByCategory(questActiveData, questActiveCategory, selectedQuestCategory));
            } else {
                questFinishedScroll.setList(getSearchList(questFinishedData, questFinishedSearch));
                questActiveScroll.setList(getSearchList(questActiveData, questActiveSearch));
            }
        } else {
            if (questFinishedScroll == null) {
                questFinishedScroll = new GuiCustomScroll(this, 1, 0);
            }
            questFinishedScroll.guiLeft = regionLeft;
            questFinishedScroll.guiTop = guiTop + 30;
            questFinishedScroll.setSize((regionRight - regionLeft) / 2, 150);
            addScroll(questFinishedScroll);
            addLabel(new GuiNpcLabel(1003, "Finished", regionLeft, guiTop + 15, 0xFFFFFF));

            if (questActiveScroll == null) {
                questActiveScroll = new GuiCustomScroll(this, 2, 0);
            }
            questActiveScroll.guiLeft = regionLeft + (regionRight - regionLeft) / 2;
            questActiveScroll.guiTop = guiTop + 30;
            questActiveScroll.setSize((regionRight - regionLeft) / 2, 150);
            addScroll(questActiveScroll);
            addLabel(new GuiNpcLabel(1004, "Active", questActiveScroll.guiLeft, guiTop + 15, 0xFFFFFF));
        }
        if (viewMode == 0) {
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, guiTop + 30 + 153, dividerX1 - regionLeft, 20, questCatSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, dividerX1 + dividerWidth, guiTop + 30 + 153, dividerX2 - dividerX1 - dividerWidth, 20, questFinishedSearch));
            addTextField(new GuiNpcTextField(57, this, fontRendererObj, dividerX2 + dividerWidth, guiTop + 30 + 153, regionRight - dividerX2 - dividerWidth, 20, questActiveSearch));
        } else {
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, guiTop + 30 + 153, (regionRight - regionLeft) / 2, 20, questFinishedSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, questActiveScroll.guiLeft, guiTop + 30 + 153, (regionRight - regionLeft) / 2, 20, questActiveSearch));
        }
    }

    private void initDialogTab() {
        int regionLeft = leftMargin;
        int regionRight = rightMargin;
        int dividerX = regionLeft + dividerOffset1;

        if (viewMode == 0) {
            if (dialogCatScroll == null) {
                dialogCatScroll = new GuiCustomScroll(this, 0, 0);
            }
            dialogCatScroll.guiLeft = regionLeft;
            dialogCatScroll.guiTop = guiTop + 30;
            dialogCatScroll.setSize(dividerX - regionLeft, 150);
            addScroll(dialogCatScroll);
            addLabel(new GuiNpcLabel(2000, "Categories", regionLeft, guiTop + 15, 0xFFFFFF));

            if (dialogReadScroll == null) {
                dialogReadScroll = new GuiCustomScroll(this, 1, 0);
            }
            dialogReadScroll.guiLeft = dividerX + dividerWidth;
            dialogReadScroll.guiTop = guiTop + 30;
            dialogReadScroll.setSize(regionRight - dividerX - dividerWidth, 150);
            addScroll(dialogReadScroll);
            addLabel(new GuiNpcLabel(2001, "Read", dividerX + dividerWidth, guiTop + 15, 0xFFFFFF));

            if (!selectedDialogCategory.isEmpty()) {
                dialogReadScroll.setList(filterDialogsByCategory(dialogReadData, dialogCategory, selectedDialogCategory));
            } else {
                dialogReadScroll.setList(getSearchList(dialogReadData, dialogReadSearch));
            }
        } else {
            if (dialogCompactScroll == null) {
                dialogCompactScroll = new GuiCustomScroll(this, 5, 0);
            }
            dialogCompactScroll.guiLeft = regionLeft;
            dialogCompactScroll.guiTop = guiTop + 30;
            dialogCompactScroll.setSize(regionRight - regionLeft, 150);
            addScroll(dialogCompactScroll);
            addLabel(new GuiNpcLabel(2002, "All", regionLeft, guiTop + 15, 0xFFFFFF));
            dialogCompactScroll.setList(getSearchList(dialogCompactData, dialogCompactSearch));
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, guiTop + 30 + 153, regionRight - regionLeft, 20, dialogCompactSearch));
        }
        if (viewMode == 0) {
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, guiTop + 30 + 153, dividerX - regionLeft, 20, dialogCatSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, dividerX + dividerWidth, guiTop + 30 + 153, regionRight - dividerX - dividerWidth, 20, dialogReadSearch));
        }
    }

    private void initTransportTab() {
        int regionLeft = leftMargin;
        int regionRight = rightMargin;
        int dividerX = regionLeft + dividerOffset1;

        if (viewMode == 0) {
            if (transCatScroll == null) {
                transCatScroll = new GuiCustomScroll(this, 3, 0);
            }
            transCatScroll.guiLeft = regionLeft;
            transCatScroll.guiTop = guiTop + 30;
            transCatScroll.setSize(dividerX - regionLeft, 150);
            addScroll(transCatScroll);
            addLabel(new GuiNpcLabel(3000, "Categories", regionLeft, guiTop + 15, 0xFFFFFF));

            if (transLocScroll == null) {
                transLocScroll = new GuiCustomScroll(this, 4, 0);
            }
            transLocScroll.guiLeft = dividerX + dividerWidth;
            transLocScroll.guiTop = guiTop + 30;
            transLocScroll.setSize(regionRight - dividerX - dividerWidth, 150);
            addScroll(transLocScroll);
            addLabel(new GuiNpcLabel(3001, "Locations", dividerX + dividerWidth, guiTop + 15, 0xFFFFFF));

            if (!selectedTransCategory.isEmpty()) {
                transLocScroll.setList(filterTransportsByCategory(transLocData, transLocationCategory, selectedTransCategory));
            } else {
                transLocScroll.setList(getSearchList(transLocData, transLocSearch));
            }
        } else {
            if (transCompactScroll == null) {
                transCompactScroll = new GuiCustomScroll(this, 5, 0);
            }
            transCompactScroll.guiLeft = regionLeft;
            transCompactScroll.guiTop = guiTop + 30;
            transCompactScroll.setSize(regionRight - regionLeft, 150);
            addScroll(transCompactScroll);
            addLabel(new GuiNpcLabel(3002, "All", regionLeft, guiTop + 15, 0xFFFFFF));
            transCompactScroll.setList(getSearchList(transCompactData, transCompactSearch));
            addTextField(new GuiNpcTextField(60, this, fontRendererObj, regionLeft, guiTop + 30 + 153, regionRight - regionLeft, 20, transCompactSearch));
        }
        if (viewMode == 0) {
            addTextField(new GuiNpcTextField(60, this, fontRendererObj, regionLeft, guiTop + 30 + 153, dividerX - regionLeft, 20, transCatSearch));
            addTextField(new GuiNpcTextField(61, this, fontRendererObj, dividerX + dividerWidth, guiTop + 30 + 153, regionRight - dividerX - dividerWidth, 20, transLocSearch));
        }
    }

    private void initSingleScroll() {
        int regionLeft = leftMargin;
        int regionRight = rightMargin;
        if (singleScroll == null) {
            singleScroll = new GuiCustomScroll(this, 6, 0);
        }
        singleScroll.guiLeft = regionLeft;
        singleScroll.guiTop = guiTop + 30;
        singleScroll.setSize(regionRight - regionLeft, 150);
        addScroll(singleScroll);
        addLabel(new GuiNpcLabel(4000, "Data", regionLeft, guiTop + 15, 0xFFFFFF));
        addTextField(new GuiNpcTextField(70, this, fontRendererObj, regionLeft, guiTop + 30 + 153, regionRight - regionLeft, 20, singleSearch));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id >= 10 && id <= 14) {
            currentTab = id;
            clearData();
            selectedQuestCategory = "";
            selectedDialogCategory = "";
            selectedTransCategory = "";
            switch (currentTab) {
                case 10:
                    PacketClient.sendClient(new PlayerDataGetPacket(EnumPlayerData.Quest, playerName));
                    break;
                case 11:
                    PacketClient.sendClient(new PlayerDataGetPacket(EnumPlayerData.Dialog, playerName));
                    break;
                case 12:
                    PacketClient.sendClient(new PlayerDataGetPacket(EnumPlayerData.Transport, playerName));
                    break;
                case 13:
                    PacketClient.sendClient(new PlayerDataGetPacket(EnumPlayerData.Bank, playerName));
                    break;
                case 14:
                    PacketClient.sendClient(new PlayerDataGetPacket(EnumPlayerData.Factions, playerName));
                    break;
            }
            initGui();
            return;
        }
        if (id == 20) {
            viewMode = (viewMode == 0) ? 1 : 0;
            initGui();
            return;
        }
        if (id == 30) {
            displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("gui.deleteconfirm"), "", 100 + currentTab));
            return;
        }
    }

    private void clearData() {
        questCatData.clear();
        questFinishedData.clear();
        questActiveData.clear();
        questFinishedCategory.clear();
        questActiveCategory.clear();
        dialogCatData.clear();
        dialogReadData.clear();
        dialogCompactData.clear();
        dialogCategory.clear();
        transCatData.clear();
        transLocData.clear();
        transLocationCategory.clear();
        transCompactData.clear();
        singleData.clear();
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (currentTab == 10) {
            if (getTextField(55) != null && getTextField(55).isFocused()) {
                String newSearch = getTextField(55).getText().toLowerCase();
                if (!questCatSearch.equals(newSearch)) {
                    questCatSearch = newSearch;
                    if (questCatScroll != null)
                        questCatScroll.setList(getSearchList(questCatData, questCatSearch));
                }
            }
            if (getTextField(56) != null && getTextField(56).isFocused()) {
                String newSearch = getTextField(56).getText().toLowerCase();
                if (!questFinishedSearch.equals(newSearch)) {
                    questFinishedSearch = newSearch;
                    if (questFinishedScroll != null)
                        questFinishedScroll.setList(getSearchList(questFinishedData, questFinishedSearch));
                }
            }
            if (getTextField(57) != null && getTextField(57).isFocused()) {
                String newSearch = getTextField(57).getText().toLowerCase();
                if (!questActiveSearch.equals(newSearch)) {
                    questActiveSearch = newSearch;
                    if (questActiveScroll != null)
                        questActiveScroll.setList(getSearchList(questActiveData, questActiveSearch));
                }
            }
        }
        if (currentTab == 11) {
            if (viewMode == 0) {
                if (getTextField(55) != null && getTextField(55).isFocused()) {
                    String newSearch = getTextField(55).getText().toLowerCase();
                    if (!dialogCatSearch.equals(newSearch)) {
                        dialogCatSearch = newSearch;
                        if (dialogCatScroll != null)
                            dialogCatScroll.setList(getSearchList(dialogCatData, dialogCatSearch));
                    }
                }
                if (getTextField(56) != null && getTextField(56).isFocused()) {
                    String newSearch = getTextField(56).getText().toLowerCase();
                    if (!dialogReadSearch.equals(newSearch)) {
                        dialogReadSearch = newSearch;
                        if (dialogReadScroll != null)
                            dialogReadScroll.setList(getSearchList(dialogReadData, dialogReadSearch));
                    }
                }
            } else {
                if (getTextField(55) != null && getTextField(55).isFocused()) {
                    String newSearch = getTextField(55).getText().toLowerCase();
                    if (!dialogCompactSearch.equals(newSearch)) {
                        dialogCompactSearch = newSearch;
                        if (dialogCompactScroll != null)
                            dialogCompactScroll.setList(getSearchList(dialogCompactData, dialogCompactSearch));
                    }
                }
            }
        }
        if (currentTab == 12) {
            if (viewMode == 0) {
                if (getTextField(60) != null && getTextField(60).isFocused()) {
                    String newSearch = getTextField(60).getText().toLowerCase();
                    if (!transCatSearch.equals(newSearch)) {
                        transCatSearch = newSearch;
                        if (transCatScroll != null)
                            transCatScroll.setList(getSearchList(transCatData, transCatSearch));
                    }
                }
                if (getTextField(61) != null && getTextField(61).isFocused()) {
                    String newSearch = getTextField(61).getText().toLowerCase();
                    if (!transLocSearch.equals(newSearch)) {
                        transLocSearch = newSearch;
                        if (transLocScroll != null)
                            transLocScroll.setList(getSearchList(transLocData, transLocSearch));
                    }
                }
            } else {
                if (getTextField(60) != null && getTextField(60).isFocused()) {
                    String newSearch = getTextField(60).getText().toLowerCase();
                    if (!transCompactSearch.equals(newSearch)) {
                        transCompactSearch = newSearch;
                        if (transCompactScroll != null)
                            transCompactScroll.setList(getSearchList(transCompactData, transCompactSearch));
                    }
                }
            }
        }
        if (currentTab == 13 || currentTab == 14) {
            if (getTextField(70) != null && getTextField(70).isFocused()) {
                String newSearch = getTextField(70).getText().toLowerCase();
                if (!singleSearch.equals(newSearch)) {
                    singleSearch = newSearch;
                    if (singleScroll != null)
                        singleScroll.setList(getSearchList(singleData, singleSearch));
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if ((currentTab == 10 || currentTab == 11 || currentTab == 12) && viewMode == 0) {
            int regionLeft = leftMargin;
            if (currentTab == 10) {
                int dividerX1 = regionLeft + dividerOffset1;
                int dividerX2 = regionLeft + dividerOffset2;
                int regionTop = guiTop + 30;
                int regionHeight = 150;
                int dividerLineHeight = 20;
                int dividerLineYOffset = (regionHeight - dividerLineHeight) / 2;
                if (mouseX >= dividerX1 && mouseX <= dividerX1 + dividerWidth &&
                    mouseY >= regionTop + dividerLineYOffset && mouseY <= regionTop + dividerLineYOffset + dividerLineHeight) {
                    isResizing = true;
                    draggingDivider = 1;
                    initialDragX = mouseX;
                    return;
                }
                if (mouseX >= dividerX2 && mouseX <= dividerX2 + dividerWidth &&
                    mouseY >= regionTop + dividerLineYOffset && mouseY <= regionTop + dividerLineYOffset + dividerLineHeight) {
                    isResizing = true;
                    draggingDivider = 2;
                    initialDragX = mouseX;
                    return;
                }
            } else {
                int dividerX = regionLeft + dividerOffset1;
                int regionTop = guiTop + 30;
                int regionHeight = 150;
                int dividerLineHeight = 20;
                int dividerLineYOffset = (regionHeight - dividerLineHeight) / 2;
                if (mouseX >= dividerX && mouseX <= dividerX + dividerWidth &&
                    mouseY >= regionTop + dividerLineYOffset && mouseY <= regionTop + dividerLineYOffset + dividerLineHeight) {
                    isResizing = true;
                    draggingDivider = 1;
                    initialDragX = mouseX;
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isResizing) {
            int dx = mouseX - initialDragX;
            initialDragX = mouseX;
            if (currentTab == 10) {
                if (draggingDivider == 1) {
                    dividerOffset1 += dx;
                    int regionLeft = leftMargin;
                    if (dividerOffset1 < minScrollWidth)
                        dividerOffset1 = minScrollWidth;
                    if (dividerOffset1 > dividerOffset2 - dividerWidth - minScrollWidth)
                        dividerOffset1 = dividerOffset2 - dividerWidth - minScrollWidth;
                }
                if (draggingDivider == 2) {
                    dividerOffset2 += dx;
                    int regionLeft = leftMargin;
                    int regionRight = rightMargin;
                    if (dividerOffset2 < dividerOffset1 + dividerWidth + minScrollWidth)
                        dividerOffset2 = dividerOffset1 + dividerWidth + minScrollWidth;
                    if (dividerOffset2 > (regionRight - regionLeft) - minScrollWidth)
                        dividerOffset2 = (regionRight - regionLeft) - minScrollWidth;
                }
                int regionLeft = leftMargin;
                int regionRight = rightMargin;
                int dividerX1 = regionLeft + dividerOffset1;
                int dividerX2 = regionLeft + dividerOffset2;
                if (questCatScroll != null)
                    questCatScroll.setSize(dividerX1 - regionLeft, 150);
                if (questFinishedScroll != null) {
                    questFinishedScroll.guiLeft = dividerX1 + dividerWidth;
                    questFinishedScroll.setSize(dividerX2 - dividerX1 - dividerWidth, 150);
                }
                if (questActiveScroll != null) {
                    questActiveScroll.guiLeft = dividerX2 + dividerWidth;
                    questActiveScroll.setSize(regionRight - dividerX2 - dividerWidth, 150);
                }
                if (getTextField(55) != null)
                    getTextField(55).width = dividerX1 - regionLeft;
                if (getTextField(56) != null)
                    getTextField(56).width = dividerX2 - dividerX1 - dividerWidth;
                if (getTextField(57) != null)
                    getTextField(57).width = regionRight - dividerX2 - dividerWidth;
            } else if ((currentTab == 11 || currentTab == 12) && viewMode == 0) {
                int regionLeft = leftMargin;
                int regionRight = rightMargin;
                dividerOffset1 += dx;
                if (dividerOffset1 < minScrollWidth)
                    dividerOffset1 = minScrollWidth;
                if (dividerOffset1 > (regionRight - regionLeft) - dividerWidth - minScrollWidth)
                    dividerOffset1 = (regionRight - regionLeft) - dividerWidth - minScrollWidth;
                int dividerX = regionLeft + dividerOffset1;
                if (currentTab == 11) {
                    if (dialogCatScroll != null)
                        dialogCatScroll.setSize(dividerX - regionLeft, 150);
                    if (dialogReadScroll != null) {
                        dialogReadScroll.guiLeft = dividerX + dividerWidth;
                        dialogReadScroll.setSize(regionRight - dividerX - dividerWidth, 150);
                    }
                    if (getTextField(55) != null)
                        getTextField(55).width = dividerX - regionLeft;
                    if (getTextField(56) != null)
                        getTextField(56).width = regionRight - dividerX - dividerWidth;
                } else {
                    if (transCatScroll != null)
                        transCatScroll.setSize(dividerX - regionLeft, 150);
                    if (transLocScroll != null) {
                        transLocScroll.guiLeft = dividerX + dividerWidth;
                        transLocScroll.setSize(regionRight - dividerX - dividerWidth, 150);
                    }
                    if (getTextField(60) != null)
                        getTextField(60).width = dividerX - regionLeft;
                    if (getTextField(61) != null)
                        getTextField(61).width = regionRight - dividerX - dividerWidth;
                }
            }
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (isResizing) {
            isResizing = false;
            draggingDivider = 0;
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void confirmClicked(boolean confirm, int id) {
        if (confirm && id == 100 + currentTab) {
            int removeId = -1;
            String selected = null;
            switch (currentTab) {
                case 10:
                    if (questFinishedScroll != null && questFinishedScroll.getSelected() != null) {
                        selected = questFinishedScroll.getSelected();
                        removeId = questFinishedData.get(selected);
                    } else if (questActiveScroll != null && questActiveScroll.getSelected() != null) {
                        selected = questActiveScroll.getSelected();
                        removeId = questActiveData.get(selected);
                    }
                    if (removeId != -1)
                        PacketClient.sendClient(new PlayerDataRemovePacket(EnumPlayerData.Quest, playerName, removeId));
                    break;
                case 11:
                    if (viewMode == 0) {
                        if (dialogReadScroll != null && dialogReadScroll.getSelected() != null) {
                            selected = dialogReadScroll.getSelected();
                            removeId = dialogReadData.get(selected);
                        }
                    } else {
                        if (dialogCompactScroll != null && dialogCompactScroll.getSelected() != null) {
                            selected = dialogCompactScroll.getSelected();
                            removeId = dialogCompactData.get(selected);
                        }
                    }
                    if (removeId != -1)
                        PacketClient.sendClient(new PlayerDataRemovePacket(EnumPlayerData.Dialog, playerName, removeId));
                    break;
                case 12:
                    if (viewMode == 0) {
                        if (transLocScroll != null && transLocScroll.getSelected() != null) {
                            selected = transLocScroll.getSelected();
                            removeId = transLocData.get(selected);
                        }
                    } else {
                        if (transCompactScroll != null && transCompactScroll.getSelected() != null) {
                            selected = transCompactScroll.getSelected();
                            removeId = transCompactData.get(selected);
                        }
                    }
                    if (removeId != -1)
                        PacketClient.sendClient(new PlayerDataRemovePacket(EnumPlayerData.Transport, playerName, removeId));
                    break;
                case 13:
                case 14:
                    if (singleScroll != null && singleScroll.getSelected() != null) {
                        selected = singleScroll.getSelected();
                        removeId = singleData.get(selected);
                    }
                    if (removeId != -1) {
                        EnumPlayerData type = (currentTab == 13) ? EnumPlayerData.Bank : EnumPlayerData.Factions;
                        PacketClient.sendClient(new PlayerDataRemovePacket(type, playerName, removeId));
                    }
                    break;
                default:
                    break;
            }
        }
        NoppesUtil.openGUI(mc.thePlayer, this);
    }

    // In setData, for quests we now extract category and remove the suffix from the displayed quest title.
    public void setData(Vector<String> list, HashMap<String, Integer> data, int dataType) {
        lastDataType = dataType;
        if (currentTab == 10 && viewMode == 0 && (dataType == 11 || dataType == 12)) {
            if (dataType == 11) {
                questFinishedData.clear();
                questFinishedCategory.clear();
                for (String key : data.keySet()) {
                    int colon = key.indexOf(":");
                    int paren = key.lastIndexOf("(");
                    if (colon != -1 && paren != -1) {
                        String category = key.substring(0, colon).trim();
                        String questName = key.substring(colon + 1, paren).trim();
                        questFinishedData.put(questName, data.get(key));
                        questFinishedCategory.put(questName, category);
                        if (!questCatData.containsKey(category))
                            questCatData.put(category, -1);
                    } else {
                        questFinishedData.put(key, data.get(key));
                    }
                }
                if (questFinishedScroll != null)
                    questFinishedScroll.setList(getSearchList(questFinishedData, questFinishedSearch));
            } else if (dataType == 12) {
                questActiveData.clear();
                questActiveCategory.clear();
                for (String key : data.keySet()) {
                    int colon = key.indexOf(":");
                    int paren = key.lastIndexOf("(");
                    if (colon != -1 && paren != -1) {
                        String category = key.substring(0, colon).trim();
                        String questName = key.substring(colon + 1, paren).trim();
                        questActiveData.put(questName, data.get(key));
                        questActiveCategory.put(questName, category);
                        if (!questCatData.containsKey(category))
                            questCatData.put(category, -1);
                    } else {
                        questActiveData.put(key, data.get(key));
                    }
                }
                if (questActiveScroll != null)
                    questActiveScroll.setList(getSearchList(questActiveData, questActiveSearch));
            }
            if (questCatScroll != null)
                questCatScroll.setList(new ArrayList<>(questCatData.keySet()));
            return;
        }
        switch (dataType) {
            case 10:
                questCatData.clear();
                questCatData.putAll(data);
                if (questCatScroll != null)
                    questCatScroll.setList(getSearchList(questCatData, questCatSearch));
                break;
            case 20:
                dialogCatData.clear();
                dialogCatData.putAll(data);
                // For dialogs, split key into category and title:
                dialogCategory.clear();
                HashMap<String, Integer> temp = new HashMap<>();
                for (String key : data.keySet()) {
                    int colon = key.indexOf(":");
                    if (colon != -1) {
                        String cat = key.substring(0, colon).trim();
                        String title = key.substring(colon + 1).trim();
                        temp.put(title, data.get(key));
                        dialogCategory.put(title, cat);
                        if (!dialogCatData.containsKey(cat))
                            dialogCatData.put(cat, -1);
                    } else {
                        temp.put(key, data.get(key));
                    }
                }
                dialogCatData.putAll(temp);
                if (dialogCatScroll != null)
                    dialogCatScroll.setList(getSearchList(dialogCatData, dialogCatSearch));
                break;
            case 21:
                dialogReadData.clear();
                dialogReadData.putAll(data);
                if (dialogReadScroll != null)
                    dialogReadScroll.setList(getSearchList(dialogReadData, dialogReadSearch));
                break;
            case 22:
                dialogCompactData.clear();
                dialogCompactData.putAll(data);
                if (dialogCompactScroll != null)
                    dialogCompactScroll.setList(getSearchList(dialogCompactData, dialogCompactSearch));
                break;
            case 30:
                transCatData.clear();
                transCatData.putAll(data);
                if (transCatScroll != null)
                    transCatScroll.setList(getSearchList(transCatData, transCatSearch));
                break;
            case 31:
                transLocData.clear();
                transLocData.putAll(data);
                // For transports, split key into category and location name:
                transLocationCategory.clear();
                HashMap<String, Integer> temp2 = new HashMap<>();
                for (String key : data.keySet()) {
                    int colon = key.indexOf(":");
                    if (colon != -1) {
                        String cat = key.substring(0, colon).trim();
                        String loc = key.substring(colon + 1).trim();
                        temp2.put(loc, data.get(key));
                        transLocationCategory.put(loc, cat);
                        if (!transCatData.containsKey(cat))
                            transCatData.put(cat, -1);
                    } else {
                        temp2.put(key, data.get(key));
                    }
                }
                transLocData.putAll(temp2);
                if (transLocScroll != null)
                    transLocScroll.setList(getSearchList(transLocData, transLocSearch));
                break;
            case 32:
                transCompactData.clear();
                transCompactData.putAll(data);
                if (transCompactScroll != null)
                    transCompactScroll.setList(getSearchList(transCompactData, transCompactSearch));
                break;
            case 40:
                singleData.clear();
                singleData.putAll(data);
                if (singleScroll != null)
                    singleScroll.setList(getSearchList(singleData, singleSearch));
                break;
            default:
                break;
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data) {
        setData(list, data, lastDataType);
    }

    @Override
    public void setSelected(String selected) {
        // Handled in customScrollClicked.
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (currentTab == 10) {
            if (scroll.id == 0) {
                selectedQuestCategory = scroll.getSelected();
                questFinishedScroll.setList(filterQuestsByCategory(questFinishedData, questFinishedCategory, selectedQuestCategory));
                questActiveScroll.setList(filterQuestsByCategory(questActiveData, questActiveCategory, selectedQuestCategory));
            } else if (scroll.id == 1) {
                lastDataType = 11;
                if (questActiveScroll != null)
                    questActiveScroll.setSelected(null);
            } else if (scroll.id == 2) {
                lastDataType = 12;
                if (questFinishedScroll != null)
                    questFinishedScroll.setSelected(null);
            }
        } else if (currentTab == 11) {
            if (viewMode == 0) {
                if (scroll.id == 0) {
                    selectedDialogCategory = scroll.getSelected();
                    dialogReadScroll.setList(filterDialogsByCategory(dialogReadData, dialogCategory, selectedDialogCategory));
                } else if (scroll.id == 1) {
                    lastDataType = 21;
                    if (dialogCatScroll != null)
                        dialogCatScroll.setSelected(null);
                }
            } else {
                lastDataType = 22;
            }
        } else if (currentTab == 12) {
            if (viewMode == 0) {
                if (scroll.id == 3) {
                    selectedTransCategory = scroll.getSelected();
                    transLocScroll.setList(filterTransportsByCategory(transLocData, transLocationCategory, selectedTransCategory));
                } else if (scroll.id == 4) {
                    lastDataType = 31;
                    if (transCatScroll != null)
                        transCatScroll.setSelected(null);
                }
            } else {
                lastDataType = 32;
            }
        } else if (currentTab == 13 || currentTab == 14) {
            lastDataType = 40;
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        // Optional double-click behavior.
    }

    private List<String> getSearchList(HashMap<String, Integer> dataMap, String searchTerm) {
        if (searchTerm.isEmpty())
            return new ArrayList<>(dataMap.keySet());
        List<String> list = new ArrayList<>();
        for (String key : dataMap.keySet()) {
            if (key.toLowerCase().contains(searchTerm))
                list.add(key);
        }
        return list;
    }

    private List<String> filterQuestsByCategory(HashMap<String, Integer> questMap, HashMap<String, String> catMap, String category) {
        List<String> filtered = new ArrayList<>();
        for (String title : questMap.keySet()) {
            if (catMap.containsKey(title) && catMap.get(title).equalsIgnoreCase(category))
                filtered.add(title);
        }
        return filtered;
    }

    private List<String> filterDialogsByCategory(HashMap<String, Integer> dialogMap, HashMap<String, String> catMap, String category) {
        List<String> filtered = new ArrayList<>();
        for (String title : dialogMap.keySet()) {
            if (catMap.containsKey(title) && catMap.get(title).equalsIgnoreCase(category))
                filtered.add(title);
        }
        return filtered;
    }

    private List<String> filterTransportsByCategory(HashMap<String, Integer> transMap, HashMap<String, String> catMap, String category) {
        List<String> filtered = new ArrayList<>();
        for (String name : transMap.keySet()) {
            if (catMap.containsKey(name) && catMap.get(name).equalsIgnoreCase(category))
                filtered.add(name);
        }
        return filtered;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void close() {
        super.close();
    }
}
