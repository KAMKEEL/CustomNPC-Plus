package noppes.npcs.client.gui.global;

import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import kamkeel.npcs.client.gui.modern.ModernColorPicker;
import kamkeel.npcs.client.gui.modern.ModernDialogSelector;
import kamkeel.npcs.client.gui.modern.ModernFactionSelector;
import kamkeel.npcs.client.gui.modern.ModernMailEditor;
import kamkeel.npcs.client.gui.modern.ModernQuestSelector;
import kamkeel.npcs.client.gui.modern.ModernSoundSelector;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.dialog.DialogSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.gui.SubGuiNpcDialog;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.entity.EntityDialogNpc;
import kamkeel.npcs.client.gui.modern.GuiModernScreen;
import noppes.npcs.client.gui.util.GuiAdvancedDiagram;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import noppes.npcs.client.gui.util.IDialogEditorParent;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

public class GuiDialogTree extends GuiModernScreen implements IDialogEditorParent, ITextfieldListener {

    private EntityNPCInterface npc;
    private int categoryId;
    private DialogCategory category;

    private HashMap<String, Integer> dialogData = new HashMap<>();
    private GuiCustomScroll dialogListScroll;
    private String dialogQuestName = "";

    private Dialog selectedDialog;
    private DialogGraphDiagram diagram;

    // Link mode
    private boolean linkMode = false;
    private int linkSourceId = -1;

    // Saved pan/zoom to preserve across initGui()
    private float savedPanX = 0, savedPanY = 0, savedZoom = 1.0f;
    private boolean hasSavedView = false;

    // Panel visibility toggles
    private boolean leftPanelVisible = true;
    private boolean rightPanelVisible = true;

    // Search
    private String dialogSearch = "";

    // Legend bar
    private int legendBarH = 14;

    // Right divider
    private int rightDividerOffset;
    private boolean isResizingRight = false;
    private int rightDragStartX;
    private int rightDividerWidth = 5;
    private int rightDividerLineHeight = 20;
    private int minRightDividerPanelW = 80;

    // Individual mode
    private boolean individualMode = false;
    private int individualRootId = -1;
    private int graphDepth = 10;
    private Set<Integer> bfsDiscoveredIds;

    // Effective layout values (adjusted for visibility)
    private int effLeftW, effRightW, effContentX, effContentW, effRightX;

    // Right panel text entries (drawn with custom font)
    private List<int[]> rightPanelTexts = new ArrayList<>(); // [x, y, color, textIndex]
    private List<String> rightPanelTextStrings = new ArrayList<>();

    // Editor state (v4)
    private boolean editMode = false;
    private Dialog editingDialog = null;    // Clone for editing
    private Dialog originalDialog = null;   // Reference to actual dialog
    private int activeTab = 0;              // 0=Text, 1=Options, 2=Settings
    private boolean dirty = false;          // Has unsaved changes

    // Add dialog mode
    private boolean addDialogMode = false;  // Picking parent dialog for new dialog

    // Modern editor panel (v5)
    private DialogEditorPanel editorPanel;
    private boolean useModernEditor = true;  // Toggle for testing

    // Pending slot tracking for selection GUI callbacks
    private int pendingQuestSlot = -1;
    private int pendingDialogSlot = -1;
    private int pendingFactionSlot = -1;
    private int pendingOptionSlot = -1;
    private int pendingColorSlot = -1;

    /** Category graph mode */
    public GuiDialogTree(EntityNPCInterface npc, int categoryId, HashMap<String, Integer> dialogData) {
        super();
        this.npc = npc;
        this.categoryId = categoryId;
        if (dialogData != null) {
            this.dialogData = new HashMap<>(dialogData);
        }

        this.category = DialogController.Instance.categories.get(categoryId);

        leftPanelPercent = 0.15f;
        rightPanelPercent = 0.20f;
        minLeftPanelW = 100;
        minRightPanelW = 120;
        minCenterW = 200;

        enableDivider = true;
    }

    /** Individual dialog graph mode */
    public GuiDialogTree(EntityNPCInterface npc, int dialogId, int depth) {
        super();
        this.npc = npc;
        this.individualMode = true;
        this.individualRootId = dialogId;
        this.graphDepth = depth;

        // Look up the dialog and its category
        Dialog rootDialog = DialogController.Instance.dialogs.get(dialogId);
        if (rootDialog != null && rootDialog.category != null) {
            this.category = rootDialog.category;
            this.categoryId = category.id;
        } else {
            this.category = null;
            this.categoryId = -1;
        }

        leftPanelPercent = 0.15f;
        rightPanelPercent = 0.20f;
        minLeftPanelW = 100;
        minRightPanelW = 120;
        minCenterW = 200;

        enableDivider = true;
    }

    // ===== IDialogEditorParent =====

    @Override
    public HashMap<String, Integer> getDialogData() {
        return dialogData;
    }

    @Override
    public GuiCustomScroll getDialogScroll() {
        return dialogListScroll;
    }

    @Override
    public String getDialogQuestName() {
        return dialogQuestName;
    }

    @Override
    public void setDialogQuestName(String name) {
        dialogQuestName = name;
    }

    // ===== LAYOUT =====

    @Override
    protected void computeLayout() {
        super.computeLayout();

        // Initialize right divider offset
        if (rightDividerOffset == 0) {
            rightDividerOffset = rightPanelW;
        }

        // Enforce minimum width in edit mode so all controls are visible
        if (editMode && rightPanelVisible) {
            int minEditWidth = 280;
            rightDividerOffset = Math.max(rightDividerOffset, minEditWidth);
        }

        // Compute effective panel sizes based on visibility
        effLeftW = leftPanelVisible ? leftPanelW : 0;
        effRightW = rightPanelVisible ? (rightDividerOffset > 0 ? rightDividerOffset : rightPanelW) : 0;

        int gapCount = (effLeftW > 0 ? 1 : 0) + (effRightW > 0 ? 1 : 0) + 1;
        effContentW = usableW - effLeftW - effRightW - gapCount * gap;
        if (effContentW < minCenterW) effContentW = minCenterW;

        effContentX = originX + effLeftW + (effLeftW > 0 ? gap : 0);
        effRightX = effContentX + effContentW + gap;

        // Adjust contentY to account for legend bar
        contentY = originY + topBarH + legendBarH + gap;
        contentH = usableH - topBarH - legendBarH - gap;
    }

    @Override
    public void initGui() {
        super.initGui();
        // Recompute after super since super calls computeLayout
        computeLayout();
    }

    // ===== PANEL IMPLEMENTATIONS =====

