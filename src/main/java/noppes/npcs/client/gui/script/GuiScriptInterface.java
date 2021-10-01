//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.EventDataScript;
import noppes.npcs.EventScriptContainer;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.EventGuiScriptList;
import noppes.npcs.client.gui.swing.GuiJTextArea;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerScriptData;

public class GuiScriptInterface extends GuiNPCInterface implements IGuiData, ITextChangeListener, GuiYesNoCallback, ICustomScrollListener, IJTextAreaListener {
    private int activeTab = 0;
    public IScriptHandler handler;
    private String activeScriptTab = "init";
    public Map<String, List<String>> languages = new HashMap();

    public boolean showScript = false;
    private static int activeConsole = 0;

    public GuiScriptInterface() {
        this.drawDefaultBackground = true;
        this.closeOnEsc = true;
        this.xSize = 420;
        this.setBackground("menubg.png");
    }

    public void initGui() {
        super.initGui();
        guiTop += 10;
        GuiMenuTopButton top;
        addTopButton(top = new GuiMenuTopButton(13, guiLeft + 4, guiTop - 17, "eventscript.playerevent"));
        top.active = showScript;
        addTopButton(top = new GuiMenuTopButton(14, top, "gui.settings"));
        top.active = !showScript;
        addTopButton(new GuiMenuTopButton(15, top, "gui.website"));

        List<String> list = new ArrayList<String>();
        list.add("playerscript.attack");
        list.add("playerscript.chat");
        list.add("playerscript.containerclosed");
        list.add("playerscript.containeropen");
        list.add("playerscript.damagedentity");
        list.add("playerscript.damaged");
        list.add("playerscript.killed");
        list.add("playerscript.init");
        list.add("playerscript.interact");
        list.add("playerscript.keypressed");
        list.add("playerscript.kills");
        list.add("playerscript.levelup");
        list.add("playerscript.login");
        list.add("playerscript.logout");
        list.add("playerscript.pickup");
        list.add("playerscript.rangedlaunched");
        list.add("playerscript.toss");
        list.add("playerscript.update");

        if(showScript) {
            addLabel(new GuiNpcLabel(0, "script.hooks", guiLeft + 4, guiTop + 5));
            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.setSize(68, 198);
            hooks.guiLeft = guiLeft + 4;
            hooks.guiTop = guiTop + 14;
            hooks.setUnsortedList(list);
            hooks.selected = activeTab;
            addScroll(hooks);

            EventScriptContainer container = handler.getScripts().get(activeTab);

            addTextField(new GuiNpcTextArea(2, this, guiLeft + 74, guiTop + 4, 239, 208, container == null?"":container.script));

            addButton(new GuiNpcButton(102, guiLeft + 315, guiTop + 4, 50, 20, "gui.clear"));
            addButton(new GuiNpcButton(101, guiLeft + 366, guiTop + 4, 50, 20, "gui.paste"));
            addButton(new GuiNpcButton(100, guiLeft + 315, guiTop + 25, 50, 20, "gui.copy"));

            addButton(new GuiNpcButton(108, guiLeft + 315, guiTop + 47, 80, 20, "gui.editor"));

            addButton(new GuiNpcButton(107, guiLeft + 315, guiTop + 70, 80, 20, "script.loadscript"));

            GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setUnselectable();
            scroll.setSize(100, 120);
            scroll.guiLeft = guiLeft + 315;
            scroll.guiTop = guiTop + 92;
            if(container != null)
                scroll.setList(container.scripts);
            addScroll(scroll);
        }
        else{
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
            //addButton(new GuiNpcButton(104, guiLeft + 294, guiTop + 48, 50, 20, new String[]{"gui.no","gui.yes"}, handler.enabled?1:0));

            if(MinecraftServer.getServer() != null)
                addButton(new GuiNpcButton(106, guiLeft + 232, guiTop + 71, 150, 20, "script.openfolder"));
        }
    }

    private String getConsoleText() {
        Map<Long, String> map = this.handler.getConsoleText();
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<Long, String> entry = (Entry)var3.next();
            builder.insert(0, new Date((Long)entry.getKey()) + (String)entry.getValue() + "\n");
        }

