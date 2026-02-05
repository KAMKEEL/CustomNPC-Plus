package kamkeel.npcs.client.gui.modern;

import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernTextField;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.animation.AnimationsGetPacket;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumScrollData;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Modern styled animation selector.
 * Two-column layout: Built-in animations | Custom animations
 * Features search fields, double-click selection, and draggable divider.
 */
public class ModernAnimationSelector extends ModernSubGuiInterface implements IScrollData {

    // Selected values to return
    public int selectedAnimationId = -1;
    public String selectedBuiltInName = "";

    // Data maps
    private HashMap<String, Integer> builtInData = new HashMap<>();
    private HashMap<String, Integer> customData = new HashMap<>();

    // Filtered lists for display
    private List<String> filteredBuiltIn = new ArrayList<>();
    private List<String> filteredCustom = new ArrayList<>();

    // Components
    private ModernTextField builtInSearchField;
    private ModernTextField customSearchField;
    private ModernButton doneBtn;
    private ModernButton cancelBtn;
    private ModernButton clearBtn;

    // Scroll state
    private int builtInScrollY = 0;
    private int customScrollY = 0;
    private int selectedBuiltInIndex = -1;
    private int selectedCustomIndex = -1;

    // Layout
    private int listY;
    private int listHeight;
    private int builtInListX, builtInListW;
    private int customListX, customListW;
    private int rowHeight = 14;

    // Draggable divider
    private int dividerOffset;
    private boolean isDraggingDivider = false;
    private int dividerDragStartX;
    private int dividerWidth = 5;
    private int minColumnWidth = 80;

    // Search strings
    private String builtInSearch = "";
    private String customSearch = "";

    // Initial values
    private int initialAnimationId;
    private String initialAnimationName;

    // Double-click tracking
    private long lastClickTime = 0;
    private int lastClickedIndex = -1;
    private boolean lastClickWasBuiltIn = false;

    // Button IDs
    private static final int ID_DONE = 100;
    private static final int ID_CANCEL = 101;
    private static final int ID_CLEAR = 102;

    /**
     * Constructor for selecting by ID (user animations).
     */
    public ModernAnimationSelector(int animationId) {
        this(animationId, "");
    }