    @Override
    protected void initTopBar(int topBtnY) {
        // Left: Hide Dir button
        addButton(new GuiNpcButton(50, originX + 2, topBtnY, 50, btnH,
            leftPanelVisible ? "Hide Dir" : "Show Dir"));

        // Add/Remove dialog buttons (next to Hide Dir, in category mode)
        if (!individualMode && category != null) {
            addButton(new GuiNpcButton(52, originX + 55, topBtnY, 20, btnH, "+"));
            addButton(new GuiNpcButton(53, originX + 78, topBtnY, 20, btnH, "-"));
            getButton(53).setEnabled(selectedDialog != null);
        }

        // Center title
        String centerText;
        if (individualMode) {
            Dialog root = DialogController.Instance.dialogs.get(individualRootId);
            String rootName = root != null ? sanitize(root.title) : "Dialog #" + individualRootId;
            centerText = "Solo: " + rootName;
            if (selectedDialog != null && selectedDialog.id != individualRootId) {
                centerText += " > " + sanitize(selectedDialog.title);
            }
        } else {
            String catTitle = category != null ? category.title : "Unknown";
            centerText = catTitle;
            if (selectedDialog != null) {
                centerText = catTitle + ": " + sanitize(selectedDialog.title);
            }
        }
        int textW = fontRendererObj != null ? fontRendererObj.getStringWidth(centerText) : 100;
        int centerLabelX = originX + (usableW - textW) / 2;
        addLabel(new GuiNpcLabel(0, centerText, centerLabelX, topBtnY + 6, 0xFFFFFF));

        // Right buttons
        int btnX = originX + usableW;

        // Hide Info
        btnX -= 55;
        addButton(new GuiNpcButton(51, btnX, topBtnY, 53, btnH,
            rightPanelVisible ? "Hide Info" : "Show Info"));

        // Link (only when dialog selected AND NOT in edit mode)
        if (selectedDialog != null && !editMode) {
            btnX -= 48;
            GuiNpcButton linkBtn = new GuiNpcButton(4, btnX, topBtnY, 45, btnH,
                linkMode ? "Cancel" : "Link");
            if (linkMode) {
                linkBtn.packedFGColour = 0xFF5555;
            }
            addButton(linkBtn);
        }

        // Edit (only when dialog selected AND NOT in edit mode)
        if (selectedDialog != null && !editMode) {
            btnX -= 42;
            addButton(new GuiNpcButton(3, btnX, topBtnY, 40, btnH, "selectServer.edit"));
        }

        // Depth +/- buttons in individual mode (in legend bar row)
        if (individualMode) {
            int legendY = originY + topBarH;
            int depthBtnW = 14;
            int depthBtnH = legendBarH - 2;
            String depthLabel = "Depth: " + graphDepth;
            int dw = fontRendererObj != null ? fontRendererObj.getStringWidth(depthLabel) : 40;
            // Position further left to avoid clipping
            int rx = originX + usableW - 40 - dw;
            addButton(new GuiNpcButton(61, rx + dw + 2, legendY + 1, depthBtnW, depthBtnH, "+"));
            addButton(new GuiNpcButton(60, rx - depthBtnW - 2, legendY + 1, depthBtnW, depthBtnH, "-"));
        }
    }

    @Override
    protected void initLeftPanel() {
        if (!leftPanelVisible) return;

        int scrollH = contentH - 24;

        if (dialogListScroll == null) {
            dialogListScroll = new GuiCustomScroll(this, 0, 0);
        }
        dialogListScroll.guiLeft = originX;
        dialogListScroll.guiTop = contentY;
        dialogListScroll.setSize(effLeftW, scrollH);

        List<String> filteredList = getFilteredDialogList();
        dialogListScroll.setList(filteredList);

        if (selectedDialog != null) {
            dialogListScroll.setSelected(selectedDialog.title);
        }
        addScroll(dialogListScroll);

        // Search bar below scroll
        GuiNpcTextField searchField = new GuiNpcTextField(80, this, fontRendererObj,
            originX, contentY + scrollH + 2, effLeftW, 20, dialogSearch);
        searchField.setMaxStringLength(64);
        addTextField(searchField);
    }

    @Override
    protected void initCenterPanel() {
        if (!individualMode && (category == null || category.dialogs.isEmpty())) return;
        if (individualMode && individualRootId < 0) return;

        // Save pan/zoom from old diagram before recreating
        if (diagram != null) {
            savedPanX = diagram.getPanX();
            savedPanY = diagram.getPanY();
            savedZoom = diagram.getZoom();
            hasSavedView = true;
        }

        int diagramW = effContentW;
        int diagramX = effContentX;

        diagram = new DialogGraphDiagram(this, diagramX, contentY, diagramW, contentH);
        if (selectedDialog != null) {
            diagram.setSelectedNode(selectedDialog.id);
        }

        // Restore pan/zoom
        if (hasSavedView) {
            diagram.setPanX(savedPanX);
            diagram.setPanY(savedPanY);
            diagram.setZoom(savedZoom);
        }
    }

    @Override
    protected void initRightPanel(int startY) {
        rightPanelTexts.clear();
        rightPanelTextStrings.clear();
        if (!rightPanelVisible) {
            editorPanel = null;
            return;
        }

        if (selectedDialog == null) {
            editorPanel = null;
            addRightText(effRightX + 4, startY, 0x808080, "Select a dialog");
            return;
        }

        if (editMode && editingDialog != null && useModernEditor) {
            // Use modern DialogEditorPanel
            if (editorPanel == null) {
                editorPanel = new DialogEditorPanel(effRightX, contentY, effRightW, contentH);
                editorPanel.setListener(new IDialogEditorListener() {
                    @Override
                    public void onSaveRequested() {
                        saveEditingDialog();
                    }

                    @Override
                    public void onTestRequested() {
                        if (editingDialog == null) return;
                        EntityDialogNpc testNpc = new EntityDialogNpc(player.worldObj);
                        testNpc.display.name = "TEST";
                        EntityUtil.Copy(player, testNpc);
                        NoppesUtil.openGUI(player, new GuiDialogInteract(GuiDialogTree.this, testNpc, editingDialog));
                    }

                    @Override
                    public void onQuestSelectRequested(int slot) {
                        pendingQuestSlot = slot;
                        int currentQuestId = -1;
                        if (slot == 0 && editingDialog != null) {
                            currentQuestId = editingDialog.quest;
                        }
                        setSubGui(new ModernQuestSelector(currentQuestId));
                    }

                    @Override
                    public void onDialogSelectRequested(int slot) {
                        pendingDialogSlot = slot;
                        int currentDialogId = -1;
                        setSubGui(new ModernDialogSelector(currentDialogId));
                    }

                    @Override
                    public void onFactionSelectRequested(int slot) {
                        pendingFactionSlot = slot;
                        int currentFactionId = -1;
                        // Get current faction ID from availability if available
                        setSubGui(new ModernFactionSelector(currentFactionId));
                    }

                    @Override
                    public void onSoundSelectRequested() {
                        String currentSound = editingDialog != null ? editingDialog.sound : "";
                        setSubGui(new ModernSoundSelector(currentSound));
                    }

                    @Override
                    public void onColorSelectRequested(int slot, int currentColor) {
                        pendingColorSlot = slot;
                        setSubGui(new ModernColorPicker(currentColor));
                    }

                    @Override
                    public void onMailSetupRequested() {
                        if (editingDialog != null) {
                            setSubGui(new ModernMailEditor(editingDialog.mail));
                        }
                    }

                    @Override
                    public void onOptionDialogSelectRequested(int optionSlot) {
                        pendingOptionSlot = optionSlot;
                        int currentDialogId = -1;
                        if (editingDialog != null) {
                            DialogOption opt = editingDialog.options.get(optionSlot);
                            if (opt != null) {
                                currentDialogId = opt.dialogId;
                            }
                        }
                        setSubGui(new ModernDialogSelector(currentDialogId));
                    }
                });
                editorPanel.setDialog(editingDialog);
            }
            editorPanel.setBounds(effRightX, contentY, effRightW, contentH);
            // Only call setDialog() if editing a different dialog - preserve dirty state for same dialog
            if (editorPanel.getDialog() != editingDialog) {
                editorPanel.setDialog(editingDialog);
            }
        } else if (editMode && editingDialog != null) {
            // Legacy edit mode
            editorPanel = null;
            initRightPanelEditMode(startY);
        } else {
            // View mode
            editorPanel = null;
            initRightPanelViewMode(startY);
        }
    }

