package noppes.npcs.client.gui.script;

import noppes.npcs.janino.JaninoScript;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.ScriptController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class GuiJaninoScript extends GuiNPCInterface implements GuiYesNoCallback, ITextChangeListener, ICustomScrollListener, ISubGuiListener {

    private GuiScriptTextArea textArea;
    private int activeTab = 0;
    public List<String> allExternalScripts = new ArrayList<>();
    private final int scriptLimit = 1;
    public String previousHookClicked = "";
    
    // GUI display list
    List<String> hooklist = new ArrayList<>();

    // Map from display name (with parameters if overloaded) to Method
    Map<String, Method> hookMap = new HashMap<>();

    public final GuiScreen parent;
    private final JaninoScript container;

    public Runnable saveFunction;

    public GuiJaninoScript(GuiScreen parent, JaninoScript handler) {
        this.drawDefaultBackground = true;
        this.closeOnEsc = true;
        this.xSize = 420;
        this.setBackground("menubg.png");

        this.parent = parent;
        this.container = handler;

        createHookList();

        String lang = handler.getLanguage();
        allExternalScripts.addAll(ScriptController.Instance.getScripts(lang));
    }

    public GuiJaninoScript setSave(Runnable run) {
        saveFunction = run;
        return this;
    }

    public void createHookList() {
        // Group all declared methods of the handler's type by their method name
        Map<String, List<Method>> grouped = Arrays.stream(container.type.getDeclaredMethods())
            .collect(Collectors.groupingBy(Method::getName));

        for (Map.Entry<String, List<Method>> entry : grouped.entrySet()) {
            List<Method> methods = entry.getValue();

            // Sort overloads by number of parameters (ascending)
            // This ensures the simplest overload comes first
            methods.sort(Comparator.comparingInt(Method::getParameterCount));
            boolean first = true;


            for (Method m : methods) {
                if (Modifier.isFinal(m.getModifiers()))
                    continue; //skip finals

                String displayName = first ? m.getName() :
                    m.getName() + "(" + Arrays.stream(m.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ")) + ")";


                hooklist.add(displayName);
                hookMap.put(displayName, m);
                first = false;
            }
        }
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
        addTopButton(new GuiMenuTopButton(-1, guiLeft + xSize-2 , guiTop - 17, "X"));
        this.addTopButton(top = new GuiMenuTopButton(0, this.guiLeft + 4, this.guiTop - 17, "gui.settings"));
        int topXoffset = 0;
        int topYoffset = 0;


        if (this.container != null) {
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

            hooks.setUnsortedList(this.hooklist);
            this.addScroll(hooks);
            GuiNpcLabel hookLabel = new GuiNpcLabel(0, "script.hooks", hooks.guiLeft, this.guiTop + 5);
            hookLabel.color = 11184810;
            this.addLabel(hookLabel);
            JaninoScript container = this.container;
            if (textArea == null)
                textArea = new GuiScriptTextArea(this, 2, this.guiLeft + 1 + yoffset, this.guiTop + yoffset, this.xSize - 108 - yoffset, (int) ((double) this.ySize * 0.96) - yoffset * 2, container == null ? "" : container.script);
            else
                textArea.init(this.guiLeft + 1 + yoffset, this.guiTop + yoffset, this.xSize - 108 - yoffset, (int) ((double) this.ySize * 0.96) - yoffset * 2, container == null ? "" : container.script);
            textArea.enableCodeHighlighting();
            textArea.setListener(this);
            this.closeOnEsc(textArea::closeOnEsc);
            this.addTextField(textArea);
            
            int left1 = this.guiLeft + this.xSize - 104;
            this.addButton(new GuiNpcButton(102, left1, this.guiTop + yoffset, 60, 20, "gui.clear"));
            this.addButton(new GuiNpcButton(101, left1 + 61, this.guiTop + yoffset, 60, 20, "gui.paste"));
            this.addButton(new GuiNpcButton(100, left1, this.guiTop + 21 + yoffset, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(105, left1 + 61, this.guiTop + 21 + yoffset, 60, 20, "gui.remove"));

            this.addButton(new GuiNpcButton(107, left1, this.guiTop + 66 + yoffset, 80, 20, "script.loadscript"));
            //  this.getButton(107).enabled = false;

            GuiCustomScroll scroll = (new GuiCustomScroll(this, 0)).setUnselectable();
            scroll.setSize(100, (int) ((double) this.ySize * 0.54) - yoffset * 2);
            scroll.guiLeft = left1;
            scroll.guiTop = this.guiTop + 88 + yoffset;
            if (container != null)
                scroll.setList(container.externalScripts);

            this.addScroll(scroll);
        } else {
            GuiNpcTextArea var8 = new GuiNpcTextArea(2, this, this.guiLeft + 4 + yoffset, this.guiTop + 6 + yoffset, this.xSize - 160 - yoffset, (int) ((float) this.ySize * 0.92F) - yoffset * 2, this.getConsoleText());
            var8.enabled = false;
            this.addTextField(var8);
            int var9 = this.guiLeft + this.xSize - 150;
            this.addButton(new GuiNpcButton(100, var9, this.guiTop + 125, 60, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(102, var9, this.guiTop + 146, 60, 20, "gui.clear"));
            this.addLabel(new GuiNpcLabel(1, "script.language", var9, this.guiTop + 15));
            String containerLang = container.getLanguage();
            this.addButton(new GuiNpcButton(103, var9 + 60, this.guiTop + 10, 80, 20, new String[]{containerLang.equals("Java") ? "Janino (JVM 8)" : containerLang},
                this.getScriptIndex()));
            // this.getButton(103).enabled = !this.languages.isEmpty();
            this.addLabel(new GuiNpcLabel(2, "gui.enabled", var9, this.guiTop + 36));
            this.addButton(new GuiNpcButton(104, var9 + 60, this.guiTop + 31, 50, 20, new String[]{"gui.no", "gui.yes"}, this.container.getEnabled() ? 1 : 0));

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
            if (!this.getTextField(2)
                .getText()
                .isEmpty()) {
                addString = addString + "\n";
            }

            addString = addString + generateMethodStub(hookMap.get(hook));
            this.getTextField(2)
                .setText(this.getTextField(2)
                    .getText() + addString);
            this.previousHookClicked = "";
        } else {
            this.previousHookClicked = hook;
        }
    }

    public void confirmClicked(boolean flag, int i) {
        if (flag) {
            if (i == 0)
                this.openLink("https://www.curseforge.com/minecraft/mc-mods/customnpc-plus");
            else if (i == 1)
                this.openLink("https://kamkeel.github.io/CustomNPC-Plus/");
            else if (i == 2)
                this.openLink("https://kamkeel.github.io/CustomNPC-Plus/");
            else if (i == 3)
                this.openLink("http://www.minecraftforge.net/forum/index.php/board,122.0.html");

            if (i == 10) { //Remove
                this.container.setScript("");
                this.activeTab = 0;
            } else if (i == 101) { //Paste
                this.getTextField(2)
                    .setText(NoppesStringUtils.getClipboardContents());
                this.setScript();
            } else if (i == 102) { //Clear
                if (this.activeTab == 1)
                    container.script = "";
                else
                    this.container.clearConsole();

                this.initGui();
            }
        }

        this.displayGuiScreen(this);
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == -1) {
            close();
            return;
        }

        if (guibutton.id == 0) {
            this.setScript();
            this.activeTab = 0;
            this.initGui();
            container.ensureCompiled();
        }

        if (guibutton.id == this.scriptLimit) {
            // if (container.container == null)
            //this.container.container = new ScriptContainer(this.container);
            //  else
            // this.setScript();
            this.activeTab = 1;
            this.initGui();
        }

        if (guibutton.id == 109) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://kamkeel.github.io/CustomNPC-Plus/", 0, true));
        } else if (guibutton.id == 110) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://github.com/KAMKEEL/CustomNPC-Plus-API", 1, true));
        } else if (guibutton.id == 111) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "https://github.com/Noppes/CustomNPCsAPI", 2, true));
        } else if (guibutton.id == 112) {
            this.displayGuiScreen(new GuiConfirmOpenLink(this, "http://www.minecraftforge.net/forum/index.php/board,122.0.html", 3, true));
        }

        GuiYesNo confirmScreen;

        //Copy
        if (guibutton.id == 100) {
            NoppesStringUtils.setClipboardContents(this.getTextField(2)
                .getText());
        }
        //Paste
        else if (guibutton.id == 101) {
            GuiYesNo guiyesno = new GuiYesNo(this, StatCollector.translateToLocal("gui.paste"), StatCollector.translateToLocal("gui.sure"), 101);
            this.displayGuiScreen(guiyesno);
        }
        //Clear
        else if (guibutton.id == 102) {
            confirmScreen = new GuiYesNo(this, StatCollector.translateToLocal("gui.clear"), StatCollector.translateToLocal("gui.sure"), 102);
            this.displayGuiScreen(confirmScreen);
        }
        //Enable
        else if (guibutton.id == 104) {
            this.container.setEnabled(((GuiNpcButton) guibutton).getValue() == 1);
        }
        //Remove
        else if (guibutton.id == 105) {
            confirmScreen = new GuiYesNo(this, "", guibutton.displayString, 10);
            this.displayGuiScreen(confirmScreen);
        }
        //Load Script
        else if (guibutton.id == 107) {
            setSubGui(new SubGuiSelectList(allExternalScripts, container.externalScripts, "All Scripts", "Selected Scripts"));
        }
    }

    public void subGuiClosed(SubGuiInterface sub) {
        if (sub instanceof SubGuiSelectList)
            container.setExternalScripts(((SubGuiSelectList) sub).selected);

    }

    /**
     * Generates a stub string for a given Method.
     */
    public static String generateMethodStub(Method method) {
        String mods = java.lang.reflect.Modifier.toString(method.getModifiers());
        if (!mods.isEmpty())
            mods += " ";

        String returnTypeStr = method.getReturnType()
            .getSimpleName();
        String name = method.getName();

        Map<String, Integer> typeCount = new HashMap<>();
        String params = Arrays.stream(method.getParameters())
            .map(p -> {
                String typeName = p.getType()
                    .getSimpleName();
                String baseName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);

                int count = typeCount.getOrDefault(baseName, 0) + 1;
                typeCount.put(baseName, count);

                // append number if same type of arg exists
                String paramName = count == 1 ? baseName : baseName + (count - 1);

                return typeName + " " + paramName;
            })
            .collect(Collectors.joining(", "));

        String body;
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class)
            body = "";
        else if (returnType.isPrimitive()) {
            if (returnType == boolean.class)
                body = "    return false;";
            else if (returnType == char.class)
                body = "    return '\\0';";
            else
                body = "    return 0;";
        } else {
            body = "    return null;";
        }

        return String.format("%s%s %s(%s) {\n%s\n}\n", mods, returnTypeStr, name, params, body);
    }

    private String getConsoleText() {
        Map<Long, String> map = this.container.getConsoleText();
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet()
            .iterator();

        while (var3.hasNext()) {
            Map.Entry<Long, String> entry = (Map.Entry) var3.next();
            builder.insert(0, new Date(entry.getKey()) + entry.getValue() + "\n");
        }

        return builder.toString();
    }

    private int getScriptIndex() {
        return 0;
    }

    private void setScript() {
        if (this.activeTab == 1) {
            String text = this.getTextField(2)
                .getText();
            text = text.replace("\r\n", "\n");
            text = text.replace("\r", "\n");
            container.setScript(text);
        }
    }

    public void save() {
        if (saveFunction != null)
            saveFunction.run();
        else if (parent instanceof GuiNPCInterface)
            ((GuiNPCInterface) parent).save();
    }

    public void textUpdate(String text) {
//        container.setScript(text);
    }

    public static GuiJaninoScript create(GuiScreen parent, JaninoScript script, int width, int height) {
        GuiJaninoScript gui = new GuiJaninoScript(parent, script);
        gui.setWorldAndResolution(Minecraft.getMinecraft(), width, height);
        Minecraft.getMinecraft().currentScreen = gui;
        gui.initGui();
        return gui;
    }


    @Override
    public void close() {
        this.save();
        this.setScript();
        container.ensureCompiled();
        parent.setWorldAndResolution(mc, width, height);
        parent.initGui();
        mc.currentScreen = parent;
    }
}
