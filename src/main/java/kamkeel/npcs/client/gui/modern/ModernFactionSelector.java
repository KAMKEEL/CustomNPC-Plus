package kamkeel.npcs.client.gui.modern;

import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernTextField;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Modern styled faction selector SubGui.
 * Features single-column layout with search field.
 */
public class ModernFactionSelector extends ModernSubGuiInterface {

    // Selected value
    public Faction selectedFaction;

    // Data map
    private HashMap<String, Faction> factionData = new HashMap<>();

    // List for display
    private List<String> filteredFactions = new ArrayList<>();

    // Components
    private ModernTextField searchField;
    private ModernButton doneBtn;
    private ModernButton cancelBtn;

    // Scroll state
    private int scrollY = 0;
    private int selectedIndex = -1;

    // Layout
    private int listX, listY;
    private int listWidth, listHeight;
    private int rowHeight = 16;  // Slightly taller rows for faction color indicator

    // Search string
    private String searchText = "";

    // Button IDs
    private static final int ID_DONE = 100;
    private static final int ID_CANCEL = 101;

    // Double-click tracking
    private long lastClickTime = 0;
    private int lastClickedIndex = -1;

    public ModernFactionSelector(int factionId) {
        xSize = 280;
        ySize = 260;
        setHeaderTitle("Select Faction");

        // Load faction data
        loadData();

        // Find initial selection
        if (factionId >= 0) {
            selectedFaction = FactionController.getInstance().factions.get(factionId);
        }
    }

    private void loadData() {
        factionData.clear();
        for (Faction faction : FactionController.getInstance().factions.values()) {
            factionData.put(faction.name, faction);
        }
        filteredFactions = new ArrayList<>(factionData.keySet());
        Collections.sort(filteredFactions);
    }

    @Override
    public void initGui() {
        super.initGui();

        int contentY = getContentY() + 6;
        int contentH = getContentHeight() - 6;

        int pad = 8;
        listX = guiLeft + pad;
        listWidth = xSize - pad * 2;

        // Search field at top
        int searchH = 18;
        searchField = new ModernTextField(0, listX, contentY, listWidth, searchH);
        searchField.setPlaceholder("Filter factions...");
        searchField.setText(searchText);

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

        // Set initial selection index
        if (selectedFaction != null) {
            selectedIndex = filteredFactions.indexOf(selectedFaction.name);
        }
    }

