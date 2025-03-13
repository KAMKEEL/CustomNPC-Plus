package noppes.npcs.controllers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.GlobalNPCDataScript;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class ScriptController {

    public static ScriptController Instance;
    public static boolean HasStart = false;
    private final ScriptEngineManager manager;
    private ScriptEngineFactory nashornFactory;
    public Map<String, String> languages = new HashMap<String, String>();
    public Map<String, String> scripts = new HashMap<String, String>();
    public long lastLoaded = 0;
    public File dir;
    public NBTTagCompound compound = new NBTTagCompound();

    public boolean shouldSave = false;
    public PlayerDataScript playerScripts = new PlayerDataScript((EntityPlayer) null);
    public long lastPlayerUpdate = 0L;

    public ForgeDataScript forgeScripts = new ForgeDataScript();
    public long lastForgeUpdate = 0L;

    public GlobalNPCDataScript globalNpcScripts = new GlobalNPCDataScript((EntityNPCInterface) null);
    public long lastGlobalNpcUpdate = 0L;

    public ScriptController() {
        Instance = this;
        manager = new ScriptEngineManager();
        if (!ConfigScript.ScriptingEnabled)
            return;
        LogWriter.info("Script Engines Available:");

        ScriptEngine engine = manager.getEngineByName("nashorn");
        if (engine != null) {
            this.nashornFactory = engine.getFactory();
        }

        for (ScriptEngineFactory fac : manager.getEngineFactories()) {
            if (fac.getExtensions().isEmpty())
                continue;

            ScriptEngine scriptEngine = fac.getScriptEngine();
            try {
                scriptEngine.put("$RunTest", null);
                String ext = "." + fac.getExtensions().get(0).toLowerCase();
                LogWriter.info("Engine " + fac.getEngineName() + " running " + fac.getLanguageName() + " with extension: " + ext);
                languages.put(fac.getLanguageName(), ext);
            } catch (Exception ignored) {
            }
        }
    }

    private File forgeScriptsFile() {
        return new File(this.dir, "forge_scripts.json");
    }

    public boolean loadForgeScripts() {
        this.forgeScripts.clear();
        File file = this.forgeScriptsFile();

        try {
            if (!file.exists()) {
                return false;
            } else {
                this.forgeScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
                return true;
            }
        } catch (Exception var3) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), var3);
            return false;
        }
    }

    public void saveForgeScripts() {
        File file = this.forgeScriptsFile();
        try {
            NBTJsonUtil.SaveFile(file, this.forgeScripts.writeToNBT(new NBTTagCompound()));
            this.forgeScripts.lastInited = -1L;
        } catch (IOException | JsonException var4) {
            var4.printStackTrace();
        }
    }

    private File playerScriptsFile() {
        return new File(dir, "player_scripts.json");
    }

    private File npcScriptsFile() {
        return new File(dir, "npc_scripts.json");
    }

    public boolean loadPlayerScripts() {
        this.playerScripts.clear();
        File file = this.playerScriptsFile();

        try {
            if (!file.exists()) {
                return false;
            } else {
                this.playerScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
                shouldSave = false;
                return true;
            }
        } catch (Exception var3) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), var3);
            return false;
        }
    }

    public void savePlayerScripts() {
        File file = this.playerScriptsFile();
        try {
            NBTJsonUtil.SaveFile(file, this.playerScripts.writeToNBT(new NBTTagCompound()));
            this.playerScripts.lastInited = -1L;
        } catch (IOException | JsonException var4) {
            var4.printStackTrace();
        }
    }

    public boolean loadGlobalNPCScripts() {
        this.globalNpcScripts.clear();
        File file = this.npcScriptsFile();

        try {
            if (!file.exists()) {
                return false;
            } else {
                this.globalNpcScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
                shouldSave = false;
                return true;
            }
        } catch (Exception var3) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), var3);
            return false;
        }
    }

    public void saveGlobalNpcScripts() {
        File file = this.npcScriptsFile();
        try {
            NBTJsonUtil.SaveFile(file, this.globalNpcScripts.writeToNBT(new NBTTagCompound()));
            this.globalNpcScripts.lastInited = -1L;
        } catch (IOException | JsonException var4) {
            var4.printStackTrace();
        }
    }

    public synchronized void saveForgeScriptsSync() {
        customNPCThread.execute(this::saveForgeScripts);
    }

    public synchronized void savePlayerScriptsSync() {
        customNPCThread.execute(() -> {
            File file = this.playerScriptsFile();
            try {
                NBTJsonUtil.SaveFile(file, this.playerScripts.writeToNBT(new NBTTagCompound()));
                this.playerScripts.lastInited = -1L;
            } catch (IOException | JsonException var4) {
                var4.printStackTrace();
            }
        });
    }

    public synchronized void saveGlobalScriptsSync() {
        customNPCThread.execute(this::saveGlobalNpcScripts);
    }

    public void loadCategories() {
        dir = new File(CustomNpcs.getWorldSaveDirectory(), "scripts");
        if (!dir.exists())
            dir.mkdir();
        if (!getSavedFile().exists())
            shouldSave = true;
        new ScriptWorld(null).clearTempData();
        scripts.clear();
        for (String language : languages.keySet()) {
            String ext = languages.get(language);
            File scriptDir = new File(dir, language.toLowerCase());
            if (!scriptDir.exists())
                scriptDir.mkdir();
            else
                loadDir(scriptDir, "", ext);
        }
        lastLoaded = System.currentTimeMillis();
    }

    private void loadDir(File dir, String name, String ext) {
        for (File file : dir.listFiles()) {
            String filename = name + file.getName().toLowerCase();
            if (file.isDirectory()) {
                loadDir(file, filename + "/", ext);
                continue;
            }
            if (!filename.endsWith(ext))
                continue;
            try {
                scripts.put(filename, readFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean loadStoredData() {
        loadCategories();
        File file = getSavedFile();
        try {
            if (!file.exists())
                return false;
            this.compound = NBTJsonUtil.LoadFile(file);
            shouldSave = false;
        } catch (Exception e) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
            return false;
        }
        return true;
    }

    private File getSavedFile() {
        return new File(dir, "world_data.json");
    }

    private String readFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    private static final List<String> nashornNames = immutableList("nashorn", "Nashorn", "js", "JS", "JavaScript", "javascript", "ECMAScript", "ecmascript");

    public ScriptEngine getEngineByName(String language) {
        if (nashornNames.contains(language) && this.nashornFactory != null) {
            ScriptEngine scriptEngine;
            if (ConfigScript.EnableBannedClasses) {
                try {
                    ClassFilter filter = s -> {
                        for (String className : ConfigScript.BannedClasses) {
                            if (s.compareTo(className) == 0) {
                                return false;
                            }
                        }
                        return true;
                    };
                    NashornScriptEngineFactory nashornScriptEngineFactory = (NashornScriptEngineFactory) this.nashornFactory;
                    scriptEngine = nashornScriptEngineFactory.getScriptEngine(filter);
                } catch (Exception e) {
                    scriptEngine = this.nashornFactory.getScriptEngine();
                }
            } else {
                scriptEngine = this.nashornFactory.getScriptEngine();
            }
            scriptEngine.setBindings(this.manager.getBindings(), ScriptContext.GLOBAL_SCOPE);
            return scriptEngine;
        }
        return manager.getEngineByName(language);
    }

    private static List<String> immutableList(String... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public NBTTagList nbtLanguages() {
        NBTTagList list = new NBTTagList();
        for (String language : languages.keySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList scripts = new NBTTagList();
            for (String script : getScripts(language)) {
                scripts.appendTag(new NBTTagString(script));
            }
            compound.setTag("Scripts", scripts);
            compound.setString("Language", language);
            list.appendTag(compound);
        }
        return list;
    }

    private List<String> getScripts(String language) {
        List<String> list = new ArrayList<String>();
        String ext = languages.get(language);
        if (ext == null)
            return list;
        for (String script : scripts.keySet()) {
            if (script.endsWith(ext)) {
                list.add(script);
            }
        }
        return list;
    }

    @SubscribeEvent
    public void invoke(WorldEvent.Save event) {
        if (!shouldSave || event.world.isRemote || event.world != MinecraftServer.getServer().worldServers[0])
            return;

        try {
            NBTJsonUtil.SaveFile(getSavedFile(), compound);
        } catch (Exception e) {
            LogWriter.except(e);
        }

        shouldSave = false;
    }
}
