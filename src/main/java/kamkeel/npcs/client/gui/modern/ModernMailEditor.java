package kamkeel.npcs.client.gui.modern;

import kamkeel.npcs.client.gui.components.ModernButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.Quest;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern styled mail editor SubGui.
 * Features subject, sender, multi-page message editor, quest link, and item display.
 */
public class ModernMailEditor extends ModernSubGuiInterface {

    private PlayerMail mail;

    // Components
    private GuiNpcTextField subjectField;
    private GuiNpcTextField senderField;
    private ModernButton questBtn;
    private ModernButton questClearBtn;
    private ModernButton prevPageBtn;
    private ModernButton nextPageBtn;
    private ModernButton doneBtn;
    private ModernButton cancelBtn;

    // Message editing
    private List<String> messageLines = new ArrayList<>();
    private int currentPage = 0;
    private int linesPerPage = 8;
    private int cursorLine = 0;
    private int cursorPos = 0;
    private boolean messageAreaFocused = false;

    // Layout
    private int messageAreaX, messageAreaY;
    private int messageAreaW, messageAreaH;
    private int lineHeight = 12;

    // Button IDs
    private static final int ID_QUEST = 100;
    private static final int ID_QUEST_CLEAR = 101;
    private static final int ID_PREV_PAGE = 102;
    private static final int ID_NEXT_PAGE = 103;
    private static final int ID_DONE = 104;
    private static final int ID_CANCEL = 105;

    // Cursor blink
    private int cursorCounter = 0;

    // Pending quest selection
    private boolean pendingQuestSelect = false;

    public ModernMailEditor(PlayerMail mail) {
        this.mail = mail;
        xSize = 340;
        ySize = 320;
        setHeaderTitle("Mail Setup");

        // Load message into lines
        loadMessageLines();
    }

    private void loadMessageLines() {
        messageLines.clear();
        String[] pages = mail.getPageText();
        for (String page : pages) {
            // Split each page into lines
            String[] lines = page.split("\n", -1);
            for (String line : lines) {
                messageLines.add(line);
            }
            // Add page break marker (empty string signals new page)
            if (messageLines.size() > 0) {
                messageLines.add(""); // Page separator
            }
        }
        // Ensure at least one line
        if (messageLines.isEmpty()) {
            messageLines.add("");
        }
    }

    private void saveMessageToMail() {
        // Convert lines back to pages
        List<String> pages = new ArrayList<>();
        StringBuilder currentPage = new StringBuilder();
        int linesInPage = 0;

        for (String line : messageLines) {
            if (linesInPage >= linesPerPage) {
                pages.add(currentPage.toString());
                currentPage = new StringBuilder();
                linesInPage = 0;
            }
            if (currentPage.length() > 0) {
                currentPage.append("\n");
            }
            currentPage.append(line);
            linesInPage++;
        }

        if (currentPage.length() > 0 || pages.isEmpty()) {
            pages.add(currentPage.toString());
        }

        mail.setPageText(pages.toArray(new String[0]));
    }

