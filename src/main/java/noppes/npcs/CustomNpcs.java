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
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockVine;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import nikedemos.markovnames.generators.*;
import noppes.npcs.config.ConfigLoader;
import noppes.npcs.config.ConfigProp;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.*;
import noppes.npcs.enchants.EnchantInterface;
import noppes.npcs.entity.*;
import noppes.npcs.entity.old.*;
import noppes.npcs.scripted.NpcAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Mod(modid = "customnpcs", name = "CustomNpcs", version = "1.7")
public class CustomNpcs {

    @ConfigProp(info = "Disable Chat Bubbles")
    public static boolean EnableChatBubbles = true;

    private static int NewEntityStartId = 0;

    @ConfigProp(info = "Enables/Disables ALL scripting. You can still see and write code in the scripter, but these scripts won't run. True by default")
    public static boolean ScriptingEnabled = true;

    @ConfigProp(info = "Enables/Disables the use of the the ECMA6 Javascript standard instead of ECMA5.1." +
            "\nEnabling this adds many more features to JS in scripts. Only use if on Java 8 or higher." +
            "\nNot all ECMA 6 language is supported through this functionality.")
    public static boolean ScriptingECMA6 = false;

    @ConfigProp(info = "Enables global player event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.")
    public static boolean GlobalPlayerScripts = true;

    @ConfigProp(info = "Enables global forge event scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. True by default.")
    public static boolean GlobalForgeScripts = true;

    @ConfigProp(info = "Enables global NPC scripts to be used in the scripter. You can still see and write code in the scripter, but these scripts won't run. False by default, use with caution!")
    public static boolean GlobalNPCScripts = false;

    @ConfigProp(info = "If scripts are too long (>65535 characters), they normally won't be saved in NBT data.\n" +
            "This config adds additional compound tags to scripts that need it, so you can store much larger scripts!\n" +
            "Every additional compound tag adds 65535 more characters to your script length limit. Use incrementally, with caution.")
    public static int ExpandedScriptLimit = 2;

    @ConfigProp(info = "Enables if Player Information (WAND-USE) should be printed to CustomNPCs Logs. IF on Server \n" +
            "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3")
    public static boolean PlayerLogging = false;

    @ConfigProp(info = "Enables if Scripting Information should be printed to CustomNPCs Logs. IF on Server \n" +
            "Logs will only be present SERVER-SIDE only in CustomNPCs-latest, -1, -2, and -3")
    public static boolean ScriptLogging = false;

    @ConfigProp(info = "Amount of Messages marked as SPAM [5, 3000]. Lower Number means MORE accurate messages \n" +
            "This frequency will determine if the log will print a line with [SPAM] to warn the console.")
    public static int ScriptFrequency = 20;

    @ConfigProp(info = "IN Milliseconds 1s = 1000s. If a recent LOG of the same event is SENT within this threshold it will be ignored.")
    public static int ScriptIgnoreTime = 2000;

    @ConfigProp(info = "Comma separated list of NPC Script Types that will omit these from the logs,\n" +
            "INIT,TICK,INTERACT,DIALOG,DAMAGED,KILLED,ATTACK,TARGET,COLLIDE,KILLS,DIALOG_CLOSE,TIMER")
    public static String ScriptLogIgnoreType = "TICK";

    @ConfigProp(info = "Navigation search range for NPCs. Not recommended to increase if you have a slow pc or on a server. Minimum of 16, maximum of 96.")
    public static int NpcNavRange = 32;

    @ConfigProp(info = "Size limit for NPCs. Default 100, larger sizes may cause lag on clients and servers that can't take it!")
    public static int NpcSizeLimit = 100;

    @ConfigProp(info = "Set to true if you want the dialog command option to be able to use op commands like tp etc")
    public static boolean NpcUseOpCommands = false;

    @ConfigProp(info = "Client sided! Determines where tracking quest info shows up on the screen based on a number from 0 to 8. Default: 3")
    public static int TrackingInfoAlignment = 3;

    @ConfigProp(info = "Client sided! Offsets the tracking info GUI by this amount in the X direction.")
    public static int TrackingInfoX = 0;

    @ConfigProp(info = "Client sided! Offsets the tracking info GUI by this amount in the Y direction.")
    public static int TrackingInfoY = 0;

