package noppes.npcs.client.gui;

import kamkeel.npcs.network.packets.request.script.NPCScriptPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.script.GuiScriptInterface;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiScriptTextArea;
import noppes.npcs.client.gui.util.script.interpreter.ScriptTextContainer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * NPC Script GUI - extends GuiScriptInterface but with unique behavior:
 * - Uses scroll-based tab selection instead of top buttons
 * - Shows settings/script toggle
 * - Uses DataScript with getNPCScript(index) access pattern
 * - Has console filtering by hook type
 */
public class GuiScript extends GuiScriptInterface {
    public boolean showScript = false;
    public DataScript script;

    private static int activeConsole = 0;

    public GuiScript(EntityNPCInterface npc) {
        super();
        this.npc = npc;
        this.script = npc.script;
        this.handler = script;
        this.useScrollTabs = true;
        this.useSettingsToggle = true;

        // Initialize hook list for NPC scripts
        hookList.add("script.init");
        hookList.add("script.update");
        hookList.add("script.interact");
        hookList.add("dialog.dialog");
        hookList.add("script.damaged");
        hookList.add("script.killed");
        hookList.add("script.attack");
        hookList.add("script.target");
        hookList.add("script.collide");
        hookList.add("script.kills");
        hookList.add("script.dialog_closed");
        hookList.add("script.timer");
        hookList.add("script.targetLost");
        hookList.add("script.projectileTick");
        hookList.add("script.projectileImpact");

        NPCScriptPacket.Get();
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.NPC;
    }

    @Override
    protected int getActiveScriptIndex() {
        return this.activeTab;
    }

    @Override
    protected ScriptContainer getCurrentContainer() {
        return script.getNPCScript(activeTab);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiTop += 10;

        if (isFullscreen) {
            FullscreenConfig.paddingTop = 30;
        }

        // ==================== TOP BUTTONS ====================
        boolean isFullscreenView = isFullscreen && showScript;
        int menuX = isFullscreenView ? FullscreenConfig.paddingLeft : guiLeft + 4;
        int menuY = isFullscreenView ? FullscreenConfig.paddingTop - 20 : guiTop - 17;
        int rightX = isFullscreenView ? width - FullscreenConfig.paddingRight : guiLeft + xSize;

        GuiMenuTopButton top;
        addTopButton(top = new GuiMenuTopButton(13, menuX, menuY, "script.scripts"));
        addTopButton(new GuiMenuTopButton(16, rightX - 102, menuY, "eventscript.eventScripts"));
        addTopButton(new GuiMenuTopButton(17, rightX - 22, menuY, "X"));
        top.active = showScript;
        addTopButton(top = new GuiMenuTopButton(14, top, "gui.settings"));
        top.active = !showScript;
        addTopButton(new GuiMenuTopButton(15, top, "gui.website"));

        if (showScript) {
            initScriptView();
        } else {
            initSettingsView();
        }
    }

