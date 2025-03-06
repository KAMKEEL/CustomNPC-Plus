package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataGetInfoPacket;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataRemoveInfoPacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerData;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class SubGuiPlayerData extends SubGuiInterface implements IPlayerDataInfo, ICustomScrollListener {

    ////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////

    // Current tab (10=Quest, 11=Dialog, 12=Transport, 13=Bank, 14=Faction)
    private int currentTab = 10;
    // View mode: 0 = Categorical (3 or 2 scrolls), 1 = Compact (2 or 1 scroll)
    private int viewMode = 0;

    // ----- Quest Tab Components -----
    protected GuiCustomScroll questCatScroll, questActiveScroll, questFinishedScroll;
    private String questCatSearch = "", questActiveSearch = "", questFinishedSearch = "";
    private HashMap<String, Integer> questCatData = new HashMap<>();
    private HashMap<String, Integer> questActiveData = new HashMap<>();
    private HashMap<String, Integer> questFinishedData = new HashMap<>();
    private String selectedQuestCategory = "";

    // ----- Dialog Tab Components -----
    protected GuiCustomScroll dialogCatScroll, dialogReadScroll, dialogCompactScroll;
    private String dialogCatSearch = "", dialogReadSearch = "", dialogCompactSearch = "";
    private HashMap<String, Integer> dialogCatData = new HashMap<>();
    private HashMap<String, Integer> dialogReadData = new HashMap<>();
    private String selectedDialogCategory = "";

    // ----- Transport Tab Components -----
    protected GuiCustomScroll transCatScroll, transLocScroll, transCompactScroll;
    private String transCatSearch = "", transLocSearch = "", transCompactSearch = "";
    private HashMap<String, Integer> transCatData = new HashMap<>();
    private HashMap<String, Integer> transLocData = new HashMap<>();
    private String selectedTransCategory = "";

    // ----- Bank and Faction (Single Scroll) -----
    protected GuiCustomScroll singleScroll;
    private String singleSearch = "";
    private HashMap<String, Integer> bankData = new HashMap<>();
    private HashMap<String, Integer> factionData = new HashMap<>();

    // ----- Divider & Resizing Variables -----
    // In categorical mode for Quest: two dividers (3 scrolls).
    // In compact mode for Quest: one divider between two scrolls.
    private int dividerOffset1 = 0; // In categorical: width of left scroll; in compact for quests: width of left scroll.
    private int dividerOffset2 = 0; // Only used for Quest categorical mode.
    private final int dividerWidth = 5;
    private final int minScrollWidth = 50;
    private boolean isResizing = false;
    private int resizingDivider = 0;
    private int initialDragX = 0;

    // ----- Layout Constants -----
    private final int leftPadding = 6;
    private final int rightPadding = 6;
    private final int scrollTopOffset = 30;
    private final int verticalGapAboveTF = 3;
    private final int textFieldHeight = 20;
    private final int verticalGapBelowTF = 3;

    // ----- Other -----
    private String playerName;

    ////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////////////////////////////////////////////
    public SubGuiPlayerData(String playerName) {
        this.playerName = playerName;
        xSize = 420;
        ySize = 215;
        setBackground("menubg.png");
        closeOnEsc = true;
        PacketClient.sendClient(new PlayerDataGetInfoPacket(playerName));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Layout Helpers
    ////////////////////////////////////////////////////////////////////////////////

    private int getPaddedLeft() {
        return guiLeft + leftPadding;
    }

    private int getPaddedRight() {
        return guiLeft + xSize - rightPadding;
    }

    private int getAvailableWidth() {
        return getPaddedRight() - getPaddedLeft();
    }

    // Height available for scroll windows.
    private int getScrollHeight() {
        return ySize - (scrollTopOffset + verticalGapAboveTF + textFieldHeight + verticalGapBelowTF);
    }

    // Y coordinate for textfields.
    private int getTextFieldY() {
        return guiTop + scrollTopOffset + getScrollHeight() + verticalGapAboveTF;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Component Setup Helpers
    ////////////////////////////////////////////////////////////////////////////////

    // Create top buttons.
    private void setupTopButtons() {

        // Show General Display Information
        GuiMenuTopButton playerTab = new GuiMenuTopButton(-10, guiLeft + 4, guiTop - 10, playerName);

        GuiMenuTopButton btnQuest = new GuiMenuTopButton(10, playerTab.xPosition + playerTab.getButtonWidth(), guiTop - 10, "quest.quests");
        GuiMenuTopButton btnDialog = new GuiMenuTopButton(11, btnQuest.xPosition + btnQuest.getWidth(), guiTop - 10, "dialog.dialogs");
        GuiMenuTopButton btnTransport = new GuiMenuTopButton(12, btnDialog.xPosition + btnDialog.getWidth(), guiTop - 10, "global.transport");
        GuiMenuTopButton btnBank = new GuiMenuTopButton(13, btnTransport.xPosition + btnTransport.getWidth(), guiTop - 10, "global.banks");
        GuiMenuTopButton btnFaction = new GuiMenuTopButton(14, btnBank.xPosition + btnBank.getWidth(), guiTop - 10, "menu.factions");
        // Close button.
        GuiMenuTopButton close = new GuiMenuTopButton(-5, guiLeft + xSize - 22, guiTop - 10, "X");

        playerTab.active = (currentTab == -10);
        btnQuest.active = (currentTab == 10);
        btnDialog.active = (currentTab == 11);
        btnTransport.active = (currentTab == 12);
        btnBank.active = (currentTab == 13);
        btnFaction.active = (currentTab == 14);

        addTopButton(playerTab);
        addTopButton(btnQuest);
        addTopButton(btnDialog);
        addTopButton(btnTransport);
        addTopButton(btnBank);
        addTopButton(btnFaction);
        addTopButton(close);
    }

    // Helper to ensure scroll exists.
    private GuiCustomScroll ensureScroll(GuiCustomScroll scroll, int id) {
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, id, 0);
        }
        return scroll;
    }

    // Helper to update a text field's position and width.
    private void updateComponentPosition(int fieldId, int newX, int newWidth) {
        GuiNpcTextField tf = getTextField(fieldId);
        if (tf != null) {
            tf.xPosition = newX;
            tf.width = newWidth;
        }
    }

    // Helper to update a label's x position.
    private void updateLabelPosition(int labelId, int newX) {
        GuiNpcLabel lbl = getLabel(labelId);
        if (lbl != null) {
            lbl.x = newX;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // initGui() & Tab Initialization
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initGui() {
        super.initGui();
        setupTopButtons();

        if (currentTab == 10 || currentTab == 11 || currentTab == 12) {
            addButton(new GuiToggleButton(20, guiLeft + xSize - 102, guiTop + 10, viewMode == 0));
            ((GuiToggleButton) getButton(20)).setTextureOff(specialIcons).setTextureOffPos( 16, 0);
            ((GuiToggleButton) getButton(20)).setIconTexture(specialIcons).setIconPos( 16, 16, 16, 0 );
        }

        guiTop += 7;
        // Initialize components based on current tab.
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
                initSingleTab();
                break;
        }
        // Delete button.
        addButton(new GuiNpcButton(30, guiLeft + xSize - 60, guiTop + 10 - 7, 50, 20, "gui.remove"));
    }

    // ----- Quest Tab Initialization -----
    private void initQuestTab() {
        int paddedLeft = getPaddedLeft();
        int paddedRight = getPaddedRight();
        int availableWidth = getAvailableWidth();
        int scrollHeight = getScrollHeight();

        if (viewMode == 0) { // Categorical: 3 scrolls.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - 2 * dividerWidth) / 3;
                dividerOffset2 = dividerOffset1 + dividerWidth + (availableWidth - 2 * dividerWidth) / 3;
            }
            // Quest Category Scroll.
            questCatScroll = ensureScroll(questCatScroll, 0);
            questCatScroll.guiLeft = paddedLeft;
            questCatScroll.guiTop = guiTop + scrollTopOffset;
            questCatScroll.setSize(dividerOffset1, scrollHeight);
            questCatScroll.setList(new ArrayList<>(questCatData.keySet()));
            questCatScroll.selected = -1;
            questCatSearch = "";
            addScroll(questCatScroll);
            addLabel(new GuiNpcLabel(1000, "Categories", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            // Quest Finished Scroll.
            questFinishedScroll = ensureScroll(questFinishedScroll, 1);
            questFinishedScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
            questFinishedScroll.guiTop = guiTop + scrollTopOffset;
            questFinishedScroll.setSize(dividerOffset2 - dividerOffset1 - dividerWidth, scrollHeight);
            questFinishedScroll.setList(new ArrayList<>());
            questFinishedScroll.selected = -1;
            questFinishedSearch = "";
            addScroll(questFinishedScroll);
            addLabel(new GuiNpcLabel(1001, "Finished", paddedLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            // Quest Active Scroll.
            questActiveScroll = ensureScroll(questActiveScroll, 2);
            questActiveScroll.guiLeft = paddedLeft + dividerOffset2 + dividerWidth;
            questActiveScroll.guiTop = guiTop + scrollTopOffset;
            questActiveScroll.setSize(paddedRight - (paddedLeft + dividerOffset2 + dividerWidth), scrollHeight);
            questActiveScroll.setList(new ArrayList<>());
            questActiveScroll.selected = -1;
            questActiveSearch = "";
            addScroll(questActiveScroll);
            addLabel(new GuiNpcLabel(1002, "Active", paddedLeft + dividerOffset2 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            // Text Fields.
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, paddedLeft, getTextFieldY(), dividerOffset1, textFieldHeight, questCatSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, paddedLeft + dividerOffset1 + dividerWidth, getTextFieldY(), dividerOffset2 - dividerOffset1 - dividerWidth, textFieldHeight, questFinishedSearch));
            addTextField(new GuiNpcTextField(57, this, fontRendererObj, paddedLeft + dividerOffset2 + dividerWidth, getTextFieldY(), paddedRight - (paddedLeft + dividerOffset2 + dividerWidth), textFieldHeight, questActiveSearch));
        } else { // Compact: 2 scrolls with resizable divider.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - dividerWidth) / 2;
            }
            questFinishedScroll = ensureScroll(questFinishedScroll, 1);
            questFinishedScroll.guiLeft = paddedLeft;
            questFinishedScroll.guiTop = guiTop + scrollTopOffset;
            questFinishedScroll.setSize(dividerOffset1, scrollHeight);
            questFinishedScroll.setList(new ArrayList<>(questFinishedData.keySet()));
            questFinishedSearch = "";
            addScroll(questFinishedScroll);
            addLabel(new GuiNpcLabel(1003, "Finished", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            questActiveScroll = ensureScroll(questActiveScroll, 2);
            questActiveScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
            questActiveScroll.guiTop = guiTop + scrollTopOffset;
            questActiveScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, scrollHeight);
            questActiveScroll.setList(new ArrayList<>(questActiveData.keySet()));
            questActiveSearch = "";
            addScroll(questActiveScroll);
            addLabel(new GuiNpcLabel(1004, "Active", paddedLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(55, this, fontRendererObj, paddedLeft, getTextFieldY(), dividerOffset1, textFieldHeight, questFinishedSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, paddedLeft + dividerOffset1 + dividerWidth, getTextFieldY(), availableWidth - dividerOffset1 - dividerWidth, textFieldHeight, questActiveSearch));
        }
    }

    // ----- Dialog Tab Initialization -----
    private void initDialogTab() {
        int paddedLeft = getPaddedLeft();
        int availableWidth = getAvailableWidth();
        int scrollHeight = getScrollHeight();

        if (viewMode == 0) { // Categorical: 2 scrolls.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - dividerWidth) / 2;
            }
            dialogCatScroll = ensureScroll(dialogCatScroll, 0);
            dialogCatScroll.guiLeft = paddedLeft;
            dialogCatScroll.guiTop = guiTop + scrollTopOffset;
            dialogCatScroll.setSize(dividerOffset1, scrollHeight);
            dialogCatScroll.selected = -1;
            dialogCatScroll.setList(new ArrayList<>(dialogCatData.keySet()));
            addScroll(dialogCatScroll);
            addLabel(new GuiNpcLabel(2000, "Categories", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            dialogReadScroll = ensureScroll(dialogReadScroll, 1);
            dialogReadScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
            dialogReadScroll.guiTop = guiTop + scrollTopOffset;
            dialogReadScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, scrollHeight);
            dialogReadScroll.selected = -1;
            dialogReadScroll.setList(new ArrayList<>());
            addScroll(dialogReadScroll);
            addLabel(new GuiNpcLabel(2001, "Read", paddedLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(55, this, fontRendererObj, paddedLeft, getTextFieldY(), dividerOffset1, textFieldHeight, dialogCatSearch));
            addTextField(new GuiNpcTextField(56, this, fontRendererObj, paddedLeft + dividerOffset1 + dividerWidth, getTextFieldY(), availableWidth - dividerOffset1 - dividerWidth, textFieldHeight, dialogReadSearch));
        } else { // Compact: 1 scroll (no divider).
            dialogCompactScroll = ensureScroll(dialogCompactScroll, 5);
            dialogCompactScroll.guiLeft = paddedLeft;
            dialogCompactScroll.guiTop = guiTop + scrollTopOffset;
            dialogCompactScroll.setSize(availableWidth, getScrollHeight());
            dialogCompactScroll.setList(new ArrayList<>(dialogReadData.keySet()));
            dialogCompactScroll.selected = -1;
            addScroll(dialogCompactScroll);
            addLabel(new GuiNpcLabel(2002, "All", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
            addTextField(new GuiNpcTextField(55, this, fontRendererObj, paddedLeft, getTextFieldY(), availableWidth, textFieldHeight, dialogCompactSearch));
        }
    }

    // ----- Transport Tab Initialization -----
    private void initTransportTab() {
        int paddedLeft = getPaddedLeft();
        int availableWidth = getAvailableWidth();
        int scrollHeight = getScrollHeight();

        if (viewMode == 0) { // Categorical: 2 scrolls.
            if (!isResizing) {
                dividerOffset1 = (availableWidth - dividerWidth) / 2;
            }
            transCatScroll = ensureScroll(transCatScroll, 3);
            transCatScroll.guiLeft = paddedLeft;
            transCatScroll.guiTop = guiTop + scrollTopOffset;
            transCatScroll.setSize(dividerOffset1, scrollHeight);
            transCatScroll.selected = -1;
            transCatScroll.setList(new ArrayList<>(transCatData.keySet()));
            addScroll(transCatScroll);
            addLabel(new GuiNpcLabel(3000, "Categories", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            transLocScroll = ensureScroll(transLocScroll, 4);
            transLocScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
            transLocScroll.guiTop = guiTop + scrollTopOffset;
            transLocScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, scrollHeight);
            transLocScroll.selected = -1;
            transLocScroll.setList(new ArrayList<>());
            addScroll(transLocScroll);
            addLabel(new GuiNpcLabel(3001, "Locations", paddedLeft + dividerOffset1 + dividerWidth, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));

            addTextField(new GuiNpcTextField(60, this, fontRendererObj, paddedLeft, getTextFieldY(), dividerOffset1, textFieldHeight, transCatSearch));
            addTextField(new GuiNpcTextField(61, this, fontRendererObj, paddedLeft + dividerOffset1 + dividerWidth, getTextFieldY(), availableWidth - dividerOffset1 - dividerWidth, textFieldHeight, transLocSearch));
        } else { // Compact: 1 scroll (no divider).
            transCompactScroll = ensureScroll(transCompactScroll, 5);
            transCompactScroll.guiLeft = paddedLeft;
            transCompactScroll.guiTop = guiTop + scrollTopOffset;
            transCompactScroll.selected = -1;
            transCompactScroll.setSize(availableWidth, getScrollHeight());
            transCompactScroll.setList(new ArrayList<>(transLocData.keySet()));
            addScroll(transCompactScroll);
            addLabel(new GuiNpcLabel(3002, "All", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
            addTextField(new GuiNpcTextField(60, this, fontRendererObj, paddedLeft, getTextFieldY(), availableWidth, textFieldHeight, transCompactSearch));
        }
    }

    // ----- Single Tab Initialization (Bank/Faction) -----
    private void initSingleTab() {
        int paddedLeft = getPaddedLeft();
        int availableWidth = getAvailableWidth();
        int scrollHeight = getScrollHeight();
        singleScroll = ensureScroll(singleScroll, 0);
        singleScroll.guiLeft = paddedLeft;
        singleScroll.guiTop = guiTop + scrollTopOffset;
        if (currentTab == 13) {
            singleScroll.setSize(availableWidth, scrollHeight);
            singleScroll.setList(new ArrayList<>(bankData.keySet()));
            singleScroll.selected = -1;
            addLabel(new GuiNpcLabel(4000, "Bank", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
        } else {
            singleScroll.setSize(availableWidth, scrollHeight);
            singleScroll.setList(new ArrayList<>(factionData.keySet()));
            singleScroll.selected = -1;
            addLabel(new GuiNpcLabel(4000, "Faction", paddedLeft, guiTop + 15, CustomNpcResourceListener.DefaultTextColor));
        }
        addScroll(singleScroll);
        addTextField(new GuiNpcTextField(70, this, fontRendererObj, paddedLeft, getTextFieldY(), availableWidth, textFieldHeight, singleSearch));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Drawing Helpers
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawBackground() {
        super.drawBackground();

        int paddingTopBottom = 25;
        int scrollHeight = getScrollHeight();
        int paddedLeft = getPaddedLeft();
        int widthPadding = 1;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        // In compact mode for Quests, draw divider between the two scrolls.
        if (currentTab == 10 && viewMode != 0 && !hasSubGui()) {
            int dividerX = paddedLeft + dividerOffset1;
            drawRect(dividerX + widthPadding, guiTop + scrollTopOffset + paddingTopBottom, dividerX + dividerWidth - widthPadding, guiTop + scrollTopOffset + scrollHeight - paddingTopBottom, 0xFF707070);
        }
        // In categorical mode for Quest, draw both dividers.
        if (currentTab == 10 && viewMode == 0 && !hasSubGui()) {
            int dividerX1 = paddedLeft + dividerOffset1;
            int dividerX2 = paddedLeft + dividerOffset2;
            drawRect(dividerX1 + widthPadding, guiTop + scrollTopOffset + paddingTopBottom, dividerX1 + dividerWidth - widthPadding, guiTop + scrollTopOffset + scrollHeight - paddingTopBottom, 0xFF707070);
            drawRect(dividerX2 + widthPadding, guiTop + scrollTopOffset + paddingTopBottom, dividerX2 + dividerWidth - widthPadding, guiTop + scrollTopOffset + scrollHeight - paddingTopBottom, 0xFF707070);
        } else if ((currentTab == 11 || currentTab == 12) && viewMode == 0 && !hasSubGui()) {
            int dividerX = paddedLeft + dividerOffset1;
            drawRect(dividerX + widthPadding, guiTop + scrollTopOffset + paddingTopBottom, dividerX + dividerWidth - widthPadding, guiTop + scrollTopOffset + scrollHeight - paddingTopBottom, 0xFF707070);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Mouse & Resizing Handlers
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int hitMargin = 3;
        int regionTop = guiTop + scrollTopOffset;
        int regionBottom = regionTop + getScrollHeight();
        int paddedLeft = getPaddedLeft();
        int availableWidth = getAvailableWidth();
        if (!hasSubGui() && mouseY >= regionTop && mouseY <= regionBottom) {
            if (viewMode == 0) {
                if (currentTab == 10) { // Quest categorical: 3 scrolls.
                    int dividerX1 = paddedLeft + dividerOffset1;
                    int dividerX2 = paddedLeft + dividerOffset2;
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
                } else if (currentTab == 11 || currentTab == 12) {
                    int dividerX = paddedLeft + dividerOffset1;
                    if (mouseX >= dividerX - hitMargin && mouseX <= dividerX + dividerWidth + hitMargin) {
                        isResizing = true;
                        resizingDivider = 1;
                        initialDragX = mouseX;
                        return;
                    }
                }
            } else { // Compact mode.
                if (currentTab == 10) { // Quest compact: 2 scrolls.
                    int defaultWidth = (availableWidth - dividerWidth) / 2;
                    if (!isResizing) dividerOffset1 = defaultWidth;
                    int dividerX = paddedLeft + dividerOffset1;
                    if (mouseX >= dividerX - hitMargin && mouseX <= dividerX + dividerWidth + hitMargin) {
                        isResizing = true;
                        resizingDivider = 1;
                        initialDragX = mouseX;
                        return;
                    }
                }
                // Dialog/Transport compact have only one scroll => no divider.
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        int paddedLeft = getPaddedLeft();
        int paddedRight = getPaddedRight();
        int availableWidth = getAvailableWidth();
        if (isResizing) {
            int dx = mouseX - initialDragX;
            initialDragX = mouseX;
            // Handle Quest tab resizing.
            if (currentTab == 10) {
                if (viewMode == 0) { // Categorical (3 scrolls)
                    if (resizingDivider == 1) {
                        dividerOffset1 += dx;
                        int maxOffset = dividerOffset2 - dividerWidth - minScrollWidth;
                        dividerOffset1 = clamp(dividerOffset1, minScrollWidth, maxOffset);
                        questCatScroll.setSize(dividerOffset1, getScrollHeight());
                        questFinishedScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
                        questFinishedScroll.setSize(dividerOffset2 - dividerOffset1 - dividerWidth, getScrollHeight());
                        questActiveScroll.guiLeft = paddedLeft + dividerOffset2 + dividerWidth;
                        questActiveScroll.setSize(availableWidth - (dividerOffset2 + dividerWidth), getScrollHeight());
                        updateComponentPosition(55, paddedLeft, dividerOffset1);
                        updateComponentPosition(56, paddedLeft + dividerOffset1 + dividerWidth, dividerOffset2 - dividerOffset1 - dividerWidth);
                        updateComponentPosition(57, paddedLeft + dividerOffset2 + dividerWidth, availableWidth - (dividerOffset2 + dividerWidth));
                        updateLabelPosition(1000, paddedLeft);
                        updateLabelPosition(1001, paddedLeft + dividerOffset1 + dividerWidth);
                        updateLabelPosition(1002, paddedLeft + dividerOffset2 + dividerWidth);
                    } else if (resizingDivider == 2) {
                        dividerOffset2 += dx;
                        int minOffset = dividerOffset1 + dividerWidth + minScrollWidth;
                        int maxOffset = availableWidth - minScrollWidth;
                        dividerOffset2 = clamp(dividerOffset2, minOffset, maxOffset);
                        questFinishedScroll.setSize(dividerOffset2 - dividerOffset1 - dividerWidth, getScrollHeight());
                        questActiveScroll.guiLeft = paddedLeft + dividerOffset2 + dividerWidth;
                        questActiveScroll.setSize(availableWidth - (dividerOffset2 + dividerWidth), getScrollHeight());
                        updateComponentPosition(56, paddedLeft + dividerOffset1 + dividerWidth, dividerOffset2 - dividerOffset1 - dividerWidth);
                        updateComponentPosition(57, paddedLeft + dividerOffset2 + dividerWidth, availableWidth - (dividerOffset2 + dividerWidth));
                        updateLabelPosition(1001, paddedLeft + dividerOffset1 + dividerWidth);
                        updateLabelPosition(1002, paddedLeft + dividerOffset2 + dividerWidth);
                    }
                } else { // Compact mode for Quests (2 scrolls)
                    if (resizingDivider == 1) {
                        dividerOffset1 += dx;
                        dividerOffset1 = clamp(dividerOffset1, minScrollWidth, availableWidth - dividerWidth - minScrollWidth);
                        questFinishedScroll.setSize(dividerOffset1, getScrollHeight());
                        questActiveScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
                        questActiveScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, getScrollHeight());
                        updateComponentPosition(55, paddedLeft, dividerOffset1);
                        updateComponentPosition(56, paddedLeft + dividerOffset1 + dividerWidth, availableWidth - dividerOffset1 - dividerWidth);
                        updateLabelPosition(1003, paddedLeft);
                        updateLabelPosition(1004, paddedLeft + dividerOffset1 + dividerWidth);
                    }
                }
            } else if ((currentTab == 11 || currentTab == 12) && viewMode == 0) {
                // For Dialog and Transport in categorical mode.
                dividerOffset1 += dx;
                dividerOffset1 = clamp(dividerOffset1, minScrollWidth, availableWidth - minScrollWidth);
                if (currentTab == 11) {
                    dialogCatScroll.setSize(dividerOffset1, getScrollHeight());
                    dialogReadScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
                    dialogReadScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, getScrollHeight());
                    updateComponentPosition(55, paddedLeft, dividerOffset1);
                    updateComponentPosition(56, paddedLeft + dividerOffset1 + dividerWidth, availableWidth - dividerOffset1 - dividerWidth);
                    updateLabelPosition(2000, paddedLeft);
                    updateLabelPosition(2001, paddedLeft + dividerOffset1 + dividerWidth);
                } else if (currentTab == 12) {
                    transCatScroll.setSize(dividerOffset1, getScrollHeight());
                    transLocScroll.guiLeft = paddedLeft + dividerOffset1 + dividerWidth;
                    transLocScroll.setSize(availableWidth - dividerOffset1 - dividerWidth, getScrollHeight());
                    updateComponentPosition(60, paddedLeft, dividerOffset1);
                    updateComponentPosition(61, paddedLeft + dividerOffset1 + dividerWidth, availableWidth - dividerOffset1 - dividerWidth);
                    updateLabelPosition(3000, paddedLeft);
                    updateLabelPosition(3001, paddedLeft + dividerOffset1 + dividerWidth);
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

    // Simple clamp helper.
    private int clamp(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Key & Search Field Handling
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (currentTab == 10) {
            if (viewMode == 0) {
                updateQuestSearchField(55, 56, 57);
            } else {
                updateQuestSearchFieldCompact(55, 56);
            }
        } else if (currentTab == 11) {
            if (viewMode == 0) {
                updateDialogSearchField(55, 56);
            } else {
                updateDialogSearchFieldCompact(55);
            }
        } else if (currentTab == 12) {
            if (viewMode == 0) {
                updateTransportSearchField(60, 61);
            } else {
                updateTransportSearchFieldCompact(60);
            }
        } else if (currentTab == 13 || currentTab == 14) {
            updateSingleSearchField(70);
        }
    }

    // Update quest search fields (categorical mode).
    private void updateQuestSearchField(int catFieldId, int finishedFieldId, int activeFieldId) {
        GuiNpcTextField tf = getTextField(catFieldId);
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
        tf = getTextField(finishedFieldId);
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
        tf = getTextField(activeFieldId);
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
    }

    // Update quest search fields in compact mode.
    private void updateQuestSearchFieldCompact(int finishedFieldId, int activeFieldId) {
        GuiNpcTextField tf = getTextField(finishedFieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!questFinishedSearch.equals(newText)) {
                questFinishedSearch = newText;
                if (questFinishedScroll != null)
                    questFinishedScroll.setList(filterList(questFinishedData, questFinishedSearch));
            }
        }
        tf = getTextField(activeFieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!questActiveSearch.equals(newText)) {
                questActiveSearch = newText;
                if (questActiveScroll != null)
                    questActiveScroll.setList(filterList(questActiveData, questActiveSearch));
            }
        }
    }

    // Update dialog search fields (categorical).
    private void updateDialogSearchField(int catFieldId, int readFieldId) {
        GuiNpcTextField tf = getTextField(catFieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!dialogCatSearch.equals(newText)) {
                dialogCatSearch = newText;
                if (dialogCatScroll != null)
                    dialogCatScroll.setList(
                        selectedDialogCategory.isEmpty() ?
                            new ArrayList<>() :
                            filterList(dialogCatData, dialogCatSearch)
                    );
            }
        }
        tf = getTextField(readFieldId);
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
    }

    // Update dialog search fields (compact).
    private void updateDialogSearchFieldCompact(int fieldId) {
        GuiNpcTextField tf = getTextField(fieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!dialogCompactSearch.equals(newText)) {
                dialogCompactSearch = newText;
                if (dialogCompactScroll != null)
                    dialogCompactScroll.setList(filterList(dialogReadData, dialogCompactSearch));
            }
        }
    }

    // Update transport search fields (categorical).
    private void updateTransportSearchField(int catFieldId, int locFieldId) {
        GuiNpcTextField tf = getTextField(catFieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!transCatSearch.equals(newText)) {
                transCatSearch = newText;
                if (transCatScroll != null)
                    transCatScroll.setList(
                        selectedTransCategory.isEmpty() ?
                            new ArrayList<>() :
                            filterList(transCatData, transCatSearch)
                    );
            }
        }
        tf = getTextField(locFieldId);
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
    }

    // Update transport search fields (compact).
    private void updateTransportSearchFieldCompact(int fieldId) {
        GuiNpcTextField tf = getTextField(fieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!transCompactSearch.equals(newText)) {
                transCompactSearch = newText;
                if (transCompactScroll != null)
                    transCompactScroll.setList(filterList(transLocData, transCompactSearch));
            }
        }
    }

    // Update search field for single scroll (Bank/Faction).
    private void updateSingleSearchField(int fieldId) {
        GuiNpcTextField tf = getTextField(fieldId);
        if (tf != null && tf.isFocused()) {
            String newText = tf.getText().toLowerCase();
            if (!singleSearch.equals(newText)) {
                singleSearch = newText;
                if (singleScroll != null)
                    singleScroll.setList(filterList(currentTab == 13 ? bankData : factionData, singleSearch));
            }
        }
    }

    // Standard filtering helpers.
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

    ////////////////////////////////////////////////////////////////////////////////
    // Action Handling
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void actionPerformed(GuiButton button) {
        // Close if the "X" button was clicked.
        if (button.id == -5) {
            close();
            return;
        }
        // Tab switching.
        if (button.id >= 10 && button.id <= 14) {
            currentTab = button.id;
            initGui(); // Reinitialize components using stored data.
            return;
        }
        // Mode toggle.
        if (button.id == 20) {
            viewMode = (viewMode == 0) ? 1 : 0;
            initGui();
            return;
        }
        // Delete button.
        if (button.id == 30) {
            Integer selectedID = null;
            EnumPlayerData tabType = null;
            // Quest tab
            if (currentTab == 10) {
                if (viewMode == 0) { // Categorical mode
                    if (questCatScroll.getSelected() == null)
                        return;
                    // Use category + selection key.
                    if (questActiveScroll.getSelected() != null) {
                        selectedID = removeSelection(questActiveScroll, questActiveData, questCatScroll.getSelected());
                    } else if (questFinishedScroll.getSelected() != null) {
                        selectedID = removeSelection(questFinishedScroll, questFinishedData, questCatScroll.getSelected());
                    }
                } else { // Compact mode: key is just the selection.
                    if (questActiveScroll.getSelected() != null) {
                        selectedID = removeSelection(questActiveScroll, questActiveData, null);
                    } else if (questFinishedScroll.getSelected() != null) {
                        selectedID = removeSelection(questFinishedScroll, questFinishedData, null);
                    }
                }
                tabType = EnumPlayerData.Quest;
            }
            // Dialog tab
            else if (currentTab == 11) {
                if (viewMode == 0) {
                    if (dialogReadScroll.getSelected() != null && dialogCatScroll.getSelected() != null) {
                        selectedID = removeSelection(dialogReadScroll, dialogReadData, dialogCatScroll.getSelected());
                    }
                } else {
                    if (dialogCompactScroll.getSelected() != null) {
                        selectedID = removeSelection(dialogCompactScroll, dialogReadData, null);
                    }
                }
                tabType = EnumPlayerData.Dialog;
            }
            // Transport tab
            else if (currentTab == 12) {
                if (viewMode == 0) {
                    if (transLocScroll.getSelected() != null && transCatScroll.getSelected() != null) {
                        selectedID = removeSelection(transLocScroll, transLocData, transCatScroll.getSelected());
                    }
                } else {
                    if (transCompactScroll.getSelected() != null) {
                        selectedID = removeSelection(transCompactScroll, transLocData, null);
                    }
                }
                tabType = EnumPlayerData.Transport;
            }
            // Bank tab
            else if (currentTab == 13) {
                if (singleScroll.getSelected() != null) {
                    selectedID = removeSelection(singleScroll, bankData, null);
                }
                tabType = EnumPlayerData.Bank;
            }
            // Faction tab
            else if (currentTab == 14) {
                if (singleScroll.getSelected() != null) {
                    selectedID = removeSelection(singleScroll, factionData, null);
                }
                tabType = EnumPlayerData.Factions;
            }
            if (selectedID != null) {
                PacketClient.sendClient(new PlayerDataRemoveInfoPacket(playerName, tabType, selectedID));
            }
        }
    }

    /**
     * Helper method that removes the selected entry from a scroll and its backing data.
     *
     * @param scroll The GuiCustomScroll in question.
     * @param data The backing map holding keys and IDs.
     * @param prefix If non-null and non-empty, the key is built as "prefix: " + scroll.getSelected(); otherwise,
     *               scroll.getSelected() is used.
     * @return The ID of the removed item, or null if none was found.
     */
    private Integer removeSelection(GuiCustomScroll scroll, Map<String, Integer> data, String prefix) {
        if (scroll.getSelected() == null) return null;
        String key = (prefix == null || prefix.isEmpty()) ? scroll.getSelected() : prefix + ": " + scroll.getSelected();
        Integer id = data.get(key);
        if (id != null) {
            scroll.list.remove(scroll.selected);
            scroll.selected = -1;
            data.remove(key);
        }
        return id;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Data Setting Methods (IPlayerDataInfo)
    ////////////////////////////////////////////////////////////////////////////////

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
    }

    @Override
    public void setTransportData(Map<String, Integer> transportCategories, Map<String, Integer> transportLocations) {
        this.transCatData = new HashMap<>(transportCategories);
        this.transLocData = new HashMap<>(transportLocations);
        if (transCatScroll != null) {
            transCatScroll.setList(new ArrayList<>(transCatData.keySet()));
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

    ////////////////////////////////////////////////////////////////////////////////
    // Custom Scroll Click Handling
    ////////////////////////////////////////////////////////////////////////////////

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
