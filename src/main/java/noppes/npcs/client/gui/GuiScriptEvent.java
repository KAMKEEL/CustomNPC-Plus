package noppes.npcs.client.gui;

import net.minecraft.server.MinecraftServer;
import noppes.npcs.EventDataScript;
import noppes.npcs.EventScriptContainer;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.ScriptContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiScriptEvent extends GuiNPCInterface {
    public boolean showScript = false;
    private int activeTab = 0;
    private String activeScriptTab = "init";
    public EventDataScript script;
    public Map<String,List<String>> languages = new HashMap<String,List<String>>();

    private static int activeConsole = 0;

    public GuiScriptEvent(){
        super();
        drawDefaultBackground = true;
        closeOnEsc = true;
        xSize = 420;
        setBackground("menubg.png");

        //Client.sendData(EnumPacketServer.RemoteNpcsGet);  TODO: Get Player & Forge event scripts
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
        list.add("playerscript.break");
        list.add("playerscript.chat");
        list.add("playerscript.containerclosed");
        list.add("playerscript.containeropen");
        list.add("playerscript.damagedentity");
        list.add("playerscript.damaged");
        list.add("playerscript.killed");
        list.add("playerscript.factionupdate");
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
            hooks.selected = script.getScripts().indexOf(activeScriptTab);
            addScroll(hooks);

            EventScriptContainer container = script.getScripts().get(script.getScripts().indexOf(activeScriptTab));

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
            //addButton(new GuiNpcButton(104, guiLeft + 294, guiTop + 48, 50, 20, new String[]{"gui.no","gui.yes"}, script.enabled?1:0));

            if(MinecraftServer.getServer() != null)
                addButton(new GuiNpcButton(106, guiLeft + 232, guiTop + 71, 150, 20, "script.openfolder"));
        }
    }

    private int getScriptIndex() {
        /*
        int i = 0;
        for(String language : languages.keySet()){
            if(language.equalsIgnoreCase(script.scriptLanguage))
                return i;
            i++;
        }
        */
        return 0;
    }

    private String getConsoleText() {
        String console = "";
        /*
        if(activeConsole == 0){
            for(ScriptContainer container : script.scripts.values()){
                if(!container.console.isEmpty())
                    console += container.console + '\n';
            }
        }
        else{
            ScriptContainer container = script.scripts.get(activeConsole - 1);
            if(container != null)
                console = container.console;
        }
        */
        return console;
    }

    @Override
    public void save() {

    }
}
