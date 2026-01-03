package noppes.npcs.client.gui.util.script.autocomplete;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * UI component for displaying autocomplete suggestions.
 * Styled similar to SearchReplaceBar with IntelliJ-like appearance.
 */
public class AutocompleteMenu extends Gui {
    
    // ==================== CONSTANTS ====================
    private static final int MAX_VISIBLE_ITEMS = 10;
    private static final int ITEM_HEIGHT = 16;
    private static final int PADDING = 4;
    private static final int ICON_WIDTH = 16;
    private static final int MIN_WIDTH = 200;
    private static final int MAX_WIDTH = 400;
    private static final int HINT_HEIGHT = 18;
    
    // ==================== COLORS ====================
    private static final int BG_COLOR = 0xFF2d2d30;
    private static final int BORDER_COLOR = 0xFF3c3c3c;
    private static final int SELECTED_BG = 0xFF094771;
    private static final int HOVER_BG = 0xFF37373d;
    private static final int TEXT_COLOR = 0xFFe0e0e0;
    private static final int DIM_TEXT_COLOR = 0xFF808080;
    private static final int HIGHLIGHT_COLOR = 0xFFFFD866;
    private static final int HINT_BG_COLOR = 0xFF252526;
    private static final int SCROLLBAR_BG = 0xFF3c3c3c;
    private static final int SCROLLBAR_FG = 0xFF606060;
    
    // ==================== STATE ====================
    private boolean visible = false;
    private List<AutocompleteItem> items = new ArrayList<>();
    private int selectedIndex = 0;
    private int scrollOffset = 0;
    private int hoveredIndex = -1;

    // Scrollbar drag state
    private boolean isDraggingScrollbar = false;
    private int dragStartY = 0;
    private int dragStartScroll = 0;
    
    // ==================== POSITION ====================
    private int x, y;
    private int width, height;
    private int menuWidth;
    
    // ==================== FONT ====================
    private final FontRenderer font;
    
    // ==================== CALLBACK ====================
    private AutocompleteCallback callback;
    
    /**
     * Callback interface for autocomplete events.
     */
    public interface AutocompleteCallback {
        void onItemSelected(AutocompleteItem item);
        void onDismiss();
    }
    
    public AutocompleteMenu() {
        this.font = Minecraft.getMinecraft().fontRenderer;
    }
    
    public void setCallback(AutocompleteCallback callback) {
        this.callback = callback;
    }
    
    // ==================== VISIBILITY ====================
    
    /**
     * Show the menu at the specified position with the given items.
     */
    public void show(int x, int y, List<AutocompleteItem> items, int viewportWidth, int viewportHeight) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.selectedIndex = 0;
        this.scrollOffset = 0;
        this.hoveredIndex = -1;
        
        // Calculate menu dimensions
        calculateDimensions(x, y, viewportWidth, viewportHeight);
        
