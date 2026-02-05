package kamkeel.npcs.client.gui.modern;

import kamkeel.npcs.client.gui.components.*;
import kamkeel.npcs.client.gui.modern.tabs.*;
import noppes.npcs.client.gui.util.ModernColors;
import noppes.npcs.client.gui.util.IDialogEditorListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.controllers.data.Dialog;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete dialog editor panel v2 with 5 tabs.
 * Now refactored to use separate tab classes for better organization.
 *
 * Tabs:
 * 0 - Text (title, dialog text)
 * 1 - Options (6 option slots)
 * 2 - Feedback (sound, visual options)
 * 3 - Settings (quest, mail, command)
 * 4 - Availability (requirements)
 */
public class DialogEditorPanel extends Gui {

    // === Layout ===
    protected int x, y, width, height;
    protected int headerHeight = 22;
    protected int tabBarHeight = 20;
    protected int footerHeight = 24;
    protected int padding = 4;

    // === State ===
    protected Dialog dialog;
    protected boolean dirty = false;
    protected int currentTab = 0;
    protected float scrollY = 0;
    protected float targetScrollY = 0;
    protected int contentHeight = 0;

    // === Tab Names ===
    protected static final String[] TAB_NAMES = {"Text", "Options", "Feedback", "Settings", "Availability"};

    // === Tabs ===
    protected DialogTextTab textTab;
    protected DialogOptionsTab optionsTab;
    protected DialogFeedbackTab feedbackTab;
    protected DialogSettingsTab settingsTab;
    protected DialogAvailabilityTab availabilityTab;
    protected DialogEditorTab[] tabs;

    // === Tab hover ===
    protected int hoveredTab = -1;

    // === Listener ===
    protected IDialogEditorListener listener;

    // === Scrollbar ===
    protected int scrollbarWidth = 6;
    protected boolean isDraggingScrollbar = false;
    protected int dragStartY;
    protected float dragStartScroll;

    // === Footer ===
    protected boolean hoverTestBtn = false;
    protected boolean hoverSaveBtn = false;
    protected int saveBtnX, saveBtnY, saveBtnW, saveBtnH;