    /** Right panel in view mode (read-only info) */
    private void initRightPanelViewMode(int startY) {
        int y = startY;
        int panelW = effRightW - 8;
        int fh = cfontHeight();

        // Title
        addRightText(effRightX + 4, y, 0xFFFFFF, sanitize(selectedDialog.title));
        y += fh + 2;

        // ID
        addRightText(effRightX + 4, y, 0xAAAAAA, "ID: " + selectedDialog.id);
        y += fh + 4;

        // Quest info
        if (selectedDialog.quest > 0) {
            String questName = "Quest #" + selectedDialog.quest;
            if (QuestController.Instance != null) {
                Quest q = QuestController.Instance.quests.get(selectedDialog.quest);
                if (q != null) questName = q.title;
            }
            List<String> questLines = cwrapText("Quest: " + questName, panelW);
            for (String line : questLines) {
                addRightText(effRightX + 4, y, 0xFFD700, line);
                y += fh;
            }
            y += 4;
        }

        // Options header
        addRightText(effRightX + 4, y, 0xCCCCCC, "Options:");
        y += fh + 2;

        for (Map.Entry<Integer, DialogOption> entry : selectedDialog.options.entrySet()) {
            DialogOption opt = entry.getValue();
            if (opt.optionType == EnumOptionType.Disabled) continue;

            int slot = entry.getKey();
            String optText = slot + ": " + sanitize(opt.title);

            int color = 0xAAAAAA;
            if (opt.optionType == EnumOptionType.DialogOption && opt.dialogId > 0) {
                String targetName = resolveDialogName(opt.dialogId);
                optText += " -> " + targetName;
                color = 0x55FF55;
            } else if (opt.optionType == EnumOptionType.QuitOption) {
                optText += " [Quit]";
                color = 0xFF5555;
            } else if (opt.optionType == EnumOptionType.CommandBlock) {
                optText += " [Cmd]";
                color = 0xFFAA00;
            } else if (opt.optionType == EnumOptionType.RoleOption) {
                optText += " [Role]";
                color = 0x55AAFF;
            }

            List<String> lines = cwrapText(optText, panelW);
            for (String line : lines) {
                addRightText(effRightX + 4, y, color, line);
                y += fh;
            }
            y += 2;
        }

        // Dialog text preview
        if (selectedDialog.text != null && !selectedDialog.text.isEmpty()) {
            y += 6;
            addRightText(effRightX + 4, y, 0xCCCCCC, "Text:");
            y += fh + 2;
            String preview = sanitize(selectedDialog.text).replace("\n", " ");
            if (preview.length() > 200) preview = preview.substring(0, 200) + "...";
            List<String> textLines = cwrapText(preview, panelW);
            for (String line : textLines) {
                addRightText(effRightX + 4, y, 0x808080, line);
                y += fh;
            }
        }
    }

    /** Right panel in edit mode (tab bar + editable fields) */
    private void initRightPanelEditMode(int startY) {
        int y = startY;
        int panelW = effRightW - 8;

        // Tab bar
        int tabW = Math.min(50, panelW / 3 - 2);
        int tabX = effRightX + 4;

        // Tab buttons with visual state
        GuiNpcButton textTab = new GuiNpcButton(200, tabX, y, tabW, 16, "Text");
        GuiNpcButton optionsTab = new GuiNpcButton(201, tabX + tabW + 2, y, tabW, 16, "Options");
        GuiNpcButton settingsTab = new GuiNpcButton(202, tabX + 2 * (tabW + 2), y, tabW, 16, "Settings");

        // Highlight active tab
        if (activeTab == 0) textTab.setEnabled(false);
        if (activeTab == 1) optionsTab.setEnabled(false);
        if (activeTab == 2) settingsTab.setEnabled(false);

        addButton(textTab);
        addButton(optionsTab);
        addButton(settingsTab);

        y += 20;

        // Dirty indicator in title
        String titleStr = sanitize(editingDialog.title);
        if (dirty) titleStr += " *";
        addRightText(effRightX + 4, y, 0xFFFFFF, titleStr);
        y += cfontHeight() + 4;

        // Tab content
        switch (activeTab) {
            case 0:
                initTextTab(y, panelW);
                break;
            case 1:
                initOptionsTab(y, panelW);
                break;
            case 2:
                initSettingsTab(y, panelW);
                break;
        }

        // Save button at bottom
        int saveY = contentY + contentH - 24;
        addButton(new GuiNpcButton(210, effRightX + 4, saveY, panelW, 20, "gui.save"));
    }

    /** Text tab - title and dialog text editing */
    private void initTextTab(int startY, int panelW) {
        int y = startY;
        int fh = cfontHeight();

        // Title label
        addRightText(effRightX + 4, y, 0xCCCCCC, "Title:");
        y += fh + 2;

        // Title text field
        GuiNpcTextField titleField = new GuiNpcTextField(100, this, fontRendererObj,
            effRightX + 4, y, panelW, 20, editingDialog.title);
        titleField.setMaxStringLength(64);
        addTextField(titleField);
        y += 24;

        // Dialog text label
        addRightText(effRightX + 4, y, 0xCCCCCC, "Dialog Text:");
        y += fh + 2;

        // Multi-line text area (using basic text field for now - can be enhanced later)
        // Calculate remaining height for text area
        int remainingH = (contentY + contentH - 28) - y;
        int textAreaH = Math.max(60, remainingH);

        // For now, show preview text and add a note
        addRightText(effRightX + 4, y, 0x808080, "(Text editing via Edit dialog)");
    }

    /** Options tab - list of dialog options */
    private void initOptionsTab(int startY, int panelW) {
        int y = startY;
        int fh = cfontHeight();

        addRightText(effRightX + 4, y, 0xCCCCCC, "Dialog Options:");
        y += fh + 4;

        int optionBtnH = 18;
        int optionIndex = 0;

        for (Map.Entry<Integer, DialogOption> entry : editingDialog.options.entrySet()) {
            DialogOption opt = entry.getValue();
            if (opt.optionType == EnumOptionType.Disabled) continue;

            int slot = entry.getKey();
            String label = slot + ": " + sanitize(opt.title);
            if (label.length() > 20) label = label.substring(0, 17) + "...";

            int color = 0xAAAAAA;
            if (opt.optionType == EnumOptionType.DialogOption && opt.dialogId > 0) {
                color = 0x55FF55;
            } else if (opt.optionType == EnumOptionType.QuitOption) {
                color = 0xFF5555;
            } else if (opt.optionType == EnumOptionType.CommandBlock) {
                color = 0xFFAA00;
            }

            // Option row with edit button
            addRightText(effRightX + 4, y + 4, color, label);
            y += optionBtnH + 2;
            optionIndex++;

            if (y > contentY + contentH - 60) break; // Don't overflow
        }

        // Add Option button (if less than 6 options)
        if (editingDialog.options.size() < 6) {
            y += 4;
            addButton(new GuiNpcButton(220, effRightX + 4, y, panelW / 2 - 2, 18, "+ Add"));
        }
    }

