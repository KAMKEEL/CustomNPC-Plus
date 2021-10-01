package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.EventDataScript;
import noppes.npcs.EventScriptContainer;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.EventGuiScriptList;
import noppes.npcs.client.gui.swing.GuiJTextArea;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerScriptData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiScriptEvent extends GuiNPCInterface implements IGuiData, GuiYesNoCallback, ICustomScrollListener, IJTextAreaListener {
    public boolean showScript = false;
    private int activeTab = 0;
    private String activeScriptTab = "init";
    private PlayerScriptData script = new PlayerScriptData((EntityPlayer)null);
    public Map<String,List<String>> languages = new HashMap<String,List<String>>();

    private static int activeConsole = 0;

    public GuiScriptEvent(){
        super();
        drawDefaultBackground = true;
        closeOnEsc = true;
        xSize = 420;

        setBackground("menubg.png");
        Client.sendData(EnumPacketServer.ScriptPlayerGet);
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
        int i = 0;
        for(String language : languages.keySet()){
            if(language.equalsIgnoreCase(script.getLanguage()))
                return i;
            i++;
        }
        return 0;
    }

    private String getConsoleText() {
        String console = "";
        if(activeConsole == 0){
            for(EventScriptContainer container : script.scripts){
                if(!container.console.isEmpty())
                    for(Long l : container.console.keySet())
                    console += container.console.get(l) + '\n';
            }
        }
        else{
            EventScriptContainer container = script.scripts.get(activeConsole - 1);
            if(container != null)
                console = container.console.toString();
        }
        return console;
    }

    @Override
    public void confirmClicked(boolean flag, int i){
        if(flag)
            openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
        displayGuiScreen(this);
    }

    @Override
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
                    for(EventScriptContainer container : script.scripts)
                        for(Long l : container.console.keySet())
                            container.console.put(l,"");
                }
                else{
                    System.out.println("test");
                    EventScriptContainer container = script.scripts.get(activeConsole - 1);
                    System.out.println("test2");
                    if(container != null)
                        for(Long l : container.console.keySet())
                            container.console.put(l,"");
                }
            }
        }
        if (guibutton.id == 103) {
            script.setLanguage(((GuiNpcButton)guibutton).displayString);
        }
        if (guibutton.id == 104) {
            script.setEnabled(((GuiNpcButton)guibutton).getValue() == 1);
        }
        if (guibutton.id == 105) {
            activeConsole = ((GuiNpcButton)guibutton).getValue();
            initGui();
        }
        if (guibutton.id == 106) {
            NoppesUtil.openFolder(ScriptController.Instance.dir);
        }
        if (guibutton.id == 107) {
            EventScriptContainer container = script.scripts.get(activeTab);
            if(container == null)
                script.scripts.set(activeTab, container = new EventScriptContainer(new EventDataScript()));
            setSubGui(new EventGuiScriptList(languages.get(script.getLanguage()), container));
        }
        if (guibutton.id == 108) {
            EventScriptContainer container = script.scripts.get(activeTab);
            if(container != null){
                setScript();
                this.AWTWindow = new GuiJTextArea(container.script).setListener(this);
            }
        }
    }

    private void setScript(){
        if(showScript){
            EventScriptContainer container = script.scripts.get(activeTab);
            if(container == null)
                script.scripts.set(activeTab, container = new EventScriptContainer(new EventDataScript()));
            String text = getTextField(2).getText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
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
        EventScriptContainer container = script.scripts.get(activeTab);
        if(container != null)
            container.script = text;
        initGui();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        script.readFromNBT(compound);
        NBTTagList data = compound.getTagList("Languages", 10);
        Map<String,List<String>> languages = new HashMap<String,List<String>>();
        for(int i = 0; i < data.tagCount(); i++){
            NBTTagCompound comp = data.getCompoundTagAt(i);
            List<String> scripts = new ArrayList<String>();
            NBTTagList list = comp.getTagList("Scripts", 8);
            for(int j = 0; j < list.tagCount(); j++){
                scripts.add(list.getStringTagAt(j));
            }
            languages.put(comp.getString("Language"), scripts);
        }
        this.languages = languages;
        initGui();
    }

    @Override
    public void save() {
        Client.sendData(EnumPacketServer.ScriptPlayerSave, new Object[]{this.script.writeToNBT(new NBTTagCompound())});
    }
}
