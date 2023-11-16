package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import foxz.command.CommandNoppes;
import kamkeel.command.CommandKamkeel;
import kamkeel.developer.Developer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockVine;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import nikedemos.markovnames.generators.*;
import noppes.npcs.compat.PixelmonHelper;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.config.LoadConfiguration;
import noppes.npcs.config.legacy.LegacyConfig;
import noppes.npcs.controllers.*;
import noppes.npcs.enchants.EnchantInterface;
import noppes.npcs.entity.*;
import noppes.npcs.entity.old.*;
import noppes.npcs.scripted.NpcAPI;

import java.io.File;
import java.util.Set;

@Mod(modid = "customnpcs", name = "CustomNPC+", version = "1.9-beta3")
public class CustomNpcs {

    @SidedProxy(clientSide = "noppes.npcs.client.ClientProxy", serverSide = "noppes.npcs.CommonProxy")
    public static CommonProxy proxy;

    public static File Dir;

    private static int NewEntityStartId = 0;
    public static long ticks;
    public static CustomNpcs instance;

    public static boolean FreezeNPCs = false;

    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    public static final MarkovGenerator[] MARKOV_GENERATOR = new MarkovGenerator[10];

    public static boolean InitIgnore = false;
    public static boolean TickIgnore = false;
    public static boolean InteractIgnore = false;
    public static boolean DialogIgnore = false;
    public static boolean DamagedIgnore = false;
    public static boolean KilledIgnore = false;
    public static boolean AttackIgnore = false;
    public static boolean TargetIgnore = false;
    public static boolean CollideIgnore = false;
    public static boolean KillsIgnore = false;
    public static boolean DialogCloseIgnore = false;
    public static boolean TimerIgnore = false;

    public static String configPath;
    public static String legacyPath;
    public static boolean legacyExist;

    public static LegacyConfig legacyConfig;

    public CustomNpcs() {
        instance = this;
    }

    @EventHandler
    public void load(FMLPreInitializationEvent ev) {
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCs");
        ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCsPlayer");

        MinecraftServer server = MinecraftServer.getServer();
        String dir = "";
        if (server != null) {
            dir = new File(".").getAbsolutePath();
        } else {
            dir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
        }
        Dir = new File(dir, "customnpcs");
        Dir.mkdir();

        configPath = ev.getModConfigurationDirectory() + File.separator + "CustomNpcPlus";
        legacyPath = ev.getModConfigurationDirectory() + "/CustomNpcs.cfg";

        File configDir = new File(configPath);
        if(!configDir.exists()){
            // Convert Legacy Config to New Config if NO Config Folder Exists
            File legacyFile = new File(legacyPath);
            if(legacyFile.exists()){
                System.out.println("Loading Legacy Config");
                legacyExist = true;
                legacyConfig = new LegacyConfig();
                legacyConfig.init();
            }
        }
        configPath += File.separator;
        LoadConfiguration.init(configPath);

        EnchantInterface.load();
        CustomItems.load();

        proxy.load();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
        MinecraftForge.EVENT_BUS.register(new ScriptController());

        ScriptPlayerEventHandler scriptPlayerEventHandler = new ScriptPlayerEventHandler();
        MinecraftForge.EVENT_BUS.register(scriptPlayerEventHandler);
        FMLCommonHandler.instance().bus().register(scriptPlayerEventHandler);

        ScriptForgeEventHandler forgeEventHandler = (new ScriptForgeEventHandler()).registerForgeEvents();
        MinecraftForge.EVENT_BUS.register(forgeEventHandler);
        FMLCommonHandler.instance().bus().register(forgeEventHandler);

        ScriptItemEventHandler scriptItemEventHandler = new ScriptItemEventHandler();
        MinecraftForge.EVENT_BUS.register(scriptItemEventHandler);
        FMLCommonHandler.instance().bus().register(scriptItemEventHandler);

        FMLCommonHandler.instance().bus().register(new ServerTickHandler());

        registerNpc(EntityNPCHumanMale.class, "npchumanmale");
        registerNpc(EntityNPCVillager.class, "npcvillager");
        registerNpc(EntityNpcPony.class, "npcpony");
        registerNpc(EntityNPCHumanFemale.class, "npchumanfemale");
        registerNpc(EntityNPCDwarfMale.class, "npcdwarfmale");
        registerNpc(EntityNPCFurryMale.class, "npcfurrymale");
        registerNpc(EntityNpcMonsterMale.class, "npczombiemale");
        registerNpc(EntityNpcMonsterFemale.class, "npczombiefemale");
        registerNpc(EntityNpcSkeleton.class, "npcskeleton");
        registerNpc(EntityNPCDwarfFemale.class, "npcdwarffemale");
        registerNpc(EntityNPCFurryFemale.class, "npcfurryfemale");
        registerNpc(EntityNPCOrcMale.class, "npcorcfmale");
        registerNpc(EntityNPCOrcFemale.class, "npcorcfemale");
        registerNpc(EntityNPCElfMale.class, "npcelfmale");
        registerNpc(EntityNPCElfFemale.class, "npcelffemale");
        registerNpc(EntityNpcCrystal.class, "npccrystal");
        registerNpc(EntityNpcEnderchibi.class, "npcenderchibi");
        registerNpc(EntityNpcNagaMale.class, "npcnagamale");
        registerNpc(EntityNpcNagaFemale.class, "npcnagafemale");
        registerNpc(EntityNpcSlime.class, "NpcSlime");
        registerNpc(EntityNpcDragon.class, "NpcDragon");
        registerNpc(EntityNPCEnderman.class, "npcEnderman");
        registerNpc(EntityNPCGolem.class, "npcGolem");
        registerNpc(EntityCustomNpc.class, "CustomNpc");
        registerCustomModel(EntityCustomModel.class, "CustomModel");

        registerNewEntity(EntityChairMount.class, "CustomNpcChairMount", 64, 10, false);
        registerNewEntity(EntityProjectile.class, "throwableitem", 64, 3, true);
        registerNewEntity(EntityMagicProjectile.class, "magicprojectile", 64, 3, true);

        new RecipeController();

        ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkController());