    /**
     * Constructor for selecting by name (built-in) or ID (user).
     */
    public ModernAnimationSelector(int animationId, String animationName) {
        xSize = 420;
        ySize = 280;
        setHeaderTitle("Select Animation");

        this.initialAnimationId = animationId;
        this.initialAnimationName = animationName != null ? animationName : "";
        this.selectedAnimationId = animationId;
        this.selectedBuiltInName = this.initialAnimationName;

        // Request animation data from server
        PacketClient.sendClient(new AnimationsGetPacket());
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.BUILTIN_ANIMATIONS) {
            this.builtInData = data;

            // Check if initial name matches a built-in animation
            if (!initialAnimationName.isEmpty()) {
                for (String name : builtInData.keySet()) {
                    if (name.equalsIgnoreCase(initialAnimationName)) {
                        selectedBuiltInName = name;
                        selectedAnimationId = -1;
                        break;
                    }
                }
            }
            updateBuiltInList();
            if (!selectedBuiltInName.isEmpty()) {
                selectedBuiltInIndex = filteredBuiltIn.indexOf(selectedBuiltInName);
            }
        } else if (type == EnumScrollData.ANIMATIONS) {
            this.customData = data;

            // Find the name for our initial animation ID
            if (selectedBuiltInName.isEmpty() && initialAnimationId >= 0) {
                for (String name : data.keySet()) {
                    if (data.get(name) == initialAnimationId) {
                        updateCustomList();
                        selectedCustomIndex = filteredCustom.indexOf(name);
                        break;
                    }
                }
            }
            updateCustomList();
        }
    }

    @Override
    public void setSelected(String selected) {
        // Not used
    }

    @Override
    public void initGui() {
        super.initGui();

        int contentY = getContentY() + 6;
        int contentH = getContentHeight() - 6;

        // Initialize divider offset if not set
        if (dividerOffset == 0) {
            dividerOffset = (xSize - 24) / 2;
        }

        // Layout calculations
        int pad = 8;
        builtInListX = guiLeft + pad;
        builtInListW = dividerOffset - pad;
        customListX = guiLeft + dividerOffset + dividerWidth;
        customListW = xSize - dividerOffset - dividerWidth - pad;

        // Enforce minimum widths
        if (builtInListW < minColumnWidth) {
            builtInListW = minColumnWidth;
            dividerOffset = builtInListW + pad;
            customListX = guiLeft + dividerOffset + dividerWidth;
            customListW = xSize - dividerOffset - dividerWidth - pad;
        }
        if (customListW < minColumnWidth) {
            customListW = minColumnWidth;
            dividerOffset = xSize - pad - dividerWidth - customListW;
            builtInListW = dividerOffset - pad;
            customListX = guiLeft + dividerOffset + dividerWidth;
        }

        // Search fields at top
        int searchH = 18;
        builtInSearchField = new ModernTextField(0, builtInListX, contentY, builtInListW, searchH);
        builtInSearchField.setPlaceholder("Filter built-in...");
        builtInSearchField.setText(builtInSearch);

        customSearchField = new ModernTextField(1, customListX, contentY, customListW, searchH);
        customSearchField.setPlaceholder("Filter custom...");
        customSearchField.setText(customSearch);

        // List area below search
        listY = contentY + searchH + 4;
        listHeight = contentH - searchH - 36 - 8;

        // Buttons at bottom
        int btnY = guiTop + ySize - 32;
        int btnWidth = 60;
        int btnGap = 8;

        clearBtn = new ModernButton(ID_CLEAR, guiLeft + pad, btnY, btnWidth, 20, "Clear");
        cancelBtn = new ModernButton(ID_CANCEL, guiLeft + xSize - pad - btnWidth * 2 - btnGap, btnY, btnWidth, 20, "Cancel");
        doneBtn = new ModernButton(ID_DONE, guiLeft + xSize - pad - btnWidth, btnY, btnWidth, 20, "Done");
        doneBtn.setBackgroundColor(ModernColors.ACCENT_BLUE);

        // Update lists
        updateBuiltInList();
        updateCustomList();

        // Set initial selection indices
        if (!selectedBuiltInName.isEmpty()) {
            selectedBuiltInIndex = filteredBuiltIn.indexOf(selectedBuiltInName);
        }
        if (selectedAnimationId >= 0 && selectedBuiltInName.isEmpty()) {
            for (String name : customData.keySet()) {
                if (customData.get(name) == selectedAnimationId) {
                    selectedCustomIndex = filteredCustom.indexOf(name);
                    break;
                }
            }
        }
    }

    private void updateBuiltInList() {
        if (builtInSearch.isEmpty()) {
            filteredBuiltIn = new ArrayList<>(builtInData.keySet());
        } else {
            filteredBuiltIn = new ArrayList<>();
            String searchLower = builtInSearch.toLowerCase();
            for (String name : builtInData.keySet()) {
                if (name.toLowerCase().contains(searchLower)) {
                    filteredBuiltIn.add(name);
                }
            }
        }
        Collections.sort(filteredBuiltIn);
    }

    private void updateCustomList() {
        if (customSearch.isEmpty()) {
            filteredCustom = new ArrayList<>(customData.keySet());
        } else {
            filteredCustom = new ArrayList<>();
            String searchLower = customSearch.toLowerCase();
            for (String name : customData.keySet()) {
                if (name.toLowerCase().contains(searchLower)) {
                    filteredCustom.add(name);
                }
            }
        }
        Collections.sort(filteredCustom);
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Draw column headers
        int headerY = getContentY() - 2;
        fontRendererObj.drawString("Built-in Animations", builtInListX, headerY - 10, ModernColors.TEXT_LIGHT);
        fontRendererObj.drawString("Custom Animations", customListX, headerY - 10, ModernColors.TEXT_LIGHT);

        // Draw search fields
        builtInSearchField.draw(mouseX, mouseY);
        customSearchField.draw(mouseX, mouseY);

        // Draw list backgrounds
        drawRect(builtInListX, listY, builtInListX + builtInListW, listY + listHeight, ModernColors.INPUT_BG);
        drawRect(customListX, listY, customListX + customListW, listY + listHeight, ModernColors.INPUT_BG);

        // Draw divider handle
        int divX = guiLeft + dividerOffset;
        int handleTop = listY + (listHeight - 20) / 2;
        int handleColor = isDraggingDivider ? ModernColors.ACCENT_BLUE : 0xFF707070;
        drawRect(divX + 1, handleTop, divX + dividerWidth - 1, handleTop + 20, handleColor);

        // Draw built-in list
        drawList(builtInListX, listY, builtInListW, listHeight, filteredBuiltIn, selectedBuiltInIndex,
                builtInScrollY, mouseX, mouseY, true);

        // Draw custom list
        drawList(customListX, listY, customListW, listHeight, filteredCustom, selectedCustomIndex,
                customScrollY, mouseX, mouseY, false);

        // Draw selected animation info
        String info = "";
        if (!selectedBuiltInName.isEmpty()) {
            info = "Selected: " + selectedBuiltInName + " (Built-in)";
        } else if (selectedAnimationId >= 0) {
            String name = getCustomNameForId(selectedAnimationId);
            info = "Selected: " + (name != null ? name : "Unknown") + " (ID: " + selectedAnimationId + ")";
        }
        if (!info.isEmpty()) {
            int infoY = listY + listHeight + 4;
            fontRendererObj.drawString(info, guiLeft + 8, infoY, ModernColors.TEXT_GRAY);
        }

        // Draw buttons
        clearBtn.drawButton(mc, mouseX, mouseY);
        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    private String getCustomNameForId(int id) {
        for (Map.Entry<String, Integer> entry : customData.entrySet()) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void drawList(int x, int y, int w, int h, List<String> items, int selectedIdx,
                          int scrollY, int mouseX, int mouseY, boolean isBuiltIn) {
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

            if (i == selectedIdx) {
                drawRect(x, rowY, x + w, rowY + rowHeight, ModernColors.SELECTION_BG);
            } else {
                if (mouseX >= x && mouseX < x + w && mouseY >= rowY && mouseY < rowY + rowHeight) {
                    drawRect(x, rowY, x + w, rowY + rowHeight, ModernColors.HOVER_HIGHLIGHT);
                }
            }

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

    @Override
    public void updateScreen() {
        super.updateScreen();
        builtInSearchField.updateCursorCounter();
        customSearchField.updateCursorCounter();
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
        builtInSearchField.mouseClicked(mouseX, mouseY, button);
        customSearchField.mouseClicked(mouseX, mouseY, button);

        // Check built-in list click
        if (button == 0 && mouseX >= builtInListX && mouseX < builtInListX + builtInListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + builtInScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredBuiltIn.size()) {
                // Double-click detection
                long now = System.currentTimeMillis();
                if (clickedIdx == lastClickedIndex && lastClickWasBuiltIn && now - lastClickTime < 400) {
                    confirm();
                    return;
                }
                lastClickedIndex = clickedIdx;
                lastClickTime = now;
                lastClickWasBuiltIn = true;

                selectedBuiltInIndex = clickedIdx;
                selectedBuiltInName = filteredBuiltIn.get(clickedIdx);
                selectedAnimationId = -1;
                selectedCustomIndex = -1;
            }
        }

        // Check custom list click
        if (button == 0 && mouseX >= customListX && mouseX < customListX + customListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + customScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredCustom.size()) {
                // Double-click detection
                long now = System.currentTimeMillis();
                if (clickedIdx == lastClickedIndex && !lastClickWasBuiltIn && now - lastClickTime < 400) {
                    confirm();
                    return;
                }
                lastClickedIndex = clickedIdx;
                lastClickTime = now;
                lastClickWasBuiltIn = false;

                selectedCustomIndex = clickedIdx;
                String name = filteredCustom.get(clickedIdx);
                selectedAnimationId = customData.get(name);
                selectedBuiltInName = "";
                selectedBuiltInIndex = -1;
            }
        }

        // Check button clicks
        if (clearBtn.mousePressed(mc, mouseX, mouseY)) {
            selectedBuiltInName = "";
            selectedAnimationId = -1;
            selectedBuiltInIndex = -1;
            selectedCustomIndex = -1;
            close();
            return;
        }
        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            confirm();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            selectedAnimationId = initialAnimationId;
            selectedBuiltInName = initialAnimationName;
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

            int pad = 8;
            int minOffset = pad + minColumnWidth;
            int maxOffset = xSize - pad - dividerWidth - minColumnWidth;
            dividerOffset = Math.max(minOffset, Math.min(dividerOffset, maxOffset));

            builtInListW = dividerOffset - pad;
            customListX = guiLeft + dividerOffset + dividerWidth;
            customListW = xSize - dividerOffset - dividerWidth - pad;

            builtInSearchField.setBounds(builtInListX, builtInSearchField.getY(), builtInListW, 18);
            customSearchField.setBounds(customListX, customSearchField.getY(), customListW, 18);
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

            if (mouseX >= builtInListX && mouseX < builtInListX + builtInListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredBuiltIn.size() * rowHeight - listHeight);
                builtInScrollY = Math.max(0, Math.min(builtInScrollY + scrollAmount, maxScroll));
            }

            if (mouseX >= customListX && mouseX < customListX + customListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredCustom.size() * rowHeight - listHeight);
                customScrollY = Math.max(0, Math.min(customScrollY + scrollAmount, maxScroll));
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (builtInSearchField.isFocused()) {
            String prev = builtInSearchField.getText();
            builtInSearchField.keyTyped(typedChar, keyCode);
            String newText = builtInSearchField.getText();
            if (!newText.equals(prev)) {
                builtInSearch = newText;
                updateBuiltInList();
                builtInScrollY = 0;
                if (!selectedBuiltInName.isEmpty()) {
                    selectedBuiltInIndex = filteredBuiltIn.indexOf(selectedBuiltInName);
                }
            }
        }

        if (customSearchField.isFocused()) {
            String prev = customSearchField.getText();
            customSearchField.keyTyped(typedChar, keyCode);
            String newText = customSearchField.getText();
            if (!newText.equals(prev)) {
                customSearch = newText;
                updateCustomList();
                customScrollY = 0;
                String customName = getCustomNameForId(selectedAnimationId);
                if (customName != null) {
                    selectedCustomIndex = filteredCustom.indexOf(customName);
                }
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    private void confirm() {
        close();
    }

    /**
     * Get the name of the selected animation (for display purposes).
     */
    public String getSelectedName() {
        if (!selectedBuiltInName.isEmpty()) {
            return selectedBuiltInName;
        }
        return getCustomNameForId(selectedAnimationId);
    }

    /**
     * Check if a built-in animation was selected.
     */
    public boolean isBuiltInSelected() {
        return !selectedBuiltInName.isEmpty();
    }
}
