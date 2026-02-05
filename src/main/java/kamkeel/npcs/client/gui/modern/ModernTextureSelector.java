package kamkeel.npcs.client.gui.modern;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernTextField;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Modern styled texture selector.
 * Features domain/folder navigation on left, texture list on right,
 * with search fields and NPC preview.
 */
public class ModernTextureSelector extends ModernSubGuiInterface {

    private final String UP_TEXT = "..<" + StatCollector.translateToLocal("gui.up") + ">..";

    // Selection state
    private String location = "";
    private String selectedDomain;
    public ResourceLocation selectedResource;
    public boolean setNPCSkin = true;

    // Data
    private final HashMap<String, List<TextureData>> domains = new HashMap<>();
    private final HashMap<String, TextureData> textures = new HashMap<>();

    // Filtered lists
    private List<String> filteredFolders = new ArrayList<>();
    private List<String> filteredTextures = new ArrayList<>();

    // Components
    private ModernTextField folderSearchField;
    private ModernTextField textureSearchField;
    private ModernButton doneBtn;
    private ModernButton cancelBtn;

    // Layout
    private int listY;
    private int listHeight;
    private int folderListX, folderListW;
    private int textureListX, textureListW;
    private int rowHeight = 14;

    // Scroll state
    private int folderScrollY = 0;
    private int textureScrollY = 0;
    private int selectedFolderIndex = -1;
    private int selectedTextureIndex = -1;

    // Draggable divider
    private int dividerOffset;
    private boolean isDraggingDivider = false;
    private int dividerDragStartX;
    private int dividerWidth = 5;
    private int minColumnWidth = 100;

    // Search strings
    private String folderSearch = "";
    private String textureSearch = "";

    // NPC reference for preview
    private EntityNPCInterface npc;

    // Double-click tracking
    private long lastClickTime = 0;
    private int lastClickedIndex = -1;
    private boolean lastClickWasFolder = false;

    // Cache static fields
    private static final long CACHE_DURATION = 180000L; // 3 minutes
    private static long lastCacheTime = 0;
    private static HashMap<String, List<TextureData>> cachedDomains = new HashMap<>();
    public static HashMap<String, TextureData> cachedTextures = new HashMap<>();

    // Button IDs
    private static final int ID_DONE = 100;
    private static final int ID_CANCEL = 101;

    public ModernTextureSelector(EntityNPCInterface npc, String texture) {
        this.npc = npc;
        xSize = 420;
        ySize = 300;
        setHeaderTitle("Select Texture");

        loadTextureData();

        if (texture != null && !texture.isEmpty()) {
            selectedResource = new ResourceLocation(texture);
            selectedDomain = selectedResource.getResourceDomain();
            if (!domains.containsKey(selectedDomain)) {
                selectedDomain = null;
            }
            int i = selectedResource.getResourcePath().lastIndexOf('/');
            location = selectedResource.getResourcePath().substring(0, i + 1);
        }
    }

    private void loadTextureData() {
        long now = System.currentTimeMillis();
        if (cachedDomains != null && now - lastCacheTime < CACHE_DURATION) {
            domains.putAll(cachedDomains);
            textures.putAll(cachedTextures);
        } else {
            buildTextureData();
            cachedDomains = new HashMap<>(domains);
            cachedTextures = new HashMap<>(textures);
            lastCacheTime = now;
        }
    }

    private void buildTextureData() {
        SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        Map<String, FallbackResourceManager> map = ObfuscationReflectionHelper.getPrivateValue(SimpleReloadableResourceManager.class, simplemanager, 2);
        HashSet<String> set = new HashSet<>();

        for (String name : map.keySet()) {
            if (!(map.get(name) instanceof FallbackResourceManager)) continue;
            FallbackResourceManager manager = (FallbackResourceManager) map.get(name);
            List<IResourcePack> list = ObfuscationReflectionHelper.getPrivateValue(FallbackResourceManager.class, manager, 0);
            for (IResourcePack pack : list) {
                if (pack instanceof AbstractResourcePack) {
                    AbstractResourcePack p = (AbstractResourcePack) pack;
                    File file = NPCResourceHelper.getPackFile(p);
                    if (file != null) set.add(file.getAbsolutePath());
                }
            }
        }

        for (String file : set) {
            File f = new File(file);
            if (f.isDirectory()) {
                checkFolder(new File(f, "assets"), f.getAbsolutePath().length());
            } else {
                progressFile(f);
            }
        }

        for (ModContainer mod : Loader.instance().getModList()) {
            if (mod.getSource().exists()) progressFile(mod.getSource());
        }

        ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
        repos.updateRepositoryEntriesAll();
        List<?> entries = repos.getRepositoryEntries();
        for (Object obj : entries) {
            ResourcePackRepository.Entry entry = (ResourcePackRepository.Entry) obj;
            File file = new File(repos.getDirResourcepacks(), entry.getResourcePackName());
            if (file.exists()) progressFile(file);
        }

        checkFolder(new File(CustomNpcs.Dir, "assets"), CustomNpcs.Dir.getAbsolutePath().length());

        URL url = DefaultResourcePack.class.getResource("/");
        if (url != null) {
            File f = decodeFile(url.getFile());
            if (f.isDirectory()) {
                checkFolder(new File(f, "assets"), url.getFile().length());
            } else {
                progressFile(f);
            }
        }

        url = CraftingManager.class.getResource("/assets/.mcassetsroot");
        if (url != null) {
            File f = decodeFile(url.getFile());
            if (f.isDirectory()) {
                checkFolder(new File(f, "assets"), url.getFile().length());
            } else {
                progressFile(f);
            }
        }
    }