    @ConfigProp
    public static boolean InventoryGuiEnabled = true;

    @ConfigProp
    public static boolean DisableExtraItems = false;
    @ConfigProp
    public static boolean DisableExtraBlock = false;

    public static long ticks;

    @SidedProxy(clientSide = "noppes.npcs.client.ClientProxy", serverSide = "noppes.npcs.CommonProxy")
    public static CommonProxy proxy;

    @ConfigProp(info = "Enables CustomNpcs startup update message")
    public static boolean EnableUpdateChecker = true;

    public static CustomNpcs instance;

    public static boolean FreezeNPCs = false;

    @ConfigProp(info = "Only ops can create and edit npcs")
    public static boolean OpsOnly = false;

    @ConfigProp(info = "Only ops can see and edit scripts")
    public static boolean ScriptOpsOnly = false;

    @ConfigProp(info = "Comma separated list of player UUIDs that can see and edit scripts. If ScriptsOpsOnly is true,\n" +
            "ops and players with these IDs can see and edit scripts. Example:\n" +
            "b876ec32-e396-476b-a115-8438d83c67d4,069a79f4-44e9-4726-a5be-fca90e38aaf5,be951074-c7ea-4f02-a725-bf017bc88650\n" +
            "Get a player's UUID from a site like NameMC or the API IPlayer.getUniqueID() function!\n" +
            "If left empty and ScriptsOpsOnly is false, anyone can see and edit scripts with a scripter.")
    public static String ScriptDevIDs = "";

    @ConfigProp(info = "Default interact line. Leave empty to not have one")
    public static String DefaultInteractLine = "Hello @p";

    @ConfigProp
    public static boolean DisableEnchants = false;
    @ConfigProp(info = "Start Id for enchants. IDs can only range from 0-256")
    public static int EnchantStartId = 100;

    @ConfigProp(info = "Number of chunk loading npcs that can be active at the same time")
    public static int ChunkLoaders = 20;

    @ConfigProp(info = "The maximum number of images any dialog can hold.")
    public static int DialogImageLimit = 10;

    @ConfigProp(info = "The maximum number of images any dialog can hold.")
    public static int SkinOverlayLimit = 10;

    public static File Dir;

    @ConfigProp(info = "Set to false if you want to disable guns")
    public static boolean GunsEnabled = true;

    @ConfigProp(info = "Enables leaves decay")
    public static boolean LeavesDecayEnabled = true;

    @ConfigProp(info = "Enables Vine Growth")
    public static boolean VineGrowthEnabled = true;

    @ConfigProp(info = "Enables Ice Melting")
    public static boolean IceMeltsEnabled = true;

    @ConfigProp(info = "Normal players can use soulstone on animals")
    public static boolean SoulStoneAnimals = true;

    @ConfigProp(info = "Normal players can use soulstone on villagers")
    public static boolean SoulStoneVillagers = false;

    @ConfigProp(info = "Normal players can use soulstone on all npcs")
    public static boolean SoulStoneNPCs = false;

    @ConfigProp(info = "Normal players can use soulstone on friendly npcs")
    public static boolean SoulStoneFriendlyNPCs = false;

    @ConfigProp(info="When set to Minecraft it will use minecrafts font, when Default it will use OpenSans. Can only use fonts installed on your PC")
    public static String FontType = "Default";

    @ConfigProp(info="Font size for custom fonts (doesn't work with minecrafts font)")
    public static int FontSize = 18;

    @ConfigProp(info = "Enables Overlay Mixins for Conflicts relating to Optifine or other Skin Renderers. If crashes occur, please disable.")
    public static boolean EntityRendererMixin = true;

    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    public static ConfigLoader Config;

    public static ArrayList<UUID> ScriptDevs = new ArrayList<>();

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

        Config = new ConfigLoader(this.getClass(), new File(dir, "config"), "CustomNpcs");
        Config.loadConfig();

        try {
            ScriptDevs.clear();
            String[] uuidStrings = ScriptDevIDs.split(",");
            for (String s : uuidStrings) {
                ScriptDevs.add(UUID.fromString(s));
            }
        } catch (Exception ignored) {}

        if (NpcNavRange < 16) {
            NpcNavRange = 16;
        }
        if (NpcNavRange > 96) {
            NpcNavRange = 96;
        }