    /** Settings tab - quest, availability, etc. */
    private void initSettingsTab(int startY, int panelW) {
        int y = startY;
        int fh = cfontHeight();

        // Quest info
        addRightText(effRightX + 4, y, 0xCCCCCC, "Quest:");
        y += fh + 2;
        String questLabel = editingDialog.quest > 0 ? "Quest #" + editingDialog.quest : "None";
        if (editingDialog.quest > 0 && QuestController.Instance != null) {
            Quest q = QuestController.Instance.quests.get(editingDialog.quest);
            if (q != null) questLabel = q.title;
        }
        addRightText(effRightX + 4, y, 0xFFD700, questLabel);
        y += fh + 8;

        // Show Wheel toggle
        addRightText(effRightX + 4, y, 0xCCCCCC, "Show Wheel: " + (editingDialog.showWheel ? "Yes" : "No"));
        y += fh + 4;

        // Hide NPC toggle
        addRightText(effRightX + 4, y, 0xCCCCCC, "Hide NPC: " + (editingDialog.hideNPC ? "Yes" : "No"));
        y += fh + 4;

        // Disable ESC toggle
        addRightText(effRightX + 4, y, 0xCCCCCC, "Disable ESC: " + (editingDialog.disableEsc ? "Yes" : "No"));
        y += fh + 8;

        // Note about full settings
        addRightText(effRightX + 4, y, 0x808080, "(Full settings via Edit dialog)");
    }

    private void addRightText(int px, int py, int color, String text) {
        int idx = rightPanelTextStrings.size();
        rightPanelTextStrings.add(text);
        rightPanelTexts.add(new int[]{px, py, color, idx});
    }

    // ===== SEARCH =====