    private void initScriptView() {
        ScriptContainer container = getCurrentContainer();

        // ==================== CALCULATE VIEWPORT BOUNDS ====================
        int editorX, editorY, editorWidth, editorHeight;

        if (isFullscreen) {
            // Fullscreen: viewport fills screen with configured padding
            FullscreenConfig.paddingTop = 30;
            FullscreenConfig.paddingBottom = 20;
            FullscreenConfig.paddingLeft = 20;
            FullscreenConfig.paddingRight = 20;
            editorX = FullscreenConfig.paddingLeft;
            editorY = FullscreenConfig.paddingTop;
            editorWidth = this.width - FullscreenConfig.paddingLeft - FullscreenConfig.paddingRight;
            editorHeight = this.height - FullscreenConfig.paddingTop - FullscreenConfig.paddingBottom;
        } else {
            // Normal: fixed layout with hooks on left
            editorX = guiLeft + 74;
            editorY = guiTop + 4;
            editorWidth = 239;
            editorHeight = 208;
        }

        // ==================== HOOKS SCROLL (hidden in fullscreen) ====================
        if (!isFullscreen) {
            addLabel(new GuiNpcLabel(0, "script.hooks", guiLeft + 4, guiTop + 5));

            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.setSize(68, 198);
            hooks.guiLeft = guiLeft + 4;
            hooks.guiTop = guiTop + 14;
            hooks.setUnsortedList(hookList);
            hooks.selected = activeTab;
            addScroll(hooks);
        }

        // ==================== SCRIPT TEXT AREA ====================
        int idx = getActiveScriptIndex();
        GuiScriptTextArea activeArea = getActiveScriptArea();
        if (activeArea == null) {
            activeArea = new GuiScriptTextArea(this, 2, editorX, editorY, editorWidth, editorHeight,
                container == null ? "" : container.script);
            activeArea.setListener(this);
            this.closeOnEsc(activeArea::closeOnEsc);
            textAreas.put(idx, activeArea);
        } else {
            activeArea.init(editorX, editorY, editorWidth, editorHeight,
                container == null ? "" : container.script);
        }

        activeArea.setLanguage(script.getLanguage());
        activeArea.setScriptContext(getScriptContext());
        
        // Set editor globals based on the active NPC hook
        String hookName = EnumScriptType.values()[activeTab].function;
        applyEditorGlobals(activeArea, hookName);

        // Setup fullscreen key binding
        GuiScriptTextArea.KEYS.FULLSCREEN.setTask(e -> {
            if (e.isPress()) {
                toggleFullscreen();
            }
        });

        activeArea.enableCodeHighlighting();
        addTextField(activeArea);

        // Initialize fullscreen button
        int scrollbarOffset = activeArea.hasVerticalScrollbar() ? -8 : -2;
        fullscreenButton.initGui(editorX + editorWidth, editorY, scrollbarOffset);

        // ==================== RIGHT PANEL (hidden in fullscreen) ====================
        if (!isFullscreen) {
            addButton(new GuiNpcButton(102, guiLeft + 315, guiTop + 4, 50, 20, "gui.clear"));
            addButton(new GuiNpcButton(101, guiLeft + 366, guiTop + 4, 50, 20, "gui.paste"));
            addButton(new GuiNpcButton(100, guiLeft + 315, guiTop + 25, 50, 20, "gui.copy"));
            addButton(new GuiNpcButton(107, guiLeft + 315, guiTop + 70, 80, 20, "script.loadscript"));

            GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setUnselectable();
            scroll.setSize(100, 120);
            scroll.guiLeft = guiLeft + 315;
            scroll.guiTop = guiTop + 92;
            if (container != null)
                scroll.setList(container.scripts);
            addScroll(scroll);
        }
    }


    private void initSettingsView() {
        addLabel(new GuiNpcLabel(0, "script.console", guiLeft + 4, guiTop + 16));
        if (getTopButton(14) != null)
            getTopButton(14).active = true;

        GuiNpcTextArea consoleArea = new GuiNpcTextArea(2, this, guiLeft + 4, guiTop + 26, 226, 186, getConsoleText());
        consoleArea.canEdit = false;
        addTextField(consoleArea);

        addButton(new GuiNpcButton(100, guiLeft + 232, guiTop + 170, 56, 20, "gui.copy"));
        addButton(new GuiNpcButton(102, guiLeft + 232, guiTop + 192, 56, 20, "gui.clear"));

        List<String> consoleOptions = new ArrayList<>();
        consoleOptions.add("All");
        consoleOptions.addAll(hookList);
        addButton(new GuiNpcButton(105, guiLeft + 60, guiTop + 4, 80, 20,
            consoleOptions.toArray(new String[0]), activeConsole));

        addLabel(new GuiNpcLabel(1, "script.language", guiLeft + 232, guiTop + 30));
        List<String> languageOptions = getLanguageOptions();
        addButton(new GuiNpcButton(103, guiLeft + 294, guiTop + 25, 80, 20,
            languageOptions.toArray(new String[0]), getLanguageIndex(languageOptions)));
        getButton(103).enabled = languageOptions.size() > 0;

        addLabel(new GuiNpcLabel(2, "gui.enabled", guiLeft + 232, guiTop + 53));
        addButton(new GuiNpcButton(104, guiLeft + 294, guiTop + 48, 50, 20,
            new String[]{"gui.no", "gui.yes"}, script.enabled ? 1 : 0));

        if (MinecraftServer.getServer() != null)
            addButton(new GuiNpcButton(106, guiLeft + 232, guiTop + 71, 150, 20, "script.openfolder"));
    }

