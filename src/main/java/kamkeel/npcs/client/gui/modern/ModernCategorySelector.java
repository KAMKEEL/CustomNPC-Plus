package kamkeel.npcs.client.gui.modern;

import kamkeel.npcs.client.gui.components.ModernButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Abstract base class for modern styled two-column selectors (categories | items).
 * Features draggable divider, search fields, and double-click selection.
 *
 * @param <C> Category type
 * @param <I> Item type
 */
public abstract class ModernCategorySelector<C, I> extends ModernSubGuiInterface {

    // Selected values
    protected C selectedCategory;
    protected I selectedItem;

    // Data maps
    protected HashMap<String, C> categoryData = new HashMap<>();
    protected HashMap<String, I> itemData = new HashMap<>();

    // Lists for display
    protected List<String> filteredCategories = new ArrayList<>();
    protected List<String> filteredItems = new ArrayList<>();

    // Components
    protected GuiNpcTextField catSearchField;
    protected GuiNpcTextField itemSearchField;
    protected ModernButton doneBtn;
    protected ModernButton cancelBtn;

    // Scroll state
    protected int catScrollY = 0;
    protected int itemScrollY = 0;
    protected int selectedCatIndex = -1;
    protected int selectedItemIndex = -1;

    // Layout
    protected int listY;
    protected int listHeight;
    protected int catListX, catListW;
    protected int itemListX, itemListW;
    protected int rowHeight = 14;

    // Draggable divider
    protected int dividerOffset;
    protected boolean isDraggingDivider = false;
    protected int dividerDragStartX;
    protected int dividerWidth = 5;
    protected int minColumnWidth = 80;

    // Search strings
    protected String catSearch = "";
    protected String itemSearch = "";

    // Button IDs
    protected static final int ID_DONE = 100;
    protected static final int ID_CANCEL = 101;

    // Double-click tracking
    protected long lastClickTime = 0;
    protected int lastClickedIndex = -1;

    public ModernCategorySelector() {
        xSize = 420;
        ySize = 280;
    }

    // ===== Abstract methods to be implemented by subclasses =====

    /** Load all categories into categoryData map */
    protected abstract void loadAllCategories();

    /** Load items for a specific category into itemData map */
    protected abstract void loadItemsForCategory(C category);

    /** Get the display title for a category */
    protected abstract String getCategoryTitle(C category);

    /** Get the display title for an item */
    protected abstract String getItemTitle(I item);

    /** Get the ID of an item (for selection tracking) */
    protected abstract int getItemId(I item);

    /** Get the header title for the SubGui */
    protected abstract String getHeaderTitle();

    /** Get the column header for categories */
    protected abstract String getCategoryColumnTitle();

    /** Get the column header for items */
    protected abstract String getItemColumnTitle();

    /** Get the search placeholder for categories */
    protected String getCategorySearchPlaceholder() {
        return "Filter " + getCategoryColumnTitle().toLowerCase() + "...";
    }

    /** Get the search placeholder for items */
    protected String getItemSearchPlaceholder() {
        return "Filter " + getItemColumnTitle().toLowerCase() + "...";
    }

    // ===== Initialization =====

    protected void loadData() {
        categoryData.clear();
        loadAllCategories();
        filteredCategories = new ArrayList<>(categoryData.keySet());
        Collections.sort(filteredCategories);
    }

    protected void loadItemsForSelectedCategory() {
        itemData.clear();
        if (selectedCategory != null) {
            loadItemsForCategory(selectedCategory);
        }
        updateItemList();
    }

    @Override
    public void initGui() {
        super.initGui();

        int contentY = getContentY() + 6;
        int contentH = getContentHeight() - 6;

        // Initialize divider offset if not set
        if (dividerOffset == 0) {
            dividerOffset = (xSize - 24) / 2; // Default 50/50 split
        }

        // Layout calculations with draggable divider
        int pad = 8;
        catListX = guiLeft + pad;
        catListW = dividerOffset - pad;
        itemListX = guiLeft + dividerOffset + dividerWidth;
        itemListW = xSize - dividerOffset - dividerWidth - pad;

        // Enforce minimum widths
        if (catListW < minColumnWidth) {
            catListW = minColumnWidth;
            dividerOffset = catListW + pad;
            itemListX = guiLeft + dividerOffset + dividerWidth;
            itemListW = xSize - dividerOffset - dividerWidth - pad;
        }
        if (itemListW < minColumnWidth) {
            itemListW = minColumnWidth;
            dividerOffset = xSize - pad - dividerWidth - itemListW;
            catListW = dividerOffset - pad;
            itemListX = guiLeft + dividerOffset + dividerWidth;
        }

        // Search fields at top
        int searchH = 18;
        catSearchField = new GuiNpcTextField(0, catListX, contentY, catListW, searchH, catSearch);

        itemSearchField = new GuiNpcTextField(1, itemListX, contentY, itemListW, searchH, itemSearch);

        // List area below search
        listY = contentY + searchH + 4;
        listHeight = contentH - searchH - 36 - 8;

        // Buttons at bottom
        int btnY = guiTop + ySize - 32;
        int btnWidth = 60;
        int btnGap = 8;

        cancelBtn = new ModernButton(ID_CANCEL, guiLeft + xSize - pad - btnWidth * 2 - btnGap, btnY, btnWidth, 20, "Cancel");
        doneBtn = new ModernButton(ID_DONE, guiLeft + xSize - pad - btnWidth, btnY, btnWidth, 20, "Done");
        doneBtn.setBackgroundColor(ModernColors.ACCENT_BLUE);

        // Set initial selection indices
        if (selectedCategory != null) {
            selectedCatIndex = filteredCategories.indexOf(getCategoryTitle(selectedCategory));
        }
        if (selectedItem != null) {
            selectedItemIndex = filteredItems.indexOf(getItemTitle(selectedItem));
        }
    }

