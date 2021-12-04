//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.client.gui.script;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

import com.google.common.reflect.ClassPath;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.swing.GuiJTextArea;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.PlayerDataScript;
import org.apache.commons.lang3.StringUtils;

public class GuiScriptInterface extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ITextChangeListener, ICustomScrollListener, IJTextAreaListener, ITextfieldListener {
    private int activeTab = 0;
    public IScriptHandler handler;
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

        int topXoffset = 0;
        int topYoffset = 0;
        for(int ta = 0; ta < this.handler.getScripts().size(); ++ta) {
            if(ta%20 == 0 && ta > 0) {
                topYoffset -= 20;
                topXoffset -= top.width+20*22;
            }
            this.addTopButton(top = new GuiMenuTopButton(ta + 1, top.xPosition+top.width + topXoffset, top.yPosition + topYoffset, ta + 1 + ""));
            topXoffset = 0;
            topYoffset = 0;
            scriptLimit = ta + 2;
        }

        if(this.handler.getScripts().size() < 100)
            this.addTopButton(new GuiMenuTopButton(scriptLimit, top.xPosition+top.width, top.yPosition, "+"));

        top = this.getTopButton(this.activeTab);
        if(top == null) {
            this.activeTab = 0;
            top = this.getTopButton(0);
        }

        top.active = true;
        if(this.activeTab > 0) {
            List<String> hookList = new ArrayList<String>();

            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.allowUserInput = false;
            hooks.setSize(108, 198);
            hooks.guiLeft = guiLeft - 110;
            hooks.guiTop = guiTop + 14;

            if(handler instanceof PlayerDataScript) {
                hookList.add("init");
                hookList.add("tick");
                hookList.add("interact");
                hookList.add("damagedEntity");
                hookList.add("damaged");
                hookList.add("kills");
                hookList.add("killed");
                hookList.add("drop");
                hookList.add("respawn");
                hookList.add("breakBlock");
                hookList.add("chat");
                hookList.add("login");
                hookList.add("logout");
                hookList.add("keyPressed");
                hookList.add("mouseClicked");
                hookList.add("toss");
                hookList.add("pickUp");
                hookList.add("pickupXP");
                hookList.add("rangedCharge");
                hookList.add("rangedLaunched");
                hookList.add("timer");
                hookList.add("startItem");
                hookList.add("usingItem");
                hookList.add("stopItem");
                hookList.add("finishItem");
                hookList.add("containerOpen");
                hookList.add("useHoe");
                hookList.add("bonemeal");
                hookList.add("fillBucket");
                hookList.add("jump");
                hookList.add("fall");
                hookList.add("wakeUp");
                hookList.add("sleep");
                hookList.add("playSound");
                hookList.add("lightning");
                hookList.add("changedDim");
            }
            else if(handler instanceof ForgeDataScript) {
                hooks.setSize(238, 198);
                hooks.guiLeft = guiLeft - 240;

                hookList.add("init");

                ArrayList<ClassPath.ClassInfo> list = new ArrayList();
                try {
                    list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.event"));
                    list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.gameevent"));
                    list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for(ClassPath.ClassInfo e1 : list){
                    ClassPath.ClassInfo classLoader = e1;
                    Class infoClass = classLoader.load();
                    ArrayList c = new ArrayList(Arrays.asList(infoClass.getDeclaredClasses()));
                    if(c.isEmpty()) {
                        c.add(infoClass);
                    }
                    Iterator var10 = c.iterator();
                    while(var10.hasNext()) {
                        Class c1 = (Class) var10.next();
                        if (!EntityEvent.EntityConstructing.class.isAssignableFrom(c1) && !WorldEvent.PotentialSpawns.class.isAssignableFrom(c1) && !TickEvent.RenderTickEvent.class.isAssignableFrom(c1) && !TickEvent.ClientTickEvent.class.isAssignableFrom(c1) && !FMLNetworkEvent.ClientCustomPacketEvent.class.isAssignableFrom(c1) && !ItemTooltipEvent.class.isAssignableFrom(c1) && Event.class.isAssignableFrom(c1) && !Modifier.isAbstract(c1.getModifiers()) && Modifier.isPublic(c1.getModifiers())) {
                            String eventName = c1.getName();
                            int i = eventName.lastIndexOf(".");
                            eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));

                            hookList.add(eventName);
                        }
                    }
                }
            }
            hooks.setUnsortedList(hookList);
            addScroll(hooks);

            GuiNpcLabel hookLabel = new GuiNpcLabel(0, "script.hooks", hooks.guiLeft, guiTop + 5);
            hookLabel.color = 0xaaaaaa;
            addLabel(hookLabel);

            ScriptContainer var7 = (ScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            GuiNpcTextArea left = new GuiNpcTextArea(2, this, this.guiLeft + yoffset, this.guiTop + yoffset, this.xSize - 110 - yoffset, (int)((double)this.ySize * 0.96D) - yoffset * 2, var7 == null?"":var7.script);
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

    public String previousHookClicked = "";
    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        String hook = scroll.getSelected();
        if(previousHookClicked.equals(hook)){
            String addString = "";
            if(!this.getTextField(2).getText().isEmpty())
                addString += "\n";
            addString += "function "+hook+"(event) {\n    \n}\n";

            this.getTextField(2).setText(this.getTextField(2).getText()+addString);
            previousHookClicked = "";
        } else {
            previousHookClicked = hook;
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        /*if(textfield.id == 1){
            this.handler.getScripts().get(this.activeTab - 1).setType(textfield.getText());
        }*/
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
            this.handler.getScripts().add(new ScriptContainer(this.handler));
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

        ScriptContainer container;
        if(guibutton.id == 102) {
            if(this.activeTab > 0) {
                container = (ScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
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
            container = (ScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if(container == null) {
                this.handler.getScripts().add(container = new ScriptContainer(this.handler));
            }

            this.setSubGui(new EventGuiScriptList((List)this.languages.get(this.handler.getLanguage()), container));
        }

        if(guibutton.id == 108) {
            container = (ScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if(container != null) {
                this.setScript();
                this.AWTWindow = new GuiJTextArea(container.script).setListener(this);
            }
        }

    }

    private void setScript() {
        if(this.activeTab > 0) {
            ScriptContainer container = (ScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
            if(container == null) {
                this.handler.getScripts().add(container = new ScriptContainer(this.handler));
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
        ScriptContainer container = (ScriptContainer)this.handler.getScripts().get(this.activeTab - 1);
        if(container != null) {
            container.script = text;
        }

    }

    @Override
    public void saveText(String text) {
        ScriptContainer container = handler.getScripts().get(activeTab);
        if(container != null)
            container.script = text;
        initGui();
    }
}