    private List<String> getFilteredDialogList() {
        TreeMap<String, Integer> sorted = new TreeMap<>();
        if (individualMode) {
            Set<Integer> discovered = discoverDialogs();
            for (int id : discovered) {
                Dialog d = DialogController.Instance.dialogs.get(id);
                if (d != null) {
                    sorted.put(d.title, d.id);
                }
            }
        } else if (category != null) {
            for (Dialog d : category.dialogs.values()) {
                sorted.put(d.title, d.id);
            }
        }
        dialogData = new HashMap<>(sorted);

        if (dialogSearch == null || dialogSearch.isEmpty()) {
            return new ArrayList<>(sorted.keySet());
        }

        String lower = dialogSearch.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String name : sorted.keySet()) {
            if (name.toLowerCase().contains(lower)) {
                filtered.add(name);
            }
        }
        return filtered;
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == 80) {
            dialogSearch = textField.getText();
        }
        // Title field in edit mode
        if (textField.id == 100 && editMode && editingDialog != null) {
            String newTitle = textField.getText();
            if (!newTitle.equals(editingDialog.title)) {
                editingDialog.title = newTitle;
                dirty = true;
            }
        }
    }

    @Override
    public void keyTyped(char c, int key) {
        // ESC key handling - check SubGui FIRST
        if (key == 1) { // ESC key
            // If SubGui is open, let parent handle ESC to close it FIRST
            if (hasSubGui()) {
                super.keyTyped(c, key);
                return;
            }
            // Then check add dialog mode, link mode, edit mode
            if (addDialogMode) {
                addDialogMode = false;
                initGui();
                return;
            }
            if (linkMode) {
                linkMode = false;
                linkSourceId = -1;
                initGui();
                return;
            }
            // ESC exits edit mode (with auto-save)
            if (editMode) {
                exitEditMode();
                return;
            }
        }

        // Ctrl+S to save in edit mode
        if (editMode && key == 31 && net.minecraft.client.gui.GuiScreen.isCtrlKeyDown()) { // S key
            saveEditingDialog();
            return;
        }

        // Modern editor panel keyboard handling
        if (editorPanel != null && editMode && !hasSubGui()) {
            if (editorPanel.keyTyped(c, key)) {
                dirty = editorPanel.isDirty();
                return;
            }
        }

        super.keyTyped(c, key);
        // Live search update
        GuiNpcTextField searchField = getTextField(80);
        if (searchField != null && searchField.isFocused()) {
            String newText = searchField.getText();
            if (!newText.equals(dialogSearch)) {
                dialogSearch = newText;
                if (dialogListScroll != null) {
                    dialogListScroll.setList(getFilteredDialogList());
                    if (selectedDialog != null) {
                        dialogListScroll.setSelected(selectedDialog.title);
                    }
                }
            }
        }
    }

    // ===== HELPERS =====

    private static String sanitize(String s) {
        return s == null ? "" : s.replace("\r", "");
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (fontRendererObj == null || text == null || text.isEmpty()) {
            lines.add(text != null ? text : "");
            return lines;
        }
        List wrappedLines = fontRendererObj.listFormattedStringToWidth(text, maxWidth);
        for (Object line : wrappedLines) {
            lines.add((String) line);
        }
        if (lines.isEmpty()) lines.add(text);
        return lines;
    }

    // Custom font helpers for right panel (using ClientProxy.Font)
    private int cfontWidth(String text) {
        return ClientProxy.Font.width(text);
    }

    private int cfontHeight() {
        return ClientProxy.Font.height();
    }

    private void cfontDraw(String text, int px, int py, int color) {
        ClientProxy.Font.drawString(text, px, py, color);
    }

    /** Word-wrap using custom font metrics */
    private List<String> cwrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add(text != null ? text : "");
            return lines;
        }
        // Simple word wrap using custom font width
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
            } else {
                String test = current + " " + word;
                if (cfontWidth(test) > maxWidth) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current.append(" ").append(word);
                }
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        if (lines.isEmpty()) lines.add(text);
        return lines;
    }

    /** BFS from root dialog, following outgoing + incoming links, up to graphDepth */
    private Set<Integer> discoverDialogs() {
        if (bfsDiscoveredIds != null) return bfsDiscoveredIds;
        bfsDiscoveredIds = new HashSet<>();
        if (individualRootId < 0) return bfsDiscoveredIds;

        // Build reverse link map: targetId -> list of sourceIds
        Map<Integer, List<Integer>> reverseLinks = new HashMap<>();
        Map<Integer, List<Integer>> forwardLinks = new HashMap<>();
        for (Dialog d : DialogController.Instance.dialogs.values()) {
            List<Integer> fwd = new ArrayList<>();
            for (DialogOption opt : d.options.values()) {
                if (opt.optionType == EnumOptionType.DialogOption && opt.dialogId > 0) {
                    fwd.add(opt.dialogId);
                    if (!reverseLinks.containsKey(opt.dialogId)) {
                        reverseLinks.put(opt.dialogId, new ArrayList<Integer>());
                    }
                    reverseLinks.get(opt.dialogId).add(d.id);
                }
            }
            forwardLinks.put(d.id, fwd);
        }

        // BFS
        Queue<int[]> queue = new LinkedList<>(); // [dialogId, depth]
        queue.add(new int[]{individualRootId, 0});
        bfsDiscoveredIds.add(individualRootId);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int curId = current[0];
            int curDepth = current[1];
            if (curDepth >= graphDepth) continue;

            // Forward links
            List<Integer> fwd = forwardLinks.get(curId);
            if (fwd != null) {
                for (int targetId : fwd) {
                    if (!bfsDiscoveredIds.contains(targetId)) {
                        bfsDiscoveredIds.add(targetId);
                        queue.add(new int[]{targetId, curDepth + 1});
                    }
                }
            }
            // Reverse links
            List<Integer> rev = reverseLinks.get(curId);
            if (rev != null) {
                for (int sourceId : rev) {
                    if (!bfsDiscoveredIds.contains(sourceId)) {
                        bfsDiscoveredIds.add(sourceId);
                        queue.add(new int[]{sourceId, curDepth + 1});
                    }
                }
            }
        }
        return bfsDiscoveredIds;
    }

    private String resolveDialogName(int dialogId) {
        if (category != null) {
            Dialog target = category.dialogs.get(dialogId);
            if (target != null) return target.title;
        }
        Dialog global = DialogController.Instance.dialogs.get(dialogId);
        if (global != null) return global.title;
        return "External #" + dialogId;
    }

    // ===== DRAWING =====

    @Override
    protected void drawPanels() {
        // Left panel border
        if (leftPanelVisible) {
            GuiUtil.drawRectD(originX - 1, contentY - 1, originX + effLeftW + 1,
                originY + usableH + 1, panelBorder);
        }
        // Right panel border
        if (rightPanelVisible && effRightW > 0) {
            GuiUtil.drawRectD(effRightX - 1, contentY - 1, effRightX + effRightW + 1,
                originY + usableH + 1, panelBorder);
        }
    }

    @Override
    protected void drawOverlay(int mouseX, int mouseY, float partialTicks) {
        // Diagram (draw first so legend bar + buttons appear on top)
        // Skip when SubGui is open so SubGui appears on top
        if (diagram != null && !hasSubGui()) {
            diagram.drawDiagram(mouseX, mouseY);
        }

        // Legend bar (drawn after diagram so it appears on top)
        drawLegendBar();

        // Right divider handle - draw at left edge of right panel
        if (rightPanelVisible && rightDividerOffset > 0) {
            int divX = effRightX - rightDividerWidth;
            int handleTop = contentY + (contentH - rightDividerLineHeight) / 2;
            drawRect(divX + 1, handleTop, divX + rightDividerWidth - 1,
                handleTop + rightDividerLineHeight, 0xFF707070);
        }

        // Right panel - always draw (SubGui re-draws on top below)
        if (rightPanelVisible) {
            if (editorPanel != null && editMode) {
                editorPanel.draw(mouseX, mouseY);
            } else if (!rightPanelTexts.isEmpty()) {
                for (int[] entry : rightPanelTexts) {
                    String text = rightPanelTextStrings.get(entry[3]);
                    cfontDraw(text, entry[0], entry[1], entry[2]);
                }
            }
        }

        // Link mode indicator
        if (linkMode) {
            String msg;
            if (linkSourceId < 0) {
                msg = "Link Mode: Click source dialog";
            } else {
                msg = "Link Mode: Click target dialog";
            }
            int msgW = fontRendererObj.getStringWidth(msg);
            int msgX = effContentX + (effContentW - msgW) / 2;
            drawRect(msgX - 4, contentY + 2, msgX + msgW + 4, contentY + 14, 0xC0000000);
            fontRendererObj.drawString(msg, msgX, contentY + 4, 0xFFFF55);
        }

        // Add dialog mode indicator
        if (addDialogMode) {
            String msg = "Pick a Dialog to extend to (or ESC to cancel)";
            int msgW = fontRendererObj.getStringWidth(msg);
            int msgX = effContentX + (effContentW - msgW) / 2;
            drawRect(msgX - 4, contentY + 2, msgX + msgW + 4, contentY + 14, 0xC0000000);
            fontRendererObj.drawString(msg, msgX, contentY + 4, 0x55FF55);
        }

        // Re-draw SubGui on top of overlay content (z-order fix)
        if (hasSubGui()) {
            getSubGui().drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    private void drawLegendBar() {
        int ly = originY + topBarH;
        drawRect(originX, ly, originX + usableW, ly + legendBarH, 0xFF181818);

        int lx = originX + 6;
        int ty = ly + 3;
        int boxSize = 8;
        int spacing = 6;

        // Dialog (blue)
        drawRect(lx, ty, lx + boxSize, ty + boxSize, 0xFF4488CC);
        lx += boxSize + 2;
        fontRendererObj.drawString("Dialog", lx, ty, 0xCCCCCC);
        lx += fontRendererObj.getStringWidth("Dialog") + spacing;

        // Quest (gold)
        drawRect(lx, ty, lx + boxSize, ty + boxSize, 0xFFCC8800);
        lx += boxSize + 2;
        fontRendererObj.drawString("Quest", lx, ty, 0xCCCCCC);
        lx += fontRendererObj.getStringWidth("Quest") + spacing;

        // Quit (red)
        drawRect(lx, ty, lx + boxSize, ty + boxSize, 0xFFFF5555);
        lx += boxSize + 2;
        fontRendererObj.drawString("Quit", lx, ty, 0xCCCCCC);
        lx += fontRendererObj.getStringWidth("Quit") + spacing;

        // Command (orange)
        drawRect(lx, ty, lx + boxSize, ty + boxSize, 0xFFFFAA00);
        lx += boxSize + 2;
        fontRendererObj.drawString("Command", lx, ty, 0xCCCCCC);
        lx += fontRendererObj.getStringWidth("Command") + spacing;

        // Terminal (gray)
        drawRect(lx, ty, lx + boxSize, ty + boxSize, 0xFF666666);
        lx += boxSize + 2;
        fontRendererObj.drawString("Terminal", lx, ty, 0xCCCCCC);

        // Depth controls in individual mode (right side of legend bar)
        if (individualMode) {
            String depthLabel = "Depth: " + graphDepth;
            int dw = fontRendererObj.getStringWidth(depthLabel);
            int rx = originX + usableW - 40 - dw;
            fontRendererObj.drawString(depthLabel, rx, ty, 0xCCCCCC);

            // Manually draw the depth buttons on top of legend bar background
            int depthBtnW = 14;
            int depthBtnH = legendBarH - 2;
            int btnY = ly + 1;

            // Draw "-" button
            int minusBtnX = rx - depthBtnW - 2;
            drawRect(minusBtnX, btnY, minusBtnX + depthBtnW, btnY + depthBtnH, 0xFF505050);
            drawRect(minusBtnX + 1, btnY + 1, minusBtnX + depthBtnW - 1, btnY + depthBtnH - 1, 0xFF303030);
            fontRendererObj.drawString("-", minusBtnX + (depthBtnW - fontRendererObj.getStringWidth("-")) / 2, btnY + 2, 0xFFFFFF);

            // Draw "+" button
            int plusBtnX = rx + dw + 2;
            drawRect(plusBtnX, btnY, plusBtnX + depthBtnW, btnY + depthBtnH, 0xFF505050);
            drawRect(plusBtnX + 1, btnY + 1, plusBtnX + depthBtnW - 1, btnY + depthBtnH - 1, 0xFF303030);
            fontRendererObj.drawString("+", plusBtnX + (depthBtnW - fontRendererObj.getStringWidth("+")) / 2, btnY + 2, 0xFFFFFF);
        }
    }

    // ===== ESC / CLOSE =====

    @Override
    public void close() {
        // Auto-save on close
        if (editMode && dirty) {
            saveEditingDialog();
        }
        NoppesUtil.openGUI(player, new GuiNPCManageDialogs(npc));
    }

    // ===== LEFT DIVIDER =====

    @Override
    protected void onDividerMoved(int newOffset) {
        leftPanelW = newOffset;
        initGui();
    }

    // ===== INTERACTIONS =====

    public void selectDialog(int dialogId) {
        // Auto-save current editing dialog if dirty
        if (editMode && dirty && editingDialog != null) {
            saveEditingDialog();
        }

        Dialog newDialog;
        if (individualMode) {
            newDialog = DialogController.Instance.dialogs.get(dialogId);
        } else {
            if (category != null) {
                newDialog = category.dialogs.get(dialogId);
            } else {
                newDialog = null;
            }
            if (newDialog == null) {
                newDialog = DialogController.Instance.dialogs.get(dialogId);
            }
        }

        selectedDialog = newDialog;

        if (selectedDialog != null && selectedDialog.quest > 0 && QuestController.Instance != null) {
            Quest q = QuestController.Instance.quests.get(selectedDialog.quest);
            dialogQuestName = q != null ? q.title : "";
        } else {
            dialogQuestName = "";
        }

        // Exit edit mode when selecting a different dialog (switch to view mode)
        // User must explicitly click Edit or double-click to re-enter edit mode
        if (editMode) {
            editMode = false;
            editingDialog = null;
            originalDialog = null;
            dirty = false;
        }

        if (diagram != null) {
            diagram.setSelectedNode(dialogId);
        }
        initGui();
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Edit button - enter edit mode (button only shows when NOT in edit mode)
        if (id == 3 && selectedDialog != null && !editMode) {
            enterEditMode(selectedDialog);
        }

        // Link mode toggle
        if (id == 4) {
            linkMode = !linkMode;
            linkSourceId = -1;
            initGui();
        }

        // Hide Dir toggle
        if (id == 50) {
            leftPanelVisible = !leftPanelVisible;
            initGui();
        }

        // Hide Info toggle
        if (id == 51) {
            rightPanelVisible = !rightPanelVisible;
            initGui();
        }

        // Depth controls (individual mode)
        if (id == 60 && individualMode) {
            graphDepth = Math.max(5, graphDepth - 1);
            bfsDiscoveredIds = null;
            if (diagram != null) diagram.invalidateCache();
            initGui();
        }
        if (id == 61 && individualMode) {
            graphDepth = Math.min(20, graphDepth + 1);
            bfsDiscoveredIds = null;
            if (diagram != null) diagram.invalidateCache();
            initGui();
        }

        // Tab buttons (edit mode)
        if (id == 200) {
            activeTab = 0;
            initGui();
        }
        if (id == 201) {
            activeTab = 1;
            initGui();
        }
        if (id == 202) {
            activeTab = 2;
            initGui();
        }

        // Add dialog button
        if (id == 52 && !individualMode && category != null) {
            if (category.dialogs.isEmpty()) {
                // Create first dialog directly
                createNewDialog(null);
            } else {
                // Enter "pick parent" mode
                addDialogMode = true;
                initGui();
            }
        }

        // Remove dialog button
        if (id == 53 && !individualMode && selectedDialog != null) {
            // Show confirmation and delete
            deleteSelectedDialog();
        }

        // Save button (edit mode)
        if (id == 210) {
            saveEditingDialog();
        }

        // Add Option button (options tab)
        if (id == 220 && editMode && editingDialog != null) {
            // Find first available slot
            for (int slot = 0; slot < 6; slot++) {
                if (!editingDialog.options.containsKey(slot) ||
                    editingDialog.options.get(slot).optionType == EnumOptionType.Disabled) {
                    DialogOption newOpt = new DialogOption();
                    newOpt.title = "New Option";
                    newOpt.optionType = EnumOptionType.QuitOption;
                    editingDialog.options.put(slot, newOpt);
                    dirty = true;
                    initGui();
                    break;
                }
            }
        }
    }

    /** Enter edit mode for a dialog */
    private void enterEditMode(Dialog dialog) {
        // Auto-save previous if dirty
        if (editMode && dirty) {
            saveEditingDialog();
        }

        originalDialog = dialog;
        editingDialog = cloneDialog(dialog);
        editMode = true;
        activeTab = 0;
        dirty = false;
        initGui();
    }

    /** Exit edit mode */
    private void exitEditMode() {
        if (dirty) {
            saveEditingDialog();
        }
        editMode = false;
        editingDialog = null;
        originalDialog = null;
        dirty = false;
        initGui();
    }

    /** Clone a dialog for editing */
    private Dialog cloneDialog(Dialog original) {
        NBTTagCompound nbt = new NBTTagCompound();
        original.writeToNBT(nbt);
        Dialog clone = new Dialog();
        clone.readNBT(nbt);
        return clone;
    }

    /** Save the editing dialog back to the original and server */
    private void saveEditingDialog() {
        if (!editMode || editingDialog == null || originalDialog == null) return;

        // Sync from modern editor panel if active
        if (editorPanel != null && useModernEditor) {
            editorPanel.saveToDialog();
        }

        // Copy changes back to original
        originalDialog.title = editingDialog.title;
        originalDialog.text = editingDialog.text;
        originalDialog.quest = editingDialog.quest;
        originalDialog.showWheel = editingDialog.showWheel;
        originalDialog.hideNPC = editingDialog.hideNPC;
        originalDialog.disableEsc = editingDialog.disableEsc;
        originalDialog.darkenScreen = editingDialog.darkenScreen;
        originalDialog.sound = editingDialog.sound;
        originalDialog.options.clear();
        originalDialog.options.putAll(editingDialog.options);

        // Send save packet
        int catId = originalDialog.category != null ? originalDialog.category.id : categoryId;
        PacketClient.sendClient(new DialogSavePacket(catId,
            originalDialog.writeToNBT(new NBTTagCompound()), false));

        dirty = false;
        if (editorPanel != null) {
            editorPanel.clearDirty();
        }

        // Refresh diagram
        bfsDiscoveredIds = null;
        if (diagram != null) diagram.invalidateCache();
        initGui();
    }

    /** Create a new dialog, optionally linked from a parent */
    private void createNewDialog(Dialog parent) {
        if (category == null) return;

        // Generate unique name
        String name = "New Dialog";
        int suffix = 1;
        while (dialogData.containsKey(name)) {
            name = "New Dialog " + suffix++;
        }

        Dialog newDialog = new Dialog();
        newDialog.title = name;

        // Save new dialog to server
        PacketClient.sendClient(new DialogSavePacket(categoryId,
            newDialog.writeToNBT(new NBTTagCompound()), true));

        // If parent specified, open link dialog to connect them
        if (parent != null) {
            // We'll handle the linking after the dialog is created
            // For now, just refresh
        }

        addDialogMode = false;
        initGui();
    }

    /** Delete the currently selected dialog */
    private void deleteSelectedDialog() {
        if (selectedDialog == null) return;

        // Exit edit mode if editing this dialog
        if (editMode && editingDialog != null && editingDialog.id == selectedDialog.id) {
            editMode = false;
            editingDialog = null;
            originalDialog = null;
            dirty = false;
        }

        // Send delete packet
        PacketClient.sendClient(new kamkeel.npcs.network.packets.request.dialog.DialogRemovePacket(
            selectedDialog.id, true));

        // Clear selection
        selectedDialog = null;
        if (diagram != null) {
            diagram.setSelectedNode(-1);
            diagram.invalidateCache();
        }

        initGui();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Modern editor panel handling
        if (editorPanel != null && editMode && !hasSubGui()) {
            if (editorPanel.isInside(mouseX, mouseY)) {
                if (editorPanel.mouseClicked(mouseX, mouseY, mouseButton)) {
                    // Sync dirty state
                    dirty = editorPanel.isDirty();
                    return;
                }
            }
        }

        // Right divider handling - match draw position (left edge of right panel)
        if (rightPanelVisible && !hasSubGui() && rightDividerOffset > 0) {
            int divX = effRightX - rightDividerWidth;
            int handleTop = contentY + (contentH - rightDividerLineHeight) / 2;
            int handleBottom = handleTop + rightDividerLineHeight;
            if (mouseX >= divX && mouseX <= divX + rightDividerWidth &&
                mouseY >= handleTop && mouseY <= handleBottom) {
                isResizingRight = true;
                resizingActive = true;
                rightDragStartX = mouseX;
                return;
            }
        }

        // Diagram mouse handling
        if (diagram != null && !hasSubGui()) {
            if (diagram.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isResizingRight) {
            int dx = mouseX - rightDragStartX;
            rightDragStartX = mouseX;
            rightDividerOffset -= dx;
            int maxRight = usableW - effLeftW - minCenterW - 3 * gap;
            rightDividerOffset = Math.max(minRightDividerPanelW, Math.min(rightDividerOffset, maxRight));
            initGui();
            return;
        }

        if (diagram != null && !hasSubGui()) {
            diagram.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (isResizingRight) {
            isResizingRight = false;
            resizingActive = false;
            return;
        }

        if (diagram != null) {
            diagram.mouseReleased(mouseX, mouseY, state);
        }

        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0 && scroll.getSelected() != null) {
            Integer dialogId = dialogData.get(scroll.getSelected());
            if (dialogId != null) {
                selectDialog(dialogId);
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            Integer dialogId = dialogData.get(selection);
            if (dialogId != null) {
                Dialog d = individualMode
                    ? DialogController.Instance.dialogs.get(dialogId)
                    : (category != null ? category.dialogs.get(dialogId) : null);
                if (d != null) {
                    selectedDialog = d;
                    // Double-click enters inline edit mode (not SubGui popup)
                    enterEditMode(d);
                }
            }
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiNpcDialog) {
            SubGuiNpcDialog dlg = (SubGuiNpcDialog) subgui;
            dlg.save();
            if (diagram != null) diagram.invalidateCache();
        }
        if (subgui instanceof SubGuiLinkDialog) {
            SubGuiLinkDialog link = (SubGuiLinkDialog) subgui;
            if (link.confirmed) {
                Dialog source = DialogController.Instance.dialogs.get(linkSourceId);
                if (source != null) {
                    int catId = source.category != null ? source.category.id : categoryId;
                    PacketClient.sendClient(new DialogSavePacket(catId,
                        source.writeToNBT(new NBTTagCompound()), false));
                }
                bfsDiscoveredIds = null;
                if (diagram != null) diagram.invalidateCache();
            }
            linkMode = false;
            linkSourceId = -1;
        }

        // Quest selection callback (modern)
        if (subgui instanceof ModernQuestSelector && editorPanel != null) {
            ModernQuestSelector mqs = (ModernQuestSelector) subgui;
            if (mqs.getSelectedQuest() != null) {
                editorPanel.onQuestSelected(pendingQuestSlot,
                    mqs.getSelectedQuest().id, mqs.getSelectedQuest().title);
            }
            pendingQuestSlot = -1;
        }

        // Sound selection callback (modern)
        if (subgui instanceof ModernSoundSelector && editorPanel != null) {
            ModernSoundSelector mss = (ModernSoundSelector) subgui;
            if (mss.getSelectedSound() != null) {
                editorPanel.onSoundSelected(mss.getSelectedSoundString());
            }
        }

        // Dialog selection callback (modern - for availability or option targets)
        if (subgui instanceof ModernDialogSelector && editorPanel != null) {
            ModernDialogSelector mds = (ModernDialogSelector) subgui;
            if (mds.getSelectedDialog() != null) {
                if (pendingOptionSlot >= 0) {
                    editorPanel.onOptionDialogSelected(pendingOptionSlot,
                        mds.getSelectedDialog().id, mds.getSelectedDialog().title);
                    pendingOptionSlot = -1;
                } else if (pendingDialogSlot >= 0) {
                    editorPanel.onDialogSelected(pendingDialogSlot,
                        mds.getSelectedDialog().id, mds.getSelectedDialog().title);
                    pendingDialogSlot = -1;
                }
            }
        }

        // Faction selection callback (modern)
        if (subgui instanceof ModernFactionSelector && editorPanel != null) {
            ModernFactionSelector mfs = (ModernFactionSelector) subgui;
            if (mfs.getSelectedFaction() != null && pendingFactionSlot >= 0) {
                editorPanel.onFactionSelected(pendingFactionSlot,
                    mfs.getSelectedFaction().id, mfs.getSelectedFaction().name);
            }
            pendingFactionSlot = -1;
        }

        // Color selection callback (modern)
        if (subgui instanceof ModernColorPicker && editorPanel != null) {
            ModernColorPicker mcp = (ModernColorPicker) subgui;
            editorPanel.onColorSelected(pendingColorSlot, mcp.getColor());
            pendingColorSlot = -1;
        }

        // Mail editor callback (modern) - data is modified in place on editingDialog.mail
        if (subgui instanceof ModernMailEditor && editorPanel != null) {
            ModernMailEditor mme = (ModernMailEditor) subgui;
            // Check if quest selection was requested
            if (mme.isPendingQuestSelect()) {
                // Open quest selector, then reopen mail editor
                setSubGui(new ModernQuestSelector(editingDialog.mail.questId));
                return; // Don't reinit yet
            }
            // Refresh the mail display in the editor panel
            if (editingDialog != null && editingDialog.mail != null) {
                String mailSubject = editingDialog.mail.subject;
                if (mailSubject != null && !mailSubject.isEmpty()) {
                    // Mark dirty since mail was edited
                    dirty = true;
                    if (editorPanel != null) {
                        editorPanel.markDirty();
                    }
                }
            }
        }

        initGui();
    }

    @Override
    public void save() {
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Update editor panel cursor blink
        if (editorPanel != null && editMode) {
            editorPanel.updateScreen();
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        // Handle scroll wheel for editor panel
        int scroll = org.lwjgl.input.Mouse.getDWheel();
        if (scroll != 0 && editorPanel != null && editMode && !hasSubGui()) {
            int mouseX = org.lwjgl.input.Mouse.getX() * width / mc.displayWidth;
            int mouseY = height - org.lwjgl.input.Mouse.getY() * height / mc.displayHeight - 1;
            if (editorPanel.isInside(mouseX, mouseY)) {
                editorPanel.mouseScrolled(scroll > 0 ? 1 : -1);
            }
        }
    }

    // ===== Inner Diagram Implementation =====

    private class DialogGraphDiagram extends GuiAdvancedDiagram {

        private final GuiDialogTree tree;

        DialogGraphDiagram(GuiDialogTree tree, int x, int y, int width, int height) {
            super(tree, x, y, width, height);
            this.tree = tree;
        }

        /** Get the set of dialogs to render */
        private List<Dialog> getDialogsToRender() {
            List<Dialog> result = new ArrayList<>();
            if (tree.individualMode) {
                Set<Integer> discovered = tree.discoverDialogs();
                for (int id : discovered) {
                    Dialog d = DialogController.Instance.dialogs.get(id);
                    if (d != null) result.add(d);
                }
            } else if (tree.category != null) {
                result.addAll(tree.category.dialogs.values());
            }
            return result;
        }

        /** Build set of IDs in the render set for connection filtering */
        private Set<Integer> getDialogIdSet() {
            Set<Integer> ids = new HashSet<>();
            if (tree.individualMode) {
                ids.addAll(tree.discoverDialogs());
            } else if (tree.category != null) {
                ids.addAll(tree.category.dialogs.keySet());
            }
            return ids;
        }

        @Override
        protected List<AdvancedNode> createNodes() {
            List<AdvancedNode> nodeList = new ArrayList<>();

            for (Dialog dialog : getDialogsToRender()) {
                AdvancedNode node = new AdvancedNode(dialog.id, sanitize(dialog.title));

                String sub = "ID: " + dialog.id;
                if (dialog.quest > 0 && QuestController.Instance != null) {
                    Quest q = QuestController.Instance.quests.get(dialog.quest);
                    if (q != null) sub += " | Quest: " + q.title;
                }
                node.subtitle = sub;

                for (Map.Entry<Integer, DialogOption> entry : dialog.options.entrySet()) {
                    DialogOption opt = entry.getValue();
                    if (opt.optionType == EnumOptionType.Disabled) continue;

                    int slotType;
                    int slotColor;
                    switch (opt.optionType) {
                        case DialogOption:
                            slotType = 0;
                            slotColor = opt.dialogId > 0 ? 0x55FF55 : 0xAAAAAA;
                            break;
                        case QuitOption:
                            slotType = 1;
                            slotColor = 0xFF5555;
                            break;
                        case CommandBlock:
                            slotType = 2;
                            slotColor = 0xFFAA00;
                            break;
                        default:
                            slotType = 3;
                            slotColor = 0xAAAAAA;
                            break;
                    }

                    NodeSlot slot = new NodeSlot(entry.getKey(), sanitize(opt.title), slotColor,
                        opt.dialogId > 0 ? opt.dialogId : -1, slotType);
                    node.slots.add(slot);
                }

                // Header color based on state
                boolean isForeignCategory = tree.individualMode && tree.category != null
                    && dialog.category != null && dialog.category.id != tree.categoryId;

                if (isForeignCategory) {
                    node.headerColor = 0xFF7744AA; // Purple for cross-category nodes
                } else if (dialog.quest > 0) {
                    node.headerColor = 0xFFCC8800;
                } else if (node.slots.isEmpty()) {
                    node.headerColor = 0xFF666666;
                } else {
                    boolean hasLinkedOption = false;
                    for (NodeSlot s : node.slots) {
                        if (s.targetNodeId >= 0) {
                            hasLinkedOption = true;
                            break;
                        }
                    }
                    node.headerColor = hasLinkedOption ? 0xFF4488CC : 0xFF888844;
                }

                nodeList.add(node);
            }
            return nodeList;
        }

        @Override
        protected List<AdvancedConnection> createConnections() {
            List<AdvancedConnection> conns = new ArrayList<>();
            Set<Integer> visibleIds = getDialogIdSet();

            for (Dialog dialog : getDialogsToRender()) {
                int slotIndex = 0;
                for (Map.Entry<Integer, DialogOption> entry : dialog.options.entrySet()) {
                    DialogOption opt = entry.getValue();
                    if (opt.optionType == EnumOptionType.Disabled) {
                        continue;
                    }
                    if (opt.optionType == EnumOptionType.DialogOption && opt.dialogId > 0) {
                        if (visibleIds.contains(opt.dialogId)) {
                            conns.add(new AdvancedConnection(dialog.id, slotIndex, opt.dialogId, 0x55FF55));
                        } else {
                            conns.add(new AdvancedConnection(dialog.id, slotIndex, opt.dialogId, 0x555555));
                        }
                    }
                    slotIndex++;
                }
            }
            return conns;
        }

        @Override
        protected void onNodeClick(AdvancedNode node) {
            if (tree.addDialogMode) {
                // User picked a parent dialog to extend from
                Dialog parent = DialogController.Instance.dialogs.get(node.id);
                if (parent != null) {
                    tree.createNewDialog(parent);
                    // Open link dialog to connect new dialog to parent
                    // Note: the new dialog will be created, we'll need to link after
                }
                tree.addDialogMode = false;
                tree.initGui();
            } else if (tree.linkMode) {
                if (tree.linkSourceId < 0) {
                    tree.linkSourceId = node.id;
                } else if (tree.linkSourceId != node.id) {
                    Dialog source = DialogController.Instance.dialogs.get(tree.linkSourceId);
                    Dialog target = DialogController.Instance.dialogs.get(node.id);
                    if (source != null && target != null) {
                        tree.setSubGui(new SubGuiLinkDialog(source, target));
                    }
                }
            } else {
                tree.selectDialog(node.id);
            }
        }

        @Override
        protected void onNodeDoubleClick(AdvancedNode node) {
            if (!tree.linkMode && !tree.addDialogMode) {
                Dialog d = DialogController.Instance.dialogs.get(node.id);
                if (d != null) {
                    tree.selectedDialog = d;
                    // Double-click enters inline edit mode (not SubGui popup)
                    tree.enterEditMode(d);
                }
            }
        }
    }
}