        this.visible = !this.items.isEmpty();
    }
    
    /**
     * Update the items while keeping the menu open.
     */
    public void updateItems(List<AutocompleteItem> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        
        // Clamp selection
        if (selectedIndex >= items.size()) {
            selectedIndex = Math.max(0, items.size() - 1);
        }
        
        // Clamp scroll
        int maxScroll = Math.max(0, items.size() - MAX_VISIBLE_ITEMS);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
        
        visible = !items.isEmpty();
    }
    
    /**
     * Hide the menu.
     */
    public void hide() {
        visible = false;
        if (callback != null) {
            callback.onDismiss();
        }
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean hasItems() {
        return !items.isEmpty();
    }
    
    // ==================== DIMENSIONS ====================
    
    private void calculateDimensions(int cursorX, int cursorY, int viewportWidth, int viewportHeight) {
        // Calculate width based on item content
        int maxItemWidth = MIN_WIDTH;
        for (AutocompleteItem item : items) {
            int itemWidth = ICON_WIDTH + PADDING + font.getStringWidth(item.getName()) + PADDING;
            if (item.getTypeLabel() != null) {
                itemWidth += font.getStringWidth(item.getTypeLabel()) + PADDING * 2;
            }
            maxItemWidth = Math.max(maxItemWidth, itemWidth);
        }
        menuWidth = Math.min(maxItemWidth + 20, MAX_WIDTH); // +20 for scrollbar
        
        // Calculate height
        int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
        int menuHeight = visibleItems * ITEM_HEIGHT + HINT_HEIGHT + PADDING * 2;
        
        // Position below cursor, or above if not enough space
        this.x = cursorX;
        this.y = cursorY;
        
        // Check if menu would go off-screen to the right
        if (x + menuWidth > viewportWidth - 10) {
            x = viewportWidth - menuWidth - 10;
        }
        
        // Check if menu would go off-screen to the bottom
        if (y + menuHeight > viewportHeight - 10) {
            // Show above cursor instead
            y = cursorY - menuHeight - 20; // 20 for line height
        }
        
        // Ensure not off-screen to the left or top
        x = Math.max(5, x);
        y = Math.max(5, y);
        
        this.width = menuWidth;
        this.height = menuHeight;
    }
    
    // ==================== SELECTION ====================
    
    /**
     * Move selection up.
     */
    public void selectPrevious() {
        if (items.isEmpty()) return;
        
        selectedIndex--;
        if (selectedIndex < 0) {
            selectedIndex = items.size() - 1;
            scrollOffset = Math.max(0, items.size() - MAX_VISIBLE_ITEMS);
        } else if (selectedIndex < scrollOffset) {
            scrollOffset = selectedIndex;
        }
    }
    
    /**
     * Move selection down.
     */
    public void selectNext() {
        if (items.isEmpty()) return;
        
        selectedIndex++;
        if (selectedIndex >= items.size()) {
            selectedIndex = 0;
            scrollOffset = 0;
        } else if (selectedIndex >= scrollOffset + MAX_VISIBLE_ITEMS) {
            scrollOffset = selectedIndex - MAX_VISIBLE_ITEMS + 1;
        }
    }
    
    /**
     * Confirm the current selection.
     */
    public void confirmSelection() {
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size()) {
            hide();
            return;
        }
        
        AutocompleteItem selected = items.get(selectedIndex);
        if (callback != null) {
            callback.onItemSelected(selected);
        }
        hide();
    }
    
    /**
     * Get the currently selected item.
     */
    public AutocompleteItem getSelectedItem() {
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size()) {
            return null;
        }
        return items.get(selectedIndex);
    }
    
    // ==================== DRAWING ====================
    
    /**
     * Draw the autocomplete menu.
     */
    public void draw(int mouseX, int mouseY) {
        if (!visible || items.isEmpty()) return;
        
        // Update hover state
        updateHoverState(mouseX, mouseY);
        
        // Enable scissoring to clip content
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        setScissor(x - 2, y - 2, width + 4, height + 4);
        
        // Draw background
        drawRect(x, y, x + width, y + height, BG_COLOR);
        
        // Draw border
        drawRect(x, y, x + width, y + 1, BORDER_COLOR);
        drawRect(x, y + height - 1, x + width, y + height, BORDER_COLOR);
        drawRect(x, y, x + 1, y + height, BORDER_COLOR);
        drawRect(x + width - 1, y, x + width, y + height, BORDER_COLOR);
        
        // Draw items
        int itemY = y + PADDING;
        int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
        
        for (int i = 0; i < visibleItems; i++) {
            int itemIndex = scrollOffset + i;
            if (itemIndex >= items.size()) break;
            
            AutocompleteItem item = items.get(itemIndex);
            boolean isSelected = (itemIndex == selectedIndex);
            boolean isHovered = (itemIndex == hoveredIndex);
            
            drawItem(item, x + PADDING, itemY, width - PADDING * 2 - 8, isSelected, isHovered);
            itemY += ITEM_HEIGHT;
        }
        
        // Draw scrollbar if needed
        if (items.size() > MAX_VISIBLE_ITEMS) {
            drawScrollbar(mouseX, mouseY);
        }
        
        // Draw hint bar at bottom
        drawHintBar();
        
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    
    /**
     * Draw a single autocomplete item.
     */
    private void drawItem(AutocompleteItem item, int itemX, int itemY, int itemWidth, 
                          boolean selected, boolean hovered) {
        // Background
        if (selected) {
            drawRect(itemX - 2, itemY, itemX + itemWidth + 2, itemY + ITEM_HEIGHT, SELECTED_BG);
        } else if (hovered) {
            drawRect(itemX - 2, itemY, itemX + itemWidth + 2, itemY + ITEM_HEIGHT, HOVER_BG);
        }
        
        int textX = itemX;
        int textY = itemY + (ITEM_HEIGHT - font.FONT_HEIGHT) / 2;
        
        // Draw icon
        String icon = item.getIconId();
        int iconColor = item.getIconColor();
        font.drawString(icon, textX + (ICON_WIDTH - font.getStringWidth(icon)) / 2, textY, iconColor);
        textX += ICON_WIDTH;
        
        // Draw name with match highlighting
        drawHighlightedText(item.getName(), item.getMatchIndices(), textX, textY, 
            item.isDeprecated() ? DIM_TEXT_COLOR : TEXT_COLOR);
        
        // Draw type label on the right
        if (item.getTypeLabel() != null && !item.getTypeLabel().isEmpty()) {
            String typeLabel = item.getTypeLabel();
            int typeLabelWidth = font.getStringWidth(typeLabel);
            int typeLabelX = itemX + itemWidth - typeLabelWidth - PADDING;
            font.drawString(typeLabel, typeLabelX, textY, DIM_TEXT_COLOR);
        }
    }
    
    /**
     * Draw text with specific characters highlighted.
     */
    private void drawHighlightedText(String text, int[] matchIndices, int x, int y, int baseColor) {
        if (matchIndices == null || matchIndices.length == 0) {
            font.drawString(text, x, y, baseColor);
            return;
        }
        
        // Create a set of highlighted indices for quick lookup
        java.util.Set<Integer> highlightSet = new java.util.HashSet<>();
        for (int idx : matchIndices) {
            highlightSet.add(idx);
        }
        
        // Draw character by character
        int currentX = x;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            int color = highlightSet.contains(i) ? HIGHLIGHT_COLOR : baseColor;
            font.drawString(ch, currentX, y, color);
            currentX += font.getStringWidth(ch);
        }
    }
    
    /**
     * Draw the scrollbar.
     */
    private void drawScrollbar(int mouseX, int mouseY) {
        int scrollbarX = x + width - 8;
        int scrollbarY = y + PADDING;
        int scrollbarHeight = MAX_VISIBLE_ITEMS * ITEM_HEIGHT;
        
        // Background
        drawRect(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, SCROLLBAR_BG);
        
        // Thumb
        float thumbRatio = (float) MAX_VISIBLE_ITEMS / items.size();
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * thumbRatio));
        float thumbPosRatio = (float) scrollOffset / Math.max(1, items.size() - MAX_VISIBLE_ITEMS);
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * thumbPosRatio);
        
        // Check if mouse is above the scrollbar thumb
        boolean isAboveScrollbar = mouseX >= scrollbarX && mouseX <= scrollbarX + 6 && 
                                   mouseY >= thumbY && mouseY < thumbY + thumbHeight;
        
        int col = isDraggingScrollbar || isAboveScrollbar? 0xFF808080 : SCROLLBAR_FG;

        drawRect(scrollbarX + 1, thumbY, scrollbarX + 5, thumbY + thumbHeight, col);
    }
    
    /**
     * Draw the hint bar at the bottom (shows Tab/Enter to confirm).
     */
    private void drawHintBar() {
        int hintY = y + height - HINT_HEIGHT;
        
        // Darker background for hint area
        drawRect(x + 1, hintY, x + width - 1, y + height - 1, HINT_BG_COLOR);
        
        // Draw hints
        int hintTextY = hintY + (HINT_HEIGHT - font.FONT_HEIGHT) / 2;
        int hintX = x + PADDING;
        
        // Tab hint
        drawKeyHint("Tab", hintX, hintTextY);
        hintX += font.getStringWidth("Tab") + 8;
        font.drawString("or", hintX, hintTextY, DIM_TEXT_COLOR);
        hintX += font.getStringWidth("or") + 4;
        
        // Enter hint
        drawKeyHint("Enter", hintX, hintTextY);
        hintX += font.getStringWidth("Enter") + 8;
        font.drawString("to insert", hintX, hintTextY, DIM_TEXT_COLOR);
    }
    
    /**
     * Draw a key hint with a darker background box.
     */
    private void drawKeyHint(String key, int x, int y) {
        int keyWidth = font.getStringWidth(key);
        int boxPadding = 2;
        
        // Draw box
        drawRect(x - boxPadding, y - boxPadding, 
                 x + keyWidth + boxPadding, y + font.FONT_HEIGHT + boxPadding, 
                 0xFF404040);
        
        // Draw key text
        font.drawString(key, x, y, TEXT_COLOR);
    }
    
    // ==================== MOUSE HANDLING ====================
    
    /**
     * Update hover state based on mouse position.
     */
    private void updateHoverState(int mouseX, int mouseY) {
        hoveredIndex = -1;
        
        if (!isMouseInBounds(mouseX, mouseY)) return;
        
        int itemY = y + PADDING;
        int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
        
        for (int i = 0; i < visibleItems; i++) {
            if (mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT) {
                hoveredIndex = scrollOffset + i;
                break;
            }
            itemY += ITEM_HEIGHT;
        }
    }
    
    /**
     * Handle mouse click.
     * @return true if click was consumed
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!visible) return false;
        
        // Check if click is outside menu
        if (!isMouseInBounds(mouseX, mouseY)) {
            hide();
            return false;
        }

        // Check if clicking on scrollbar
        if (button == 0 && items.size() > MAX_VISIBLE_ITEMS) {
            int scrollbarX = x + width - 8;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 6) {
                // Clicked on scrollbar area - start drag
                isDraggingScrollbar = true;
                dragStartY = mouseY;
                dragStartScroll = scrollOffset;
                return true;
            }
        }
        
        // Check if clicking on an item
        if (button == 0 && hoveredIndex >= 0 && hoveredIndex < items.size()) {
            selectedIndex = hoveredIndex;
            confirmSelection();
            return true;
        }
        
        return true; // Consume click if inside menu
    }

    /**
     * Handle mouse release.
     * @return true if release was consumed
     */
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return false;
    }

    /**
     * Handle mouse drag.
     * @return true if drag was consumed
     */
    public boolean mouseDragged(int mouseX, int mouseY) {
        if (!visible || !isDraggingScrollbar)
            return false;

        // Calculate scroll area height
        int listHeight = MAX_VISIBLE_ITEMS * ITEM_HEIGHT;
        int scrollbarHeight = Math.max(20, (listHeight * MAX_VISIBLE_ITEMS) / items.size());
        int scrollTrackHeight = listHeight - scrollbarHeight;

        // Calculate new scroll offset based on drag
        int deltaY = mouseY - dragStartY;
        int maxScroll = items.size() - MAX_VISIBLE_ITEMS;

        if (scrollTrackHeight > 0) {
            int scrollDelta = (deltaY * maxScroll) / scrollTrackHeight;
            scrollOffset = Math.max(0, Math.min(maxScroll, dragStartScroll + scrollDelta));
        }

        return true;
    }
    
    /**
     * Handle mouse scroll.
     * @return true if scroll was consumed
     */
    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        if (!visible || !isMouseInBounds(mouseX, mouseY)) return false;
        
        if (items.size() > MAX_VISIBLE_ITEMS) {
            if (delta > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else {
                scrollOffset = Math.min(items.size() - MAX_VISIBLE_ITEMS, scrollOffset + 1);
            }
            return true;
        }
        
        return false;
    }
    
    private boolean isMouseInBounds(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    // ==================== UTILITY ====================
    
    private void setScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        
        int scissorX = x * scaleFactor;
        int scissorY = (sr.getScaledHeight() - y - height) * scaleFactor;
        int scissorW = width * scaleFactor;
        int scissorH = height * scaleFactor;
        
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
    }
    
    // ==================== GETTERS ====================
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<AutocompleteItem> getItems() { return items; }
}
