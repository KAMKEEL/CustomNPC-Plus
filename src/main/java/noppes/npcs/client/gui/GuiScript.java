package noppes.npcs.client.gui;

import kamkeel.npcs.network.packets.request.script.NPCScriptPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.script.GuiNPCEventScripts;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiScriptTextArea;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IJTextAreaListener;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GuiScript extends GuiNPCInterface implements IGuiData, GuiYesNoCallback, ICustomScrollListener, IJTextAreaListener, ITextChangeListener {
    public boolean showScript = false;
    private int activeTab = 0;
    public DataScript script;
    public Map<String, List<String>> languages = new HashMap<String, List<String>>();

    private static int activeConsole = 0;
    boolean loaded = false;

    public GuiScript(EntityNPCInterface npc) {
        super(npc);
        script = npc.script;
        drawDefaultBackground = true;
        closeOnEsc = true;
        xSize = 420;

        setBackground("menubg.png");
        NPCScriptPacket.Get();
    }

    public void initGui() {
        super.initGui();
        guiTop += 10;
        GuiMenuTopButton top;
        addTopButton(top = new GuiMenuTopButton(13, guiLeft + 4, guiTop - 17, "script.scripts"));
        addTopButton(new GuiMenuTopButton(16, guiLeft + (xSize - 102), guiTop - 17, "eventscript.eventScripts"));
        addTopButton(new GuiMenuTopButton(17, guiLeft + (xSize - 22), guiTop - 17, "X"));
        top.active = showScript;
        addTopButton(top = new GuiMenuTopButton(14, top, "gui.settings"));
        top.active = !showScript;
        addTopButton(new GuiMenuTopButton(15, top, "gui.website"));

        List<String> list = new ArrayList<String>();
        list.add("script.init");
        list.add("script.update");
        list.add("script.interact");
        list.add("dialog.dialog");
        list.add("script.damaged");
        list.add("script.killed");
        list.add("script.attack");
        list.add("script.target");
        list.add("script.collide");
        list.add("script.kills");
        list.add("script.dialog_closed");
        list.add("script.timer");
        list.add("script.targetLost");
        list.add("script.projectileTick");
        list.add("script.projectileImpact");

        if (showScript) {
            addLabel(new GuiNpcLabel(0, "script.hooks", guiLeft + 4, guiTop + 5));
            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.setSize(68, 198);
            hooks.guiLeft = guiLeft + 4;
            hooks.guiTop = guiTop + 14;
            hooks.setUnsortedList(list);
            hooks.selected = activeTab;
            addScroll(hooks);

            ScriptContainer container = script.getNPCScript(activeTab);

            GuiScriptTextArea ta = new GuiScriptTextArea(this, 2, guiLeft + 74, guiTop + 4, 239, 208, container == null ? "" : container.script);
            ta.enableCodeHighlighting();
            ta.setListener(this);
            this.addTextField(ta);

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
        } else {
            addLabel(new GuiNpcLabel(0, "script.console", guiLeft + 4, guiTop + 16));
            getTopButton(14).active = true;
            addTextField(new GuiNpcTextArea(2, this, guiLeft + 4, guiTop + 26, 226, 186, getConsoleText()));
            getTextField(2).canEdit = false;
            addButton(new GuiNpcButton(100, guiLeft + 232, guiTop + 170, 56, 20, "gui.copy"));
            addButton(new GuiNpcButton(102, guiLeft + 232, guiTop + 192, 56, 20, "gui.clear"));

            List<String> l = new ArrayList<String>();
            l.add("All");
            l.addAll(list);
            addButton(new GuiNpcButton(105, guiLeft + 60, guiTop + 4, 80, 20, l.toArray(new String[l.size()]), activeConsole));

            addLabel(new GuiNpcLabel(1, "script.language", guiLeft + 232, guiTop + 30));
            addButton(new GuiNpcButton(103, guiLeft + 294, guiTop + 25, 80, 20, languages.keySet().toArray(new String[languages.keySet().size()]), getScriptIndex()));
            getButton(103).enabled = languages.size() > 0;

            addLabel(new GuiNpcLabel(2, "gui.enabled", guiLeft + 232, guiTop + 53));
            addButton(new GuiNpcButton(104, guiLeft + 294, guiTop + 48, 50, 20, new String[]{"gui.no", "gui.yes"}, script.enabled ? 1 : 0));

            if (MinecraftServer.getServer() != null)
                addButton(new GuiNpcButton(106, guiLeft + 232, guiTop + 71, 150, 20, "script.openfolder"));
        }
    }

    private int getScriptIndex() {
        int i = 0;
        for (String language : languages.keySet()) {
            if (language.equalsIgnoreCase(script.scriptLanguage))
                return i;
            i++;
        }
        return 0;
    }

    private String getConsoleText() {
        Map<Long, String> map = this.script.getOldConsoleText();
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<Long, String> entry = (Map.Entry) var3.next();
            builder.insert(0, new Date((Long) entry.getKey()) + (String) entry.getValue() + "\n");
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
            GuiConfirmOpenLink guiyesno = new GuiConfirmOpenLink(this, "https://kamkeel.github.io/CustomNPC-Plus/", 0, true);
            mc.displayGuiScreen(guiyesno);
        }
        if (guibutton.id == 16) {
            close();
            mc.displayGuiScreen(new GuiNPCEventScripts(npc));
        }
        if (guibutton.id == 17) {
            close();
        }
        if (guibutton.id == 100) {
            NoppesStringUtils.setClipboardContents(getTextField(2).getText());
        }
        if (guibutton.id == 101) {
            GuiYesNo guiyesno = new GuiYesNo(this, StatCollector.translateToLocal("gui.paste"), StatCollector.translateToLocal("gui.sure"), 101);
            displayGuiScreen(guiyesno);
        }
        if (guibutton.id == 102) {
            GuiYesNo guiyesno = new GuiYesNo(this, StatCollector.translateToLocal("gui.clear"), StatCollector.translateToLocal("gui.sure"), 102);
            displayGuiScreen(guiyesno);
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
            // TODO: Uses ScriptController.Instance.dir (shared script repository); this GUI is gated behind the scripter tool
            //       and CustomNpcsPermissions.TOOL_SCRIPTER so only authorized editors touch the controller-backed scripts.
            NoppesUtil.openFolder(ScriptController.Instance.dir);
        }
        if (guibutton.id == 107) {
            ScriptContainer container = script.getNPCScript(activeTab);
            if (container == null)
                script.setNPCScript(activeTab, container = new ScriptContainer(this.script));
            setSubGui(new GuiScriptList(languages.get(script.scriptLanguage), container));
        }
    }

    private void setScript() {
        if (showScript) {
            ScriptContainer container = script.getNPCScript(activeTab);
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
        NBTTagList data = compound.getTagList("Languages", 10);
        Map<String, List<String>> languages = new HashMap<String, List<String>>();
        for (int i = 0; i < data.tagCount(); i++) {
            NBTTagCompound comp = data.getCompoundTagAt(i);
            List<String> scripts = new ArrayList<String>();
            NBTTagList list = comp.getTagList("Scripts", 8);
            for (int j = 0; j < list.tagCount(); j++) {
                scripts.add(list.getStringTagAt(j));
            }
            languages.put(comp.getString("Language"), scripts);
        }
        this.languages = languages;
        initGui();
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

    public void textUpdate(String text) {
        ScriptContainer container = script.getNPCScript(activeTab);
        if (container != null)
            container.script = text;
    }

    @Override
    public void saveText(String text) {
        ScriptContainer container = script.getNPCScript(activeTab);
        if (container != null)
            container.script = text;
        initGui();
    }

}