    public DialogEditorPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        initTabs();
    }

    // === Tab Initialization ===

    protected void initTabs() {
        textTab = new DialogTextTab(this);
        optionsTab = new DialogOptionsTab(this);
        feedbackTab = new DialogFeedbackTab(this);
        settingsTab = new DialogSettingsTab(this);
        availabilityTab = new DialogAvailabilityTab(this);

        tabs = new DialogEditorTab[] {
            textTab, optionsTab, feedbackTab, settingsTab, availabilityTab
        };
    }

    // === Data Loading ===

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
        this.dirty = false;
        this.scrollY = 0;
        this.targetScrollY = 0;

        for (DialogEditorTab tab : tabs) {
            tab.setDialog(dialog);
            tab.loadFromDialog(dialog);
        }
    }

    // === Data Saving ===

    public void saveToDialog() {
        if (dialog == null) return;

        for (DialogEditorTab tab : tabs) {
            tab.saveToDialog(dialog);
        }

        dirty = false;
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        if (dialog == null) {
            drawNoSelection();
            return;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Update scroll animation
        if (Math.abs(scrollY - targetScrollY) > 0.5f) {
            scrollY += (targetScrollY - scrollY) * 0.3f;
        } else {
            scrollY = targetScrollY;
        }

        // Background
        drawRect(x, y, x + width, y + height, ModernColors.PANEL_BG_SOLID);

        // Header
        drawHeader(mouseX, mouseY, fr);

        // Tab bar
        drawTabBar(mouseX, mouseY, fr);

        // Content area with scissor
        int contentY = y + headerHeight + tabBarHeight;
        int contentAreaH = height - headerHeight - tabBarHeight - footerHeight;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        // Update screen positions for all dropdowns BEFORE scroll translation
        updateDropdownScreenPositions();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (contentY + contentAreaH) * scale,
                      (width - scrollbarWidth) * scale, contentAreaH * scale);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, -scrollY, 0);

        // Draw current tab content
        int contentX = x + padding;
        int contentW = width - padding * 2 - scrollbarWidth;
        int startY = y + headerHeight + tabBarHeight + padding;
        contentHeight = tabs[currentTab].draw(contentX, contentW, startY, mouseX, (int)(mouseY + scrollY), fr);

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Clamp scroll position when content height changes
        int maxScroll = Math.max(0, contentHeight - contentAreaH);
        if (scrollY > maxScroll) {
            scrollY = maxScroll;
            targetScrollY = maxScroll;
        }

        // Draw scrollbar
        if (contentHeight > contentAreaH) {
            drawScrollbar(mouseX, mouseY, contentY, contentAreaH);
        }

        // Draw expanded dropdowns in SCREEN SPACE (no translation!)
        drawExpandedDropdownsScreenSpace(mouseX, mouseY);

        // Footer
        drawFooter(mouseX, mouseY, fr);
    }

    protected void drawNoSelection() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        drawRect(x, y, x + width, y + height, ModernColors.PANEL_BG_SOLID);
        String msg = "Select a dialog to edit";
        int msgW = fr.getStringWidth(msg);
        fr.drawString(msg, x + (width - msgW) / 2, y + height / 2, ModernColors.TEXT_DARK);
    }

    protected void drawHeader(int mouseX, int mouseY, FontRenderer fr) {
        drawRect(x, y, x + width, y + headerHeight, ModernColors.TOP_BAR_BG);

        String title = dialog.title;
        if (title.length() > 20) title = title.substring(0, 17) + "...";
        if (dirty) title = "* " + title;

        fr.drawString(title, x + padding, y + (headerHeight - fr.FONT_HEIGHT) / 2,
                     dirty ? ModernColors.DIRTY_INDICATOR : ModernColors.TEXT_WHITE);

        String idStr = "#" + dialog.id;
        int idW = fr.getStringWidth(idStr);
        fr.drawString(idStr, x + width - padding - idW, y + (headerHeight - fr.FONT_HEIGHT) / 2,
                     ModernColors.TEXT_GRAY);
    }

    protected void drawTabBar(int mouseX, int mouseY, FontRenderer fr) {
        int tabY = y + headerHeight;
        int tabW = width / TAB_NAMES.length;

        hoveredTab = -1;

        for (int i = 0; i < TAB_NAMES.length; i++) {
            int tabX = x + i * tabW;
            boolean active = i == currentTab;
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabW &&
                             mouseY >= tabY && mouseY < tabY + tabBarHeight;

            if (hovered) hoveredTab = i;

            int bg = active ? ModernColors.BUTTON_BG_PRESSED : (hovered ? ModernColors.BUTTON_BG_HOVER : ModernColors.BUTTON_BG);
            drawRect(tabX, tabY, tabX + tabW, tabY + tabBarHeight, bg);

            String text = TAB_NAMES[i];
            int textW = fr.getStringWidth(text);
            fr.drawString(text, tabX + (tabW - textW) / 2, tabY + (tabBarHeight - fr.FONT_HEIGHT) / 2,
                         active ? ModernColors.TEXT_WHITE : ModernColors.TEXT_LIGHT);

            if (active) {
                drawRect(tabX, tabY + tabBarHeight - 2, tabX + tabW, tabY + tabBarHeight, ModernColors.ACCENT_BLUE);
            }
        }
    }

    protected void drawScrollbar(int mouseX, int mouseY, int contentY, int contentAreaH) {
        int sbX = x + width - scrollbarWidth;
        drawRect(sbX, contentY, sbX + scrollbarWidth, contentY + contentAreaH, ModernColors.SCROLLBAR_BG);

        float viewRatio = (float) contentAreaH / contentHeight;
        int thumbH = Math.max(20, (int)(contentAreaH * viewRatio));
        int maxScroll = contentHeight - contentAreaH;
        int thumbY = maxScroll > 0 ? (int)((contentAreaH - thumbH) * (scrollY / maxScroll)) : 0;

        boolean hoverThumb = mouseX >= sbX && mouseX < sbX + scrollbarWidth &&
                            mouseY >= contentY + thumbY && mouseY < contentY + thumbY + thumbH;
        int thumbColor = (hoverThumb || isDraggingScrollbar) ? ModernColors.SCROLLBAR_THUMB_HOVER : ModernColors.SCROLLBAR_THUMB;
        drawRect(sbX + 1, contentY + thumbY, sbX + scrollbarWidth - 1, contentY + thumbY + thumbH, thumbColor);
    }

    protected void drawFooter(int mouseX, int mouseY, FontRenderer fr) {
        int footerY = y + height - footerHeight;
        drawRect(x, footerY, x + width, y + height, ModernColors.SECTION_HEADER_BG);

        // Test button on the right
        String testText = "Test";
        int testW = fr.getStringWidth(testText) + 8;
        int testH = footerHeight - 6;
        int testX = x + width - testW - 6;
        int testY = footerY + 3;

        hoverTestBtn = mouseX >= testX && mouseX < testX + testW &&
                       mouseY >= testY && mouseY < testY + testH;

        int testBg = hoverTestBtn ? ModernColors.BUTTON_BG_HOVER : ModernColors.BUTTON_BG;
        drawRect(testX, testY, testX + testW, testY + testH, testBg);
        drawRect(testX, testY, testX + testW, testY + 1, ModernColors.ACCENT_BLUE);
        fr.drawString(testText, testX + 4, testY + (testH - fr.FONT_HEIGHT) / 2 + 1, ModernColors.TEXT_LIGHT);

        // Save hint/button
        String saveText = dirty ? "Ctrl+S to Save" : "No changes";
        int saveW = fr.getStringWidth(saveText);
        int saveX = x + (width - testW - 10 - saveW) / 2;
        int saveY = footerY + (footerHeight - fr.FONT_HEIGHT) / 2;

        saveBtnX = saveX - 4;
        saveBtnY = saveY - 2;
        saveBtnW = saveW + 8;
        saveBtnH = fr.FONT_HEIGHT + 4;

        hoverSaveBtn = dirty && mouseX >= saveBtnX && mouseX < saveBtnX + saveBtnW &&
                       mouseY >= saveBtnY && mouseY < saveBtnY + saveBtnH;

        if (dirty && hoverSaveBtn) {
            drawRect(saveBtnX, saveBtnY, saveBtnX + saveBtnW, saveBtnY + saveBtnH, ModernColors.BUTTON_BG_HOVER);
        }

        fr.drawString(saveText, saveX, saveY, dirty ? (hoverSaveBtn ? ModernColors.ACCENT_GREEN : ModernColors.ACCENT_ORANGE) : ModernColors.TEXT_DARK);
    }

    // === Dropdown Handling ===

    protected void updateDropdownScreenPositions() {
        for (ModernDropdown dropdown : getAllDropdowns()) {
            int screenDropdownY = dropdown.getY() - (int)scrollY;
            dropdown.setScreenPosition(dropdown.getX(), screenDropdownY);
        }
    }

    protected void drawExpandedDropdownsScreenSpace(int mouseX, int mouseY) {
        for (ModernDropdown dropdown : getAllDropdowns()) {
            if (dropdown.isExpanded()) {
                dropdown.drawOverlayScreenSpace(mouseX, mouseY);
            }
        }
    }

    protected List<ModernDropdown> getAllDropdowns() {
        List<ModernDropdown> all = new ArrayList<>();
        for (DialogEditorTab tab : tabs) {
            all.addAll(tab.getDropdowns());
        }
        return all;
    }

    protected void closeAllDropdowns() {
        for (ModernDropdown dropdown : getAllDropdowns()) {
            dropdown.close();
        }
    }

    // === Interaction ===

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (dialog == null) return false;

        // Tab clicks
        if (hoveredTab >= 0 && mouseY >= y + headerHeight && mouseY < y + headerHeight + tabBarHeight) {
            currentTab = hoveredTab;
            scrollY = 0;
            targetScrollY = 0;
            closeAllDropdowns();
            return true;
        }

        // Footer clicks
        int footerY = y + height - footerHeight;
        if (mouseY >= footerY && mouseY < y + height) {
            if (hoverTestBtn && listener != null) {
                listener.onTestRequested();
                return true;
            }
            if (hoverSaveBtn && dirty && listener != null) {
                listener.onSaveRequested();
                return true;
            }
            return true;
        }

        // Scrollbar
        int contentY = y + headerHeight + tabBarHeight;
        int contentAreaH = height - headerHeight - tabBarHeight - footerHeight;
        int sbX = x + width - scrollbarWidth;
        if (contentHeight > contentAreaH && mouseX >= sbX && mouseX < sbX + scrollbarWidth) {
            isDraggingScrollbar = true;
            dragStartY = mouseY;
            dragStartScroll = scrollY;
            return true;
        }

        // CHECK EXPANDED DROPDOWNS FIRST - use SCREEN coordinates
        for (ModernDropdown dropdown : getAllDropdowns()) {
            if (dropdown.isExpanded()) {
                int prevIndex = dropdown.getSelectedIndex();
                if (dropdown.mouseClickedScreenSpace(mouseX, mouseY, button)) {
                    if (dropdown.getSelectedIndex() != prevIndex) {
                        markDirty();
                    }
                    return true;
                }
            }
        }

        // Adjust mouseY for scroll (for components inside scrollable content)
        int adjMouseY = (int)(mouseY + scrollY);

        // Delegate to current tab
        return tabs[currentTab].mouseClicked(mouseX, adjMouseY, button);
    }

    public boolean keyTyped(char c, int keyCode) {
        if (dialog == null) return false;
        return tabs[currentTab].keyTyped(c, keyCode);
    }

    public void mouseScrolled(int delta) {
        int contentAreaH = height - headerHeight - tabBarHeight - footerHeight;
        if (contentHeight <= contentAreaH) return;

        int maxScroll = contentHeight - contentAreaH;
        targetScrollY = Math.max(0, Math.min(targetScrollY - delta * 20, maxScroll));
    }

    public void mouseReleased(int mouseX, int mouseY) {
        isDraggingScrollbar = false;
        // Let text areas handle mouse release
        textTab.getTextArea().mouseReleased(mouseX, mouseY);
        settingsTab.getCommandArea().mouseReleased(mouseX, mouseY);
    }

    public void mouseDragged(int mouseX, int mouseY) {
        if (isDraggingScrollbar) {
            int contentY = y + headerHeight + tabBarHeight;
            int contentAreaH = height - headerHeight - tabBarHeight - footerHeight;
            int maxScroll = contentHeight - contentAreaH;

            float viewRatio = (float) contentAreaH / contentHeight;
            int thumbH = Math.max(20, (int)(contentAreaH * viewRatio));
            int trackH = contentAreaH - thumbH;

            if (trackH > 0) {
                int deltaY = mouseY - dragStartY;
                float scrollDelta = (deltaY / (float)trackH) * maxScroll;
                scrollY = Math.max(0, Math.min(dragStartScroll + scrollDelta, maxScroll));
                targetScrollY = scrollY;
            }
        }
        textTab.getTextArea().mouseDragged(mouseX, mouseY);
        settingsTab.getCommandArea().mouseDragged(mouseX, mouseY);
    }

    public void updateScreen() {
        for (DialogEditorTab tab : tabs) {
            tab.updateScreen();
        }
    }

    // === Option Management ===

    public void removeOption(int slot) {
        optionsTab.removeOption(slot);
    }

    // === Utility ===

    public void markDirty() { dirty = true; }
    public void clearDirty() { dirty = false; }
    public boolean isDirty() { return dirty; }
    public Dialog getDialog() { return dialog; }
    public IDialogEditorListener getListener() { return listener; }

    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setListener(IDialogEditorListener listener) {
        this.listener = listener;
    }

    /**
     * Get available content height for tabs to use for sizing components.
     */
    public int getAvailableContentHeight() {
        return height - headerHeight - tabBarHeight - footerHeight;
    }

    // === Selection Callbacks ===

    public void onQuestSelected(int slot, int questId, String questName) {
        if (slot == 0) {
            settingsTab.onQuestSelected(questId, questName);
        } else {
            availabilityTab.onQuestSelected(slot, questId, questName);
        }
    }

    public void onDialogSelected(int slot, int dialogId, String dialogName) {
        availabilityTab.onDialogSelected(slot, dialogId, dialogName);
    }

    public void onFactionSelected(int slot, int factionId, String factionName) {
        availabilityTab.onFactionSelected(slot, factionId, factionName);
    }

    public void onSoundSelected(String soundPath) {
        feedbackTab.onSoundSelected(soundPath);
    }

    public void onColorSelected(int slot, int color) {
        if (slot < 100) {
            feedbackTab.onColorSelected(slot, color);
        } else {
            // Option color (slot 100+)
            int optionSlot = slot - 100;
            for (OptionEditorV2 editor : optionsTab.getOptionEditors()) {
                if (editor.getSlot() == optionSlot) {
                    editor.getColorBtn().setColor(color);
                    markDirty();
                    return;
                }
            }
        }
    }

    public void onOptionDialogSelected(int optionSlot, int dialogId, String dialogName) {
        optionsTab.onOptionDialogSelected(optionSlot, dialogId, dialogName);
    }
}
