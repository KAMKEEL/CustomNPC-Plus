package kamkeel.npcs.client.gui.modern;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.ReflectionHelper;
import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernTextField;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.controllers.MusicController;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Modern styled sound selector SubGui.
 * Features two-column layout (domains | sounds), search fields, and preview button.
 */
public class ModernSoundSelector extends ModernSubGuiInterface {
`
    // Selected values
    private String selectedDomain;
    public ResourceLocation selectedResource;

    // Sound data
    private final HashMap<String, List<String>> domains = new HashMap<>();

    // Components
    private ModernTextField domainSearchField;
    private ModernTextField soundSearchField;
    private ModernButton playBtn;
    private ModernButton stopBtn;
    private ModernButton doneBtn;
    private ModernButton cancelBtn;

    // Lists
    private List<String> filteredDomains = new ArrayList<>();
    private List<String> filteredSounds = new ArrayList<>();

    // Scroll state
    private int domainScrollY = 0;
    private int soundScrollY = 0;
    private int selectedDomainIndex = -1;
    private int selectedSoundIndex = -1;

    // Layout
    private int listY;
    private int listHeight;
    private int domainListX, domainListW;
    private int soundListX, soundListW;
    private int rowHeight = 14;

    // Search strings
    private String domainSearch = "";
    private String soundSearch = "";

    // Button IDs
    private static final int ID_PLAY = 100;
    private static final int ID_STOP = 101;
    private static final int ID_DONE = 102;
    private static final int ID_CANCEL = 103;

    // --- Cache static fields ---
    private static final long CACHE_DURATION = 180000L; // 3 minutes
    private static long lastCacheTime = 0;
    private static HashMap<String, List<String>> cachedDomains = new HashMap<>();

    public ModernSoundSelector(String sound) {
        xSize = 400;
        ySize = 280;
        setHeaderTitle("Sound Selector");

        loadSounds();

        if (sound != null && !sound.isEmpty()) {
            selectedResource = new ResourceLocation(sound);
            selectedDomain = selectedResource.getResourceDomain();
            if (!domains.containsKey(selectedDomain)) {
                selectedDomain = null;
                selectedResource = null;
            }
        }
    }

    private void loadSounds() {
        long now = System.currentTimeMillis();
        if (now - lastCacheTime < CACHE_DURATION && !cachedDomains.isEmpty()) {
            domains.putAll(cachedDomains);
        } else {
            SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
            SoundRegistry registry = ReflectionHelper.getPrivateValue(SoundHandler.class, handler, 4);
            Set<ResourceLocation> set = registry.getKeys();
            for (ResourceLocation location : set) {
                List<String> list = domains.get(location.getResourceDomain());
                if (list == null) {
                    domains.put(location.getResourceDomain(), list = new ArrayList<String>());
                }
                list.add(location.getResourcePath());
            }
            cachedDomains.clear();
            cachedDomains.putAll(domains);
            lastCacheTime = now;
        }

        // Sort domain keys
        filteredDomains = new ArrayList<>(domains.keySet());
        Collections.sort(filteredDomains);
    }

    @Override
    public void initGui() {
        super.initGui();

        int contentY = getContentY() + 6;
        int contentH = getContentHeight() - 6;

        // Layout calculations
        int pad = 6;
        domainListX = guiLeft + pad;
        domainListW = 100;
        soundListX = domainListX + domainListW + pad;
        soundListW = xSize - domainListW - pad * 3;

        // Search fields at top
        int searchH = 18;
        domainSearchField = new ModernTextField(0, domainListX, contentY, domainListW, searchH);
        domainSearchField.setPlaceholder("Filter...");
        domainSearchField.setText(domainSearch);

        soundSearchField = new ModernTextField(1, soundListX, contentY, soundListW, searchH);
        soundSearchField.setPlaceholder("Filter sounds...");
        soundSearchField.setText(soundSearch);

        // List area below search
        listY = contentY + searchH + 4;
        listHeight = contentH - searchH - 36 - 8; // Leave room for buttons

        // Buttons at bottom
        int btnY = guiTop + ySize - 32;
        int btnWidth = 50;
        int btnGap = 6;

        playBtn = new ModernButton(ID_PLAY, guiLeft + pad, btnY, btnWidth, 20, "Play");
        playBtn.setBackgroundColor(ModernColors.ACCENT_GREEN);

        stopBtn = new ModernButton(ID_STOP, guiLeft + pad + btnWidth + btnGap, btnY, btnWidth, 20, "Stop");
        stopBtn.setBackgroundColor(ModernColors.ACCENT_RED);

        cancelBtn = new ModernButton(ID_CANCEL, guiLeft + xSize - pad - btnWidth * 2 - btnGap, btnY, btnWidth, 20, "Cancel");

        doneBtn = new ModernButton(ID_DONE, guiLeft + xSize - pad - btnWidth, btnY, btnWidth, 20, "Done");
        doneBtn.setBackgroundColor(ModernColors.ACCENT_BLUE);

        // Set initial selection
        if (selectedDomain != null) {
            selectedDomainIndex = filteredDomains.indexOf(selectedDomain);
            updateSoundList();
            if (selectedResource != null) {
                selectedSoundIndex = filteredSounds.indexOf(selectedResource.getResourcePath());
            }
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        playBtn.enabled = selectedResource != null;
    }

    private void updateSoundList() {
        if (selectedDomain == null || !domains.containsKey(selectedDomain)) {
            filteredSounds = new ArrayList<>();
            return;
        }

        List<String> allSounds = domains.get(selectedDomain);
        if (soundSearch.isEmpty()) {
            filteredSounds = new ArrayList<>(allSounds);
        } else {
            filteredSounds = new ArrayList<>();
            String searchLower = soundSearch.toLowerCase();
            for (String sound : allSounds) {
                if (sound.toLowerCase().contains(searchLower)) {
                    filteredSounds.add(sound);
                }
            }
        }
        Collections.sort(filteredSounds);
    }

    private void updateDomainList() {
        if (domainSearch.isEmpty()) {
            filteredDomains = new ArrayList<>(domains.keySet());
        } else {
            filteredDomains = new ArrayList<>();
            String searchLower = domainSearch.toLowerCase();
            for (String domain : domains.keySet()) {
                if (domain.toLowerCase().contains(searchLower)) {
                    filteredDomains.add(domain);
                }
            }
        }
        Collections.sort(filteredDomains);
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Draw search fields
        domainSearchField.draw(mouseX, mouseY);
        soundSearchField.draw(mouseX, mouseY);

        // Draw list backgrounds
        drawRect(domainListX, listY, domainListX + domainListW, listY + listHeight, ModernColors.INPUT_BG);
        drawRect(soundListX, listY, soundListX + soundListW, listY + listHeight, ModernColors.INPUT_BG);

        // Draw domain list
        drawList(domainListX, listY, domainListW, listHeight, filteredDomains, selectedDomainIndex,
                domainScrollY, mouseX, mouseY);

        // Draw sound list
        drawList(soundListX, listY, soundListW, listHeight, filteredSounds, selectedSoundIndex,
                soundScrollY, mouseX, mouseY);

        // Draw selected sound info
        if (selectedResource != null) {
            String info = selectedResource.toString();
            int infoY = listY + listHeight + 2;
            fontRendererObj.drawString(info, guiLeft + 6, infoY, ModernColors.TEXT_GRAY);
        }

        // Draw buttons
        playBtn.drawButton(mc, mouseX, mouseY);
        stopBtn.drawButton(mc, mouseX, mouseY);
        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    private void drawList(int x, int y, int w, int h, List<String> items, int selectedIdx,
                          int scrollY, int mouseX, int mouseY) {
        // Set up scissor
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (y + h) * scale, w * scale, h * scale);

        int visibleRows = h / rowHeight;
        int startIdx = scrollY / rowHeight;
        int endIdx = Math.min(startIdx + visibleRows + 1, items.size());

        for (int i = startIdx; i < endIdx; i++) {
            int rowY = y + i * rowHeight - scrollY;

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
            int thumbY = (int) ((scrollY / maxScroll) * (h - thumbH));

            drawRect(sbX, y, sbX + 4, y + h, ModernColors.SCROLLBAR_BG);
            drawRect(sbX, y + thumbY, sbX + 4, y + thumbY + thumbH, ModernColors.SCROLLBAR_THUMB);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        domainSearchField.updateCursorCounter();
        soundSearchField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Check search field clicks
        domainSearchField.mouseClicked(mouseX, mouseY, button);
        soundSearchField.mouseClicked(mouseX, mouseY, button);

        // Check domain list click
        if (button == 0 && mouseX >= domainListX && mouseX < domainListX + domainListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + domainScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredDomains.size()) {
                selectedDomainIndex = clickedIdx;
                selectedDomain = filteredDomains.get(clickedIdx);
                selectedResource = null;
                selectedSoundIndex = -1;
                soundScrollY = 0;
                updateSoundList();
                updateButtonStates();
            }
        }

        // Check sound list click
        if (button == 0 && mouseX >= soundListX && mouseX < soundListX + soundListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + soundScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredSounds.size()) {
                selectedSoundIndex = clickedIdx;
                String soundPath = filteredSounds.get(clickedIdx);
                selectedResource = new ResourceLocation(selectedDomain, soundPath);
                updateButtonStates();
            }
        }

        // Check button clicks
        if (playBtn.mousePressed(mc, mouseX, mouseY)) {
            playSelectedSound();
            return;
        }
        if (stopBtn.mousePressed(mc, mouseX, mouseY)) {
            stopSound();
            return;
        }
        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            close();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            selectedResource = null;
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

            int scrollAmount = delta > 0 ? -rowHeight * 2 : rowHeight * 2;

            // Check which list is hovered
            if (mouseX >= domainListX && mouseX < domainListX + domainListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredDomains.size() * rowHeight - listHeight);
                domainScrollY = Math.max(0, Math.min(domainScrollY + scrollAmount, maxScroll));
            }

            if (mouseX >= soundListX && mouseX < soundListX + soundListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredSounds.size() * rowHeight - listHeight);
                soundScrollY = Math.max(0, Math.min(soundScrollY + scrollAmount, maxScroll));
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Handle domain search field
        if (domainSearchField.isFocused()) {
            String prev = domainSearchField.getText();
            domainSearchField.keyTyped(typedChar, keyCode);
            String newText = domainSearchField.getText();
            if (!newText.equals(prev)) {
                domainSearch = newText;
                updateDomainList();
                domainScrollY = 0;
                // Update selection index
                if (selectedDomain != null) {
                    selectedDomainIndex = filteredDomains.indexOf(selectedDomain);
                }
            }
        }

        // Handle sound search field
        if (soundSearchField.isFocused()) {
            String prev = soundSearchField.getText();
            soundSearchField.keyTyped(typedChar, keyCode);
            String newText = soundSearchField.getText();
            if (!newText.equals(prev)) {
                soundSearch = newText;
                updateSoundList();
                soundScrollY = 0;
                // Update selection index
                if (selectedResource != null) {
                    selectedSoundIndex = filteredSounds.indexOf(selectedResource.getResourcePath());
                }
            }
        }

        // Handle ESC
        super.keyTyped(typedChar, keyCode);
    }

    private void playSelectedSound() {
        if (selectedResource == null) return;
        MusicController.Instance.stopMusic();
        MusicController.Instance.playSound(selectedResource.toString(),
                (float) player.posX, (float) player.posY, (float) player.posZ);
    }

    private void stopSound() {
        MusicController.Instance.stopAllSounds();
    }

    @Override
    public void close() {
        MusicController.Instance.stopAllSounds();
        super.close();
    }

    /**
     * Get the selected sound resource location, or null if none selected.
     */
    public ResourceLocation getSelectedSound() {
        return selectedResource;
    }

    /**
     * Get the selected sound as a string, or empty string if none selected.
     */
    public String getSelectedSoundString() {
        return selectedResource != null ? selectedResource.toString() : "";
    }
}
