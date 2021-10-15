//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.PlayerScriptContainer;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.swing.GuiJTextArea;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerDataScript;

public class GuiScriptInterface extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ITextChangeListener, ICustomScrollListener, IJTextAreaListener {
    private int activeTab = 0;
    public PlayerDataScript handler;
    public Map<String, List<String>> languages = new HashMap();
    private int scriptLimit = 1;

    public GuiScriptInterface() {
        this.drawDefaultBackground = true;
        this.closeOnEsc = true;
        this.xSize = 420;
        this.setBackground("menubg.png");
    }

    public void initGui() {
        //this.xSize = (int)((double)this.width * 0.88D);
        this.ySize = (int)((double)this.xSize * 0.56D);
        if((double)this.ySize > (double)this.height * 0.95D) {
            this.ySize = (int)((double)this.height * 0.95D);
            this.xSize = (int)((double)this.ySize / 0.56D);
        }

        this.bgScale = (float)this.xSize / 400.0F;
        super.initGui();
        this.guiTop += 10;
        int yoffset = (int)((double)this.ySize * 0.02D);
        GuiMenuTopButton top;
        this.addTopButton(top = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.settings"));

        List<String> hookList = new ArrayList<String>();
        hookList.add("playerscript.disabled");
        hookList.add("playerscript.init");
        hookList.add("playerscript.tick");
        hookList.add("playerscript.interact");
        hookList.add("playerscript.damaged");
        hookList.add("playerscript.killed");
        hookList.add("playerscript.kills");
        hookList.add("playerscript.chat");
        hookList.add("playerscript.drop");
        hookList.add("playerscript.containerOpen");
        hookList.add("playerscript.login");
        hookList.add("playerscript.logout");
        hookList.add("playerscript.chat");
        hookList.add("playerscript.damagedentity");
        hookList.add("playerscript.keyPressed");
        hookList.add("playerscript.mouseClicked");
        hookList.add("playerscript.pickUp");
        hookList.add("playerscript.rangedLaunched");
        hookList.add("playerscript.pickupXP");
        hookList.add("playerscript.timer");
        hookList.add("playerscript.startItem");
        hookList.add("playerscript.usingItem");
        hookList.add("playerscript.stopItem");
        hookList.add("playerscript.finishItem");
        hookList.add("playerscript.fall");
        hookList.add("playerscript.playSound");
        hookList.add("playerscript.lightning");
        hookList.add("playerscript.break");
        hookList.add("playerscript.changedDim");
        hookList.add("playerscript.respawn");

        int topXoffset = 0;
        int topYoffset = 0;
        for(int ta = 0; ta < this.handler.getScripts().size(); ++ta) {
            if(ta%15 == 0 && ta > 0) {
                topYoffset -= 20;
                topXoffset -= top.width+260;
            }
            this.addTopButton(top = new GuiMenuTopButton(ta + 1, top.xPosition+top.width + topXoffset, top.yPosition + topYoffset, ta + 1 + ""));
            topXoffset = 0;
            topYoffset = 0;
            scriptLimit = ta + 2;
        }

        if(this.handler.getScripts().size() < 75)
            this.addTopButton(new GuiMenuTopButton(scriptLimit, top.xPosition+top.width, top.yPosition, "+"));

        top = this.getTopButton(this.activeTab);
        if(top == null) {
            this.activeTab = 0;
            top = this.getTopButton(0);
        }

        top.active = true;
        if(this.activeTab > 0) {
            addLabel(new GuiNpcLabel(0, "script.hooks", guiLeft + 4, guiTop + 5));
            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.setSize(68, 198);
            hooks.guiLeft = guiLeft + 4;
            hooks.guiTop = guiTop + 14;
            hooks.setUnsortedList(hookList);
            hooks.selected = getScriptHook(this.handler.getScripts().get(this.activeTab - 1),hookList);
            addScroll(hooks);

            PlayerScriptContainer var7 = (PlayerScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            GuiNpcTextArea left = new GuiNpcTextArea(2, this, this.guiLeft + 72 + yoffset, this.guiTop + yoffset, this.xSize - 180 - yoffset, (int)((double)this.ySize * 0.96D) - yoffset * 2, var7 == null?"":var7.script);
            this.addTextField(left);
            int left1 = this.guiLeft + this.xSize - 104;
            this.addButton(new GuiNpcButton(102, left1, this.guiTop + yoffset, 60, 20, "gui.clear"));
            this.addButton(new GuiNpcButton(101, left1 + 61, this.guiTop + yoffset, 60, 20, "gui.paste"));
            this.addButton(new GuiNpcButton(100, left1, this.guiTop + 21 + yoffset, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(105, left1 + 61, this.guiTop + 21 + yoffset, 60, 20, "gui.remove"));
            this.addButton(new GuiNpcButton(107, left1, this.guiTop + 66 + yoffset, 80, 20, "script.loadscript"));
            GuiCustomScroll scroll = (new GuiCustomScroll(this, 0)).setUnselectable();
            scroll.setSize(100, (int)((double)this.ySize * 0.54D) - yoffset * 2);
            scroll.guiLeft = left1;
            scroll.guiTop = this.guiTop + 88 + yoffset;
            if(var7 != null) {
                scroll.setList(var7.scripts);
            }

            this.addScroll(scroll);
        } else {
            GuiNpcTextArea var8 = new GuiNpcTextArea(2, this, this.guiLeft + 4 + yoffset, this.guiTop + 6 + yoffset, this.xSize - 160 - yoffset, (int)((float)this.ySize * 0.92F) - yoffset * 2, this.getConsoleText());
            var8.enabled = false;
            this.addTextField(var8);
            int var9 = this.guiLeft + this.xSize - 150;
            this.addButton(new GuiNpcButton(100, var9, this.guiTop + 125, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(102, var9, this.guiTop + 146, 60, 20, "gui.clear"));
            this.addLabel(new GuiNpcLabel(1, "script.language", var9, this.guiTop + 15));
            this.addButton(new GuiNpcButton(103, var9 + 60, this.guiTop + 10, 80, 20, (String[])this.languages.keySet().toArray(new String[this.languages.keySet().size()]), this.getScriptIndex()));
            this.getButton(103).enabled = this.languages.size() > 0;
            this.addLabel(new GuiNpcLabel(2, "gui.enabled", var9, this.guiTop + 36));
            this.addButton(new GuiNpcButton(104, var9 + 60, this.guiTop + 31, 50, 20, new String[]{"gui.no", "gui.yes"}, this.handler.getEnabled()?1:0));
            if(this.player.worldObj.isRemote) {
                this.addButton(new GuiNpcButton(106, var9, this.guiTop + 55, 150, 20, "script.openfolder"));
            }

            this.addButton(new GuiNpcButton(109, var9, this.guiTop + 78, 80, 20, "gui.website"));
            this.addButton(new GuiNpcButton(112, var9 + 81, this.guiTop + 78, 80, 20, "gui.forum"));
            this.addButton(new GuiNpcButton(110, var9, this.guiTop + 99, 80, 20, "script.apidoc"));
            this.addButton(new GuiNpcButton(111, var9 + 81, this.guiTop + 99, 80, 20, "script.apisrc"));
        }

        this.xSize = 420;
        this.ySize = 256;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        this.handler.getScripts().get(this.activeTab - 1).setType(scroll.getSelected().replace("playerscript.",""));
    }

    private int getScriptHook(PlayerScriptContainer playerScriptContainer, List<String> list) {
        for(String s : list) {
            if (s.replace("playerscript.", "").equals(playerScriptContainer.type)) {
                return list.indexOf(s);
            }
        }
        return 0;
    }

    private String getConsoleText() {
        Map map = this.handler.getConsoleText();
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Entry entry = (Entry)var3.next();
            builder.insert(0, new Date(((Long)entry.getKey()).longValue()) + (String)entry.getValue() + "\n");
        }

        return builder.toString();
    }

    private int getScriptIndex() {
        int i = 0;

        for(Iterator var2 = this.languages.keySet().iterator(); var2.hasNext(); ++i) {
            String language = (String)var2.next();
            if(language.equalsIgnoreCase(this.handler.getLanguage())) {
                return i;
            }
        }

        return 0;
    }

    public void confirmClicked(boolean flag, int i) {
        if(flag) {
            if(i == 0) {
                this.openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
            }

            if(i == 1) {
                this.openLink("http://www.kodevelopment.nl/customnpcs/api/");
            }

            if(i == 2) {
                this.openLink("http://www.kodevelopment.nl/minecraft/customnpcs/scripting");
            }

            if(i == 3) {
                this.openLink("http://www.minecraftforge.net/forum/index.php/board,122.0.html");
            }

            if(i == 10) {
                this.handler.getScripts().remove(this.activeTab - 1);
                this.activeTab = 0;
            }
        }

        this.displayGuiScreen(this);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if(guibutton.id >= 0 && guibutton.id < scriptLimit) {
            this.setScript();
            this.activeTab = guibutton.id;
            this.initGui();
        }

        if(guibutton.id == scriptLimit) {
            this.handler.getScripts().add(new PlayerScriptContainer(this.handler));
            this.activeTab = this.handler.getScripts().size();
            this.initGui();
        }

        if(guibutton.id == 109) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.kodevelopment.nl/minecraft/customnpcs/scripting", 0, true));
        }

        if(guibutton.id == 110) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.kodevelopment.nl/customnpcs/api/", 1, true));
        }

        if(guibutton.id == 111) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://github.com/Noppes/CustomNPCsAPI", 2, true));
        }

        if(guibutton.id == 112) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.minecraftforge.net/forum/index.php/board,122.0.html", 3, true));
        }

