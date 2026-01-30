package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import foxz.command.CommandNoppes;
import io.github.somehussar.janinoloader.api.IDynamicCompiler;
import io.github.somehussar.janinoloader.api.IDynamicCompilerBuilder;
import kamkeel.npcs.addon.AddonManager;
import kamkeel.npcs.command.CommandKamkeel;
import kamkeel.npcs.command.profile.CommandProfile;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.SyncController;
import kamkeel.npcs.controllers.TelegraphController;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.profile.CNPCData;
import kamkeel.npcs.developer.Developer;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.util.BukkitUtil;
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
import nikedemos.markovnames.generators.MarkovAncientGreek;
import nikedemos.markovnames.generators.MarkovAztec;
import nikedemos.markovnames.generators.MarkovCustomNPCsClassic;
import nikedemos.markovnames.generators.MarkovGenerator;
import nikedemos.markovnames.generators.MarkovJapanese;
import nikedemos.markovnames.generators.MarkovOldNorse;
import nikedemos.markovnames.generators.MarkovRoman;
import nikedemos.markovnames.generators.MarkovSaami;
import nikedemos.markovnames.generators.MarkovSlavic;
import nikedemos.markovnames.generators.MarkovSpanish;
import nikedemos.markovnames.generators.MarkovWelsh;
import noppes.npcs.compat.PixelmonHelper;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.config.LoadConfiguration;
import noppes.npcs.config.legacy.LegacyConfig;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.GlobalDataController;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.ServerTagMapController;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.enchants.EnchantInterface;
import noppes.npcs.entity.EntityChairMount;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityMagicProjectile;
import noppes.npcs.entity.EntityNPCGolem;
import noppes.npcs.entity.EntityNpcCrystal;
import noppes.npcs.entity.EntityNpcDragon;
import noppes.npcs.entity.EntityNpcPony;
import noppes.npcs.entity.EntityNpcSlime;
import noppes.npcs.entity.EntityProjectile;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityAbilitySweeper;
import noppes.npcs.entity.old.EntityNPCDwarfFemale;
import noppes.npcs.entity.old.EntityNPCDwarfMale;
import noppes.npcs.entity.old.EntityNPCElfFemale;
import noppes.npcs.entity.old.EntityNPCElfMale;
import noppes.npcs.entity.old.EntityNPCEnderman;
import noppes.npcs.entity.old.EntityNPCFurryFemale;
import noppes.npcs.entity.old.EntityNPCFurryMale;
import noppes.npcs.entity.old.EntityNPCHumanFemale;
import noppes.npcs.entity.old.EntityNPCHumanMale;
import noppes.npcs.entity.old.EntityNPCOrcFemale;
import noppes.npcs.entity.old.EntityNPCOrcMale;
import noppes.npcs.entity.old.EntityNPCVillager;
import noppes.npcs.entity.old.EntityNpcEnderchibi;
import noppes.npcs.entity.old.EntityNpcMonsterFemale;
import noppes.npcs.entity.old.EntityNpcMonsterMale;
import noppes.npcs.entity.old.EntityNpcNagaFemale;
import noppes.npcs.entity.old.EntityNpcNagaMale;
import noppes.npcs.entity.old.EntityNpcSkeleton;
import noppes.npcs.scripted.NpcAPI;
import somehussar.janino.AdvancedClassFilter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Mod(modid = "customnpcs", name = "CustomNPC+", version = "1.11-beta1")
public class CustomNpcs {

    @SidedProxy(clientSide = "noppes.npcs.client.ClientProxy", serverSide = "noppes.npcs.CommonProxy")
    public static CommonProxy proxy;

    public static File Dir;

    private static int NewEntityStartId = 0;
    public static long ticks;
    public static CustomNpcs instance;

    public static boolean FreezeNPCs = false;

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

    public static MinecraftServer Server;

    private static IDynamicCompiler globalJaninoCompiler;
    @SideOnly(Side.CLIENT)
    private static IDynamicCompiler clientJaninoCompiler;
    @SideOnly(Side.CLIENT)
    private final static Set<Consumer<AdvancedClassFilter>> clientClassFilterConsumer = new HashSet<>();

    @SideOnly(Side.CLIENT)
    public static void addClassesToClientClassFilter(Consumer<AdvancedClassFilter> consumer) {
        clientClassFilterConsumer.add(consumer);
    }

    public CustomNpcs() {
        instance = this;
    }

    public static IDynamicCompiler getDynamicCompiler() {
        if (globalJaninoCompiler == null) {
            globalJaninoCompiler = IDynamicCompilerBuilder.createBuilder().getCompiler();
        }

        return globalJaninoCompiler;
    }