        new CustomNpcsPermissions();
        new Developer();

        PixelmonHelper.load();
    }

    @EventHandler
    public void load(FMLInitializationEvent ev) {

        MARKOV_GENERATOR[0] = new MarkovRoman(3);
        MARKOV_GENERATOR[1] = new MarkovJapanese(4);
        MARKOV_GENERATOR[2] = new MarkovSlavic(3);
        MARKOV_GENERATOR[3] = new MarkovWelsh(3);
        MARKOV_GENERATOR[4] = new MarkovSaami(3);
        MARKOV_GENERATOR[5] = new MarkovOldNorse(4);
        MARKOV_GENERATOR[6] = new MarkovAncientGreek(3);
        MARKOV_GENERATOR[7] = new MarkovAztec(3);
        MARKOV_GENERATOR[8] = new MarkovCustomNPCsClassic(3);
        MARKOV_GENERATOR[9] = new MarkovSpanish(3);

    }

    @EventHandler
    public void setAboutToStart(FMLServerAboutToStartEvent event) {
        ChunkController.Instance.clear();
        new QuestController();
        new PlayerDataController();
        new FactionController();
        new TagController();
        new TransportController();
        new GlobalDataController();
        new SpawnController();
        new LinkedNpcController();
        new AnimationController();
        ScriptController.Instance.loadStoredData();
        ScriptController.Instance.loadForgeScripts();
        ScriptController.Instance.loadGlobalNPCScripts();
        ScriptController.Instance.loadPlayerScripts();
        ScriptController.HasStart = false;
        NpcAPI.clearCache();
        PlayerDataController.Instance.clearCache();

        Set<String> names = Block.blockRegistry.getKeys();
        for(String name : names){
            Block block = (Block) Block.blockRegistry.getObject(name);
            if(block instanceof BlockLeavesBase){
                block.setTickRandomly(ConfigMain.LeavesDecayEnabled);
            }
            if(block instanceof BlockVine){
                block.setTickRandomly(ConfigMain.VineGrowthEnabled);
            }
            if(block instanceof BlockIce){
                block.setTickRandomly(ConfigMain.IceMeltsEnabled);
            }
        }
    }

    //Loading items in the about to start event was corrupting items with a damage value
    @EventHandler
    public void started(FMLServerStartedEvent event) {
        RecipeController.Instance.load();
        new DialogController();
        new BankController();
        QuestController.Instance.load();
        ScriptController.HasStart = true;
        ServerCloneController.Instance = new ServerCloneController();
        ServerTagMapController.Instance = new ServerTagMapController();
    }

    @EventHandler
    public void stopped(FMLServerStoppedEvent event){
        ServerCloneController.Instance = null;
        GlobalDataController.Instance.saveData();
        ScriptController.Instance.saveForgeScripts();
        ScriptController.Instance.savePlayerScripts();
        ScriptController.Instance.saveGlobalNpcScripts();
    }

    @EventHandler
    public void serverstart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandNoppes());
        event.registerServerCommand(new CommandKamkeel());
    }

    private void registerNpc(Class<? extends Entity> cl, String name) {
        EntityRegistry.registerModEntity(cl, name, NewEntityStartId++, this, 64, 3, true);
        EntityList.stringToClassMapping.put(name, cl);
    }

    private void registerCustomModel(Class<? extends Entity> cl, String name) {
        EntityRegistry.registerModEntity(cl, name, NewEntityStartId++, this, 64, 10, false);
        EntityList.stringToClassMapping.put(name, cl);
    }

    private void registerNewEntity(Class<? extends Entity> cl, String name, int range, int update, boolean velocity) {
        EntityRegistry.registerModEntity(cl, name, NewEntityStartId++, this, range, update, velocity);
    }


    public static File getWorldSaveDirectory() {
        MinecraftServer server = MinecraftServer.getServer();
        File saves = new File(".");
        if (server != null && !server.isDedicatedServer()) {
            saves = new File(Minecraft.getMinecraft().mcDataDir, "saves");
        }
        if (server != null) {
            File savedir = new File(new File(saves, server.getFolderName()), "customnpcs");
            if (!savedir.exists()) {
                savedir.mkdir();
            }
            return savedir;
        }
        return null;
    }

    public static File getWorldSaveDirectory(String s) {
        try {
            File dir = new File(".");
            if (getServer() != null) {
                if (!getServer().isDedicatedServer()) {
                    dir = new File(Minecraft.getMinecraft().mcDataDir, "saves");
                }

                dir = new File(new File(dir, getServer().getFolderName()), "customnpcs");
            }

            if (s != null) {
                dir = new File(dir, s);
            }

            if (!dir.exists()) {
                dir.mkdirs();
            }

            return dir;
        } catch (Exception var2) {
            LogWriter.error("Error getting worldsave", var2);
            return null;
        }
    }

    public static MinecraftServer getServer(){
        return MinecraftServer.getServer();
    }
}