    @Override
    public void initGui() {
        super.initGui();

        int contentY = getContentY() + 8;
        int pad = 10;
        int fieldW = xSize - pad * 2 - 70;
        int labelW = 60;
        int rowH = 24;

        // Subject row
        int rowY = contentY;
        fontRendererObj.drawString("Subject:", guiLeft + pad, rowY + 5, ModernColors.TEXT_LIGHT);
        subjectField = new GuiNpcTextField(0, guiLeft + pad + labelW, rowY, fieldW, 18, mail.subject);
        subjectField.setMaxStringLength(64);
        rowY += rowH;

        // Sender row
        senderField = new GuiNpcTextField(1, guiLeft + pad + labelW, rowY, fieldW, 18, mail.sender);
        senderField.setMaxStringLength(64);
        rowY += rowH;

        // Quest row
        String questLabel = mail.questId > 0 ? mail.questTitle : "None";
        if (questLabel.isEmpty() && mail.questId > 0) {
            Quest q = QuestController.Instance != null ? QuestController.Instance.quests.get(mail.questId) : null;
            questLabel = q != null ? q.title : "Quest #" + mail.questId;
        }
        if (questLabel.length() > 25) {
            questLabel = questLabel.substring(0, 22) + "...";
        }
        questBtn = new ModernButton(ID_QUEST, guiLeft + pad + labelW, rowY, fieldW - 24, 18, questLabel);
        questClearBtn = new ModernButton(ID_QUEST_CLEAR, guiLeft + pad + labelW + fieldW - 20, rowY, 20, 18, "X");
        questClearBtn.setBackgroundColor(ModernColors.ACCENT_RED);
        rowY += rowH + 8;

        // Message area
        messageAreaX = guiLeft + pad;
        messageAreaY = rowY;
        messageAreaW = xSize - pad * 2;
        messageAreaH = linesPerPage * lineHeight + 8;

        // Page navigation (below message area)
        int pageNavY = messageAreaY + messageAreaH + 4;
        int totalPages = getTotalPages();
        prevPageBtn = new ModernButton(ID_PREV_PAGE, messageAreaX, pageNavY, 30, 16, "<");
        nextPageBtn = new ModernButton(ID_NEXT_PAGE, messageAreaX + messageAreaW - 30, pageNavY, 30, 16, ">");
        prevPageBtn.enabled = currentPage > 0;
        nextPageBtn.enabled = currentPage < totalPages - 1 || canAddPage();

        // Items display row (below page nav)
        // Items are visual-only in this SubGui, actual container is handled by parent
        int itemRowY = pageNavY + 24;

        // Done/Cancel buttons at bottom
        int btnY = guiTop + ySize - 32;
        int btnWidth = 60;
        int btnGap = 8;

        cancelBtn = new ModernButton(ID_CANCEL, guiLeft + xSize - pad - btnWidth * 2 - btnGap, btnY, btnWidth, 20, "Cancel");
        doneBtn = new ModernButton(ID_DONE, guiLeft + xSize - pad - btnWidth, btnY, btnWidth, 20, "Done");
        doneBtn.setBackgroundColor(ModernColors.ACCENT_BLUE);
    }

    private int getTotalPages() {
        return Math.max(1, (messageLines.size() + linesPerPage - 1) / linesPerPage);
    }

