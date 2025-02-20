package noppes.npcs.client.gui.test;

import kamkeel.npcs.network.PacketClient;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.constants.EnumPlayerData;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubGuiPlayerDataNew extends SubGuiInterface implements IPlayerDataInfo, ICustomScrollListener {

    // Tabs: 10 = Quest, 11 = Dialog, 12 = Transport, 13 = Bank, 14 = Faction.
    private int currentTab = 10;
    // View mode: 0 = Categorical (3 or 2 scrolls), 1 = Compact (2 or 1 scroll).
    private int viewMode = 0;

    // Quest Tab components:
    protected GuiCustomScroll questCatScroll, questActiveScroll, questFinishedScroll;
    private String questCatSearch = "", questActiveSearch = "", questFinishedSearch = "";
    private HashMap<String, Integer> questCatData = new HashMap<>();
    private HashMap<String, Integer> questActiveData = new HashMap<>();
    private HashMap<String, Integer> questFinishedData = new HashMap<>();
    private String selectedQuestCategory = "";

    // Dialog Tab components:
    protected GuiCustomScroll dialogCatScroll, dialogReadScroll, dialogCompactScroll;
    private String dialogCatSearch = "", dialogReadSearch = "", dialogCompactSearch = "";
    private HashMap<String, Integer> dialogCatData = new HashMap<>();
    private HashMap<String, Integer> dialogReadData = new HashMap<>();
    private String selectedDialogCategory = "";

    // Transport Tab components:
    protected GuiCustomScroll transCatScroll, transLocScroll, transCompactScroll;
    private String transCatSearch = "", transLocSearch = "", transCompactSearch = "";
    private HashMap<String, Integer> transCatData = new HashMap<>();
    private HashMap<String, Integer> transLocData = new HashMap<>();
    private String selectedTransCategory = "";

    // Bank and Faction tabs – separate data and one scroll:
    protected GuiCustomScroll singleScroll;
    private String singleSearch = "";
    private HashMap<String, Integer> bankData = new HashMap<>();
    private HashMap<String, Integer> factionData = new HashMap<>();

    // Divider/resizing variables for categorical mode.
    // For Quest tab: in categorical mode we use 2 dividers (3 scrolls)
    // In compact mode for Quests we use one divider between the two scrolls.
    private int dividerOffset1 = 0; // In categorical: width of left scroll; in compact for quests: width of left scroll.
    private int dividerOffset2 = 0; // Only for Quest categorical mode.
    private final int dividerWidth = 5;
    private final int minScrollWidth = 50;
    private boolean isResizing = false;
    private int resizingDivider = 0;
    private int initialDragX = 0;

    // Layout constants.
    private final int leftPadding = 6;
    private final int rightPadding = 6;
    private final int scrollTopOffset = 30;
    private final int verticalGapAboveTF = 3;
    private final int textFieldHeight = 20;
    private final int verticalGapBelowTF = 3;

    // The player name for which we are displaying data.
    private String playerName;

    public SubGuiPlayerDataNew(String playerName) {
        this.playerName = playerName;
        xSize = 420;
        ySize = 215;
        setBackground("menubg.png");
        closeOnEsc = true;
        PacketClient.sendClient(new PlayerDataGetPacketNew(playerName));
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
    }

    @Override
    public void initGui() {
        super.initGui();
        // Top tab buttons:
        GuiMenuTopButton btnQuest = new GuiMenuTopButton(10, guiLeft + 4, guiTop - 10, StatCollector.translateToLocal("tab.quests"));
        GuiMenuTopButton btnDialog = new GuiMenuTopButton(11, btnQuest.xPosition + btnQuest.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.dialog"));
        GuiMenuTopButton btnTransport = new GuiMenuTopButton(12, btnDialog.xPosition + btnDialog.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.transport"));
        GuiMenuTopButton btnBank = new GuiMenuTopButton(13, btnTransport.xPosition + btnTransport.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.bank"));
        GuiMenuTopButton btnFaction = new GuiMenuTopButton(14, btnBank.xPosition + btnBank.getWidth(), guiTop - 10, StatCollector.translateToLocal("tab.faction"));
        GuiMenuTopButton close = new GuiMenuTopButton(-5, guiLeft + xSize - 22, guiTop - 10, "X");

        btnQuest.active = (currentTab == 10);
        btnDialog.active = (currentTab == 11);
        btnTransport.active = (currentTab == 12);
        btnBank.active = (currentTab == 13);
        btnFaction.active = (currentTab == 14);
        addTopButton(btnQuest);
        addTopButton(btnDialog);
        addTopButton(btnTransport);
        addTopButton(btnBank);
        addTopButton(btnFaction);
        addTopButton(close);

        // Mode toggle button (50px wide)
        if (currentTab == 10 || currentTab == 11 || currentTab == 12) {
            String modeText = viewMode == 0 ? StatCollector.translateToLocal("view.categorical") : StatCollector.translateToLocal("view.compact");
            addButton(new GuiNpcButton(20, guiLeft + xSize - 60, guiTop + 10, 50, 20, modeText));
        }

        guiTop += 7;


        // Initialize components for the current tab:
        switch (currentTab) {
            case 10: initQuestTab(); break;
            case 11: initDialogTab(); break;
            case 12: initTransportTab(); break;
            case 13:
            case 14: initSingleTab(); break;
        }
        // Delete button (50px wide)
        addButton(new GuiNpcButton(30, guiLeft + xSize - 60, guiTop + ySize - 30, 50, 20, StatCollector.translateToLocal("button.delete")));
    }

    private int getScrollHeight() {
        return ySize - (scrollTopOffset + verticalGapAboveTF + textFieldHeight + verticalGapBelowTF);
    }

    private int getTextFieldY() {
        return guiTop + scrollTopOffset + getScrollHeight() + verticalGapAboveTF;
    }

    // Init Quest Tab.
    private void initQuestTab() {
        int regionLeft = guiLeft + leftPadding;
        int regionRight = guiLeft + xSize - rightPadding;
        int availableWidth = regionRight - regionLeft;
        int scrollHeight = getScrollHeight();

        if (viewMode == 0) { // Categorical: 3 scrolls.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - 2 * dividerWidth) / 3;
                dividerOffset2 = dividerOffset1 + dividerWidth + (availableWidth - 2 * dividerWidth) / 3;
            }
            if (questCatScroll == null) questCatScroll = new GuiCustomScroll(this, 0, 0);
            questCatScroll.guiLeft = regionLeft;
            questCatScroll.guiTop = guiTop + scrollTopOffset;
            questCatScroll.setSize(dividerOffset1, scrollHeight);
            questCatScroll.setList(new ArrayList<>(questCatData.keySet()));
            questCatScroll.selected = -1;
            questCatSearch = "";
            addScroll(questCatScroll);
            addLabel(new GuiNpcLabel(1000, "Categories", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            if (questFinishedScroll == null) questFinishedScroll = new GuiCustomScroll(this, 1, 0);
            questFinishedScroll.guiLeft = regionLeft + dividerOffset1 + dividerWidth;
            questFinishedScroll.guiTop = guiTop + scrollTopOffset;
            questFinishedScroll.setSize(dividerOffset2 - dividerOffset1 - dividerWidth, scrollHeight);
            questFinishedScroll.setList(new ArrayList<>());
            questFinishedScroll.selected = -1;
            questFinishedSearch = "";
            addScroll(questFinishedScroll);
            addLabel(new GuiNpcLabel(1001, "Finished", regionLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            if (questActiveScroll == null)
                questActiveScroll = new GuiCustomScroll(this, 2, 0);

            questActiveScroll.guiLeft = regionLeft + dividerOffset2 + dividerWidth;
            questActiveScroll.guiTop = guiTop + scrollTopOffset;
            questActiveScroll.setSize(regionRight - (regionLeft + dividerOffset2 + dividerWidth), scrollHeight);
            questActiveScroll.setList(new ArrayList<>());
            questActiveScroll.selected = -1;
            questActiveSearch = "";
            addScroll(questActiveScroll);
            addLabel(new GuiNpcLabel(1002, "Active", regionLeft + dividerOffset2 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, getTextFieldY(), dividerOffset1, textFieldHeight, questCatSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, regionLeft + dividerOffset1 + dividerWidth, getTextFieldY(), dividerOffset2 - dividerOffset1 - dividerWidth, textFieldHeight, questFinishedSearch));
            addTextField(new GuiNpcTextField(57, this, fontRendererObj, regionLeft + dividerOffset2 + dividerWidth, getTextFieldY(), regionRight - (regionLeft + dividerOffset2 + dividerWidth), textFieldHeight, questActiveSearch));
        } else { // Compact: 2 scrolls with resizable divider.
            // Default divider for compact mode.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - dividerWidth) / 2;
            }
            if (questFinishedScroll == null) questFinishedScroll = new GuiCustomScroll(this, 1, 0);
            questFinishedScroll.guiLeft = regionLeft;
            questFinishedScroll.guiTop = guiTop + scrollTopOffset;
            questFinishedScroll.setSize(dividerOffset1, scrollHeight);
            questFinishedScroll.setList(new ArrayList<>(questFinishedData.keySet()));
            questFinishedSearch = "";
            addScroll(questFinishedScroll);
            addLabel(new GuiNpcLabel(1003, "Finished", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            if (questActiveScroll == null) questActiveScroll = new GuiCustomScroll(this, 2, 0);
            questActiveScroll.guiLeft = regionLeft + dividerOffset1 + dividerWidth;
            questActiveScroll.guiTop = guiTop + scrollTopOffset;
            questActiveScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, scrollHeight);
            questActiveScroll.setList(new ArrayList<>(questActiveData.keySet()));
            questActiveSearch = "";
            addScroll(questActiveScroll);
            addLabel(new GuiNpcLabel(1004, "Active", regionLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, getTextFieldY(), dividerOffset1, textFieldHeight, questFinishedSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, regionLeft + dividerOffset1 + dividerWidth, getTextFieldY(), availableWidth - dividerOffset1 - dividerWidth, textFieldHeight, questActiveSearch));
        }
    }

    private void initDialogTab() {
        int regionLeft = guiLeft + leftPadding;
        int regionRight = guiLeft + xSize - rightPadding;
        int availableWidth = regionRight - regionLeft;
        int scrollHeight = getScrollHeight();
        if (viewMode == 0) { // Categorical: 2 scrolls.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - dividerWidth) / 2;
            }
            if (dialogCatScroll == null) dialogCatScroll = new GuiCustomScroll(this, 0, 0);
            dialogCatScroll.guiLeft = regionLeft;
            dialogCatScroll.guiTop = guiTop + scrollTopOffset;
            dialogCatScroll.setSize(dividerOffset1, scrollHeight);
            dialogCatScroll.setList(new ArrayList<>(dialogCatData.keySet()));
            addScroll(dialogCatScroll);
            addLabel(new GuiNpcLabel(2000, "Categories", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            if (dialogReadScroll == null) dialogReadScroll = new GuiCustomScroll(this, 1, 0);
            dialogReadScroll.guiLeft = regionLeft + dividerOffset1 + dividerWidth;
            dialogReadScroll.guiTop = guiTop + scrollTopOffset;
            dialogReadScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, scrollHeight);
            dialogReadScroll.setList(new ArrayList<>(dialogReadData.keySet()));
            addScroll(dialogReadScroll);
            addLabel(new GuiNpcLabel(2001, "Read", regionLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, getTextFieldY(), dividerOffset1, textFieldHeight, dialogCatSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, regionLeft + dividerOffset1 + dividerWidth, getTextFieldY(), availableWidth - dividerOffset1 - dividerWidth, textFieldHeight, dialogReadSearch));
        } else { // Compact: 1 scroll – no divider.
            if (dialogCompactScroll == null) dialogCompactScroll = new GuiCustomScroll(this, 5, 0);
            dialogCompactScroll.guiLeft = regionLeft;
            dialogCompactScroll.guiTop = guiTop + scrollTopOffset;
            dialogCompactScroll.setSize(availableWidth, scrollHeight);
            dialogCompactScroll.setList(new ArrayList<>(dialogReadData.keySet()));
            addScroll(dialogCompactScroll);
            addLabel(new GuiNpcLabel(2002, "All", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, getTextFieldY(), availableWidth, textFieldHeight, dialogCompactSearch));
        }
    }

    private void initTransportTab() {
        int regionLeft = guiLeft + leftPadding;
        int regionRight = guiLeft + xSize - rightPadding;
        int availableWidth = regionRight - regionLeft;
        int scrollHeight = getScrollHeight();
        if (viewMode == 0) { // Categorical: 2 scrolls.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - dividerWidth) / 2;
            }
            if (transCatScroll == null) transCatScroll = new GuiCustomScroll(this, 3, 0);
            transCatScroll.guiLeft = regionLeft;
            transCatScroll.guiTop = guiTop + scrollTopOffset;
            transCatScroll.setSize(dividerOffset1, scrollHeight);
            transCatScroll.setList(new ArrayList<>(transCatData.keySet()));
            addScroll(transCatScroll);
            addLabel(new GuiNpcLabel(3000, "Categories", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            if (transLocScroll == null) transLocScroll = new GuiCustomScroll(this, 4, 0);
            transLocScroll.guiLeft = regionLeft + dividerOffset1 + dividerWidth;
            transLocScroll.guiTop = guiTop + scrollTopOffset;
            transLocScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, scrollHeight);
            transLocScroll.setList(new ArrayList<>(transLocData.keySet()));
            addScroll(transLocScroll);
            addLabel(new GuiNpcLabel(3001, "Locations", regionLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(60, this, fontRendererObj, regionLeft, getTextFieldY(), dividerOffset1, textFieldHeight, transCatSearch));
            addTextField(new GuiNpcTextField(61, this, fontRendererObj, regionLeft + dividerOffset1 + dividerWidth, getTextFieldY(), availableWidth - dividerOffset1 - dividerWidth, textFieldHeight, transLocSearch));
        } else { // Compact: 1 scroll – no divider.
            if (transCompactScroll == null) transCompactScroll = new GuiCustomScroll(this, 5, 0);
            transCompactScroll.guiLeft = regionLeft;
            transCompactScroll.guiTop = guiTop + scrollTopOffset;
            transCompactScroll.setSize(availableWidth, scrollHeight);
            transCompactScroll.setList(new ArrayList<>(transLocData.keySet()));
            addScroll(transCompactScroll);
            addLabel(new GuiNpcLabel(3002, "All", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
            addTextField(new GuiNpcTextField(60, this, fontRendererObj, regionLeft, getTextFieldY(), availableWidth, textFieldHeight, transCompactSearch));
        }
    }

    private void initSingleTab() {
        int regionLeft = guiLeft + leftPadding;
        int regionRight = guiLeft + xSize - rightPadding;
        int availableWidth = regionRight - regionLeft;
        int scrollHeight = getScrollHeight();
        if (singleScroll == null) singleScroll = new GuiCustomScroll(this, 0, 0);
        singleScroll.guiLeft = regionLeft;
        singleScroll.guiTop = guiTop + scrollTopOffset;
        if (currentTab == 13) {
            singleScroll.setSize(availableWidth, scrollHeight);
            singleScroll.setList(new ArrayList<>(bankData.keySet()));
            addLabel(new GuiNpcLabel(4000, "Bank", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
        } else {
            singleScroll.setSize(availableWidth, scrollHeight);
            singleScroll.setList(new ArrayList<>(factionData.keySet()));
            addLabel(new GuiNpcLabel(4000, "Faction", regionLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
        }
        addScroll(singleScroll);
        addTextField(new GuiNpcTextField(70, this, fontRendererObj, regionLeft, getTextFieldY(), availableWidth, textFieldHeight, singleSearch));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int scrollHeight = getScrollHeight();
        int regionLeft = guiLeft + leftPadding;
        int regionRight = guiLeft + xSize - rightPadding;
        int availableWidth = regionRight - regionLeft;
        // In compact mode for Quests (2-scroll) draw divider.
        if (currentTab == 10 && viewMode != 0 && !hasSubGui()) {
            int dividerX = regionLeft + dividerOffset1;
            drawRect(dividerX, guiTop + scrollTopOffset, dividerX + dividerWidth, guiTop + scrollTopOffset + scrollHeight, 0xFF707070);
        }
        // In categorical mode, draw dividers.
        if (currentTab == 10 && viewMode == 0 && !hasSubGui()) {
            int dividerX1 = regionLeft + dividerOffset1;
            int dividerX2 = regionLeft + dividerOffset2;
            drawRect(dividerX1, guiTop + scrollTopOffset, dividerX1 + dividerWidth, guiTop + scrollTopOffset + scrollHeight, 0xFF707070);
            drawRect(dividerX2, guiTop + scrollTopOffset, dividerX2 + dividerWidth, guiTop + scrollTopOffset + scrollHeight, 0xFF707070);
        } else if ((currentTab == 11 || currentTab == 12) && viewMode == 0 && !hasSubGui()) {
            int dividerX = regionLeft + dividerOffset1;
            drawRect(dividerX, guiTop + scrollTopOffset, dividerX + dividerWidth, guiTop + scrollTopOffset + scrollHeight, 0xFF707070);
        }
        // Do not draw any divider for compact mode in Dialog/Transport.
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int hitMargin = 3;
        int regionTop = guiTop + scrollTopOffset;
        int regionBottom = regionTop + getScrollHeight();
        int regionLeft = guiLeft + leftPadding;
        int availableWidth = (guiLeft + xSize - rightPadding) - regionLeft;
        if (!hasSubGui() && mouseY >= regionTop && mouseY <= regionBottom) {
            if (viewMode == 0) {
                if (currentTab == 10) { // Categorical: 3 scrolls.
                    int dividerX1 = regionLeft + dividerOffset1;
                    int dividerX2 = regionLeft + dividerOffset2;
                    if (mouseX >= dividerX1 - hitMargin && mouseX <= dividerX1 + dividerWidth + hitMargin) {
                        isResizing = true;
                        resizingDivider = 1;
                        initialDragX = mouseX;
                        return;
                    } else if (mouseX >= dividerX2 - hitMargin && mouseX <= dividerX2 + dividerWidth + hitMargin) {
                        isResizing = true;
                        resizingDivider = 2;
                        initialDragX = mouseX;
                        return;
                    }
                } else if (currentTab == 11 || currentTab == 12) { // Categorical for Dialog/Transport.
                    int dividerX = regionLeft + dividerOffset1;
                    if (mouseX >= dividerX - hitMargin && mouseX <= dividerX + dividerWidth + hitMargin) {
                        isResizing = true;
                        resizingDivider = 1;
                        initialDragX = mouseX;
                        return;
                    }
                }
            } else { // Compact mode.
                if (currentTab == 10) { // Compact mode for Quests (2 scrolls)
                    // Use dividerOffset1 as the divider between the two scrolls.
                    int defaultWidth = (availableWidth - dividerWidth) / 2;
                    if (!isResizing) dividerOffset1 = defaultWidth;
                    int dividerX = regionLeft + dividerOffset1;
                    if (mouseX >= dividerX - hitMargin && mouseX <= dividerX + dividerWidth + hitMargin) {
                        isResizing = true;
                        resizingDivider = 1;
                        initialDragX = mouseX;
                        return;
                    }
                }
                // For Dialog/Transport compact, there's no divider.
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        int regionLeft = guiLeft + leftPadding;
        int regionRight = guiLeft + xSize - rightPadding;
        int availableWidth = regionRight - regionLeft;
        if (isResizing) {
            int dx = mouseX - initialDragX;
            initialDragX = mouseX;
            // For Quest tab:
            if (currentTab == 10) {
                if (viewMode == 0) { // Categorical (3 scrolls)
                    if (resizingDivider == 1) {
                        dividerOffset1 += dx;
                        int maxOffset = dividerOffset2 - dividerWidth - minScrollWidth;
                        if (dividerOffset1 < minScrollWidth) dividerOffset1 = minScrollWidth;
                        if (dividerOffset1 > maxOffset) dividerOffset1 = maxOffset;
                        questCatScroll.setSize(dividerOffset1, getScrollHeight());
                        questFinishedScroll.guiLeft = regionLeft + dividerOffset1 + dividerWidth;
                        questFinishedScroll.setSize(dividerOffset2 - dividerOffset1 - dividerWidth, getScrollHeight());
                        questActiveScroll.guiLeft = regionLeft + dividerOffset2 + dividerWidth;
                        questActiveScroll.setSize(availableWidth - (dividerOffset2 + dividerWidth), getScrollHeight());
                        // Update textfields and labels.
                        GuiNpcTextField tf55 = getTextField(55);
                        if(tf55 != null) { tf55.xPosition = regionLeft; tf55.width = dividerOffset1; }
                        GuiNpcTextField tf56 = getTextField(56);
                        if(tf56 != null) { tf56.xPosition = regionLeft + dividerOffset1 + dividerWidth; tf56.width = dividerOffset2 - dividerOffset1 - dividerWidth; }
                        GuiNpcTextField tf57 = getTextField(57);
                        if(tf57 != null) { tf57.xPosition = regionLeft + dividerOffset2 + dividerWidth; tf57.width = availableWidth - (dividerOffset2 + dividerWidth); }
                        GuiNpcLabel lbl1000 = getLabel(1000);
                        if(lbl1000 != null) { lbl1000.x = regionLeft; }
                        GuiNpcLabel lbl1001 = getLabel(1001);
                        if(lbl1001 != null) { lbl1001.x = regionLeft + dividerOffset1 + dividerWidth; }
                        GuiNpcLabel lbl1002 = getLabel(1002);
                        if(lbl1002 != null) { lbl1002.x = regionLeft + dividerOffset2 + dividerWidth; }
                    } else if (resizingDivider == 2) {
                        dividerOffset2 += dx;
                        int minOffset = dividerOffset1 + dividerWidth + minScrollWidth;
                        int maxOffset = availableWidth - minScrollWidth;
                        if (dividerOffset2 < minOffset) dividerOffset2 = minOffset;
                        if (dividerOffset2 > maxOffset) dividerOffset2 = maxOffset;
                        questFinishedScroll.setSize(dividerOffset2 - dividerOffset1 - dividerWidth, getScrollHeight());
                        questActiveScroll.guiLeft = regionLeft + dividerOffset2 + dividerWidth;
                        questActiveScroll.setSize(availableWidth - (dividerOffset2 + dividerWidth), getScrollHeight());
                        GuiNpcTextField tf56 = getTextField(56);
                        if(tf56 != null) { tf56.xPosition = regionLeft + dividerOffset1 + dividerWidth; tf56.width = dividerOffset2 - dividerOffset1 - dividerWidth; }
                        GuiNpcTextField tf57 = getTextField(57);
                        if(tf57 != null) { tf57.xPosition = regionLeft + dividerOffset2 + dividerWidth; tf57.width = availableWidth - (dividerOffset2 + dividerWidth); }
                        GuiNpcLabel lbl1001 = getLabel(1001);
                        if(lbl1001 != null) { lbl1001.x = regionLeft + dividerOffset1 + dividerWidth; }
                        GuiNpcLabel lbl1002 = getLabel(1002);
                        if(lbl1002 != null) { lbl1002.x = regionLeft + dividerOffset2 + dividerWidth; }
                    }
                } else { // Compact mode for Quests (2 scrolls)
                    if (resizingDivider == 1) {
                        dividerOffset1 += dx;
                        if (dividerOffset1 < minScrollWidth) dividerOffset1 = minScrollWidth;
                        if (dividerOffset1 > availableWidth - dividerWidth - minScrollWidth)
                            dividerOffset1 = availableWidth - dividerWidth - minScrollWidth;
                        questFinishedScroll.setSize(dividerOffset1, getScrollHeight());
                        questActiveScroll.guiLeft = regionLeft + dividerOffset1 + dividerWidth;
                        questActiveScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, getScrollHeight());
                        // Update textfields and labels.
                        GuiNpcTextField tf55 = getTextField(55);
                        if(tf55 != null) { tf55.xPosition = regionLeft; tf55.width = dividerOffset1; }
                        GuiNpcTextField tf56 = getTextField(56);
                        if(tf56 != null) { tf56.xPosition = regionLeft + dividerOffset1 + dividerWidth; tf56.width = availableWidth - dividerOffset1 - dividerWidth; }
                        GuiNpcLabel lbl1003 = getLabel(1003);
                        if(lbl1003 != null) { lbl1003.x = regionLeft; }
                        GuiNpcLabel lbl1004 = getLabel(1004);
                        if(lbl1004 != null) { lbl1004.x = regionLeft + dividerOffset1 + dividerWidth; }
                    }
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
            resizingDivider = 0;
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        GuiNpcTextField tf;
        if (currentTab == 10) {
            if (viewMode == 0) {
                tf = getTextField(55);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!questCatSearch.equals(newText)) {
                        questCatSearch = newText;
                        if (questCatScroll != null) {
                            if (selectedQuestCategory.isEmpty())
                                questCatScroll.setList(new ArrayList<>());
                            else
                                questCatScroll.setList(filterList(questCatData, questCatSearch));
                        }
                    }
                }
                tf = getTextField(56);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!questFinishedSearch.equals(newText)) {
                        questFinishedSearch = newText;
                        if (questFinishedScroll != null)
                            questFinishedScroll.setList(
                                selectedQuestCategory.isEmpty() ?
                                    new ArrayList<>() :
                                    filterAndTrimListByCategory(questFinishedData, selectedQuestCategory, questFinishedSearch)
                            );
                    }
                }
                tf = getTextField(57);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!questActiveSearch.equals(newText)) {
                        questActiveSearch = newText;
                        if (questActiveScroll != null)
                            questActiveScroll.setList(
                                selectedQuestCategory.isEmpty() ?
                                    new ArrayList<>() :
                                    filterAndTrimListByCategory(questActiveData, selectedQuestCategory, questActiveSearch)
                            );
                    }
                }
            } else {
                tf = getTextField(55);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!questFinishedSearch.equals(newText)) {
                        questFinishedSearch = newText;
                        if (questFinishedScroll != null)
                            questFinishedScroll.setList(filterList(questFinishedData, questFinishedSearch));
                    }
                }
                tf = getTextField(56);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!questActiveSearch.equals(newText)) {
                        questActiveSearch = newText;
                        if (questActiveScroll != null)
                            questActiveScroll.setList(filterList(questActiveData, questActiveSearch));
                    }
                }
            }
        } else if (currentTab == 11) {
            if (viewMode == 0) {
                tf = getTextField(55);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!dialogCatSearch.equals(newText)) {
                        dialogCatSearch = newText;
                        if (dialogCatScroll != null)
                            dialogCatScroll.setList(selectedDialogCategory.isEmpty() ?
                                new ArrayList<>() :
                                filterList(dialogCatData, dialogCatSearch));
                    }
                }
                tf = getTextField(56);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!dialogReadSearch.equals(newText)) {
                        dialogReadSearch = newText;
                        if (dialogReadScroll != null)
                            dialogReadScroll.setList(
                                selectedDialogCategory.isEmpty() ?
                                    new ArrayList<>() :
                                    filterAndTrimListByCategory(dialogReadData, selectedDialogCategory, dialogReadSearch)
                            );
                    }
                }
            } else {
                tf = getTextField(55);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!dialogCompactSearch.equals(newText)) {
                        dialogCompactSearch = newText;
                        if (dialogCompactScroll != null)
                            dialogCompactScroll.setList(filterList(dialogReadData, dialogCompactSearch));
                    }
                }
            }
        } else if (currentTab == 12) {
            if (viewMode == 0) {
                tf = getTextField(60);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!transCatSearch.equals(newText)) {
                        transCatSearch = newText;
                        if (transCatScroll != null)
                            transCatScroll.setList(selectedTransCategory.isEmpty() ?
                                new ArrayList<>() :
                                filterList(transCatData, transCatSearch));
                    }
                }
                tf = getTextField(61);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!transLocSearch.equals(newText)) {
                        transLocSearch = newText;
                        if (transLocScroll != null)
                            transLocScroll.setList(
                                selectedTransCategory.isEmpty() ?
                                    new ArrayList<>() :
                                    filterAndTrimListByCategory(transLocData, selectedTransCategory, transLocSearch)
                            );
                    }
                }
            } else {
                tf = getTextField(60);
                if (tf != null && tf.isFocused()) {
                    String newText = tf.getText().toLowerCase();
                    if (!transCompactSearch.equals(newText)) {
                        transCompactSearch = newText;
                        if (transCompactScroll != null)
                            transCompactScroll.setList(filterList(transLocData, transCompactSearch));
                    }
                }
            }
        } else if (currentTab == 13 || currentTab == 14) {
            tf = getTextField(70);
            if (tf != null && tf.isFocused()) {
                String newText = tf.getText().toLowerCase();
                if (!singleSearch.equals(newText)) {
                    singleSearch = newText;
                    if (singleScroll != null)
                        singleScroll.setList(filterList(currentTab == 13 ? bankData : factionData, singleSearch));
                }
            }
        }
    }

    private List<String> filterList(HashMap<String, Integer> data, String search) {
        List<String> list = new ArrayList<>();
        for (String key : data.keySet()) {
            if (key.toLowerCase().contains(search)) {
                list.add(key);
            }
        }
        return list;
    }

    private List<String> filterAndTrimListByCategory(HashMap<String, Integer> data, String category, String search) {
        List<String> list = new ArrayList<>();
        String prefix = category + ":";
        for (String key : data.keySet()) {
            if (key.startsWith(prefix)) {
                String trimmed = key.substring(prefix.length()).trim();
                if (trimmed.toLowerCase().contains(search)) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if(button.id == -5){
            close();
            return;
        }
        if (button.id >= 10 && button.id <= 14) {
            currentTab = button.id;
            initGui();
            return;
        }
        if (button.id == 20) {
            viewMode = (viewMode == 0) ? 1 : 0;
            initGui();
        } else if (button.id == 30) {
            Integer selectedID = null;
            EnumPlayerData tabType = null;
            if (currentTab == 10) {
                if(viewMode == 0){
                    if(questCatScroll.getSelected() == null)
                        return;
                    if (questActiveScroll.getSelected() != null) {
                        selectedID = questActiveData.get(questCatScroll.getSelected() + ": " + questActiveScroll.getSelected());
                        if(selectedID != null){
                            questActiveScroll.list.remove(questActiveScroll.selected);
                            questActiveData.remove(questCatScroll.getSelected() + ": " + questActiveScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Quest;
                    } else if (questFinishedScroll.getSelected() != null) {
                        selectedID = questFinishedData.get(questCatScroll.getSelected() + ": " + questFinishedScroll.getSelected());
                        if(selectedID != null){
                            questFinishedScroll.list.remove(questFinishedScroll.selected);
                            questFinishedData.remove(questCatScroll.getSelected() + ": " + questFinishedScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Quest;
                    }
                } else {
                    if (questActiveScroll.getSelected() != null) {
                        selectedID = questActiveData.get(questActiveScroll.getSelected());
                        if(selectedID != null){
                            questActiveScroll.list.remove(questActiveScroll.selected);
                            questActiveData.remove(questActiveScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Quest;
                    } else if (questFinishedScroll.getSelected() != null) {
                        selectedID = questFinishedData.get(questFinishedScroll.getSelected());
                        if(selectedID != null){
                            questFinishedScroll.list.remove(questFinishedScroll.selected);
                            questFinishedData.remove(questFinishedScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Quest;
                    }
                }
            } else if (currentTab == 11) {
                if (viewMode == 0) {
                    if (dialogReadScroll.getSelected() != null && dialogCatScroll.getSelected() != null) {
                        selectedID = dialogReadData.get(dialogCatScroll.getSelected() + ": " + dialogReadScroll.getSelected());
                        if(selectedID != null){
                            dialogReadScroll.list.remove(dialogReadScroll.selected);
                            dialogReadData.remove(dialogCatScroll.getSelected() + ": " + dialogReadScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Dialog;
                    }
                } else {
                    if (dialogCompactScroll.getSelected() != null) {
                        selectedID = dialogReadData.get(dialogCompactScroll.getSelected());
                        if(selectedID != null){
                            dialogCompactScroll.list.remove(dialogCompactScroll.selected);
                            dialogReadData.remove(dialogCompactScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Dialog;
                    }
                }
            } else if (currentTab == 12) {
                if (viewMode == 0) {
                    if (transLocScroll.getSelected() != null && transCatScroll.getSelected() != null) {
                        selectedID = transLocData.get(transCatScroll.getSelected() + ": " + transLocScroll.getSelected());
                        if(selectedID != null){
                            transLocScroll.list.remove(transLocScroll.selected);
                            transLocData.remove(transCatScroll.getSelected() + ": " + transLocScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Transport;
                    }
                } else {
                    if (transCompactScroll.getSelected() != null) {
                        selectedID = transLocData.get(transCompactScroll.getSelected());
                        if(selectedID != null){
                            transCompactScroll.list.remove(transCompactScroll.selected);
                            transLocData.remove(transCompactScroll.getSelected());
                        }
                        tabType = EnumPlayerData.Transport;
                    }
                }
            } else if (currentTab == 13) {
                if (singleScroll.getSelected() != null) {
                    selectedID = bankData.get(singleScroll.getSelected());
                    if(selectedID != null){
                        singleScroll.list.remove(singleScroll.selected);
                        bankData.remove(singleScroll.getSelected());
                    }
                    tabType = EnumPlayerData.Bank;
                }
            } else if (currentTab == 14) {
                if (singleScroll.getSelected() != null) {
                    selectedID = factionData.get(singleScroll.getSelected());
                    if(selectedID != null){
                        singleScroll.list.remove(singleScroll.selected);
                        bankData.remove(singleScroll.getSelected());
                    }
                    tabType = EnumPlayerData.Factions;
                }
            }
            if (selectedID != null) {
                PacketClient.sendClient(new PlayerDataRemovePacketNew(playerName, tabType, selectedID));
            }
        }
    }

    @Override
    public void setQuestData(Map<String, Integer> questCategories, Map<String, Integer> questActive, Map<String, Integer> questFinished) {
        this.questCatData = new HashMap<>(questCategories);
        this.questActiveData = new HashMap<>(questActive);
        this.questFinishedData = new HashMap<>(questFinished);
        if (questCatScroll != null) {
            questCatScroll.setList(new ArrayList<>(questCatData.keySet()));
        }
    }

    @Override
    public void setDialogData(Map<String, Integer> dialogCategories, Map<String, Integer> dialogRead) {
        this.dialogCatData = new HashMap<>(dialogCategories);
        this.dialogReadData = new HashMap<>(dialogRead);
        if (dialogCatScroll != null) {
            dialogCatScroll.setList(new ArrayList<>(dialogCatData.keySet()));
        }
        if (dialogReadScroll != null) {
            dialogReadScroll.setList(new ArrayList<>(dialogReadData.keySet()));
        }
        if (dialogCompactScroll != null) {
            List<String> merged = new ArrayList<>(dialogReadData.keySet());
            dialogCompactScroll.setList(merged);
        }
    }

    @Override
    public void setTransportData(Map<String, Integer> transportCategories, Map<String, Integer> transportLocations) {
        this.transCatData = new HashMap<>(transportCategories);
        this.transLocData = new HashMap<>(transportLocations);
        if (transCatScroll != null) {
            transCatScroll.setList(new ArrayList<>(transCatData.keySet()));
        }
        if (transLocScroll != null) {
            transLocScroll.setList(new ArrayList<>(transLocData.keySet()));
        }
        if (transCompactScroll != null) {
            List<String> merged = new ArrayList<>(transLocData.keySet());
            transCompactScroll.setList(merged);
        }
    }

    @Override
    public void setBankData(Map<String, Integer> bankData) {
        this.bankData = new HashMap<>(bankData);
        if (singleScroll != null && currentTab == 13) {
            singleScroll.setList(new ArrayList<>(this.bankData.keySet()));
        }
    }

    @Override
    public void setFactionData(Map<String, Integer> factionData) {
        this.factionData = new HashMap<>(factionData);
        if (singleScroll != null && currentTab == 14) {
            singleScroll.setList(new ArrayList<>(this.factionData.keySet()));
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll == questCatScroll) {
            selectedQuestCategory = questCatScroll.getSelected();
            questFinishedScroll.resetScroll();
            questActiveScroll.resetScroll();
            if (!selectedQuestCategory.isEmpty()) {
                questFinishedScroll.setList(filterAndTrimListByCategory(questFinishedData, selectedQuestCategory, questFinishedSearch));
                questActiveScroll.setList(filterAndTrimListByCategory(questActiveData, selectedQuestCategory, questActiveSearch));
            } else {
                questFinishedScroll.setList(new ArrayList<>());
                questActiveScroll.setList(new ArrayList<>());
            }
        } else if (guiCustomScroll == dialogCatScroll) {
            selectedDialogCategory = dialogCatScroll.getSelected();
            dialogReadScroll.resetScroll();
            if (!selectedDialogCategory.isEmpty()) {
                dialogReadScroll.setList(filterAndTrimListByCategory(dialogReadData, selectedDialogCategory, dialogReadSearch));
            } else {
                dialogReadScroll.setList(new ArrayList<>());
            }
        } else if (guiCustomScroll == transCatScroll) {
            selectedTransCategory = transCatScroll.getSelected();
            transLocScroll.resetScroll();
            if (!selectedTransCategory.isEmpty()) {
                transLocScroll.setList(filterAndTrimListByCategory(transLocData, selectedTransCategory, transLocSearch));
            } else {
                transLocScroll.setList(new ArrayList<>());
            }
        } else if (guiCustomScroll == questActiveScroll) {
            questFinishedScroll.selected = -1;
        } else if (guiCustomScroll == questFinishedScroll) {
            questActiveScroll.selected = -1;
        }
    }
}