        if(guibutton.id == 100) {
            NoppesStringUtils.setClipboardContents((this.getTextField(2)).getText());
        }

        if(guibutton.id == 101) {
            (this.getTextField(2)).setText(NoppesStringUtils.getClipboardContents());
        }

        PlayerScriptContainer container;
        if(guibutton.id == 102) {
            if(this.activeTab > 0) {
                container = (PlayerScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
                container.script = "";
            } else {
                this.handler.clearConsole();
            }

            this.initGui();
        }

        if(guibutton.id == 103) {
            this.handler.setLanguage(((GuiNpcButton)guibutton).displayString);
        }

        if(guibutton.id == 104) {
            //ScriptController.Instance.playerScripts.enabled = (((GuiNpcButton)guibutton).getValue() == 1);
            this.handler.setEnabled((((GuiNpcButton)guibutton).getValue() == 1));
        }

        if(guibutton.id == 105) {
            GuiYesNo container1 = new GuiYesNo(this, "", ((GuiNpcButton)guibutton).displayString, 10);
            this.displayGuiScreen(container1);
        }

        if(guibutton.id == 106) {
            NoppesUtil.openFolder(ScriptController.Instance.dir);
        }

        if(guibutton.id == 107) {
            container = (PlayerScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if(container == null) {
                this.handler.getScripts().add(container = new PlayerScriptContainer(this.handler));
            }

            this.setSubGui(new EventGuiScriptList((List)this.languages.get(this.handler.getLanguage()), container));
        }

        if(guibutton.id == 108) {
            container = (PlayerScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if(container != null) {
                this.setScript();
                this.AWTWindow = new GuiJTextArea(container.script).setListener(this);
            }
        }

    }

    private void setScript() {
        if(this.activeTab > 0) {
            PlayerScriptContainer container = (PlayerScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if(container == null) {
                this.handler.getScripts().add(container = new PlayerScriptContainer(this.handler));
            }

            String text = (this.getTextField(2)).getText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            container.script = text;
        }

    }

    public void setGuiData(NBTTagCompound compound) {
        NBTTagList data = compound.getTagList("Languages", 10);
        HashMap languages = new HashMap();

        for(int i = 0; i < data.tagCount(); ++i) {
            NBTTagCompound comp = data.getCompoundTagAt(i);
            ArrayList scripts = new ArrayList();
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
        PlayerScriptContainer container = (PlayerScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
        if(container != null) {
            container.script = text;
        }

    }

    @Override
    public void saveText(String text) {
        PlayerScriptContainer container = handler.getScripts().get(activeTab);
        if(container != null)
            container.script = text;
        initGui();
    }
}