    @SideOnly(Side.CLIENT)
    public static IDynamicCompiler getClientCompiler() {

        if (clientJaninoCompiler == null) {
            AdvancedClassFilter filter = new AdvancedClassFilter()
                .addRegexes("noppes\\.npcs\\.api\\..*")
                .banRegexes(".*ClassLoader.*",
                    ".*File*.",
                    "java\\.lang\\.reflect\\..*");

            for (Consumer<AdvancedClassFilter> consumer : clientClassFilterConsumer) {
                consumer.accept(filter);
            }


            clientJaninoCompiler = IDynamicCompilerBuilder.createBuilder().setClassFilter(filter).getCompiler();
        }

        return clientJaninoCompiler;
    }

    @EventHandler
    public void load(FMLPreInitializationEvent ev) {
        PacketHandler.Instance = new PacketHandler();

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
        if (!configDir.exists()) {
            // Convert Legacy Config to New Config if NO Config Folder Exists
            File legacyFile = new File(legacyPath);
            if (legacyFile.exists()) {
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
        new ScriptHookController();

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
        registerNewEntity(EntityAbilityOrb.class, "abilityorb", 64, 3, true);
        registerNewEntity(EntityAbilityDisc.class, "abilitydisc", 64, 3, true);
        registerNewEntity(EntityAbilityLaser.class, "abilitylaser", 64, 3, true);
        registerNewEntity(EntityAbilityBeam.class, "abilitybeam", 64, 3, true);
        registerNewEntity(EntityAbilitySweeper.class, "abilitysweeper", 64, 3, true);

        new RecipeController();

        ForgeChunkManager.setForcedChunkLoadingCallback(this, new ChunkController());

        new CustomNpcsPermissions();
        new Developer();

        // Load Mod Support
        PixelmonHelper.load();
        new AddonManager();
        new AttributeController();
        new MagicController();
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

        PacketHandler.Instance.registerChannels();
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent ev) {
        proxy.buildPackageIndex();
    }

    @EventHandler
    public void setAboutToStart(FMLServerAboutToStartEvent event) {
        globalJaninoCompiler = null;
        if (FMLCommonHandler.instance().getSide().isClient())
            clientJaninoCompiler = null;

        Server = event.getServer();
        ChunkController.Instance.clear();
        FactionController.getInstance().load();
        MagicController.getInstance().load();
        new PlayerDataController();
        new TagController();
        new TransportController();
        new GlobalDataController();
        new SpawnController();
        new LinkedNpcController();
        new AnimationController();
        AbilityController.Instance.load();
        TelegraphController.init();

        LinkedItemController.getInstance().load();

        // Custom Effects
        CustomEffectController.getInstance().load();

        // Profile Controller
        new ProfileController();
        ProfileController.registerProfileType(new CNPCData());

        ScriptController.Instance.loadStoredData();
        ScriptController.Instance.loadForgeScripts();
        ScriptController.Instance.loadGlobalNPCScripts();
        ScriptController.Instance.loadPlayerScripts();
        ScriptController.HasStart = false;
        NpcAPI.clearCache();
        PlayerDataController.Instance.clearCache();

        Set<String> names = Block.blockRegistry.getKeys();
        for (String name : names) {
            Block block = (Block) Block.blockRegistry.getObject(name);
            if (block instanceof BlockLeavesBase) {
                block.setTickRandomly(ConfigMain.LeavesDecayEnabled);
            }
            if (block instanceof BlockVine) {
                block.setTickRandomly(ConfigMain.VineGrowthEnabled);
            }
            if (block instanceof BlockIce) {
                block.setTickRandomly(ConfigMain.IceMeltsEnabled);
            }
        }
    }

    //Loading items in the about to start event was corrupting items with a damage value
    @EventHandler
    public void started(FMLServerStartedEvent event) {
        RecipeController.Instance.load();
        new BankController();
        new AuctionController();
        DialogController.Instance.load();
        QuestController.Instance.load();
        ScriptController.HasStart = true;
        ServerCloneController.Instance = new ServerCloneController();
        ServerTagMapController.Instance = new ServerTagMapController();
        SyncController.load();

        // Initialize Bukkit integration (loads Vault and Permissions)
        BukkitUtil.init();
        CustomNpcsPermissions.Instance.init();
    }


    @EventHandler
    public void stopped(FMLServerStoppedEvent event) {
        ServerCloneController.Instance = null;
        GlobalDataController.Instance.saveData();
        ScriptController.Instance.saveForgeScripts();
        ScriptController.Instance.savePlayerScripts();
        ScriptController.Instance.saveGlobalNpcScripts();

        // Save auction data synchronously on shutdown
        if (AuctionController.Instance != null) {
            AuctionController.Instance.save();
        }

        if (FMLCommonHandler.instance().getSide().isClient())
            clientJaninoCompiler = null;
        globalJaninoCompiler = null;
    }

    @EventHandler
    public void serverstart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandNoppes());
        event.registerServerCommand(new CommandKamkeel());
        if (ConfigMain.ProfilesEnabled)
            event.registerServerCommand(new CommandProfile());
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

    public static MinecraftServer getServer() {
        return MinecraftServer.getServer();
    }

    public static Side side() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }
}
