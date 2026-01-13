package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScriptTextArea;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IJTextAreaListener;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.scripted.item.ScriptCustomItem;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GuiScriptInterface extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ITextChangeListener, ICustomScrollListener, IJTextAreaListener, ITextfieldListener {
    protected int activeTab = 0;
    public IScriptHandler handler;
    public Map<String, List<String>> languages = new HashMap();
    protected int scriptLimit = 1;
    public List<String> hookList = new ArrayList<String>();
    protected boolean loaded = false;

    protected Map<Integer, GuiScriptTextArea> textAreas = new HashMap<>();
    protected GuiScreen parent;

    public boolean singleContainer = false;
    
    /** If true, uses scroll-based tab selection instead of top buttons */
    protected boolean useScrollTabs = false;
    
    /** If true, shows settings/script toggle instead of numbered tabs */
    protected boolean useSettingsToggle = false; 

    // ==================== FULLSCREEN MODE ====================
    /** Whether the editor viewport is currently in fullscreen mode */
    public static boolean isFullscreen = false;

    /**
     * Fullscreen Layout Configuration
     * Adjust these values to customize the fullscreen viewport appearance.
     * All padding values are in scaled GUI pixels.
     */
    public static class FullscreenConfig {
        /** Padding from the top edge of the screen */
        public static int paddingTop = 20;
        /** Padding from the bottom edge of the screen */
        public static int paddingBottom = 20;
        /** Padding from the left edge of the screen */
        public static int paddingLeft = 20;
        /** Padding from the right edge of the screen */
        public static int paddingRight = 20;
    }

    /** Fullscreen toggle button drawn at top-right of viewport */
    public final FullscreenButton fullscreenButton = new FullscreenButton();
    
    public GuiScriptInterface() {
        this.drawDefaultBackground = true;
        this.closeOnEsc = true;
        this.xSize = 420;
        this.setBackground("menubg.png");
    }

    public void initGui() {
        // ==================== BASE LAYOUT CALCULATION ====================
        this.ySize = (int) ((double) this.xSize * 0.56D);
        if ((double) this.ySize > (double) this.height * 0.95D) {
            this.ySize = (int) ((double) this.height * 0.95D);
            this.xSize = (int) ((double) this.ySize / 0.56D);
        }

        this.bgScale = (float) this.xSize / 400.0F;
        super.initGui();
        this.guiTop += 10;
        int yoffset = (int) ((double) this.ySize * 0.02D);

        // ==================== TAB BUTTONS ====================
        // Skip standard tab creation for GUIs that use custom toggle/scroll tabs
        if (!useSettingsToggle) {
            GuiMenuTopButton top;
            int topButtonsX = isFullscreen && activeTab != 0 ? FullscreenConfig.paddingLeft : this.guiLeft + 4;
            int topButtonsY = isFullscreen && activeTab != 0 ? FullscreenConfig.paddingTop - 20 : this.guiTop - 17;
            this.addTopButton(top = new GuiMenuTopButton(0, topButtonsX, topButtonsY, "gui.settings"));

            int topXoffset = 0;
            int topYoffset = 0;
            if(!singleContainer) {
                for (int ta = 0; ta < this.handler.getScripts().size(); ++ta) {
                    if (ta % 20 == 0 && ta > 0) {
                        topYoffset -= 20;
                        topXoffset -= top.width + 20 * 22;
                    }
                    this.addTopButton(top = new GuiMenuTopButton(ta + 1, top.xPosition + top.width + topXoffset,
                            top.yPosition + topYoffset, ta + 1 + ""));
                    topXoffset = 0;
                    topYoffset = 0;
                    scriptLimit = ta + 2;
                }
            }

            if (singleContainer && getFirst() != null)
                this.addTopButton(
                        top = new GuiMenuTopButton(1, top.xPosition + top.width + topXoffset, top.yPosition + topYoffset,
                                "Script"));
            else if (this.handler.getScripts().size() < 100)
                this.addTopButton(new GuiMenuTopButton(scriptLimit, top.xPosition + top.width, top.yPosition, "+"));


            top = this.getTopButton(this.activeTab);
            if (top == null) {
                this.activeTab = 0;
                top = this.getTopButton(0);
            }
            top.active = true;
        }

        // ==================== SCRIPT EDITOR TAB (activeTab > 0) ====================
        // Skip for GUIs that handle their own view initialization (useSettingsToggle)
        if (!useSettingsToggle) {
            if (this.activeTab > 0) {
                initScriptEditorTab(yoffset);
            } else {
                // ==================== SETTINGS TAB (activeTab == 0) ====================
                initSettingsTab(yoffset);
            }
        }

        this.xSize = 420;
        this.ySize = 256;
    }

    /**
     * Initialize the script editor tab layout.
     * Handles both normal and fullscreen modes.
     */
    private void initScriptEditorTab(int yoffset) {
        IScriptUnit container = getCurrentContainer();

        // ==================== CALCULATE VIEWPORT BOUNDS ====================
        int editorX, editorY, editorWidth, editorHeight;

        FullscreenConfig.paddingTop = 30;
        FullscreenConfig.paddingBottom = 20;
        FullscreenConfig.paddingLeft = 20;
        FullscreenConfig.paddingRight = 20;
        if (isFullscreen) {
            // Fullscreen: viewport fills screen with configured padding
            editorX = FullscreenConfig.paddingLeft;
            editorY = FullscreenConfig.paddingTop;
            editorWidth = this.width - FullscreenConfig.paddingLeft - FullscreenConfig.paddingRight;
            editorHeight = this.height - FullscreenConfig.paddingTop - FullscreenConfig.paddingBottom;
        } else {
            // Normal: viewport within the GUI panel, leaving space for right panel
            editorX = guiLeft + 1 + yoffset;
            editorY = guiTop + yoffset;
            editorWidth = xSize - 108 - yoffset;
            editorHeight = (int) (ySize * 0.96) - yoffset * 2;
        }

        // ==================== HOOKS SCROLL (hidden in fullscreen) ====================
        if (!isFullscreen) {
            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.allowUserInput = false;
            hooks.setSize(108, 198);
            hooks.guiLeft = guiLeft - 110;
            hooks.guiTop = guiTop + 14;

            if (handler instanceof ForgeDataScript) {
                hooks.setSize(238, 198);
                hooks.guiLeft = guiLeft - 240;
            }
            hooks.setUnsortedList(hookList);
            addScroll(hooks);

            GuiNpcLabel hookLabel = new GuiNpcLabel(0, "script.hooks", hooks.guiLeft, guiTop + 5);
            hookLabel.color = 0xaaaaaa;
            addLabel(hookLabel);
        }

        // ==================== SCRIPT TEXT AREA ====================
        int idx = getActiveScriptIndex();
        GuiScriptTextArea activeArea = getActiveScriptArea();
        if (activeArea == null) {
            activeArea = new GuiScriptTextArea(this, 2, editorX, editorY, editorWidth, editorHeight,
                    container == null ? "" : container.getScript());
            activeArea.setListener(this);
            this.closeOnEsc(activeArea::closeOnEsc);
            textAreas.put(idx, activeArea);
        } else {
            activeArea.init(editorX, editorY, editorWidth, editorHeight,
                    container == null ? "" : container.getScript());
        }
        
        // Set the scripting language for proper syntax highlighting
        // Use the container's language if available, otherwise fall back to handler's language
        String language = (container != null) ? container.getLanguage() : this.handler.getLanguage();
      activeArea.setLanguage(language);

        // Set the script context for context-aware hook autocomplete
        activeArea.setScriptContext(getScriptContext());

        // Set the script context for context-aware hook autocomplete
        activeArea.setScriptContext(getScriptContext());

        // Setup fullscreen key binding
        GuiScriptTextArea.KEYS.FULLSCREEN.setTask(e -> {
            if (e.isPress()) {
                toggleFullscreen();
            }
        });

        activeArea.enableCodeHighlighting();
        this.addTextField(activeArea);

        // ==================== FULLSCREEN BUTTON ====================
        int scrollbarOffset = activeArea.hasVerticalScrollbar() ? -8 : -2;
        fullscreenButton.initGui(editorX + editorWidth, editorY, scrollbarOffset);

        // ==================== RIGHT PANEL BUTTONS (hidden in fullscreen) ====================
        if (!isFullscreen) {
            int left1 = this.guiLeft + this.xSize - 104;
            this.addButton(new GuiNpcButton(102, left1, this.guiTop + yoffset, 60, 20, "gui.clear"));
            this.addButton(new GuiNpcButton(101, left1 + 61, this.guiTop + yoffset, 60, 20, "gui.paste"));
            this.addButton(new GuiNpcButton(100, left1, this.guiTop + 21 + yoffset, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(105, left1 + 61, this.guiTop + 21 + yoffset, 60, 20, "gui.remove"));
            
            // Language toggle button (only if handler supports Janino)
            if (handler.supportsJanino() && container != null) {
                String langLabel = container.isJanino() ? "Java" : "ECMAScript";
                this.addButton(new GuiNpcButton(113, left1, this.guiTop + 42 + yoffset, 121, 20, langLabel));
            }
            
            this.addButton(new GuiNpcButton(107, left1, this.guiTop + 66 + yoffset, 80, 20, "script.loadscript"));

            GuiCustomScroll scroll = (new GuiCustomScroll(this, 0)).setUnselectable();
            scroll.setSize(100, (int) ((double) this.ySize * 0.54D) - yoffset * 2);
            scroll.guiLeft = left1;
            scroll.guiTop = this.guiTop + 88 + yoffset;
            if (container != null) {
                scroll.setList(container.getExternalScripts());
            }
            this.addScroll(scroll);
        }
    }

    /**
     * Initialize the settings tab layout (console view).
     */
    private void initSettingsTab(int yoffset) {
        GuiNpcTextArea var8 = new GuiNpcTextArea(2, this, this.guiLeft + 4 + yoffset, this.guiTop + 6 + yoffset,
                this.xSize - 160 - yoffset, (int) ((float) this.ySize * 0.92F) - yoffset * 2, this.getConsoleText());
        var8.canEdit = false;
        var8.setFocused(true);
        this.addTextField(var8);

        int var9 = this.guiLeft + this.xSize - 150;
        this.addButton(new GuiNpcButton(100, var9, this.guiTop + 125, 60, 20, "gui.copy"));
        this.addButton(new GuiNpcButton(102, var9, this.guiTop + 146, 60, 20, "gui.clear"));
        this.addLabel(new GuiNpcLabel(1, "script.language", var9, this.guiTop + 15));
        this.addButton(new GuiNpcButton(103, var9 + 60, this.guiTop + 10, 80, 20,
                (String[]) this.languages.keySet().toArray(new String[this.languages.keySet().size()]),
                this.getScriptIndex()));
        this.getButton(103).enabled = this.languages.size() > 0;
        this.addLabel(new GuiNpcLabel(2, "gui.enabled", var9, this.guiTop + 36));
        this.addButton(new GuiNpcButton(104, var9 + 60, this.guiTop + 31, 50, 20,
                new String[]{"gui.no", "gui.yes"}, this.handler.getEnabled() ? 1 : 0));

        if (this.player.worldObj.isRemote) {
            this.addButton(new GuiNpcButton(106, var9, this.guiTop + 55, 150, 20, "script.openfolder"));
        }

        this.addButton(new GuiNpcButton(109, var9, this.guiTop + 78, 80, 20, "gui.website"));
        this.addButton(new GuiNpcButton(112, var9 + 81, this.guiTop + 78, 80, 20, "gui.forum"));
        this.addButton(new GuiNpcButton(110, var9, this.guiTop + 99, 80, 20, "script.apidoc"));
        this.addButton(new GuiNpcButton(111, var9 + 81, this.guiTop + 99, 80, 20, "script.apisrc"));
    }

    // ==================== RENDERING ====================

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw fullscreen button on top of everything when on script editor tab
        if (this.activeTab > 0) {
            fullscreenButton.draw(mouseX, mouseY);
        }
    }

    // ==================== MOUSE HANDLING ====================

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Check if click is within autocomplete menu bounds and consume it if so
        GuiScriptTextArea activeArea = getActiveScriptArea();
        boolean isOverAutocomplete = activeArea != null
                && activeArea.isPointOnAutocompleteMenu(mouseX, mouseY);
        if (isOverAutocomplete) {
            activeArea.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        // Check fullscreen button first when on script editor tab
        // BUT only if autocomplete is not visible (don't let clicks pass through autocomplete menu)
        if (this.activeTab > 0 && !isOverAutocomplete
                && fullscreenButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public String previousHookClicked = "";

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        String hook = scroll.getSelected();
        if (previousHookClicked.equals(hook)) {
            IScriptUnit container = getCurrentContainer();
            if (container == null)
                return;
            
            String addString = "";
            if (!this.getTextField(2).getText().isEmpty())
                addString += "\n";
            
            // Generate the appropriate stub based on language
            // hook is already the proper method name from EnumScriptType.function
            addString += container.generateHookStub(hook, null);

            this.getTextField(2).setText(this.getTextField(2).getText() + addString);
            previousHookClicked = "";
        } else {
            previousHookClicked = hook;
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {

    }

    protected String getConsoleText() {
        Map<Long, String> map = this.handler.getConsoleText();
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet().iterator();

        while (var3.hasNext()) {
            Entry<Long, String> entry = (Entry) var3.next();
            builder.insert(0, new Date((Long) entry.getKey()) + (String) entry.getValue() + "\n");
        }

        return builder.toString();
    }

    public int getScriptIndex() {
        int i = 0;

        for (Iterator var2 = this.languages.keySet().iterator(); var2.hasNext(); ++i) {
            String language = (String) var2.next();
            if (language.equalsIgnoreCase(this.handler.getLanguage())) {
                return i;
            }
        }

        return 0;
    }

    public void confirmClicked(boolean flag, int i) {
        if (flag) {
            if (i == 0) {
                this.openLink("https://www.curseforge.com/minecraft/mc-mods/customnpc-plus");
            }

            if (i == 1) {
                this.openLink("https://kamkeel.github.io/CustomNPC-Plus/");
            }

            if (i == 2) {
                this.openLink("https://kamkeel.github.io/CustomNPC-Plus/");
            }

            if (i == 3) {
                this.openLink("http://www.minecraftforge.net/forum/index.php/board,122.0.html");
            }
            if (i == 10) {
                if (singleContainer)
                    setHandlerContainer(null);
                else
                    this.handler.getScripts().remove(this.activeTab - 1);
                this.activeTab = 0;
            }
            if (i == 101) {
                (this.getTextField(2)).setText(NoppesStringUtils.getClipboardContents());
                this.setScript();
            }
            if (i == 102) {
                ScriptContainer container;
                if (this.activeTab > 0) {
                    container = (ScriptContainer) this.handler.getScripts().get(this.activeTab - 1);
                    container.script = "";
                } else {
                    this.handler.clearConsole();
                    if (this.handler instanceof ScriptCustomItem) {
                        ((ScriptCustomItem) this.handler).saveScriptData();
                    }
                }
                this.initGui();
            }
        }

        this.displayGuiScreen(this);
    }

    public GuiScriptTextArea getActiveScriptArea() {
        int idx = getActiveScriptIndex();
        if (idx >= 0 && textAreas.containsKey(idx))
            return textAreas.get(idx);
        return null;
    }
    
    /**
     * Get the current script index for text area storage.
     * Override in subclasses with different tab logic.
     */
    protected int getActiveScriptIndex() {
        return this.activeTab - 1;
    }
    
    /**
     * Get the current script container.
     * Override in subclasses with different container access patterns.
     */
    protected IScriptUnit getCurrentContainer() {
        int idx = getActiveScriptIndex();
        if (idx >= 0 && idx < this.handler.getScripts().size())
            return this.handler.getScripts().get(idx);
        return null;
    }

    @Override
    public GuiNpcTextField getTextField(int id) {
        if (id == 2) {
            GuiScriptTextArea area = getActiveScriptArea();
            if (area != null)
                return area;
        }
        return super.getTextField(id);
    }

    public IScriptUnit getFirst() {
        if (this.handler.getScripts().isEmpty())
            return null;
        return this.handler.getScripts().get(0);
    }
    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id >= 0 && guibutton.id < scriptLimit) {
            this.setScript();
            this.activeTab = guibutton.id;
            this.initGui();
        }

        if (guibutton.id == scriptLimit) {
            if (singleContainer) {
                if (getFirst() == null)
                    setHandlerContainer(new ScriptContainer(handler));
                else
                    this.setScript();
                this.activeTab = 1;
            } else {
                this.handler.getScripts().add(new ScriptContainer(this.handler));
                this.activeTab = this.handler.getScripts().size();
            }
            this.initGui();
        }

        if (guibutton.id == 109) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://kamkeel.github.io/CustomNPC-Plus/", 0, true));
        }

        if (guibutton.id == 110) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://github.com/KAMKEEL/CustomNPC-Plus-API", 1, true));
        }

        if (guibutton.id == 111) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://github.com/Noppes/CustomNPCsAPI", 2, true));
        }

        if (guibutton.id == 112) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.minecraftforge.net/forum/index.php/board,122.0.html", 3, true));
        }

        if (guibutton.id == 100) {
            NoppesStringUtils.setClipboardContents((this.getTextField(2)).getText());
        }

        if (guibutton.id == 101) {
            GuiYesNo guiyesno = new GuiYesNo(this, StatCollector.translateToLocal("gui.paste"), StatCollector.translateToLocal("gui.sure"), 101);
            displayGuiScreen(guiyesno);
        }

        ScriptContainer container;
        if (guibutton.id == 102) {
            GuiYesNo guiyesno = new GuiYesNo(this, StatCollector.translateToLocal("gui.clear"), StatCollector.translateToLocal("gui.sure"), 102);
            displayGuiScreen(guiyesno);
        }

        if (guibutton.id == 103) {
            this.handler.setLanguage(((GuiNpcButton) guibutton).displayString);
        }

        if (guibutton.id == 104) {
            this.handler.setEnabled((((GuiNpcButton) guibutton).getValue() == 1));
        }

        if (guibutton.id == 105) {
            GuiYesNo container1 = new GuiYesNo(this, "", ((GuiNpcButton) guibutton).displayString, 10);
            this.displayGuiScreen(container1);
        }

        if (guibutton.id == 106) {
            // TODO: Opens the ScriptController shared directory; this screen is available only to editors with the scripter
            //       tool and CustomNpcsPermissions.TOOL_SCRIPTER.
            NoppesUtil.openFolder(ScriptController.Instance.dir);
        }

        if (guibutton.id == 107) {
            container = (ScriptContainer) this.handler.getScripts().get(this.activeTab - 1);
            if (container == null) {
                container = new ScriptContainer(this.handler);
                if (singleContainer)
                    setHandlerContainer(container);
                else
                    this.handler.getScripts().add(container);
            }

            this.setSubGui(new EventGuiScriptList((List) this.languages.get(this.handler.getLanguage()), container));
        }
        
        // Language toggle button - switch between ECMAScript and Java
        if (guibutton.id == 113) {
            int idx = getActiveScriptIndex();
            if (idx >= 0 && idx < handler.getScripts().size()) {
                IScriptUnit currentUnit = handler.getScripts().get(idx);
                IScriptUnit newUnit;
                
                if (currentUnit.isJanino()) {
                    // Switch from Java to ECMAScript
                    newUnit = new ScriptContainer(handler);
                    newUnit.setScript(currentUnit.getScript());
                    newUnit.setExternalScripts(new ArrayList<>(currentUnit.getExternalScripts()));
                } else {
                    // Switch from ECMAScript to Java
                    newUnit = handler.createJaninoScriptUnit();
                    if (newUnit != null) {
                        newUnit.setScript(currentUnit.getScript());
                        newUnit.setExternalScripts(new ArrayList<>(currentUnit.getExternalScripts()));
                    } else {
                        return; // Handler doesn't support Janino
                    }
                }
                
                // Replace the script unit in the list
                handler.getScripts().set(idx, newUnit);
                
                // Clear the cached text area so it recreates with new language
                textAreas.remove(idx);
                
                // Reinitialize the GUI
                initGui();
            }
        }
    }

    protected void setScript() {
        if (this.activeTab > 0 || useScrollTabs) {
            IScriptUnit container = getCurrentContainer();
            if (container == null) {
                container = new ScriptContainer(this.handler);
                if (singleContainer)
                    setHandlerContainer(container);
                else
                    this.handler.getScripts().add(container);
            }

            String text = (this.getTextField(2)).getText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            container.setScript(text);
        }

    }

    public void setGuiData(NBTTagCompound compound) {
        NBTTagList data = compound.getTagList("Languages", 10);
        HashMap languages = new HashMap();

        for (int i = 0; i < data.tagCount(); ++i) {
            NBTTagCompound comp = data.getCompoundTagAt(i);
            ArrayList scripts = new ArrayList();
            NBTTagList list = comp.getTagList("Scripts", 8);

            for (int j = 0; j < list.tagCount(); ++j) {
                scripts.add(list.getStringTagAt(j));
            }

            languages.put(comp.getString("Language"), scripts);
        }

        this.languages = languages;
        this.initGui();
    }

    /**
     * Get the script context for this GUI.
     * Override in subclasses to return the appropriate context.
     *
     * @return The script context (default: GLOBAL)
     */
    protected ScriptContext getScriptContext() {
        return ScriptContext.GLOBAL;
    }

    // ==================== UNIFIED SCRIPT DATA HANDLING ====================

    /**
     * Unified setGuiData for script GUIs with old container system.
     * Handles both language data and tab-specific script loading.
     */
    protected void setGuiDataWithOldContainer(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            loaded = true;
            return;
        }

        if (!compound.hasKey("Tab")) {
            this.handler.setLanguage(compound.getString("ScriptLanguage"));
            this.handler.setEnabled(compound.getBoolean("ScriptEnabled"));
            this.loadLanguagesData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            NBTTagCompound scriptCompound = compound.getCompoundTag("Script");
            IScriptUnit container = IScriptUnit.createFromNBT(scriptCompound, this.handler);
            this.setHandlerContainer(container);
            this.initGui();
        }
    }

    /**
     * Load languages data from NBT.
     * Separated for potential override in subclasses if needed.
     */
    protected void loadLanguagesData(NBTTagCompound compound) {
        NBTTagList data = compound.getTagList("Languages", 10);
        HashMap languages = new HashMap();

        for (int i = 0; i < data.tagCount(); ++i) {
            NBTTagCompound comp = data.getCompoundTagAt(i);
            java.util.ArrayList scripts = new java.util.ArrayList();
            NBTTagList list = comp.getTagList("Scripts", 8);

            for (int j = 0; j < list.tagCount(); ++j) {
                scripts.add(list.getStringTagAt(j));
            }

            languages.put(comp.getString("Language"), scripts);
        }

        this.languages = languages;
        this.initGui();
    }

    /**
     * Set the handler's container. Override if handler is not IScriptHandler.
     */
    protected void setHandlerContainer(IScriptUnit container) {
        // Default implementation - subclasses may need to cast and set differently
        // e.g., ((LinkedItemScript) handler).container = container;
    }

    /**
     * Unified save method for script GUIs with packet-based saving.
     * Subclasses only need to override sendScriptPackets() and sendMetadataPacket().
     */
    protected void saveWithPackets() {
        if (loaded) {
            this.setScript();

            List<IScriptUnit> containers = this.handler.getScripts();
            for (int i = 0; i < containers.size(); i++) {
                IScriptUnit container = containers.get(i);
                sendSavePacket(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
            }

            NBTTagCompound scriptData = new NBTTagCompound();
            scriptData.setString("ScriptLanguage", this.handler.getLanguage());
            scriptData.setBoolean("ScriptEnabled", this.handler.getEnabled());
            scriptData.setTag("ScriptConsole", noppes.npcs.NBTTags.NBTLongStringMap(this.handler.getConsoleText()));

            sendSavePacket(-1, containers.size(), scriptData);
        }
    }

    /**
     * Send a script container packet. Override in subclasses.
     * @param index The index of this script
     * @param totalCount Total number of scripts
     * @param scriptNBT The script container NBT data
     */
    protected void sendSavePacket(int index, int totalCount, NBTTagCompound scriptNBT) {
        // Default: do nothing (for non-packet-based GUIs like GuiScriptItem)
    }

    public void save() {
        if (loaded)
            this.setScript();
    }

    public void textUpdate(String text) {
        IScriptUnit container = getCurrentContainer();
        if (container != null) {
            container.setScript(text);
        }

    }

    @Deprecated
    @Override
    //NEVER USED
    public void saveText(String text) {
        IScriptUnit container = getCurrentContainer();
        if (container != null)
            container.setScript(text);
        initGui();
    }

    @Override
    public void close() {
        if (parent != null) {
            this.save();
            parent.setWorldAndResolution(mc, width, height);
            parent.initGui();
            mc.currentScreen = parent;
        } else
            super.close();
    }

    // ==================== FULLSCREEN METHODS ====================

    /**
     * Toggle fullscreen mode for the editor viewport.
     * When entering fullscreen, maximizes the game window if it's not already.
     */
    public void toggleFullscreen() {
        isFullscreen = !isFullscreen;

        // When entering fullscreen, maximize the game window if not already fullscreen
        if (isFullscreen && !Display.isFullscreen()) {
            // Note: Display.setFullscreen(true) would make it truly fullscreen,
            // but that's usually not desirable for an in-game editor.
            // The viewport will expand to fill whatever window size exists.
        }

        initGui();
    }

    /**
     * Check if editor is currently in fullscreen mode
     */
    public boolean isEditorFullscreen() {
        return isFullscreen;
    }

    // ==================== FULLSCREEN BUTTON ====================

    /**
     * Button to toggle fullscreen mode, displayed at top-right corner of viewport.
     * Shows expand icon when minimized, collapse icon when fullscreen.
     */

    public class FullscreenButton {
        public int x, y;
        public int size = 12;
        public boolean hovered = false;

        /** Initialize button position relative to viewport bounds */
        public void initGui(int viewportEndX, int viewportY, int scrollbarOffset) {
            // Position at top-right, accounting for scrollbar
            this.x = viewportEndX + scrollbarOffset - size - 4;
            this.y = viewportY + 4;
        }

        /** Draw the fullscreen toggle button */
        public void draw(int mouseX, int mouseY) {
            hovered = isMouseOver(mouseX, mouseY);
            int y = this.y + getYOffset();

            // Button background
            int bgColor = hovered ? 0x80666666 : 0x60444444;
            GuiUtil.drawRectD(x, y, x + size, y + size, bgColor);

            // Draw corner brackets to indicate expand/collapse
            int iconColor = hovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            int inset = 2;
            int cornerLen = 3;

            if (isFullscreen) {
                // Draw collapse icon (corners pointing inward) — inward L-shapes
                int top = y + inset + 2;
                int left = x + inset + 2;
                int right = x + size - inset - 3;
                int bottom = y + size - inset - 3;

                drawRect(left, top - cornerLen + 1, left + 1, top + 1, iconColor);
                drawRect(left - cornerLen + 1, top, left + 1, top + 1, iconColor);

                drawRect(right, top, right + cornerLen, top + 1, iconColor);
                drawRect(right, top - cornerLen + 1, right + 1, top + 1, iconColor);

                drawRect(left - cornerLen + 1, bottom, left + 1, bottom + 1, iconColor);
                drawRect(left, bottom, left + 1, bottom + cornerLen, iconColor);

                drawRect(right, bottom, right + cornerLen, bottom + 1, iconColor);
                drawRect(right, bottom, right + 1, bottom + cornerLen, iconColor);
                  
            } else {
                // Draw expand icon (corners pointing outward) ⌐¬⌙⌞
                // Top-left corner pointing outward
                drawRect(x + inset, y + inset, x + inset + 1, y + inset + cornerLen, iconColor);
                drawRect(x + inset, y + inset, x + inset + cornerLen, y + inset + 1, iconColor);
                // Top-right corner pointing outward
                drawRect(x + size - inset - 1, y + inset, x + size - inset, y + inset + cornerLen, iconColor);
                drawRect(x + size - inset - cornerLen, y + inset, x + size - inset, y + inset + 1, iconColor);
                // Bottom-left corner pointing outward
                drawRect(x + inset, y + size - inset - cornerLen, x + inset + 1, y + size - inset, iconColor);
                drawRect(x + inset, y + size - inset - 1, x + inset + cornerLen, y + size - inset, iconColor);
                // Bottom-right corner pointing outward
                drawRect(x + size - inset - 1, y + size - inset - cornerLen, x + size - inset, y + size - inset,
                        iconColor);
                drawRect(x + size - inset - cornerLen, y + size - inset - 1, x + size - inset, y + size - inset,
                        iconColor);
            }
        }
        
        public void drawRect(int x1, int y1, int x2, int y2, int color) {
            GuiUtil.drawRectD(x1, y1, x2, y2, color);
        }

        /** Check if mouse is over the button */
        public boolean isMouseOver(int mouseX, int mouseY) {
            int y = this.y + getYOffset();
            return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        }

        /** Handle click - returns true if click was consumed */
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0 && isMouseOver(mouseX, mouseY)) {
                toggleFullscreen();
                return true;
            }
            return false;
        }

        public int getYOffset() {
            return GuiScriptTextArea.searchBar.getTotalHeight();
        }
    }
}