        return builder.toString();
    }

    private int getScriptIndex() {
        int i = 0;

        for(Iterator var2 = this.languages.keySet().iterator(); var2.hasNext(); ++i) {
            String language = (String)var2.next();
            if (language.equalsIgnoreCase(this.handler.getLanguage())) {
                return i;
            }
        }

        return 0;
    }

    public void func_73878_a(boolean flag, int i) {
        if (flag) {
            if (i == 0) {
                this.openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
            }

            if (i == 1) {
                this.openLink("http://www.kodevelopment.nl/customnpcs/api/");
            }

            if (i == 2) {
                this.openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
            }

            if (i == 3) {
                this.openLink("http://www.minecraftforge.net/forum/index.php/board,122.0.html");
            }

            if (i == 10) {
                this.handler.getScripts().remove(this.activeTab - 1);
                this.activeTab = 0;
            }
        }

        this.displayGuiScreen(this);
    }

    protected void actionPerformed(GuiButton guibutton) {
        if(guibutton.id == 13){
            showScript = true;
            initGui();
        }
        if(guibutton.id == 14){
            setScript();
            showScript = false;
            initGui();
        }
        if(guibutton.id == 15){
            GuiConfirmOpenLink guiyesno = new GuiConfirmOpenLink(this, "http://www.kodevelopment.nl/minecraft/customnpcs/scripting", 0, true);
            mc.displayGuiScreen(guiyesno);
        }
        if (guibutton.id == 100) {
            NoppesStringUtils.setClipboardContents(getTextField(2).getText());
        }
        if (guibutton.id == 101) {
            getTextField(2).setText(NoppesStringUtils.getClipboardContents());
        }
        if (guibutton.id == 102) {
            getTextField(2).setText("");
            if(!showScript){
                if(activeConsole == 0){
                    for(EventScriptContainer container : handler.getScripts())
                        for(Long l : container.console.keySet())
                            container.console.put(l,"");
                }
                else{
                    System.out.println("test");
                    EventScriptContainer container = handler.getScripts().get(activeConsole - 1);
                    System.out.println("test2");
                    if(container != null)
                        for(Long l : container.console.keySet())
                            container.console.put(l,"");
                }
            }
        }
        if (guibutton.id == 103) {
            handler.setLanguage(((GuiNpcButton)guibutton).displayString);
        }
        if (guibutton.id == 104) {
            handler.setEnabled(((GuiNpcButton)guibutton).getValue() == 1);
        }
        if (guibutton.id == 105) {
            activeConsole = ((GuiNpcButton)guibutton).getValue();
            initGui();
        }
        if (guibutton.id == 106) {
            NoppesUtil.openFolder(ScriptController.Instance.dir);
        }
        if (guibutton.id == 107) {
            EventScriptContainer container = handler.getScripts().get(activeTab);
            if(container == null)
                handler.getScripts().set(activeTab, container = new EventScriptContainer(new EventDataScript()));
            setSubGui(new EventGuiScriptList(languages.get(handler.getLanguage()), container));
        }
        if (guibutton.id == 108) {
            EventScriptContainer container = handler.getScripts().get(activeTab);
            if(container != null){
                setScript();
                this.AWTWindow = new GuiJTextArea(container.script).setListener(this);
            }
        }
    }

    private void setScript() {
        if (this.activeTab > 0) {
            EventScriptContainer container = (EventScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if (container == null) {
                this.handler.getScripts().add(container = new EventScriptContainer(this.handler));
            }

            String text = this.getConsoleText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            container.script = text;
        }

    }

    public void setGuiData(NBTTagCompound compound) {
        NBTTagList data = compound.getTagList("Languages", 10);
        Map<String, List<String>> languages = new HashMap();

        for(int i = 0; i < data.tagCount(); ++i) {
            NBTTagCompound comp = data.getCompoundTagAt(i);
            List<String> scripts = new ArrayList();
            NBTTagList list = comp.getTagList("Scripts", 8);

            for(int j = 0; j < list.tagCount(); ++j) {
                scripts.add(list.getStringTagAt(j));
            }

            languages.put(comp.getString("Language"), scripts);
        }

        this.languages = languages;
        this.initGui();
    }

    public void save() {
        this.setScript();
    }

    public void textUpdate(String text) {
        EventScriptContainer container = (EventScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
        if (container != null) {
            container.script = text;
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if(scroll.id == 1){
            setScript();
            activeTab = scroll.selected;
            initGui();
        }
    }

    @Override
    public void saveText(String text) {
        EventScriptContainer container = handler.getScripts().get(activeTab);
        if(container != null)
            container.script = text;
        initGui();
    }
}