    private File decodeFile(String url) {
        if (url.startsWith("file:")) url = url.substring(5);
        url = url.replace('/', File.separatorChar);
        int i = url.indexOf('!');
        if (i > 0) url = url.substring(0, i);
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ignored) {}
        return new File(url);
    }

    private void progressFile(File file) {
        try {
            if (!file.isDirectory() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
                ZipFile zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipentry = entries.nextElement();
                    addFile(zipentry.getName());
                }
                zip.close();
            } else if (file.isDirectory()) {
                checkFolder(file, file.getAbsolutePath().length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkFolder(File file, int length) {
        File[] files = file.listFiles();
        if (files == null) return;
        for (File f : files) {
            String name = null;
            try {
                name = f.getAbsolutePath().substring(length);
                name = name.replace("\\", "/");
                if (!name.startsWith("/")) name = "/" + name;
                if (f.isDirectory()) {
                    addFile(name + "/");
                    checkFolder(f, length);
                } else {
                    addFile(name);
                }
            } catch (Throwable e) {
                LogWriter.error("error with: " + name);
            }
        }
    }

    private void addFile(String name) {
        if (name.startsWith("/")) name = name.substring(1);
        if (!name.startsWith("assets/") || !name.endsWith(".png")) return;
        name = name.substring(7);
        int i = name.indexOf('/');
        String domain = name.substring(0, i);
        name = name.substring(i + 1);

        List<TextureData> list = domains.get(domain);
        if (list == null) {
            domains.put(domain, list = new ArrayList<>());
        }
        boolean contains = false;
        for (TextureData data : list) {
            if (data.absoluteName.equals(name)) {
                contains = true;
                break;
            }
        }
        if (!contains) list.add(new TextureData(domain, name));
    }

    @Override
    public void initGui() {
        super.initGui();

        int contentY = getContentY() + 6;
        int contentH = getContentHeight() - 6;

        if (dividerOffset == 0) {
            dividerOffset = (xSize - 24) / 2;
        }

        int pad = 8;
        folderListX = guiLeft + pad;
        folderListW = dividerOffset - pad;
        textureListX = guiLeft + dividerOffset + dividerWidth;
        textureListW = xSize - dividerOffset - dividerWidth - pad;

        if (folderListW < minColumnWidth) {
            folderListW = minColumnWidth;
            dividerOffset = folderListW + pad;
            textureListX = guiLeft + dividerOffset + dividerWidth;
            textureListW = xSize - dividerOffset - dividerWidth - pad;
        }
        if (textureListW < minColumnWidth) {
            textureListW = minColumnWidth;
            dividerOffset = xSize - pad - dividerWidth - textureListW;
            folderListW = dividerOffset - pad;
            textureListX = guiLeft + dividerOffset + dividerWidth;
        }

        int searchH = 18;
        folderSearchField = new ModernTextField(0, folderListX, contentY, folderListW, searchH);
        folderSearchField.setPlaceholder("Filter folders...");
        folderSearchField.setText(folderSearch);

        textureSearchField = new ModernTextField(1, textureListX, contentY, textureListW, searchH);
        textureSearchField.setPlaceholder("Filter textures...");
        textureSearchField.setText(textureSearch);

        listY = contentY + searchH + 4;
        listHeight = contentH - searchH - 36 - 8;

        int btnY = guiTop + ySize - 32;
        int btnWidth = 60;
        int btnGap = 8;

        cancelBtn = new ModernButton(ID_CANCEL, guiLeft + xSize - pad - btnWidth * 2 - btnGap, btnY, btnWidth, 20, "Cancel");
        doneBtn = new ModernButton(ID_DONE, guiLeft + xSize - pad - btnWidth, btnY, btnWidth, 20, "Done");
        doneBtn.setBackgroundColor(ModernColors.ACCENT_BLUE);

        updateFolderList();
        updateTextureList();
    }

    private void updateFolderList() {
        filteredFolders = new ArrayList<>();

        if (selectedDomain == null) {
            // Show domains
            for (String domain : domains.keySet()) {
                if (folderSearch.isEmpty() || domain.toLowerCase().contains(folderSearch.toLowerCase())) {
                    filteredFolders.add(domain);
                }
            }
        } else {
            // Show folders within domain
            filteredFolders.add(UP_TEXT);

            List<TextureData> data = domains.get(selectedDomain);
            Set<String> folders = new TreeSet<>();
            for (TextureData td : data) {
                if (location.isEmpty() || td.path.startsWith(location) && !td.path.equals(location)) {
                    String path = td.path.substring(location.length());
                    int i = path.indexOf('/');
                    if (i < 0) continue;
                    path = path.substring(0, i);
                    if (!path.isEmpty()) folders.add(path);
                }
            }

            for (String folder : folders) {
                if (folderSearch.isEmpty() || folder.toLowerCase().contains(folderSearch.toLowerCase())) {
                    filteredFolders.add(folder);
                }
            }
        }

        Collections.sort(filteredFolders.subList(filteredFolders.contains(UP_TEXT) ? 1 : 0, filteredFolders.size()));
    }

    private void updateTextureList() {
        textures.clear();
        filteredTextures = new ArrayList<>();

        if (selectedDomain == null) return;

        List<TextureData> data = domains.get(selectedDomain);
        String loc = location;
        if (selectedFolderIndex > 0 && selectedFolderIndex < filteredFolders.size()) {
            String folder = filteredFolders.get(selectedFolderIndex);
            if (!folder.equals(UP_TEXT)) {
                loc += folder + '/';
            }
        }

        for (TextureData td : data) {
            if (td.path.equals(loc)) {
                if (textureSearch.isEmpty() || td.name.toLowerCase().contains(textureSearch.toLowerCase())) {
                    if (!filteredTextures.contains(td.name)) {
                        filteredTextures.add(td.name);
                        textures.put(td.name, td);
                    }
                }
            }
        }

        Collections.sort(filteredTextures);
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Draw breadcrumb path
        String pathText = selectedDomain != null ? selectedDomain + ":" + location : "Select a domain";
        fontRendererObj.drawString(pathText, guiLeft + 8, getContentY() - 12, ModernColors.TEXT_GRAY);

        // Draw column headers
        int headerY = getContentY() - 2;
        fontRendererObj.drawString(selectedDomain == null ? "Domains" : "Folders", folderListX, headerY - 10, ModernColors.TEXT_LIGHT);
        fontRendererObj.drawString("Textures", textureListX, headerY - 10, ModernColors.TEXT_LIGHT);

        folderSearchField.draw(mouseX, mouseY);
        textureSearchField.draw(mouseX, mouseY);

        drawRect(folderListX, listY, folderListX + folderListW, listY + listHeight, ModernColors.INPUT_BG);
        drawRect(textureListX, listY, textureListX + textureListW, listY + listHeight, ModernColors.INPUT_BG);

        int divX = guiLeft + dividerOffset;
        int handleTop = listY + (listHeight - 20) / 2;
        int handleColor = isDraggingDivider ? ModernColors.ACCENT_BLUE : 0xFF707070;
        drawRect(divX + 1, handleTop, divX + dividerWidth - 1, handleTop + 20, handleColor);

        drawFolderList(mouseX, mouseY);
        drawTextureList(mouseX, mouseY);

        if (selectedResource != null) {
            String info = "Selected: " + selectedResource.toString();
            info = fontRendererObj.trimStringToWidth(info, xSize - 16);
            int infoY = listY + listHeight + 4;
            fontRendererObj.drawString(info, guiLeft + 8, infoY, ModernColors.TEXT_GRAY);
        }

        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    private void drawFolderList(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(folderListX * scale, mc.displayHeight - (listY + listHeight) * scale, folderListW * scale, listHeight * scale);

        int visibleRows = listHeight / rowHeight;
        int startIdx = folderScrollY / rowHeight;
        int endIdx = Math.min(startIdx + visibleRows + 2, filteredFolders.size());

        for (int i = startIdx; i < endIdx; i++) {
            int rowY = listY + i * rowHeight - folderScrollY;
            if (rowY + rowHeight < listY || rowY > listY + listHeight) continue;

            String text = filteredFolders.get(i);
            boolean isUp = text.equals(UP_TEXT);

            if (i == selectedFolderIndex) {
                drawRect(folderListX, rowY, folderListX + folderListW, rowY + rowHeight, ModernColors.SELECTION_BG);
            } else if (mouseX >= folderListX && mouseX < folderListX + folderListW && mouseY >= rowY && mouseY < rowY + rowHeight) {
                drawRect(folderListX, rowY, folderListX + folderListW, rowY + rowHeight, ModernColors.HOVER_HIGHLIGHT);
            }

            String displayText = fontRendererObj.trimStringToWidth(text, folderListW - 4);
            int textColor = isUp ? ModernColors.ACCENT_BLUE : (i == selectedFolderIndex ? ModernColors.TEXT_WHITE : ModernColors.TEXT_LIGHT);
            fontRendererObj.drawString(displayText, folderListX + 2, rowY + 2, textColor);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        int totalHeight = filteredFolders.size() * rowHeight;
        if (totalHeight > listHeight) {
            int sbX = folderListX + folderListW - 4;
            float viewRatio = (float) listHeight / totalHeight;
            int thumbH = Math.max(10, (int) (listHeight * viewRatio));
            float maxScroll = totalHeight - listHeight;
            int thumbY = maxScroll > 0 ? (int) ((folderScrollY / maxScroll) * (listHeight - thumbH)) : 0;
            drawRect(sbX, listY, sbX + 4, listY + listHeight, ModernColors.SCROLLBAR_BG);
            drawRect(sbX, listY + thumbY, sbX + 4, listY + thumbY + thumbH, ModernColors.SCROLLBAR_THUMB);
        }
    }

    private void drawTextureList(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(textureListX * scale, mc.displayHeight - (listY + listHeight) * scale, textureListW * scale, listHeight * scale);

        int visibleRows = listHeight / rowHeight;
        int startIdx = textureScrollY / rowHeight;
        int endIdx = Math.min(startIdx + visibleRows + 2, filteredTextures.size());

        for (int i = startIdx; i < endIdx; i++) {
            int rowY = listY + i * rowHeight - textureScrollY;
            if (rowY + rowHeight < listY || rowY > listY + listHeight) continue;

            if (i == selectedTextureIndex) {
                drawRect(textureListX, rowY, textureListX + textureListW, rowY + rowHeight, ModernColors.SELECTION_BG);
            } else if (mouseX >= textureListX && mouseX < textureListX + textureListW && mouseY >= rowY && mouseY < rowY + rowHeight) {
                drawRect(textureListX, rowY, textureListX + textureListW, rowY + rowHeight, ModernColors.HOVER_HIGHLIGHT);
            }

            String text = filteredTextures.get(i);
            String displayText = fontRendererObj.trimStringToWidth(text, textureListW - 4);
            int textColor = i == selectedTextureIndex ? ModernColors.TEXT_WHITE : ModernColors.TEXT_LIGHT;
            fontRendererObj.drawString(displayText, textureListX + 2, rowY + 2, textColor);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        int totalHeight = filteredTextures.size() * rowHeight;
        if (totalHeight > listHeight) {
            int sbX = textureListX + textureListW - 4;
            float viewRatio = (float) listHeight / totalHeight;
            int thumbH = Math.max(10, (int) (listHeight * viewRatio));
            float maxScroll = totalHeight - listHeight;
            int thumbY = maxScroll > 0 ? (int) ((textureScrollY / maxScroll) * (listHeight - thumbH)) : 0;
            drawRect(sbX, listY, sbX + 4, listY + listHeight, ModernColors.SCROLLBAR_BG);
            drawRect(sbX, listY + thumbY, sbX + 4, listY + thumbY + thumbH, ModernColors.SCROLLBAR_THUMB);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        folderSearchField.updateCursorCounter();
        textureSearchField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            int divX = guiLeft + dividerOffset;
            if (mouseX >= divX && mouseX < divX + dividerWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {
                isDraggingDivider = true;
                dividerDragStartX = mouseX;
                return;
            }
        }

        folderSearchField.mouseClicked(mouseX, mouseY, button);
        textureSearchField.mouseClicked(mouseX, mouseY, button);

        // Folder list click
        if (button == 0 && mouseX >= folderListX && mouseX < folderListX + folderListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + folderScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredFolders.size()) {
                long now = System.currentTimeMillis();
                if (clickedIdx == lastClickedIndex && lastClickWasFolder && now - lastClickTime < 400) {
                    // Double-click on folder
                    handleFolderDoubleClick(clickedIdx);
                    return;
                }
                lastClickedIndex = clickedIdx;
                lastClickTime = now;
                lastClickWasFolder = true;

                selectedFolderIndex = clickedIdx;
                textureScrollY = 0;
                selectedTextureIndex = -1;
                updateTextureList();
            }
        }

        // Texture list click
        if (button == 0 && mouseX >= textureListX && mouseX < textureListX + textureListW &&
            mouseY >= listY && mouseY < listY + listHeight) {
            int clickedIdx = (mouseY - listY + textureScrollY) / rowHeight;
            if (clickedIdx >= 0 && clickedIdx < filteredTextures.size()) {
                long now = System.currentTimeMillis();
                if (clickedIdx == lastClickedIndex && !lastClickWasFolder && now - lastClickTime < 400) {
                    // Double-click on texture - confirm and close
                    confirm();
                    return;
                }
                lastClickedIndex = clickedIdx;
                lastClickTime = now;
                lastClickWasFolder = false;

                selectedTextureIndex = clickedIdx;
                String textureName = filteredTextures.get(clickedIdx);
                TextureData data = textures.get(textureName);
                if (data != null) {
                    selectedResource = new ResourceLocation(selectedDomain, data.absoluteName);
                    if (setNPCSkin && npc != null) {
                        npc.textureLocation = selectedResource;
                    }
                }
            }
        }

        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            confirm();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            selectedResource = null;
            close();
            return;
        }
    }

    private void handleFolderDoubleClick(int idx) {
        String selection = filteredFolders.get(idx);

        if (selectedDomain == null) {
            // Select domain
            selectedDomain = selection;
            folderScrollY = 0;
            selectedFolderIndex = -1;
        } else if (selection.equals(UP_TEXT)) {
            // Go up
            int i = location.lastIndexOf('/', location.length() - 2);
            if (i < 0) {
                if (location.isEmpty()) {
                    selectedDomain = null;
                }
                location = "";
            } else {
                location = location.substring(0, i + 1);
            }
            folderScrollY = 0;
            selectedFolderIndex = -1;
        } else {
            // Navigate into folder
            location = location + selection + '/';
            folderScrollY = 0;
            selectedFolderIndex = -1;
        }

        textureScrollY = 0;
        selectedTextureIndex = -1;
        updateFolderList();
        updateTextureList();
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

            folderListW = dividerOffset - pad;
            textureListX = guiLeft + dividerOffset + dividerWidth;
            textureListW = xSize - dividerOffset - dividerWidth - pad;

            folderSearchField.setBounds(folderListX, folderSearchField.getY(), folderListW, 18);
            textureSearchField.setBounds(textureListX, textureSearchField.getY(), textureListW, 18);
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

            if (mouseX >= folderListX && mouseX < folderListX + folderListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredFolders.size() * rowHeight - listHeight);
                folderScrollY = Math.max(0, Math.min(folderScrollY + scrollAmount, maxScroll));
            }

            if (mouseX >= textureListX && mouseX < textureListX + textureListW &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int maxScroll = Math.max(0, filteredTextures.size() * rowHeight - listHeight);
                textureScrollY = Math.max(0, Math.min(textureScrollY + scrollAmount, maxScroll));
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (folderSearchField.isFocused()) {
            String prev = folderSearchField.getText();
            folderSearchField.keyTyped(typedChar, keyCode);
            String newText = folderSearchField.getText();
            if (!newText.equals(prev)) {
                folderSearch = newText;
                updateFolderList();
                folderScrollY = 0;
            }
        }

        if (textureSearchField.isFocused()) {
            String prev = textureSearchField.getText();
            textureSearchField.keyTyped(typedChar, keyCode);
            String newText = textureSearchField.getText();
            if (!newText.equals(prev)) {
                textureSearch = newText;
                updateTextureList();
                textureScrollY = 0;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    private void confirm() {
        if (selectedResource != null && setNPCSkin && npc != null) {
            npc.display.setSkinTexture(selectedResource.toString());
        }
        if (npc != null) {
            npc.textureLocation = null;
        }
        close();
    }

    public void setLocation(String domain, String location) {
        this.selectedDomain = domain;
        this.location = location;
    }

    /**
     * Get the selected resource location.
     */
    public ResourceLocation getSelectedResource() {
        return selectedResource;
    }

    // Inner class for texture data
    static class TextureData {
        String domain;
        String absoluteName;
        String name;
        String path;

        public TextureData(String domain, String absoluteName) {
            this.domain = domain;
            int i = absoluteName.lastIndexOf('/');
            name = absoluteName.substring(i + 1);
            path = absoluteName.substring(0, i + 1);
            this.absoluteName = absoluteName;
        }
    }
}
