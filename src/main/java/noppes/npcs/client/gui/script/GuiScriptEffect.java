package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.EffectScriptPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.global.GuiNPCManageEffects;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScriptTextArea;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IJTextAreaListener;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.EffectScript;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GuiScriptEffect extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ITextChangeListener, ICustomScrollListener, IJTextAreaListener, ITextfieldListener {

    private int activeTab = 0;
    public Map<String, List<String>> languages = new HashMap();
    private final int scriptLimit = 1;
    public List<String> hookList = new ArrayList();
    public String previousHookClicked = "";

    public final GuiNPCManageEffects parent;
    public final CustomEffect effect;
    private final EffectScript scriptHandler;
    boolean loaded = false;

    public GuiScriptEffect(GuiNPCManageEffects parent, CustomEffect effect) {
        this.drawDefaultBackground = true;
        this.closeOnEsc = true;
        this.xSize = 420;
        this.setBackground("menubg.png");

        this.parent = parent;
        this.effect = effect;
        this.scriptHandler = new EffectScript();

        for (EffectScript.ScriptType type : EffectScript.ScriptType.values()) {
            this.hookList.add(type.function);
        }

        EffectScriptPacket.Get(effect.id);
    }

    public void initGui() {
        this.ySize = (int) ((double) this.xSize * 0.56);
        if ((double) this.ySize > (double) this.height * 0.95) {
            this.ySize = (int) ((double) this.height * 0.95);
            this.xSize = (int) ((double) this.ySize / 0.56);
        }

        this.bgScale = (float) this.xSize / 400.0F;
        super.initGui();
        this.guiTop += 10;
        int yoffset = (int) ((double) this.ySize * 0.02);
        GuiMenuTopButton top;
        this.addTopButton(top = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.settings"));
        int topXoffset = 0;
        int topYoffset = 0;


        if (this.scriptHandler.container != null) {
            this.addTopButton(top = new GuiMenuTopButton(1, top.xPosition + top.width + topXoffset, top.yPosition + topYoffset, "Script"));

        } else {
            this.addTopButton(new GuiMenuTopButton(this.scriptLimit, top.xPosition + top.width, top.yPosition, "+"));
        }

        top = this.getTopButton(this.activeTab);
        if (top == null) {
            this.activeTab = 0;
            top = this.getTopButton(0);
        }

        top.active = true;
        if (this.activeTab == 1) {
            GuiCustomScroll hooks = new GuiCustomScroll(this, 1);
            hooks.allowUserInput = false;
            hooks.setSize(108, 198);
            hooks.guiLeft = this.guiLeft - 110;
            hooks.guiTop = this.guiTop + 14;

            hooks.setUnsortedList(this.hookList);
            this.addScroll(hooks);
            GuiNpcLabel hookLabel = new GuiNpcLabel(0, "script.hooks", hooks.guiLeft, this.guiTop + 5);
            hookLabel.color = 11184810;
            this.addLabel(hookLabel);
            ScriptContainer container = this.scriptHandler.container;
            GuiScriptTextArea ta = new GuiScriptTextArea(this, 2, this.guiLeft + 1 + yoffset, this.guiTop + yoffset, this.xSize - 108 - yoffset, (int) ((double) this.ySize * 0.96) - yoffset * 2, container == null ? "" : container.script);
            ta.enableCodeHighlighting();
            ta.setListener(this);
            this.addTextField(ta);
            int left1 = this.guiLeft + this.xSize - 104;
            this.addButton(new GuiNpcButton(102, left1, this.guiTop + yoffset, 60, 20, "gui.clear"));
            this.addButton(new GuiNpcButton(101, left1 + 61, this.guiTop + yoffset, 60, 20, "gui.paste"));
            this.addButton(new GuiNpcButton(100, left1, this.guiTop + 21 + yoffset, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(105, left1 + 61, this.guiTop + 21 + yoffset, 60, 20, "gui.remove"));

            this.addButton(new GuiNpcButton(107, left1, this.guiTop + 66 + yoffset, 80, 20, "script.loadscript"));
            this.getButton(107).enabled = false;

            GuiCustomScroll scroll = (new GuiCustomScroll(this, 0)).setUnselectable();
            scroll.setSize(100, (int) ((double) this.ySize * 0.54) - yoffset * 2);
            scroll.guiLeft = left1;
            scroll.guiTop = this.guiTop + 88 + yoffset;
            if (container != null) {
                scroll.setList(container.scripts);
            }

            this.addScroll(scroll);
        } else {
            GuiNpcTextArea var8 = new GuiNpcTextArea(2, this, this.guiLeft + 4 + yoffset, this.guiTop + 6 + yoffset, this.xSize - 160 - yoffset, (int) ((float) this.ySize * 0.92F) - yoffset * 2, this.getConsoleText());
            var8.enabled = false;
            this.addTextField(var8);
            int var9 = this.guiLeft + this.xSize - 150;
            this.addButton(new GuiNpcButton(100, var9, this.guiTop + 125, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(102, var9, this.guiTop + 146, 60, 20, "gui.clear"));
            this.addLabel(new GuiNpcLabel(1, "script.language", var9, this.guiTop + 15));
            this.addButton(new GuiNpcButton(103, var9 + 60, this.guiTop + 10, 80, 20, this.languages.keySet().toArray(new String[this.languages.keySet().size()]), this.getScriptIndex()));
            this.getButton(103).enabled = !this.languages.isEmpty();
            this.addLabel(new GuiNpcLabel(2, "gui.enabled", var9, this.guiTop + 36));
            this.addButton(new GuiNpcButton(104, var9 + 60, this.guiTop + 31, 50, 20, new String[]{"gui.no", "gui.yes"}, this.scriptHandler.getEnabled() ? 1 : 0));

            this.addButton(new GuiNpcButton(109, var9, this.guiTop + 78, 80, 20, "gui.website"));
            this.addButton(new GuiNpcButton(112, var9 + 81, this.guiTop + 78, 80, 20, "gui.forum"));
            this.addButton(new GuiNpcButton(110, var9, this.guiTop + 99, 80, 20, "script.apidoc"));
            this.addButton(new GuiNpcButton(111, var9 + 81, this.guiTop + 99, 80, 20, "script.apisrc"));
        }

        this.xSize = 420;
        this.ySize = 256;
    }

    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        String hook = scroll.getSelected();
        if (this.previousHookClicked.equals(hook)) {
            String addString = "";
            if (!this.getTextField(2).getText().isEmpty()) {
                addString = addString + "\n";
            }

            addString = addString + "function " + hook + "(event) {\n    \n}\n";
            this.getTextField(2).setText(this.getTextField(2).getText() + addString);
            this.previousHookClicked = "";
        } else {
            this.previousHookClicked = hook;
        }

    }

    public void unFocused(GuiNpcTextField textfield) {
    }

    private String getConsoleText() {
        Map<Long, String> map = this.scriptHandler.getConsoleText();
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<Long, String> entry = (Map.Entry) var3.next();
            builder.insert(0, new Date((Long) entry.getKey()) + (String) entry.getValue() + "\n");
        }

        return builder.toString();
    }

    private int getScriptIndex() {
        int i = 0;

        for (Iterator var2 = this.languages.keySet().iterator(); var2.hasNext(); ++i) {
            String language = (String) var2.next();
            if (language.equalsIgnoreCase(this.scriptHandler.getLanguage())) {
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
                this.scriptHandler.container = null;
                this.activeTab = 0;
            }

            if (i == 101) {
                this.getTextField(2).setText(NoppesStringUtils.getClipboardContents());
                this.setScript();
            }

            if (i == 102) {
                if (this.activeTab == 1) {
                    ScriptContainer container = this.scriptHandler.container;
                    container.script = "";
                } else {
                    this.scriptHandler.clearConsole();
                }

                this.initGui();
            }
        }

        this.displayGuiScreen(this);
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            this.setScript();
            this.activeTab = 0;
            this.initGui();
        }

        if (guibutton.id == this.scriptLimit) {
            if (scriptHandler.container == null)
                this.scriptHandler.container = new ScriptContainer(this.scriptHandler);
            else
                this.setScript();
            this.activeTab = 1;
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
            NoppesStringUtils.setClipboardContents(this.getTextField(2).getText());
        }

        if (guibutton.id == 101) {
            GuiYesNo guiyesno = new GuiYesNo(this, StatCollector.translateToLocal("gui.paste"), StatCollector.translateToLocal("gui.sure"), 101);
            this.displayGuiScreen(guiyesno);
        }

        GuiYesNo container1;
        if (guibutton.id == 102) {
            container1 = new GuiYesNo(this, StatCollector.translateToLocal("gui.clear"), StatCollector.translateToLocal("gui.sure"), 102);
            this.displayGuiScreen(container1);
        }

        if (guibutton.id == 103) {
            this.scriptHandler.setLanguage(guibutton.displayString);
        }

        if (guibutton.id == 104) {
            this.scriptHandler.setEnabled(((GuiNpcButton) guibutton).getValue() == 1);
        }

        if (guibutton.id == 105) {
            container1 = new GuiYesNo(this, "", ((GuiNpcButton) guibutton).displayString, 10);
            this.displayGuiScreen(container1);
        }

        if (guibutton.id == 107) {
            ScriptContainer container = this.scriptHandler.container;
            if (container == null) {
                container = new ScriptContainer(this.scriptHandler);
                this.scriptHandler.container = container;
            }

//            this.setSubGui(new EventGuiScriptList((List)this.languages.get(this.script.getLanguage()), container));
        }

    }

    private void setScript() {
        if (this.activeTab == 1) {
            ScriptContainer container = this.scriptHandler.container;
            if (container == null) {
                container = new ScriptContainer(this.scriptHandler);
                this.scriptHandler.container = container;
            }

            String text = this.getTextField(2).getText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            container.script = text;
        }

    }

    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            loaded = true;
            return;
        }

        if (!compound.hasKey("Tab")) {
            this.scriptHandler.setLanguage(compound.getString("ScriptLanguage"));
            this.scriptHandler.setEnabled(compound.getBoolean("ScriptEnabled"));
            this.copiedSetGuiData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            ScriptContainer container = new ScriptContainer(this.scriptHandler);
            container.readFromNBT(compound.getCompoundTag("Script"));
            this.scriptHandler.container = container;
            this.initGui();
        }
    }

    private void copiedSetGuiData(NBTTagCompound compound) {
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

    public void save() {
        if (loaded) {
            this.setScript();

            List<ScriptContainer> containers = this.scriptHandler.getScripts();
            for (int i = 0; i < containers.size(); i++) {
                ScriptContainer container = containers.get(i);
                EffectScriptPacket.Save(effect.id, i, containers.size(), container.writeToNBT(new NBTTagCompound()));
            }
            NBTTagCompound scriptData = new NBTTagCompound();
            scriptData.setString("ScriptLanguage", this.scriptHandler.getLanguage());
            scriptData.setBoolean("ScriptEnabled", this.scriptHandler.getEnabled());
            scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.scriptHandler.getConsoleText()));

            EffectScriptPacket.Save(effect.id, -1, containers.size(), scriptData);
        }
    }

    public void textUpdate(String text) {
        ScriptContainer container = this.scriptHandler.container;
        if (container != null) {
            container.script = text;
        }

    }

    public void saveText(String text) {
        ScriptContainer container = this.scriptHandler.container;
        if (container != null) {
            container.script = text;
        }

        this.initGui();
    }

    @Override
    public void close() {
        this.save();
        parent.setWorldAndResolution(mc, width, height);
        parent.initGui();
        mc.currentScreen = parent;
    }
}