    private boolean canAddPage() {
        return getTotalPages() < 50; // Max 50 pages
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        int pad = 10;
        int labelW = 60;
        int contentY = getContentY() + 8;
        int rowH = 24;

        // Draw labels
        int rowY = contentY;
        fontRendererObj.drawString("Subject:", guiLeft + pad, rowY + 5, ModernColors.TEXT_LIGHT);
        rowY += rowH;
        fontRendererObj.drawString("Sender:", guiLeft + pad, rowY + 5, ModernColors.TEXT_LIGHT);
        rowY += rowH;
        fontRendererObj.drawString("Quest:", guiLeft + pad, rowY + 5, ModernColors.TEXT_LIGHT);

        // Draw text fields
        subjectField.draw(mouseX, mouseY);
        senderField.draw(mouseX, mouseY);

        // Draw quest button
        questBtn.drawButton(mc, mouseX, mouseY);
        questClearBtn.drawButton(mc, mouseX, mouseY);

        // Draw message area background
        Gui.drawRect(messageAreaX - 1, messageAreaY - 1,
                messageAreaX + messageAreaW + 1, messageAreaY + messageAreaH + 1,
                messageAreaFocused ? ModernColors.ACCENT_BLUE : ModernColors.PANEL_BORDER);
        Gui.drawRect(messageAreaX, messageAreaY,
                messageAreaX + messageAreaW, messageAreaY + messageAreaH,
                ModernColors.INPUT_BG);

        // Draw message lines for current page
        int startLine = currentPage * linesPerPage;
        int endLine = Math.min(startLine + linesPerPage, messageLines.size());

        for (int i = startLine; i < endLine; i++) {
            int lineIdx = i - startLine;
            int lineY = messageAreaY + 4 + lineIdx * lineHeight;
            String line = messageLines.get(i);

            // Draw line text
            String displayLine = fontRendererObj.trimStringToWidth(line, messageAreaW - 8);
            fontRendererObj.drawString(displayLine, messageAreaX + 4, lineY, ModernColors.TEXT_LIGHT);

            // Draw cursor if focused and on this line
            if (messageAreaFocused && i == cursorLine && (cursorCounter / 6) % 2 == 0) {
                String beforeCursor = line.substring(0, Math.min(cursorPos, line.length()));
                int cursorX = messageAreaX + 4 + fontRendererObj.getStringWidth(beforeCursor);
                Gui.drawRect(cursorX, lineY - 1, cursorX + 1, lineY + lineHeight - 2, ModernColors.TEXT_WHITE);
            }
        }

        // Draw page navigation
        prevPageBtn.drawButton(mc, mouseX, mouseY);
        nextPageBtn.drawButton(mc, mouseX, mouseY);

        // Draw page indicator
        String pageText = "Page " + (currentPage + 1) + " / " + getTotalPages();
        int pageTextW = fontRendererObj.getStringWidth(pageText);
        int pageNavY = messageAreaY + messageAreaH + 4;
        fontRendererObj.drawString(pageText, messageAreaX + (messageAreaW - pageTextW) / 2,
                pageNavY + 4, ModernColors.TEXT_GRAY);

        // Draw items label and slots
        int itemRowY = pageNavY + 24;
        fontRendererObj.drawString("Items:", guiLeft + pad, itemRowY + 5, ModernColors.TEXT_LIGHT);

        int itemSlotX = guiLeft + pad + 50;
        int slotSize = 18;
        for (int i = 0; i < 4; i++) {
            int slotX = itemSlotX + i * (slotSize + 2);
            // Draw slot background
            Gui.drawRect(slotX, itemRowY, slotX + slotSize, itemRowY + slotSize, ModernColors.INPUT_BG);
            Gui.drawRect(slotX + 1, itemRowY + 1, slotX + slotSize - 1, itemRowY + slotSize - 1, ModernColors.PANEL_BG_SOLID);

            // Draw item if present
            if (mail.items != null && i < mail.items.length && mail.items[i] != null) {
                ItemStack stack = mail.items[i];
                // Draw a placeholder indicator since we can't render items easily in SubGui
                fontRendererObj.drawString("?", slotX + 5, itemRowY + 4, ModernColors.TEXT_GRAY);
            }
        }

        // Draw buttons
        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        subjectField.updateCursorCounter();
        senderField.updateCursorCounter();
        cursorCounter++;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Check text field clicks
        boolean fieldClicked = false;
        if (subjectField.handleClick(mouseX, mouseY, button)) {
            fieldClicked = true;
            messageAreaFocused = false;
        }
        if (senderField.handleClick(mouseX, mouseY, button)) {
            fieldClicked = true;
            messageAreaFocused = false;
        }

        // Check message area click
        if (button == 0 && mouseX >= messageAreaX && mouseX < messageAreaX + messageAreaW &&
            mouseY >= messageAreaY && mouseY < messageAreaY + messageAreaH) {
            messageAreaFocused = true;
            subjectField.setFocused(false);
            senderField.setFocused(false);

            // Calculate clicked line and position
            int clickedLineIdx = (mouseY - messageAreaY - 4) / lineHeight;
            int absoluteLine = currentPage * linesPerPage + clickedLineIdx;
            if (absoluteLine >= 0 && absoluteLine < messageLines.size()) {
                cursorLine = absoluteLine;
                String line = messageLines.get(cursorLine);
                // Find cursor position based on click X
                int clickX = mouseX - messageAreaX - 4;
                cursorPos = 0;
                for (int i = 0; i <= line.length(); i++) {
                    int textW = fontRendererObj.getStringWidth(line.substring(0, i));
                    if (textW >= clickX) {
                        cursorPos = i;
                        break;
                    }
                    cursorPos = i;
                }
            }
            return;
        } else if (!fieldClicked) {
            messageAreaFocused = false;
        }

        // Check button clicks
        if (questBtn.mousePressed(mc, mouseX, mouseY)) {
            pendingQuestSelect = true;
            // Save current state and close, parent will handle quest selection
            saveCurrentState();
            close();
            return;
        }
        if (questClearBtn.mousePressed(mc, mouseX, mouseY)) {
            mail.questId = -1;
            mail.questTitle = "";
            initGui();
            return;
        }
        if (prevPageBtn.mousePressed(mc, mouseX, mouseY)) {
            if (currentPage > 0) {
                currentPage--;
                initGui();
            }
            return;
        }
        if (nextPageBtn.mousePressed(mc, mouseX, mouseY)) {
            if (currentPage < getTotalPages() - 1) {
                currentPage++;
            } else if (canAddPage()) {
                // Add new page
                for (int i = 0; i < linesPerPage; i++) {
                    messageLines.add("");
                }
                currentPage++;
            }
            initGui();
            return;
        }
        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            saveCurrentState();
            close();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            // Don't save changes
            close();
            return;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Handle text fields
        if (subjectField.isFocused()) {
            subjectField.keyTyped(typedChar, keyCode);
            mail.subject = subjectField.getText();
            return;
        }
        if (senderField.isFocused()) {
            senderField.keyTyped(typedChar, keyCode);
            mail.sender = senderField.getText();
            return;
        }

        // Handle message area
        if (messageAreaFocused) {
            handleMessageInput(typedChar, keyCode);
            return;
        }

        // Handle ESC
        super.keyTyped(typedChar, keyCode);
    }