        if(NpcSizeLimit < 1)
            NpcSizeLimit = 1;

        if (DialogImageLimit < 0) {
            DialogImageLimit = 0;
        }

        if (SkinOverlayLimit < 0) {
            SkinOverlayLimit = 0;
        }

        if (TrackingInfoAlignment < 0)
            TrackingInfoAlignment = 0;
        if (TrackingInfoAlignment > 8)
            TrackingInfoAlignment = 8;

        if (ExpandedScriptLimit < 0)
            ExpandedScriptLimit = 0;

        if (ScriptFrequency < 5)
            ScriptFrequency = 5;
        if (ScriptFrequency > 3000)
            ScriptFrequency = 3000;

        if (ScriptIgnoreTime < 0)
            ScriptIgnoreTime = 0;

        try {
            String[] ignoreTypes = ScriptLogIgnoreType.split(",");
            for (String s : ignoreTypes) {
                EnumScriptType type = EnumScriptType.valueOfIgnoreCase(s);
                if(type != null){
                    switch (type){
                        case INIT:
                            InitIgnore = true;
                            break;
                        case TICK:
                            TickIgnore = true;
                            break;
                        case INTERACT:
                            InteractIgnore = true;
                            break;
                        case DIALOG:
                            DialogIgnore = true;
                            break;
                        case DAMAGED:
                            DamagedIgnore = true;
                            break;
                        case KILLED:
                            KilledIgnore = true;
                            break;
                        case ATTACK:
                            AttackIgnore = true;
                            break;
                        case TARGET:
                            TargetIgnore = true;
                            break;
                        case COLLIDE:
                            CollideIgnore = true;
                            break;
                        case KILLS:
                            KillsIgnore = true;
                            break;
                        case DIALOG_CLOSE:
                            DialogCloseIgnore = true;
                            break;
                        case TIMER:
                            TimerIgnore = true;
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception ignored) {}

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

        registerNewEntity(EntityChairMount.class, "CustomNpcChairMount", 64, 10, false);
        registerNewEntity(EntityProjectile.class, "throwableitem", 64, 3, true);
        registerNewEntity(EntityMagicProjectile.class, "magicprojectile", 64, 3, true);

        new RecipeController();

        ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkController());

        new CustomNpcsPermissions();

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
        ChunkController.instance.clear();
        new QuestController();
        new PlayerDataController();
        new FactionController();
        new TransportController();
        new GlobalDataController();
        new SpawnController();
        new LinkedNpcController();
        ScriptController.Instance.loadStoredData();
        ScriptController.Instance.loadForgeScripts();
        ScriptController.Instance.loadNPCScripts();
        ScriptController.Instance.loadPlayerScripts();
        ScriptController.HasStart = false;
        NpcAPI.clearCache();

        Set<String> names = Block.blockRegistry.getKeys();
        for(String name : names){
            Block block = (Block) Block.blockRegistry.getObject(name);
            if(block instanceof BlockLeavesBase){
                block.setTickRandomly(LeavesDecayEnabled);
            }
            if(block instanceof BlockVine){
                block.setTickRandomly(VineGrowthEnabled);
            }
            if(block instanceof BlockIce){
                block.setTickRandomly(IceMeltsEnabled);
            }
        }
    }

    //Loading items in the about to start event was corrupting items with a damage value
    @EventHandler
    public void started(FMLServerStartedEvent event) {
        RecipeController.instance.load();
        new DialogController();
        new BankController();
        QuestController.instance.load();
        ScriptController.HasStart = true;
        ServerCloneController.Instance = new ServerCloneController();
    }

    @EventHandler
    public void stopped(FMLServerStoppedEvent event){
        ServerCloneController.Instance = null;
        GlobalDataController.instance.saveData();
    }

    @EventHandler
    public void serverstart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandNoppes());
    }

    private void registerNpc(Class<? extends Entity> cl, String name) {
        EntityRegistry.registerModEntity(cl, name, NewEntityStartId++, this, 64, 3, true);
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

    public static boolean isScriptDev(EntityPlayer player) {
        if(CustomNpcs.ScriptOpsOnly && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()) ||
                ScriptDevs.contains(player.getUniqueID())){
            return true;
        } else return ScriptDevIDs.isEmpty();
    }
}