    private void updateList() {
        if (searchText.isEmpty()) {
            filteredFactions = new ArrayList<>(factionData.keySet());
        } else {
            filteredFactions = new ArrayList<>();
            String searchLower = searchText.toLowerCase();
            for (String name : factionData.keySet()) {
                if (name.toLowerCase().contains(searchLower)) {
                    filteredFactions.add(name);
                }
            }
        }
        Collections.sort(filteredFactions);
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Draw search field
        searchField.draw(mouseX, mouseY);

        // Draw list background
        drawRect(listX, listY, listX + listWidth, listY + listHeight, ModernColors.INPUT_BG);

        // Draw faction list
        drawFactionList(mouseX, mouseY);

        // Draw selected faction info
        if (selectedFaction != null) {
            String info = "Selected: " + selectedFaction.name + " (ID: " + selectedFaction.id + ")";
            int infoY = listY + listHeight + 4;
            fontRendererObj.drawString(info, guiLeft + 8, infoY, ModernColors.TEXT_GRAY);
        }

        // Draw buttons
        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    private void drawFactionList(int mouseX, int mouseY) {
        // Set up scissor
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(listX * scale, mc.displayHeight - (listY + listHeight) * scale, listWidth * scale, listHeight * scale);

        int visibleRows = listHeight / rowHeight;
        int startIdx = scrollY / rowHeight;
        int endIdx = Math.min(startIdx + visibleRows + 2, filteredFactions.size());

        for (int i = startIdx; i < endIdx; i++) {
            int rowY = listY + i * rowHeight - scrollY;
            if (rowY + rowHeight < listY || rowY > listY + listHeight) continue;

            String factionName = filteredFactions.get(i);
            Faction faction = factionData.get(factionName);

            // Draw selection background
            if (i == selectedIndex) {
                drawRect(listX, rowY, listX + listWidth, rowY + rowHeight, ModernColors.SELECTION_BG);
            } else {
                // Hover highlight
                if (mouseX >= listX && mouseX < listX + listWidth && mouseY >= rowY && mouseY < rowY + rowHeight) {
                    drawRect(listX, rowY, listX + listWidth, rowY + rowHeight, ModernColors.HOVER_HIGHLIGHT);
                }
            }

            // Draw faction color indicator
            int colorBoxX = listX + 4;
            int colorBoxY = rowY + 2;
            int colorBoxSize = rowHeight - 4;
            if (faction != null) {
                drawRect(colorBoxX, colorBoxY, colorBoxX + colorBoxSize, colorBoxY + colorBoxSize, 0xFF000000 | faction.color);
            }

            // Draw text
            String displayText = fontRendererObj.trimStringToWidth(factionName, listWidth - colorBoxSize - 12);
            int textColor = i == selectedIndex ? ModernColors.TEXT_WHITE : ModernColors.TEXT_LIGHT;
            fontRendererObj.drawString(displayText, colorBoxX + colorBoxSize + 4, rowY + 3, textColor);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scrollbar if needed
        int totalHeight = filteredFactions.size() * rowHeight;
        if (totalHeight > listHeight) {
            int sbX = listX + listWidth - 4;
            float viewRatio = (float) listHeight / totalHeight;
            int thumbH = Math.max(10, (int) (listHeight * viewRatio));
            float maxScroll = totalHeight - listHeight;
            int thumbY = maxScroll > 0 ? (int) ((scrollY / maxScroll) * (listHeight - thumbH)) : 0;

            drawRect(sbX, listY, sbX + 4, listY + listHeight, ModernColors.SCROLLBAR_BG);
            drawRect(sbX, listY + thumbY, sbX + 4, listY + thumbY + thumbH, ModernColors.SCROLLBAR_THUMB);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Check search field click
        searchField.mouseClicked(mouseX, mouseY, button);

        // Check list click
        if (button == 0 && mouseX >= listX && mouseX < listX + listWidth &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + scrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredFactions.size()) {
                // Double-click detection
                long now = System.currentTimeMillis();
                if (clickedIdx == lastClickedIndex && now - lastClickTime < 400) {
                    // Double-click - confirm and close
                    confirm();
                    return;
                }
                lastClickedIndex = clickedIdx;
                lastClickTime = now;

                selectedIndex = clickedIdx;
                String factionName = filteredFactions.get(clickedIdx);
                selectedFaction = factionData.get(factionName);
            }
        }

        // Check button clicks
        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            confirm();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            selectedFaction = null;
            close();
            return;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int delta = org.lwjgl.input.Mouse.getEventDWheel();
        if (delta != 0) {
            int mouseX = org.lwjgl.input.Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - org.lwjgl.input.Mouse.getEventY() * height / mc.displayHeight - 1;

            // Check if mouse is over list
            if (mouseX >= listX && mouseX < listX + listWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int scrollAmount = delta > 0 ? -rowHeight * 2 : rowHeight * 2;
                int maxScroll = Math.max(0, filteredFactions.size() * rowHeight - listHeight);
                scrollY = Math.max(0, Math.min(scrollY + scrollAmount, maxScroll));
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Handle search field
        if (searchField.isFocused()) {
            String prev = searchField.getText();
            searchField.keyTyped(typedChar, keyCode);
            String newText = searchField.getText();
            if (!newText.equals(prev)) {
                searchText = newText;
                updateList();
                scrollY = 0;
                if (selectedFaction != null) {
                    selectedIndex = filteredFactions.indexOf(selectedFaction.name);
                }
            }
        }

        // Handle ESC
        super.keyTyped(typedChar, keyCode);
    }

    private void confirm() {
        close();
    }

    /**
     * Get the selected faction, or null if none selected.
     */
    public Faction getSelectedFaction() {
        return selectedFaction;
    }
}