    protected void updateCategoryList() {
        if (catSearch.isEmpty()) {
            filteredCategories = new ArrayList<>(categoryData.keySet());
        } else {
            filteredCategories = new ArrayList<>();
            String searchLower = catSearch.toLowerCase();
            for (String name : categoryData.keySet()) {
                if (name.toLowerCase().contains(searchLower)) {
                    filteredCategories.add(name);
                }
            }
        }
        Collections.sort(filteredCategories);
    }

    protected void updateItemList() {
        if (itemSearch.isEmpty()) {
            filteredItems = new ArrayList<>(itemData.keySet());
        } else {
            filteredItems = new ArrayList<>();
            String searchLower = itemSearch.toLowerCase();
            for (String name : itemData.keySet()) {
                if (name.toLowerCase().contains(searchLower)) {
                    filteredItems.add(name);
                }
            }
        }
        Collections.sort(filteredItems);
    }

    // ===== Drawing =====

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Draw column headers
        int headerY = getContentY() - 2;
        fontRendererObj.drawString(getCategoryColumnTitle(), catListX, headerY - 10, ModernColors.TEXT_LIGHT);
        fontRendererObj.drawString(getItemColumnTitle(), itemListX, headerY - 10, ModernColors.TEXT_LIGHT);

        // Draw search fields
        catSearchField.draw(mouseX, mouseY);
        itemSearchField.draw(mouseX, mouseY);

        // Draw list backgrounds
        drawRect(catListX, listY, catListX + catListW, listY + listHeight, ModernColors.INPUT_BG);
        drawRect(itemListX, listY, itemListX + itemListW, listY + listHeight, ModernColors.INPUT_BG);

        // Draw divider handle
        int divX = guiLeft + dividerOffset;
        int handleTop = listY + (listHeight - 20) / 2;
        int handleColor = isDraggingDivider ? ModernColors.ACCENT_BLUE : 0xFF707070;
        drawRect(divX + 1, handleTop, divX + dividerWidth - 1, handleTop + 20, handleColor);

        // Draw category list
        drawList(catListX, listY, catListW, listHeight, filteredCategories, selectedCatIndex,
                catScrollY, mouseX, mouseY);

        // Draw item list
        drawList(itemListX, listY, itemListW, listHeight, filteredItems, selectedItemIndex,
                itemScrollY, mouseX, mouseY);

        // Draw selected item info
        if (selectedItem != null) {
            String info = "Selected: " + getItemTitle(selectedItem) + " (ID: " + getItemId(selectedItem) + ")";
            int infoY = listY + listHeight + 4;
            fontRendererObj.drawString(info, guiLeft + 8, infoY, ModernColors.TEXT_GRAY);
        }

        // Draw buttons
        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    protected void drawList(int x, int y, int w, int h, List<String> items, int selectedIdx,
                            int scrollY, int mouseX, int mouseY) {
        // Set up scissor
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (y + h) * scale, w * scale, h * scale);

        int visibleRows = h / rowHeight;
        int startIdx = scrollY / rowHeight;
        int endIdx = Math.min(startIdx + visibleRows + 2, items.size());

        for (int i = startIdx; i < endIdx; i++) {
            int rowY = y + i * rowHeight - scrollY;
            if (rowY + rowHeight < y || rowY > y + h) continue;

            // Draw selection background
            if (i == selectedIdx) {
                drawRect(x, rowY, x + w, rowY + rowHeight, ModernColors.SELECTION_BG);
            } else {
                // Hover highlight
                if (mouseX >= x && mouseX < x + w && mouseY >= rowY && mouseY < rowY + rowHeight) {
                    drawRect(x, rowY, x + w, rowY + rowHeight, ModernColors.HOVER_HIGHLIGHT);
                }
            }

            // Draw text
            String text = items.get(i);
            String displayText = fontRendererObj.trimStringToWidth(text, w - 4);
            int textColor = i == selectedIdx ? ModernColors.TEXT_WHITE : ModernColors.TEXT_LIGHT;
            fontRendererObj.drawString(displayText, x + 2, rowY + 2, textColor);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scrollbar if needed
        int totalHeight = items.size() * rowHeight;
        if (totalHeight > h) {
            int sbX = x + w - 4;
            float viewRatio = (float) h / totalHeight;
            int thumbH = Math.max(10, (int) (h * viewRatio));
            float maxScroll = totalHeight - h;
            int thumbY = maxScroll > 0 ? (int) ((scrollY / maxScroll) * (h - thumbH)) : 0;

            drawRect(sbX, y, sbX + 4, y + h, ModernColors.SCROLLBAR_BG);
            drawRect(sbX, y + thumbY, sbX + 4, y + thumbY + thumbH, ModernColors.SCROLLBAR_THUMB);
        }
    }

