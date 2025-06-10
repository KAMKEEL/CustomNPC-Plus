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
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.GlobalNPCDataScript;
import noppes.npcs.controllers.data.PlayerData;
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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
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
    private URLClassLoader loader;

    public ScriptController() {
        Instance = this;
        manager = new ScriptEngineManager();
        if (!ConfigScript.ScriptingEnabled)
            return;
        LogWriter.info("Script Engines Available:");

        try {
            File nashornJar = new File("mods/nashorn.jar"); // Adjust path if needed
            this.loader = new URLClassLoader(new URL[]{nashornJar.toURI().toURL()}, CustomNpcs.class.getClassLoader());

            ClassLoader cl = CustomNpcs.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(cl);
            LogWriter.info("ClassLoader: " + cl);
            LogWriter.info("Parent: " + cl.getParent());

            Class<?> factoryClass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory", true, loader);
            this.nashornFactory = (ScriptEngineFactory) factoryClass.getDeclaredConstructor().newInstance();
            this.languages.put("ECMAScript", ".js");
            LogWriter.info("→ standalone Nashorn loaded: " + this.nashornFactory.getEngineName());
        } catch (Exception e) {
            // fallback to built-in (Java 8–11) — also guarded
            try {
                ScriptEngine eng = manager.getEngineByName("nashorn");
                if (eng != null) {
                    this.nashornFactory = eng.getFactory();
                    this.languages.put("ECMAScript", ".js");
                    LogWriter.info("→ built-in Nashorn loaded");
                }
            } catch (Throwable t) {
                LogWriter.error("No Nashorn engine available");
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

    public PlayerDataScript getPlayerScripts(EntityPlayer player) {
        if (ConfigScript.IndividualPlayerScripts)
            return PlayerData.get(player).scriptData;

        return this.playerScripts;
    }

    public PlayerDataScript getPlayerScripts(IPlayer player) {
        if (ConfigScript.IndividualPlayerScripts)
            return PlayerData.get((EntityPlayer) player.getMCEntity()).scriptData;

        return this.playerScripts;
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
            try {
                ScriptEngine scriptEngine;
                Class<?> factoryClass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory",
                    true, this.loader);

                if (ConfigScript.EnableBannedClasses) {
                    Class<?> classFilterClass = Class.forName("jdk.nashorn.api.scripting.ClassFilter",
                        true, this.loader);

                    Object classFilter = Proxy.newProxyInstance(
                        classFilterClass.getClassLoader(),
                        new Class[]{classFilterClass},
                        (proxy, method, args) -> {
                            if ("exposeToScripts".equals(method.getName()) && args.length == 1) {
                                String className = (String) args[0];
                                for (String banned : ConfigScript.BannedClasses) {
                                    if (className.equals(banned)) return false;
                                }
                                return true;
                            }
                            throw new UnsupportedOperationException("Unsupported method: " + method);
                        });

                    Method getScriptEngine = factoryClass.getMethod(
                        "getScriptEngine",
                        classFilterClass);

                    scriptEngine = (ScriptEngine) getScriptEngine.invoke(this.nashornFactory, classFilter);
                } else {
                    Method getScriptEngine = factoryClass.getMethod("getScriptEngine");
                    scriptEngine = (ScriptEngine) getScriptEngine.invoke(this.nashornFactory);
                }
                scriptEngine.setBindings(this.manager.getBindings(), ScriptContext.GLOBAL_SCOPE);
                return scriptEngine;
            } catch (Exception exception) {
                LogWriter.except(exception);
                return null;
            }
        }
        return manager.getEngineByName(language);
    }

    public ScriptEngine getOpenJDKNashornEngine() {
        ScriptEngine engine;
        try {
            // Load factory and class filter
            Class<?> factoryClass = Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory");
            Object factory = factoryClass.getConstructor().newInstance();

            // Load ClassFilter interface (from Nashorn 15.6)
            Class<?> classFilterClass = Class.forName("org.openjdk.nashorn.api.scripting.ClassFilter");

            Object classFilter;
            if (ConfigScript.EnableBannedClasses) {
                // Create proxy instance of ClassFilter
                classFilter = Proxy.newProxyInstance(
                    classFilterClass.getClassLoader(),
                    new Class[]{classFilterClass},
                    (proxy, method, args) -> {
                        if ("exposeToScripts".equals(method.getName()) && args.length == 1) {
                            String className = (String) args[0];
                            for (String banned : ConfigScript.BannedClasses) {
                                if (className.equals(banned)) return false;
                            }
                            return true;
                        }
                        throw new UnsupportedOperationException("Unsupported method: " + method);
                    });

                Method getEngineMethod = factoryClass.getMethod(
                    "getScriptEngine",
                    String[].class,
                    ClassLoader.class,
                    classFilterClass
                );

                String[] args = new String[] {
                    "--language=es6",
                    "--optimistic-types=true",
                    "--lazy-compilation=true"
                };
                ClassLoader loader = Thread.currentThread().getContextClassLoader();

                engine = (ScriptEngine) getEngineMethod.invoke(
                    factory,
                    (Object) args,
                    loader,
                    classFilter
                );
            } else {
                Method getEngineMethod = factoryClass.getMethod("getScriptEngine", String[].class);
                String[] args = new String[] {
                    "--language=es6",
                    "--optimistic-types=true",
                    "--lazy-compilation=true"
                };
                engine = (ScriptEngine) getEngineMethod.invoke(factory, (Object) args);
            }

            engine.setBindings(this.manager.getBindings(), ScriptContext.GLOBAL_SCOPE);
            return engine;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
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
