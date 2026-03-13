package noppes.npcs.client.gui.util.script.autocomplete;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
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

    // Panel pan/drag state
    private boolean isDraggingPanel;
    private int panelDragOffsetX, panelDragOffsetY;
    private boolean panelPositionOverridden;
    private int overriddenX, overriddenY;
    private boolean hasPendingPanelDrag;
    private int pendingDragStartX, pendingDragStartY;
    private boolean pendingItemConfirm;

    // Panel resize state
    private boolean isResizingPanel;
    private int resizeInitMouseX, resizeInitMouseY;
    private int resizeInitW, resizeInitH;
    private boolean panelSizeOverridden;
    private int overriddenW, overriddenH;
    private static final int MIN_RESIZE_W = 140;
    private static final int MIN_RESIZE_H = 60;
    private static final int DRAG_THRESHOLD = 4;
    
    // ==================== POSITION ====================
    private int x, y;
    public GuiNPCInterface ownerGui;
    private int width, height;
    private int menuWidth;
    private int visibleItemsCount; // Actual number of items that fit in the menu
    
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
        this.visibleItemsCount = MAX_VISIBLE_ITEMS;
    }

    private int getVisibleItemCapacity() {
        if (items == null || items.isEmpty())
            return 0;
        int cap = visibleItemsCount > 0 ? visibleItemsCount : MAX_VISIBLE_ITEMS;
        if (!panelSizeOverridden) cap = Math.min(cap, MAX_VISIBLE_ITEMS);
        return Math.max(1, Math.min(cap, items.size()));
    }

    private int getMaxScrollOffset() {
        int cap = getVisibleItemCapacity();
        if (cap <= 0)
            return 0;
        return Math.max(0, items.size() - cap);
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
        int maxScroll = getMaxScrollOffset();
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
        isDraggingPanel = false;
        isResizingPanel = false;
        panelPositionOverridden = false;
        panelSizeOverridden = false;
        hasPendingPanelDrag = false;
        pendingItemConfirm = false;
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
        
        // Calculate initial height
        int visibleItems = Math.min(items.size(), MAX_VISIBLE_ITEMS);
        int menuHeight = visibleItems * ITEM_HEIGHT + HINT_HEIGHT + PADDING * 2;
        
        // Line height estimation (cursor position is typically at baseline)
        int lineHeight = 20;
        
        // Calculate available space
        int spaceBelow = viewportHeight - cursorY - 10; // Space below cursor
        int spaceAbove = cursorY - lineHeight - 10;     // Space above current line
        
        // Determine if we should use horizontal positioning
        // Use horizontal if both vertical positions would be cramped or block the cursor line
        boolean useHorizontalPosition = false;
        if (spaceBelow < menuHeight && spaceAbove < menuHeight) {
            // Check if horizontal positioning would work better
            int spaceRight = viewportWidth - (cursorX + 50) - 10; // 50 = offset from cursor
            if (spaceRight >= menuWidth && viewportHeight > menuHeight + 20) {
                useHorizontalPosition = true;
            } else {
                // Reduce height to fit in the best available vertical space
                int availableVerticalSpace = Math.max(spaceBelow, spaceAbove);
                if (availableVerticalSpace < menuHeight) {
                    // Calculate how many items we can show in available space
                    int maxItemsForSpace = (availableVerticalSpace - HINT_HEIGHT - PADDING * 2) / ITEM_HEIGHT;
                    maxItemsForSpace = Math.max(3, Math.min(maxItemsForSpace, items.size())); // At least 3 items
                    visibleItems = maxItemsForSpace;
                    menuHeight = visibleItems * ITEM_HEIGHT + HINT_HEIGHT + PADDING * 2;
                }
            }
        }
        
        // Store the calculated visible items count
        this.visibleItemsCount = visibleItems;
        
        // Position the menu
        if (useHorizontalPosition) {
            // Position to the right of the cursor
            this.x = cursorX + 50;
            this.y = cursorY - lineHeight; // Align with current line
            
            // Ensure doesn't go off bottom
            if (y + menuHeight > viewportHeight - 10) {
                y = viewportHeight - menuHeight - 10;
            }
        } else {
            // Standard vertical positioning
            this.x = cursorX;
            
            // Try below first
            if (spaceBelow >= menuHeight) {
                this.y = cursorY ;
            } else if (spaceAbove >= menuHeight) {
                // Show above cursor line
                this.y = cursorY - menuHeight - lineHeight;
            } else {
                // Use the larger space
                if (spaceBelow >= spaceAbove) {
                    this.y = cursorY;
                } else {
                    this.y = cursorY - menuHeight - lineHeight;
                }
            }
            
            // Check if menu would go off-screen to the right
            if (x + menuWidth > viewportWidth - 10) {
                x = viewportWidth - menuWidth - 10;
            }
        }
        
        // Ensure not off-screen to the left or top
        x = Math.max(5, x);
        y = Math.max(5, y);
        
        this.width = menuWidth;
        this.height = menuHeight;
        // Apply position override if user has dragged the panel
        if (panelPositionOverridden) { this.x = overriddenX; this.y = overriddenY; }
        // Apply size override if user has resized the panel
        if (panelSizeOverridden) {
            this.menuWidth = overriddenW;
            this.width = overriddenW;
            this.height = overriddenH;
            this.visibleItemsCount = Math.max(1, (overriddenH - HINT_HEIGHT - PADDING * 2) / ITEM_HEIGHT);
        }
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
            scrollOffset = getMaxScrollOffset();
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
        } else {
            int cap = getVisibleItemCapacity();
            if (cap > 0 && selectedIndex >= scrollOffset + cap) {
                scrollOffset = selectedIndex - cap + 1;
            }
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
        
        // Draw items — tighter scissor so partial last item is clipped at the items-area boundary
        int itemAreaH = height - HINT_HEIGHT - PADDING * 2;
        setScissor(x + 1, y + PADDING , width - 2, itemAreaH);
        int itemY = y + PADDING;

        int visibleCount = getVisibleItemCapacity();
        int renderCount = Math.min(visibleCount + 1, items.size() - scrollOffset);
        for (int i = 0; i < renderCount; i++) {
            int itemIndex = scrollOffset + i;
            if (itemIndex >= items.size()) break;

            AutocompleteItem item = items.get(itemIndex);
            boolean isSelected = (itemIndex == selectedIndex);
            boolean isHovered  = (itemIndex == hoveredIndex);

            drawItem(item, x + PADDING, itemY, width - PADDING * 2 - 8, isSelected, isHovered);
            itemY += ITEM_HEIGHT;
        }

        // Restore full-panel scissor for scrollbar and hint bar
        setScissor(x - 2, y - 2, width + 4, height + 4);
        
        // Draw scrollbar if needed
        if (items.size() > visibleCount) {
            drawScrollbar(mouseX, mouseY);
        }
        
        // Draw hint bar at bottom
        drawHintBar();
        
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        // Draw resize handle outside scissor
        drawResizeHandle(mouseX, mouseY);
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

        // Draw modifier text (static/final) if present
        boolean isStatic = item.isStatic();
        boolean isFinal = item.isFinal();
        if (isStatic || isFinal) {
            GL11.glPushMatrix();
            float scale = 0.5f;
            GL11.glScalef(scale, scale, scale);
            int col = TokenType.KEYWORD.getHexColor();
            
            if (isStatic)
                font.drawString("s", (int) (textX / scale), (int) (textY / scale), col);
            if (isFinal)
                font.drawString("f", (int) (textX / scale), (int) (textY / scale) + 10, col);
            GL11.glPopMatrix();
        }
        
        // Draw icon
        String icon = item.getIconId();
        int iconColor = item.getIconColor();
        font.drawString(icon, textX + (ICON_WIDTH - font.getStringWidth(icon)) / 2, textY, iconColor);
        textX += ICON_WIDTH;
        
        // Calculate available width for text (leave space for type label)
        int availableWidth = itemWidth - (textX - itemX);
        if (item.getTypeLabel() != null && !item.getTypeLabel().isEmpty()) {
            int typeLabelWidth = font.getStringWidth(item.getTypeLabel());
            availableWidth -= typeLabelWidth + PADDING * 2;
        }
        
        // Determine text color - gray for inherited Object methods
        int textColor = getColor(item);
        if (item.getKind() == AutocompleteItem.Kind.METHOD) {
            drawMethodNameTruncated(item.getName(), item.getMatchIndices(), textX, textY,
                    textColor, availableWidth);
        } else {
            drawHighlightedTextTruncated(item.getName(), item.getMatchIndices(), textX, textY, 
                textColor, availableWidth);
        }
        
        // Draw type label on the right
        if (item.getTypeLabel() != null && !item.getTypeLabel().isEmpty()) {
            String typeLabel = item.getTypeLabel();
            int typeLabelWidth = font.getStringWidth(typeLabel);
            int typeLabelX = itemX + itemWidth - typeLabelWidth - PADDING;

            TypeInfo type = item.getTypeInfo();
            int col = type != null ? type.getTokenType().getHexColor() : DIM_TEXT_COLOR;
            drawTypeLabel(item, typeLabel, typeLabelX, textY, col, type);
        }
    }

    public int getColor(AutocompleteItem item) {
        int col = item.getColor();
        if (col != -1)
            return col;
        
        if (item.isInheritedObjectMethod() || item.isDeprecated())
            return DIM_TEXT_COLOR;

        switch (item.getKind()) {
            case METHOD:
                return TokenType.METHOD_CALL.getHexColor();
            case FIELD:
                return TokenType.GLOBAL_FIELD.getHexColor();
            case ENUM_CONSTANT:
                return TokenType.ENUM_CONSTANT.getHexColor();
            case CLASS:
                return TokenType.getColor(item.getTypeInfo());
            case VARIABLE:
                return TokenType.LOCAL_FIELD.getHexColor();
            case KEYWORD:
                return TokenType.KEYWORD.getHexColor();
            default:
                return TEXT_COLOR;
        }
    }
    
    /**
     * Draw method name with parameters colored gray.
     */
    private void drawMethodName(String text, int[] matchIndices, int x, int y, int baseColor) {
        // Find the opening parenthesis
        int parenIndex = text.indexOf('(');
        if (parenIndex == -1) {
            // No parameters, just draw normally
            drawHighlightedText(text, matchIndices, x, y, baseColor);
            return;
        }
        
        // Draw method name part with highlighting
        String methodName = text.substring(0, parenIndex);
        drawHighlightedText(methodName, matchIndices, x, y, baseColor);
        
        // Draw parameters part in gray (no highlighting)
        String params = text.substring(parenIndex);
        int paramX = x + font.getStringWidth(methodName);
        font.drawString(params, paramX, y, DIM_TEXT_COLOR);
    }
    
    /**
     * Draw method name with parameters colored gray, truncated if too long.
     */
    private void drawMethodNameTruncated(String text, int[] matchIndices, int x, int y, int baseColor, int maxWidth) {
        // Find the opening parenthesis
        int parenIndex = text.indexOf('(');
        if (parenIndex == -1) {
            // No parameters, just draw normally
            drawHighlightedTextTruncated(text, matchIndices, x, y, baseColor, maxWidth);
            return;
        }
        
        String methodName = text.substring(0, parenIndex);
        String params = text.substring(parenIndex);
        
        int methodNameWidth = font.getStringWidth(methodName);
        int paramsWidth = font.getStringWidth(params);
        int totalWidth = methodNameWidth + paramsWidth;
        
        if (totalWidth <= maxWidth) {
            // Fits, draw normally
            drawHighlightedText(methodName, matchIndices, x, y, baseColor);
            int paramX = x + methodNameWidth;
            font.drawString(params, paramX, y, DIM_TEXT_COLOR);
        } else {
            // Need to truncate
            String ellipsis = "...";
            int ellipsisWidth = font.getStringWidth(ellipsis);
            
            // Always show method name, truncate params if needed
            if (methodNameWidth + ellipsisWidth < maxWidth) {
                drawHighlightedText(methodName, matchIndices, x, y, baseColor);
                int paramX = x + methodNameWidth;
                
                // Truncate parameters
                int availableForParams = maxWidth - methodNameWidth - ellipsisWidth;
                String truncatedParams = truncateString(params, availableForParams);
                font.drawString(truncatedParams + ellipsis, paramX, y, DIM_TEXT_COLOR);
            } else {
                // Even method name doesn't fit, truncate it too
                int availableForMethod = maxWidth - ellipsisWidth;
                String truncatedMethod = truncateString(methodName, availableForMethod);
                drawHighlightedText(truncatedMethod, matchIndices, x, y, baseColor);
                font.drawString(ellipsis, x + font.getStringWidth(truncatedMethod), y, baseColor);
            }
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
     * Draw text with highlighting, truncated if too long.
     */
    private void drawHighlightedTextTruncated(String text, int[] matchIndices, int x, int y, int baseColor, int maxWidth) {
        int textWidth = font.getStringWidth(text);
        
        if (textWidth <= maxWidth) {
            // Fits, draw normally
            drawHighlightedText(text, matchIndices, x, y, baseColor);
        } else {
            // Truncate with ellipsis
            String ellipsis = "...";
            int ellipsisWidth = font.getStringWidth(ellipsis);
            String truncated = truncateString(text, maxWidth - ellipsisWidth);
            
            // Adjust match indices for truncated text
            int[] adjustedIndices = null;
            if (matchIndices != null) {
                java.util.List<Integer> validIndices = new java.util.ArrayList<>();
                for (int idx : matchIndices) {
                    if (idx < truncated.length()) {
                        validIndices.add(idx);
                    }
                }
                adjustedIndices = new int[validIndices.size()];
                for (int i = 0; i < validIndices.size(); i++) {
                    adjustedIndices[i] = validIndices.get(i);
                }
            }
            
            drawHighlightedText(truncated, adjustedIndices, x, y, baseColor);
            font.drawString(ellipsis, x + font.getStringWidth(truncated), y, baseColor);
        }
    }
    
    /**
     * Truncate a string to fit within the given width.
     */
    private String truncateString(String text, int maxWidth) {
        if (text.isEmpty()) return text;
        
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += font.getStringWidth(String.valueOf(text.charAt(i)));
            if (width > maxWidth) {
                return text.substring(0, Math.max(0, i));
            }
        }
        return text;
    }
    
    private void drawTypeLabel(AutocompleteItem item, String typeLabel, int x, int y, int typeColor, TypeInfo typeInfo) {
        if (typeInfo == null || item.getKind() == AutocompleteItem.Kind.CLASS) { //draw package on right
            drawSimpleTypeWithArraySuffix(typeLabel, x, y, typeColor);
            return;
        }
        drawTypeLabel(x, y, typeInfo, 0);
    }

    private int drawTypeLabel(int x, int y, TypeInfo typeInfo, int depth) {
        if (depth > 25) {
            String name = typeInfo.getDisplayName();
            font.drawString(name, x, y, TokenType.getColor(typeInfo));
            return x + font.getStringWidth(name);
        }
        int defaultColor = TokenType.DEFAULT.getHexColor();
        if (typeInfo.isParameterized()) {
            TypeInfo raw = typeInfo.getRawType();
            x = drawSimpleTypeWithArraySuffix(raw.getDisplayName(), x, y, TokenType.getColor(raw));
            font.drawString("<", x, y, defaultColor);
            x += font.getStringWidth("<");
            java.util.List<TypeInfo> args = typeInfo.getAppliedTypeArgs();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    font.drawString(", ", x, y, defaultColor);
                    x += font.getStringWidth(", ");
                }
                x = drawTypeLabel(x, y, args.get(i), depth + 1);
            }
            font.drawString(">", x, y, defaultColor);
            return x + font.getStringWidth(">");
        }
        return drawSimpleTypeWithArraySuffix(typeInfo.getDisplayName(), x, y, TokenType.getColor(typeInfo));
    }

    /** @return x position after last drawn character */
    private int drawSimpleTypeWithArraySuffix(String typeLabel, int x, int y, int typeColor) {
        if (!typeLabel.endsWith("[]")) {
            font.drawString(typeLabel, x, y, typeColor);
            return x + font.getStringWidth(typeLabel);
        }
        int suffixStart = typeLabel.length() - 2;
        while (suffixStart >= 2 && typeLabel.charAt(suffixStart - 2) == '[' && typeLabel.charAt(suffixStart - 1) == ']') {
            suffixStart -= 2;
        }
        String core = typeLabel.substring(0, suffixStart);
        String suffix = typeLabel.substring(suffixStart);
        font.drawString(core, x, y, typeColor);
        int coreWidth = font.getStringWidth(core);
        font.drawString(suffix, x + coreWidth, y, TokenType.DEFAULT.getHexColor());
        return x + coreWidth + font.getStringWidth(suffix);
    }

    private void drawScrollbar(int mouseX, int mouseY) {
        int scrollbarX = x + width - 8;
        int scrollbarY = y + PADDING;
        int visibleCount = getVisibleItemCapacity();
        if (visibleCount <= 0)
            return;
        int scrollbarHeight = visibleCount * ITEM_HEIGHT - 8; // leave room for resize handle
        
        // Background
        drawRect(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, SCROLLBAR_BG);
        
        // Thumb
        float thumbRatio = (float) visibleCount / items.size();
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * thumbRatio));
        float thumbPosRatio = Math.min(1f, (float) scrollOffset / Math.max(1, items.size() - visibleCount));
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * thumbPosRatio);
        thumbY = Math.max(scrollbarY, Math.min(scrollbarY + scrollbarHeight - thumbHeight, thumbY));
        
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

        int visibleItems = getVisibleItemCapacity();

        for (int i = 0; i <= visibleItems; i++) {
            int idx = scrollOffset + i;
            if (idx >= items.size()) break;
            if (mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT) {
                hoveredIndex = idx;
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

        // Resize handle has top priority
        if (button == 0 && isMouseOverResizeHandle(mouseX, mouseY)) {
            isResizingPanel = true;
            resizeInitMouseX = mouseX; resizeInitMouseY = mouseY;
            resizeInitW = width; resizeInitH = height;
            return true;
        }

        // Scrollbar
        if (button == 0 && items.size() > getVisibleItemCapacity()) {
            int scrollbarX = x + width - 8;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 6) {
                isDraggingScrollbar = true;
                dragStartY = mouseY;
                dragStartScroll = scrollOffset;
                return true;
            }
        }

        // Anywhere inside panel body — start pending drag, defer item confirm to release
        if (button == 0) {
            hasPendingPanelDrag = true;
            pendingDragStartX = mouseX;
            pendingDragStartY = mouseY;
            panelDragOffsetX = mouseX - x;
            panelDragOffsetY = mouseY - y;
            if (hoveredIndex >= 0 && hoveredIndex < items.size()) {
                selectedIndex = hoveredIndex;
                pendingItemConfirm = true;
            }
            return true;
        }

        return true; // Consume click if inside menu
    }

    /**
     * Handle mouse release.
     * @return true if release was consumed
     */
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) {
            if (isDraggingScrollbar) { isDraggingScrollbar = false; return true; }
            if (isDraggingPanel)     { isDraggingPanel = false;     return true; }
            if (isResizingPanel)     { isResizingPanel = false;     return true; }
            hasPendingPanelDrag = false;
            if (pendingItemConfirm) {
                pendingItemConfirm = false;
                confirmSelection();
                return true;
            }
        }
        return false;
    }

    /**
     * Handle mouse drag.
     * @return true if drag was consumed
     */
    public boolean mouseDragged(int mouseX, int mouseY) {
        if (!visible) return false;
        if (isDraggingPanel)     { updatePanelDrag(mouseX, mouseY);   return true; }
        if (isResizingPanel)     { updatePanelResize(mouseX, mouseY); return true; }

        // Activate panel drag once movement exceeds threshold
        if (hasPendingPanelDrag) {
            int dx = Math.abs(mouseX - pendingDragStartX);
            int dy = Math.abs(mouseY - pendingDragStartY);
            if (dx > DRAG_THRESHOLD || dy > DRAG_THRESHOLD) {
                isDraggingPanel = true;
                hasPendingPanelDrag = false;
                pendingItemConfirm = false; // cancel any pending selection
                updatePanelDrag(mouseX, mouseY);
                return true;
            }
            return true;
        }

        if (!isDraggingScrollbar) return false;
        // Scrollbar drag (kept for backward compat — also handled via draw loop)
        int visibleCount = getVisibleItemCapacity();
        if (visibleCount <= 0) return false;
        int scrollbarHeight = visibleCount * ITEM_HEIGHT - 8;
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * (float) visibleCount / Math.max(1, items.size())));
        int scrollTrackHeight = Math.max(1, scrollbarHeight - thumbHeight);
        int maxScroll = items.size() - visibleCount;
        int deltaY = mouseY - dragStartY;
        int scrollDelta = (deltaY * maxScroll) / scrollTrackHeight;
        scrollOffset = Math.max(0, Math.min(maxScroll, dragStartScroll + scrollDelta));
        return true;
    }
    
    /**
     * Handle mouse scroll.
     * @return true if scroll was consumed
     */
    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        if (!visible || !isMouseInBounds(mouseX, mouseY)) return false;

        int visibleCount = getVisibleItemCapacity();
        if (items.size() > visibleCount) {
            if (delta > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else {
                scrollOffset = Math.min(items.size() - visibleCount, scrollOffset + 1);
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

        if (ownerGui != null) {
            x -= (int) ownerGui.getPanX();
            y -= (int) ownerGui.getPanY();
        }

        int scissorX = x * scaleFactor;
        int scissorY = mc.displayHeight - (y + height) * scaleFactor;
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
    // ==================== PANEL DRAG / RESIZE ====================

    public boolean isMouseOverResizeHandle(int mx, int my) {
        if (!visible) return false;
        return mx >= x + width - 8 && mx <= x + width && my >= y + height - 8 && my <= y + height;
    }

    public void updatePanelDrag(int mouseX, int mouseY) {
        if (!isDraggingPanel) return;
        x = mouseX - panelDragOffsetX;
        y = mouseY - panelDragOffsetY;
        overriddenX = x;
        overriddenY = y;
        panelPositionOverridden = true;
    }

    public void releasePanelDrag() { isDraggingPanel = false; hasPendingPanelDrag = false; }

    public void updatePanelResize(int mouseX, int mouseY) {
        if (!isResizingPanel) return;
        overriddenW = Math.max(MIN_RESIZE_W, resizeInitW + (mouseX - resizeInitMouseX));
        overriddenH = Math.max(MIN_RESIZE_H, resizeInitH + (mouseY - resizeInitMouseY));
        panelSizeOverridden = true;
        menuWidth = overriddenW;
        width = overriddenW;
        height = overriddenH;
        visibleItemsCount = Math.max(1, (overriddenH - HINT_HEIGHT - PADDING * 2) / ITEM_HEIGHT);
    }

    public void releasePanelResize() { isResizingPanel = false; }

    /** Used by draw-loop scrollbar update in GuiScriptTextArea */
    public void updateScrollbarDragDraw(int mouseY) {
        if (!isDraggingScrollbar || items.isEmpty()) return;
        int visibleCount = getVisibleItemCapacity();
        if (visibleCount <= 0) return;
        int scrollbarHeight = visibleCount * ITEM_HEIGHT - 8;
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * (float) visibleCount / items.size()));
        int scrollTrackHeight = Math.max(1, scrollbarHeight - thumbHeight);
        int maxScroll = items.size() - visibleCount;
        int deltaY = mouseY - dragStartY;
        int scrollDelta = (deltaY * maxScroll) / scrollTrackHeight;
        scrollOffset = Math.max(0, Math.min(maxScroll, dragStartScroll + scrollDelta));
    }

    public void releaseScrollbarDrag() { isDraggingScrollbar = false; }

    public boolean isDraggingPanel()     { return isDraggingPanel; }
    public boolean isDraggingScrollbarMenu() { return isDraggingScrollbar; }
    public boolean isResizingPanel()     { return isResizingPanel; }

    private void drawResizeHandle(int mouseX, int mouseY) {
        boolean resizeActive = isResizingPanel || isMouseOverResizeHandle(mouseX, mouseY);
        int dotColor = resizeActive ? 0xFFCCCCCC : 0x80888888;
        int rhX = x + width - 2;
        int rhY = y + height - 2;
        // Three-dot diagonal pattern (◢)
        drawRect(rhX - 1, rhY - 1, rhX,     rhY,     dotColor);
        drawRect(rhX - 3, rhY - 1, rhX - 2, rhY,     dotColor);
        drawRect(rhX - 1, rhY - 3, rhX,     rhY - 2, dotColor);
        drawRect(rhX - 5, rhY - 1, rhX - 4, rhY,     dotColor);
        drawRect(rhX - 3, rhY - 3, rhX - 2, rhY - 2, dotColor);
        drawRect(rhX - 1, rhY - 5, rhX,     rhY - 4, dotColor);
    }

}