    // ===== Interaction =====

    @Override
    public void updateScreen() {
        super.updateScreen();
        catSearchField.updateCursorCounter();
        itemSearchField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Check divider drag start
        if (button == 0) {
            int divX = guiLeft + dividerOffset;
            if (mouseX >= divX && mouseX < divX + dividerWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {
                isDraggingDivider = true;
                dividerDragStartX = mouseX;
                return;
            }
        }

        // Check search field clicks
        catSearchField.mouseClicked(mouseX, mouseY, button);
        itemSearchField.mouseClicked(mouseX, mouseY, button);

        // Check category list click
        if (button == 0 && mouseX >= catListX && mouseX < catListX + catListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + catScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredCategories.size()) {
                selectedCatIndex = clickedIdx;
                String catName = filteredCategories.get(clickedIdx);
                selectedCategory = categoryData.get(catName);
                selectedItem = null;
                selectedItemIndex = -1;
                itemScrollY = 0;
                loadItemsForSelectedCategory();
            }
        }

        // Check item list click
        if (button == 0 && mouseX >= itemListX && mouseX < itemListX + itemListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + itemScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredItems.size()) {
                // Double-click detection
                long now = System.currentTimeMillis();
                if (clickedIdx == lastClickedIndex && now - lastClickTime < 400) {
                    // Double-click - confirm and close
                    confirm();
                    return;
                }
                lastClickedIndex = clickedIdx;
                lastClickTime = now;

                selectedItemIndex = clickedIdx;
                String itemName = filteredItems.get(clickedIdx);
                selectedItem = itemData.get(itemName);
            }
        }

        // Check button clicks
        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            confirm();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            selectedItem = null;
            close();
            return;
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (isDraggingDivider) {
            int dx = mouseX - dividerDragStartX;
            dividerDragStartX = mouseX;
            dividerOffset += dx;

            // Enforce minimum widths
            int pad = 8;
            int minOffset = pad + minColumnWidth;
            int maxOffset = xSize - pad - dividerWidth - minColumnWidth;
            dividerOffset = Math.max(minOffset, Math.min(dividerOffset, maxOffset));

            // Recalculate column widths
            catListW = dividerOffset - pad;
            itemListX = guiLeft + dividerOffset + dividerWidth;
            itemListW = xSize - dividerOffset - dividerWidth - pad;

            // Update search field widths
            catSearchField.setBounds(catListX, catSearchField.getY(), catListW, 18);
            itemSearchField.setBounds(itemListX, itemSearchField.getY(), itemListW, 18);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        isDraggingDivider = false;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int delta = org.lwjgl.input.Mouse.getEventDWheel();
        if (delta != 0) {
            int mouseX = org.lwjgl.input.Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - org.lwjgl.input.Mouse.getEventY() * height / mc.displayHeight - 1;

            int scrollAmount = delta > 0 ? -rowHeight * 2 : rowHeight * 2;

            // Check which list is hovered
            if (mouseX >= catListX && mouseX < catListX + catListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredCategories.size() * rowHeight - listHeight);
                catScrollY = Math.max(0, Math.min(catScrollY + scrollAmount, maxScroll));
            }

            if (mouseX >= itemListX && mouseX < itemListX + itemListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredItems.size() * rowHeight - listHeight);
                itemScrollY = Math.max(0, Math.min(itemScrollY + scrollAmount, maxScroll));
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Handle category search field
        if (catSearchField.isFocused()) {
            String prev = catSearchField.getText();
            catSearchField.keyTyped(typedChar, keyCode);
            String newText = catSearchField.getText();
            if (!newText.equals(prev)) {
                catSearch = newText;
                updateCategoryList();
                catScrollY = 0;
                if (selectedCategory != null) {
                    selectedCatIndex = filteredCategories.indexOf(getCategoryTitle(selectedCategory));
                }
            }
        }

        // Handle item search field
        if (itemSearchField.isFocused()) {
            String prev = itemSearchField.getText();
            itemSearchField.keyTyped(typedChar, keyCode);
            String newText = itemSearchField.getText();
            if (!newText.equals(prev)) {
                itemSearch = newText;
                updateItemList();
                itemScrollY = 0;
                if (selectedItem != null) {
                    selectedItemIndex = filteredItems.indexOf(getItemTitle(selectedItem));
                }
            }
        }

        // Handle ESC
        super.keyTyped(typedChar, keyCode);
    }

    protected void confirm() {
        close();
    }

    /**
     * Get the selected item, or null if none selected.
     */
    public I getSelectedItem() {
        return selectedItem;
    }
}