    private void handleMessageInput(char typedChar, int keyCode) {
        String currentLine = messageLines.get(cursorLine);

        // Backspace
        if (keyCode == 14) {
            if (cursorPos > 0) {
                String newLine = currentLine.substring(0, cursorPos - 1) + currentLine.substring(cursorPos);
                messageLines.set(cursorLine, newLine);
                cursorPos--;
            } else if (cursorLine > 0) {
                // Merge with previous line
                String prevLine = messageLines.get(cursorLine - 1);
                messageLines.set(cursorLine - 1, prevLine + currentLine);
                messageLines.remove(cursorLine);
                cursorLine--;
                cursorPos = prevLine.length();
                updatePageAfterEdit();
            }
            return;
        }

        // Delete
        if (keyCode == 211) {
            if (cursorPos < currentLine.length()) {
                String newLine = currentLine.substring(0, cursorPos) + currentLine.substring(cursorPos + 1);
                messageLines.set(cursorLine, newLine);
            } else if (cursorLine < messageLines.size() - 1) {
                // Merge with next line
                String nextLine = messageLines.get(cursorLine + 1);
                messageLines.set(cursorLine, currentLine + nextLine);
                messageLines.remove(cursorLine + 1);
            }
            return;
        }

        // Enter
        if (keyCode == 28) {
            String before = currentLine.substring(0, cursorPos);
            String after = currentLine.substring(cursorPos);
            messageLines.set(cursorLine, before);
            messageLines.add(cursorLine + 1, after);
            cursorLine++;
            cursorPos = 0;
            updatePageAfterEdit();
            return;
        }

        // Arrow keys
        if (keyCode == 203) { // Left
            if (cursorPos > 0) {
                cursorPos--;
            } else if (cursorLine > 0) {
                cursorLine--;
                cursorPos = messageLines.get(cursorLine).length();
                updatePageAfterEdit();
            }
            return;
        }
        if (keyCode == 205) { // Right
            if (cursorPos < currentLine.length()) {
                cursorPos++;
            } else if (cursorLine < messageLines.size() - 1) {
                cursorLine++;
                cursorPos = 0;
                updatePageAfterEdit();
            }
            return;
        }
        if (keyCode == 200) { // Up
            if (cursorLine > 0) {
                cursorLine--;
                cursorPos = Math.min(cursorPos, messageLines.get(cursorLine).length());
                updatePageAfterEdit();
            }
            return;
        }
        if (keyCode == 208) { // Down
            if (cursorLine < messageLines.size() - 1) {
                cursorLine++;
                cursorPos = Math.min(cursorPos, messageLines.get(cursorLine).length());
                updatePageAfterEdit();
            }
            return;
        }

        // Home/End
        if (keyCode == 199) { // Home
            cursorPos = 0;
            return;
        }
        if (keyCode == 207) { // End
            cursorPos = currentLine.length();
            return;
        }

        // Regular character input
        if (typedChar >= 32 && typedChar < 127) {
            // Check line length limit
            if (currentLine.length() < 200) {
                String newLine = currentLine.substring(0, cursorPos) + typedChar + currentLine.substring(cursorPos);
                messageLines.set(cursorLine, newLine);
                cursorPos++;
            }
        }
    }

    private void updatePageAfterEdit() {
        // Ensure cursor is on visible page
        int cursorPage = cursorLine / linesPerPage;
        if (cursorPage != currentPage) {
            currentPage = cursorPage;
            initGui();
        }
    }

    private void saveCurrentState() {
        mail.subject = subjectField.getText();
        mail.sender = senderField.getText();
        saveMessageToMail();
    }

    /**
     * Check if quest selection was requested.
     */
    public boolean isPendingQuestSelect() {
        return pendingQuestSelect;
    }

    /**
     * Set the selected quest after returning from quest selector.
     */
    public void setQuest(int questId, String questTitle) {
        mail.questId = questId;
        mail.questTitle = questTitle;
        pendingQuestSelect = false;
    }

    /**
     * Get the mail being edited.
     */
    public PlayerMail getMail() {
        return mail;
    }
}