    // Apply editor globals for the active NPC hook.
    private void applyEditorGlobals(GuiScriptTextArea activeArea, String hookName) {
        if (activeArea == null) 
            return;
        
        ScriptTextContainer textContainer = activeArea.getContainer();
        if (textContainer == null) 
            return;
        
        if (script != null) 
            textContainer.setEditorGlobalsMap(script.getEditorGlobals(hookName));
    }

    @Override
    protected String getConsoleText() {
        Map<Long, String> map = this.script.getOldConsoleText();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, String> entry : map.entrySet()) {
            builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n");
        }
        return builder.toString();
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 0) {
            openLink("https://kamkeel.github.io/CustomNPC-Plus/");
        }
        if (id == 101) {
            getTextField(2).setText(NoppesStringUtils.getClipboardContents());
        }
        if (id == 102) {
            getTextField(2).setText("");
            if (!showScript) {
                if (activeConsole == 0) {
                    for (ScriptContainer container : script.getNPCScripts())
                        container.console.clear();
                } else {
                    ScriptContainer container = script.getNPCScript(activeConsole - 1);
                    if (container != null)
                        container.console.clear();
                }
            }
        }
        displayGuiScreen(this);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 13) {
            showScript = true;
            initGui();
        }
        if (guibutton.id == 14) {
            setScript();
            showScript = false;
            initGui();
        }
        if (guibutton.id == 15) {
            displayGuiScreen(new GuiConfirmOpenLink(this, "https://kamkeel.github.io/CustomNPC-Plus/", 0, true));
        }
        if (guibutton.id == 16) {
            close();
            GuiScriptInterface.open(this, this.script);
        }
        if (guibutton.id == 17) {
            close();
        }
        if (guibutton.id == 100) {
            NoppesStringUtils.setClipboardContents(getTextField(2).getText());
        }
        if (guibutton.id == 101) {
            displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("gui.paste"),
                StatCollector.translateToLocal("gui.sure"), 101));
        }
        if (guibutton.id == 102) {
            displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("gui.clear"),
                StatCollector.translateToLocal("gui.sure"), 102));
        }
        if (guibutton.id == 103) {
            script.scriptLanguage = ((GuiNpcButton) guibutton).displayString;
        }
        if (guibutton.id == 104) {
            script.enabled = ((GuiNpcButton) guibutton).getValue() == 1;
        }
        if (guibutton.id == 105) {
            activeConsole = ((GuiNpcButton) guibutton).getValue();
            initGui();
        }
        if (guibutton.id == 106) {
            NoppesUtil.openFolder(ScriptController.Instance.dir);
        }
        if (guibutton.id == 107) {
            ScriptContainer container = getCurrentContainer();
            if (container == null)
                script.setNPCScript(activeTab, container = new ScriptContainer(this.script));
            setSubGui(new GuiScriptList(languages.get(script.scriptLanguage), container));
        }
    }

    @Override
    protected void setScript() {
        if (showScript) {
            ScriptContainer container = getCurrentContainer();
            if (container == null)
                script.setNPCScript(activeTab, container = new ScriptContainer(this.script));
            String text = getTextField(2).getText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            container.script = text;
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        script.readFromNBT(compound);
        loadLanguagesData(compound);
        loaded = true;
    }

    @Override
    public void save() {
        if (loaded) {
            setScript();
            NPCScriptPacket.Save(script.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 1) {
            setScript();
            activeTab = scroll.selected;
            initGui();
        }
    }
}
