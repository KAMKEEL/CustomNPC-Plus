package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.controllers.data.Category;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.constants.EnumScrollData;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Fullscreen categorized manager base class (Cloner-inspired layout).
 * Left=categories with icons, Center=items, Right=preview+details.
 * Top bar has item operations (Add/Remove/Clone/Move) + search.
 */
public abstract class GuiDirectoryCategorized extends GuiDirectory
    implements IScrollData, IGuiData, GuiYesNoCallback, ISubGuiListener {

    // ========== Scrolls ==========
    protected GuiCustomScrollIcons catScroll = new GuiCustomScrollIcons(this, 2);
    protected GuiCustomScroll itemScroll = new GuiCustomScroll(this, 0);

    // ========== Data ==========
    protected HashMap<String, Integer> catData = new HashMap<>();
    protected HashMap<String, Integer> itemData = new HashMap<>();

    // ========== Selection ==========
    protected String prevCatName = "";
    protected String prevItemName = "";
    protected int selectedCatId = -1;

    // ========== Search ==========
    protected String catSearch = "";
    protected String itemSearch = "";

    // ========== Preview ==========
    protected float zoomed = 60;
    protected float rotation;
    protected int previewX, previewY, previewW, previewH;

    // ========== Move Workflow ==========
    protected int movePhase = 0; // 0=off, 1=selecting items, 2=selecting destination
    protected HashSet<String> moveSelection = new HashSet<>();
    protected static final int MAX_MOVE_ITEMS = 5;

    // ========== New Item Category ==========
    protected int pendingNewItemCatId = -1;

    // ========== Category Data Loaded ==========
    private boolean itemDataLoaded = false;

    // ========== Collapse ==========
    protected boolean leftCollapsed = false;
    private static final ResourceLocation DIRECTORY_ICON = new ResourceLocation("customnpcs", "textures/gui/directory.png");

    // ========== Scroll Reset Flag ==========
    private boolean resetItemScroll = false;

    // ========== Nav list data ==========
    private List<String> navNames = new ArrayList<>();
    private List<Integer> navIcons = new ArrayList<>();

    // ========== Constructor ==========
    public GuiDirectoryCategorized() {
        super();
        leftPanelPercent = 0.15f;
        rightPanelPercent = 0.35f;
        minLeftPanelW = 120;
        minRightPanelW = 150;
        requestCategoryList();
    }

    /**
     * Whether this GUI currently has a category panel.
     * Subclasses override to return false for built-in modes.
     */
    protected boolean hasCategories() { return true; }

    @Override
    protected void computeLayout() {
        if (leftCollapsed || !hasCategories()) {
            leftPanelPercent = 0.0f;
            minLeftPanelW = 0;
        } else if (leftPanelPercent <= 0) {
            leftPanelPercent = 0.15f;
            minLeftPanelW = 120;
        }
        super.computeLayout();

        // Force 50/50 split between center and right when left panel is hidden
        if (leftPanelW <= 0) {
            int available = usableW - 3 * gap;
            rightPanelW = available / 2;
            contentW = available - rightPanelW;
            contentX = originX + gap;
            rightX = contentX + contentW + gap;
        }
    }

    @Override
    protected void drawPanels() {
        if (leftPanelW <= 0) {
            if (rightPanelW > 0) {
                GuiUtil.drawRectD(rightX - 1, contentY - 1, rightX + rightPanelW + 1, originY + usableH + 1, panelBorder);
            }
        } else {
            super.drawPanels();
        }
    }

    // ========== Abstract hooks ==========
    protected abstract String getTitle();
    protected abstract void requestCategoryList();
    protected abstract void requestItemsInCategory(int catId);
    protected abstract void requestItemData(int itemId);
    protected abstract void onSaveCategory(Category cat);
    protected abstract void onRemoveCategory(int catId);
    protected abstract void onAddItem(int catId);
    protected abstract void onRemoveItem(int itemId);
    protected abstract void onEditItem();
    protected abstract void onCloneItem();
    protected abstract void onItemReceived(NBTTagCompound compound);
    protected abstract boolean hasSelectedItem();
    protected abstract int getSelectedItemId();

    protected GuiScreen getWindowedVariant() { return null; }
    protected void drawItemPreview(int centerX, int centerY, int mouseX, int mouseY, float partialTicks) {}
    protected void drawItemDetails(int x, int y, int w) {}
    protected void saveCurrentItem() {}

    protected void setPrevItemName(String name) {
        this.prevItemName = name;
    }

    /**
     * Hook for subclasses to add buttons at the START of the top bar (before Add/Remove/Clone/Move).
     * Return the new x position after adding buttons.
     */
    protected int initExtraTopBarButtons(int x, int topBtnY) {
        return x;
    }

    // ========== Top Bar ==========
    @Override
    protected void initTopBar(int topBtnY) {
        int x = originX + 2;

        // Collapse/Expand button
        GuiNpcButton collapseBtn = new GuiNpcButton(19, x, topBtnY, btnH, btnH, "");
        collapseBtn.enabled = hasCategories() && movePhase == 0;
        addButton(collapseBtn);
        x += btnH + 2;

        x = initExtraTopBarButtons(x, topBtnY);
        int topBtnW = 55;

        // ADD
        GuiNpcButton addBtn = new GuiNpcButton(50, x, topBtnY, topBtnW, btnH, "gui.add");
        addBtn.enabled = selectedCatId >= 0 && movePhase == 0;
        addButton(addBtn);
        x += topBtnW + 2;

        // MOVE
        String moveLabel = movePhase > 0 ? "Moving" : "Move";
        GuiNpcButton moveBtn = new GuiNpcButton(54, x, topBtnY, topBtnW, btnH, moveLabel);
        moveBtn.enabled = selectedCatId >= 0 && !leftCollapsed && hasCategories();
        addButton(moveBtn);
        x += topBtnW + 2;

        // Confirm (only during move phase 1)
        if (movePhase == 1) {
            GuiNpcButton confirmBtn = new GuiNpcButton(55, x, topBtnY, topBtnW, btnH, "Confirm");
            confirmBtn.enabled = !moveSelection.isEmpty();
            addButton(confirmBtn);
            x += topBtnW + 2;
        }

        // Right side: close, minimize
        int closeX = originX + usableW - btnH - 2;
        addButton(new GuiNpcButton(17, closeX, topBtnY, btnH, btnH, "X"));
        int minimizeX = closeX - btnH - 2;
        if (getWindowedVariant() != null) {
            addButton(new GuiNpcButton(18, minimizeX, topBtnY, btnH, btnH, "-"));
        }

        // Search field is now under the center panel — see initCenterPanel()
    }

    // ========== Left Panel: Category Navigation ==========
    @Override
    protected void initLeftPanel() {
        if (leftCollapsed || !hasCategories()) return;
        boolean catSelected = selectedCatId > 0; // non-Uncategorized selected
        int bottomRows = catSelected ? 3 : 2;
        int bottomH = bottomRows * (btnH + gap);
        int navH = contentH - bottomH;

        buildNavList();

        int savedCatScrollY = catScroll.scrollY;
        catScroll.clear();
        catScroll.colors.clear();
        catScroll.setSize(leftPanelW, navH);
        catScroll.guiLeft = originX;
        catScroll.guiTop = contentY;
        catScroll.setListWithIcons(navNames, navIcons);
        catScroll.setSelected(prevCatName);
        catScroll.scrollY = Math.max(0, Math.min(savedCatScrollY, catScroll.maxScrollY));

        // Phase 2: ensure scroll is clickable for destination selection
        if (movePhase == 2) {
            catScroll.setSelectable(true);
        }

        addScroll(catScroll);

        // Category search
        int searchY = contentY + navH + gap;
        addTextField(new GuiNpcTextField(3, this, fontRendererObj, originX, searchY, leftPanelW, btnH, catSearch));

        // Add category button
        int addY = searchY + btnH + gap;
        GuiNpcButton addCatBtn = new GuiNpcButton(40, originX, addY, leftPanelW, btnH, "gui.addCategory");
        if (movePhase > 0) addCatBtn.enabled = false;
        addButton(addCatBtn);

        // Rename / Remove (non-Uncategorized only)
        if (catSelected) {
            int crudY = addY + btnH + gap;
            int halfW = (leftPanelW - gap) / 2;
            GuiNpcButton renameBtn = new GuiNpcButton(41, originX, crudY, halfW, btnH, "gui.edit");
            if (movePhase > 0) renameBtn.enabled = false;
            addButton(renameBtn);

            GuiNpcButton removeBtn = new GuiNpcButton(42, originX + halfW + gap, crudY, halfW, btnH, "gui.remove");
            removeBtn.enabled = movePhase == 0 && isCategoryEmpty(selectedCatId);
            addButton(removeBtn);
        }
    }

    // ========== Center Panel: Items ==========
    @Override
    protected void initCenterPanel() {
        int searchH = btnH + gap;
        int scrollH = contentH - (movePhase == 0 ? searchH : 0);

        int savedItemScrollY = resetItemScroll ? 0 : itemScroll.scrollY;
        resetItemScroll = false;

        itemScroll.clear();
        itemScroll.colors.clear();
        itemScroll.setSize(contentW, scrollH);
        itemScroll.guiLeft = contentX;
        itemScroll.guiTop = contentY;

        if (movePhase == 1) {
            itemScroll.multipleSelection = true;
            itemScroll.setSelectable(true);
            itemScroll.setSelectedList(moveSelection);
        } else if (movePhase == 2) {
            itemScroll.multipleSelection = true;
            itemScroll.setSelectedList(moveSelection);
            itemScroll.setSelectable(false);
            for (String name : moveSelection) {
                itemScroll.colors.put(name, 0xFFFF55);
            }
        } else {
            itemScroll.multipleSelection = false;
            itemScroll.setSelectable(true);
        }

        itemScroll.setList(getItemSearchList());
        if (movePhase == 0) {
            itemScroll.setSelected(prevItemName);
        }
        itemScroll.scrollY = Math.max(0, Math.min(savedItemScrollY, itemScroll.maxScrollY));
        addScroll(itemScroll);

        // Search field under center panel
        if (movePhase == 0) {
            int searchY = contentY + scrollH + gap;
            addTextField(new GuiNpcTextField(1, this, fontRendererObj, contentX, searchY, contentW, btnH, itemSearch));
        }
    }

    // ========== Right Panel: Preview + Edit/Copy/Remove ==========
    @Override
    protected void initRightPanel(int startY) {
        // Preview area — reserve space for 3 buttons + ID label at bottom
        int bottomH = (btnH + gap) * 2 + 14; // edit+copy row, remove row, ID label
        previewX = rightX;
        previewY = contentY;
        previewW = rightPanelW;
        previewH = contentH - bottomH - gap;

        // Bottom buttons: Edit + Copy on one row, Remove below
        int btnY = contentY + contentH - btnH * 2 - gap;
        int halfW = (rightPanelW - gap) / 2;

        GuiNpcButton editBtn = new GuiNpcButton(51, rightX, btnY, halfW, btnH, "gui.edit");
        editBtn.enabled = hasSelectedItem() && movePhase == 0;
        addButton(editBtn);

        GuiNpcButton cloneBtn = new GuiNpcButton(52, rightX + halfW + gap, btnY, halfW, btnH, "gui.copy");
        cloneBtn.enabled = hasSelectedItem() && movePhase == 0;
        addButton(cloneBtn);

        int removeY = btnY + btnH + gap;
        GuiNpcButton removeBtn = new GuiNpcButton(53, rightX, removeY, rightPanelW, btnH, "gui.remove");
        removeBtn.enabled = hasSelectedItem() && movePhase == 0;
        removeBtn.setTextColor(0xFF5555);
        addButton(removeBtn);
    }

    // ========== Drawing ==========
    @Override
    protected void drawOverlay(int mouseX, int mouseY, float partialTicks) {
        // Draw collapse button icon
        GuiNpcButton collapseBtn = getButton(19);
        if (collapseBtn != null && collapseBtn.visible) {
            mc.getTextureManager().bindTexture(DIRECTORY_ICON);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            int iconSize = 16;
            int iconX = collapseBtn.xPosition + (collapseBtn.width - iconSize) / 2;
            int iconY = collapseBtn.yPosition + (collapseBtn.height - iconSize) / 2;
            int texY;
            if (!collapseBtn.enabled) {
                texY = 32;
            } else {
                boolean hovered = mouseX >= collapseBtn.xPosition && mouseY >= collapseBtn.yPosition
                    && mouseX < collapseBtn.xPosition + collapseBtn.width
                    && mouseY < collapseBtn.yPosition + collapseBtn.height;
                texY = hovered ? 16 : 0;
            }
            func_152125_a(iconX, iconY, 0, texY, 16, 16, iconSize, iconSize, 256, 256);
            GL11.glDisable(GL11.GL_BLEND);
        }

        if (!hasSubGui()) {
            // Draw preview background
            GuiUtil.drawRectD(previewX, previewY, previewX + previewW, previewY + previewH, 0xA0101010);

            // Zoom/rotation in preview area
            if (isMouseOverPreview(mouseX, mouseY)) {
                float wheel = Mouse.getDWheel() * 0.035f;
                if (wheel != 0) {
                    zoomed += wheel;
                    zoomed = Math.max(5, Math.min(200, zoomed));
                }
                if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
                    rotation -= Mouse.getDX() * 0.75f;
                }
            }

            // Draw preview content
            if (hasSelectedItem()) {
                int centerX = previewX + previewW / 2;
                int centerY = previewY + (int)(previewH * 0.75f);
                drawItemPreview(centerX, centerY, mouseX, mouseY, partialTicks);
            }

            // Draw details text
            if (hasSelectedItem()) {
                drawItemDetails(previewX + 4, previewY + 4, previewW - 8);
            }

            // Draw ID (skip if -1, e.g. abilities use names not IDs)
            if (hasSelectedItem() && getSelectedItemId() >= 0) {
                String idStr = "ID: " + getSelectedItemId();
                int idY = contentY + contentH - btnH - 14;
                fontRendererObj.drawString(idStr, previewX + 4, idY, 0xFFFFFF, true);
            }
        }

        // Move phase overlay text
        if (movePhase == 1) {
            String text = "Select Items to Move (max " + MAX_MOVE_ITEMS + ")";
            int textW = fontRendererObj.getStringWidth(text);
            fontRendererObj.drawStringWithShadow(text, width / 2 - textW / 2, height - pad - 10, 0x55FF55);
        } else if (movePhase == 2) {
            String text = "Select Destination Category";
            int textW = fontRendererObj.getStringWidth(text);
            fontRendererObj.drawStringWithShadow(text, width / 2 - textW / 2, height - pad - 10, 0xFF5555);
        }
    }

    protected boolean isMouseOverPreview(int mouseX, int mouseY) {
        return mouseX >= previewX && mouseX <= previewX + previewW
            && mouseY >= previewY && mouseY <= previewY + previewH;
    }

    // ========== Nav List Builder ==========
    private void buildNavList() {
        navNames.clear();
        navIcons.clear();
        String searchLower = catSearch.toLowerCase();

        // Uncategorized always first
        String uncatName = "Uncategorized";
        if (searchLower.isEmpty() || uncatName.toLowerCase().contains(searchLower)) {
            navNames.add(uncatName);
            navIcons.add(GuiCustomScrollIcons.ICON_TAB);
        }

        // Other categories
        for (String name : catData.keySet()) {
            if (name.equals(uncatName)) continue;
            if (searchLower.isEmpty() || name.toLowerCase().contains(searchLower)) {
                navNames.add(name);
                navIcons.add(GuiCustomScrollIcons.ICON_FOLDER);
            }
        }
    }

    protected boolean isCategoryEmpty(int catId) {
        if (!itemDataLoaded) return false;
        return itemData == null || itemData.isEmpty();
    }

    // ========== Search ==========
    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);

        GuiNpcTextField itemSearchField = getTextField(1);
        if (itemSearchField != null) {
            String newText = itemSearchField.getText().toLowerCase();
            if (!itemSearch.equals(newText)) {
                itemSearch = newText;
                itemScroll.setList(getItemSearchList());
            }
        }

        GuiNpcTextField catSearchField = getTextField(3);
        if (catSearchField != null) {
            String newText = catSearchField.getText().toLowerCase();
            if (!catSearch.equals(newText)) {
                catSearch = newText;
                buildNavList();
                catScroll.setListWithIcons(navNames, navIcons);
                catScroll.setSelected(prevCatName);
            }
        }
    }

    protected List<String> getItemSearchList() {
        if (selectedCatId < 0) return new ArrayList<>();
        if (itemSearch.isEmpty()) return new ArrayList<>(itemData.keySet());
        List<String> list = new ArrayList<>();
        for (String name : itemData.keySet()) {
            if (name.toLowerCase().contains(itemSearch)) list.add(name);
        }
        return list;
    }

    // ========== Actions ==========
    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        // Close
        if (id == 17) close();

        // Collapse/Expand
        if (id == 19 && hasCategories() && movePhase == 0) {
            leftCollapsed = !leftCollapsed;
            initGui();
            return;
        }

        // Minimize
        if (id == 18) {
            GuiScreen windowed = getWindowedVariant();
            if (windowed != null) {
                Minecraft.getMinecraft().displayGuiScreen(windowed);
                return;
            }
        }

        // ===== Item operations =====

        // Add item
        if (id == 50 && selectedCatId >= 0 && movePhase == 0) {
            pendingNewItemCatId = selectedCatId > 0 ? selectedCatId : -1;
            onAddItem(selectedCatId);
            requestItemsInCategory(selectedCatId);
        }

        // Edit item
        if (id == 51 && hasSelectedItem() && movePhase == 0) {
            onEditItem();
        }

        // Clone item
        if (id == 52 && hasSelectedItem() && movePhase == 0) {
            onCloneItem();
            requestItemsInCategory(selectedCatId);
        }

        // Remove item
        if (id == 53 && hasSelectedItem() && movePhase == 0) {
            GuiYesNo guiyesno = new GuiYesNo(this, itemScroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 2);
            displayGuiScreen(guiyesno);
        }

        // Move button
        if (id == 54) {
            if (movePhase == 0) {
                // Start move phase 1
                movePhase = 1;
                moveSelection.clear();
                initGui();
            } else {
                // Cancel move
                movePhase = 0;
                moveSelection.clear();
                initGui();
            }
        }

        // Confirm move (transition phase 1 → 2)
        if (id == 55 && movePhase == 1 && !moveSelection.isEmpty()) {
            movePhase = 2;
            initGui();
        }

        // ===== Left panel category operations =====

        // Add category
        if (id == 40 && movePhase == 0) {
            String name = "New";
            while (catData.containsKey(name)) name += "_";
            Category cat = new Category(-1, name);
            onSaveCategory(cat);
        }

        // Rename category
        if (id == 41 && selectedCatId > 0 && movePhase == 0) {
            setSubGui(new SubGuiEditText(prevCatName));
        }

        // Remove category
        if (id == 42 && selectedCatId > 0 && movePhase == 0) {
            GuiYesNo guiyesno = new GuiYesNo(this, prevCatName, StatCollector.translateToLocal("gui.delete"), 5);
            displayGuiScreen(guiyesno);
        }
    }

    // ========== Scroll Events ==========
    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        // Category scroll
        if (guiCustomScroll.id == 2) {
            String selected = catScroll.getSelected();
            if (selected == null) return;

            // Move phase 2: destination selected
            if (movePhase == 2) {
                if (!selected.equals(prevCatName)) {
                    Integer destCatId = catData.get(selected);
                    if (destCatId != null) {
                        executeMoveItems(destCatId);
                    }
                }
                return;
            }

            // Normal category selection
            if (!selected.equals(prevCatName)) {
                pendingNewItemCatId = -1;
                itemDataLoaded = false;
                Integer catId = catData.get(selected);
                if (catId != null) {
                    selectedCatId = catId;
                    itemScroll.selected = -1;
                    prevItemName = "";
                    requestItemsInCategory(selectedCatId);
                    prevCatName = selected;
                    resetItemScroll = true;
                    initGui();
                }
            }
        }

        // Item scroll
        if (guiCustomScroll.id == 0) {
            pendingNewItemCatId = -1;
            if (movePhase == 1) {
                // Enforce max selection during move
                if (moveSelection.size() > MAX_MOVE_ITEMS) {
                    // Remove excess — keep only first MAX_MOVE_ITEMS
                    HashSet<String> trimmed = new HashSet<>();
                    int count = 0;
                    for (String s : moveSelection) {
                        if (count++ >= MAX_MOVE_ITEMS) break;
                        trimmed.add(s);
                    }
                    moveSelection.clear();
                    moveSelection.addAll(trimmed);
                    itemScroll.setSelectedList(moveSelection);
                }
                // Update confirm button state
                if (getButton(55) != null) {
                    getButton(55).enabled = !moveSelection.isEmpty();
                }
                return;
            }

            String selected = itemScroll.getSelected();
            if (selected != null && !selected.equals(prevItemName)) {
                Integer itemId = itemData.get(selected);
                if (itemId != null) {
                    requestItemData(itemId);
                    prevItemName = selected;
                }
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0 && hasSelectedItem() && movePhase == 0) {
            onEditItem();
        }
    }

    // ========== Move Execution ==========
    protected abstract void sendMovePacket(int itemId, int destCatId);

    protected void executeMoveItems(int destCatId) {
        for (String name : moveSelection) {
            Integer itemId = itemData.get(name);
            if (itemId != null) {
                sendMovePacket(itemId, destCatId);
            }
        }
        movePhase = 0;
        moveSelection.clear();
        // Refresh current category items
        if (selectedCatId >= 0) {
            requestItemsInCategory(selectedCatId);
        }
        initGui();
    }

    // ========== Data from Server ==========
    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.CATEGORY_LIST) {
            // Category list arrived
            String name = catScroll.getSelected();
            this.catData = data;
            buildNavList();
            catScroll.setListWithIcons(navNames, navIcons);
            if (name != null) catScroll.setSelected(name);
            else catScroll.setSelected(prevCatName);

            // Auto-select Uncategorized on first load
            if (selectedCatId < 0 && catData.containsKey("Uncategorized")) {
                selectedCatId = catData.get("Uncategorized");
                prevCatName = "Uncategorized";
                catScroll.setSelected(prevCatName);
                requestItemsInCategory(selectedCatId);
            }
            initGui();
        } else if (type == EnumScrollData.CATEGORY_GROUP) {
            // Items within selected category
            String name = itemScroll.getSelected();
            this.itemData = data;
            this.itemDataLoaded = true;
            itemScroll.setList(getItemSearchList());
            if (name != null) itemScroll.setSelected(name);
            initGui();
        }
        // Ignore OPTIONAL and other types silently
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        onItemReceived(compound);
        if (pendingNewItemCatId > 0 && hasSelectedItem() && getSelectedItemId() >= 0) {
            sendMovePacket(getSelectedItemId(), pendingNewItemCatId);
            if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
        }
        pendingNewItemCatId = -1;
        initGui();
    }

    // ========== SubGui ==========
    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiEditText) {
            if (!((SubGuiEditText) subgui).cancelled && selectedCatId > 0) {
                String name = ((SubGuiEditText) subgui).text;
                if (name != null && !name.isEmpty() && !catData.containsKey(name)) {
                    Category cat = new Category(selectedCatId, name);
                    onSaveCategory(cat);
                    catData.remove(prevCatName);
                    catData.put(name, selectedCatId);
                    prevCatName = name;
                }
            }
        }
        onSubGuiClosed(subgui);
    }

    protected void onSubGuiClosed(SubGuiInterface subgui) {}

    // ========== Confirm Dialog ==========
    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result) return;

        // Remove category
        if (id == 5 && selectedCatId > 0) {
            onRemoveCategory(selectedCatId);
            selectedCatId = -1;
            prevCatName = "";
            itemData.clear();
            catScroll.selected = -1;
        }

        // Remove item
        if (id == 2 && itemScroll.getSelected() != null && itemData.containsKey(itemScroll.getSelected())) {
            onRemoveItem(itemData.get(itemScroll.getSelected()));
            itemData.clear();
            prevItemName = "";
            if (selectedCatId >= 0) requestItemsInCategory(selectedCatId);
        }
        initGui();
    }

    @Override
    public void setSelected(String selected) {}

    @Override
    public void save() {}
}
