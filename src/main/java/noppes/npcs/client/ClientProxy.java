package noppes.npcs.client;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.PacketHandlerPlayer;
import noppes.npcs.blocks.tiles.TileBanner;
import noppes.npcs.blocks.tiles.TileBarrel;
import noppes.npcs.blocks.tiles.TileBeam;
import noppes.npcs.blocks.tiles.TileBigSign;
import noppes.npcs.blocks.tiles.TileBlockAnvil;
import noppes.npcs.blocks.tiles.TileBook;
import noppes.npcs.blocks.tiles.TileCampfire;
import noppes.npcs.blocks.tiles.TileCandle;
import noppes.npcs.blocks.tiles.TileChair;
import noppes.npcs.blocks.tiles.TileCouchWood;
import noppes.npcs.blocks.tiles.TileCouchWool;
import noppes.npcs.blocks.tiles.TileCrate;
import noppes.npcs.blocks.tiles.TileLamp;
import noppes.npcs.blocks.tiles.TileMailbox;
import noppes.npcs.blocks.tiles.TilePedestal;
import noppes.npcs.blocks.tiles.TileShelf;
import noppes.npcs.blocks.tiles.TileSign;
import noppes.npcs.blocks.tiles.TileStool;
import noppes.npcs.blocks.tiles.TileTable;
import noppes.npcs.blocks.tiles.TileTallLamp;
import noppes.npcs.blocks.tiles.TileTombstone;
import noppes.npcs.blocks.tiles.TileWallBanner;
import noppes.npcs.blocks.tiles.TileWeaponRack;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.fx.EntityElementalStaffFX;
import noppes.npcs.client.fx.EntityEnderFX;
import noppes.npcs.client.fx.EntityRainbowFX;
import noppes.npcs.client.gui.GuiBorderBlock;
import noppes.npcs.client.gui.GuiMerchantAdd;
import noppes.npcs.client.gui.GuiNpcDimension;
import noppes.npcs.client.gui.GuiNpcMobSpawner;
import noppes.npcs.client.gui.GuiNpcMobSpawnerMounter;
import noppes.npcs.client.gui.GuiNpcPather;
import noppes.npcs.client.gui.GuiNpcRedstoneBlock;
import noppes.npcs.client.gui.GuiNpcRemoteEditor;
import noppes.npcs.client.gui.GuiNpcWaypoint;
import noppes.npcs.client.gui.GuiScript;
import noppes.npcs.client.gui.global.GuiNPCManageBanks;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.global.GuiNPCManageFactions;
import noppes.npcs.client.gui.global.GuiNPCManageLinkedNpc;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiNPCManageTransporters;
import noppes.npcs.client.gui.global.GuiNpcManageRecipes;
import noppes.npcs.client.gui.global.GuiNpcQuestReward;
import noppes.npcs.client.gui.mainmenu.GuiNPCGlobalMainMenu;
import noppes.npcs.client.gui.mainmenu.GuiNPCInv;
import noppes.npcs.client.gui.mainmenu.GuiNpcAI;
import noppes.npcs.client.gui.mainmenu.GuiNpcAdvanced;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.client.gui.mainmenu.GuiNpcStats;
import noppes.npcs.client.gui.player.GuiBigSign;
import noppes.npcs.client.gui.player.GuiCrate;
import noppes.npcs.client.gui.player.GuiMailbox;
import noppes.npcs.client.gui.player.GuiMailmanWrite;
import noppes.npcs.client.gui.player.GuiNPCBankChest;
import noppes.npcs.client.gui.player.GuiNPCTrader;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.gui.player.GuiNpcFollower;
import noppes.npcs.client.gui.player.GuiNpcFollowerHire;
import noppes.npcs.client.gui.player.GuiTransportSelection;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionInv;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionStats;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionTalents;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeItem;
import noppes.npcs.client.gui.roles.GuiNpcBankSetup;
import noppes.npcs.client.gui.roles.GuiNpcFollowerSetup;
import noppes.npcs.client.gui.roles.GuiNpcItemGiver;
import noppes.npcs.client.gui.roles.GuiNpcTraderSetup;
import noppes.npcs.client.gui.roles.GuiNpcTransporter;
import noppes.npcs.client.model.ModelNPCGolem;
import noppes.npcs.client.model.ModelNpcCrystal;
import noppes.npcs.client.model.ModelNpcDragon;
import noppes.npcs.client.model.ModelNpcSlime;
import noppes.npcs.client.model.ModelSkirtArmor;
import noppes.npcs.client.renderer.NpcItemRenderer;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.client.renderer.RenderNPCHumanMale;
import noppes.npcs.client.renderer.RenderNPCPony;
import noppes.npcs.client.renderer.RenderNpcCrystal;
import noppes.npcs.client.renderer.RenderNpcDragon;
import noppes.npcs.client.renderer.RenderNpcSlime;
import noppes.npcs.client.renderer.RenderProjectile;
import noppes.npcs.client.renderer.blocks.BlockBannerRenderer;
import noppes.npcs.client.renderer.blocks.BlockBarrelRenderer;
import noppes.npcs.client.renderer.blocks.BlockBeamRenderer;
import noppes.npcs.client.renderer.blocks.BlockBigSignRenderer;
import noppes.npcs.client.renderer.blocks.BlockBloodRenderer;
import noppes.npcs.client.renderer.blocks.BlockBookRenderer;
import noppes.npcs.client.renderer.blocks.BlockBorderRenderer;
import noppes.npcs.client.renderer.blocks.BlockCampfireRenderer;
import noppes.npcs.client.renderer.blocks.BlockCandleRenderer;
import noppes.npcs.client.renderer.blocks.BlockCarpentryBenchRenderer;
import noppes.npcs.client.renderer.blocks.BlockChairRenderer;
import noppes.npcs.client.renderer.blocks.BlockCouchWoodRenderer;
import noppes.npcs.client.renderer.blocks.BlockCouchWoolRenderer;
import noppes.npcs.client.renderer.blocks.BlockCrateRenderer;
import noppes.npcs.client.renderer.blocks.BlockLampRenderer;
import noppes.npcs.client.renderer.blocks.BlockMailboxRenderer;
import noppes.npcs.client.renderer.blocks.BlockPedestalRenderer;
import noppes.npcs.client.renderer.blocks.BlockShelfRenderer;
import noppes.npcs.client.renderer.blocks.BlockSignRenderer;
import noppes.npcs.client.renderer.blocks.BlockStoolRenderer;
import noppes.npcs.client.renderer.blocks.BlockTableRenderer;
import noppes.npcs.client.renderer.blocks.BlockTallLampRenderer;
import noppes.npcs.client.renderer.blocks.BlockTombstoneRenderer;
import noppes.npcs.client.renderer.blocks.BlockWallBannerRenderer;
import noppes.npcs.client.renderer.blocks.BlockWeaponRackRenderer;
import noppes.npcs.config.StringCache;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.containers.ContainerCrate;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCGolem;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcCrystal;
import noppes.npcs.entity.EntityNpcDragon;
import noppes.npcs.entity.EntityNpcPony;
import noppes.npcs.entity.EntityNpcSlime;
import noppes.npcs.entity.EntityProjectile;

import org.lwjgl.input.Keyboard;

import tconstruct.client.tabs.InventoryTabFactions;
import tconstruct.client.tabs.InventoryTabQuests;
import tconstruct.client.tabs.InventoryTabVanilla;
import tconstruct.client.tabs.TabRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class ClientProxy extends CommonProxy {
	public static KeyBinding QuestLog;
	
	public static FontContainer Font;
	
	public void load() {
		Font = new FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
		createFolders();
		CustomNpcs.Channel.register(new PacketHandlerClient());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
		new MusicController();

		RenderingRegistry.registerEntityRenderingHandler(EntityNpcPony.class, new RenderNPCPony());
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcCrystal.class, new RenderNpcCrystal(new ModelNpcCrystal(0.5F)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcDragon.class, new RenderNpcDragon(new ModelNpcDragon(0.0F), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcSlime.class, new RenderNpcSlime(new ModelNpcSlime(16), new ModelNpcSlime(0), 0.25F));
		RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, new RenderProjectile());
		RenderingRegistry.registerEntityRenderingHandler(EntityCustomNpc.class, new RenderCustomNpc());
		RenderingRegistry.registerEntityRenderingHandler(EntityNPCGolem.class, new RenderNPCHumanMale(
				new ModelNPCGolem(0), new ModelNPCGolem(1F), new ModelNPCGolem(0.5F)));
		FMLCommonHandler.instance().bus().register(new ClientTickHandler());

		ClientRegistry.bindTileEntitySpecialRenderer(TileBlockAnvil.class, new BlockCarpentryBenchRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileMailbox.class, new BlockMailboxRenderer());
		RenderingRegistry.registerBlockHandler(new BlockBorderRenderer());
		
		if(!CustomNpcs.DisableExtraBlock){
			ClientRegistry.bindTileEntitySpecialRenderer(TileBanner.class, new BlockBannerRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileWallBanner.class, new BlockWallBannerRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileTallLamp.class, new BlockTallLampRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileChair.class, new BlockChairRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileWeaponRack.class, new BlockWeaponRackRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileCrate.class, new BlockCrateRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileCouchWool.class, new BlockCouchWoolRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileCouchWood.class, new BlockCouchWoodRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileTable.class, new BlockTableRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileCandle.class, new BlockCandleRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileLamp.class, new BlockLampRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileStool.class, new BlockStoolRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileBigSign.class, new BlockBigSignRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileBarrel.class, new BlockBarrelRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileCampfire.class, new BlockCampfireRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileTombstone.class, new BlockTombstoneRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileShelf.class, new BlockShelfRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileSign.class, new BlockSignRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileBeam.class, new BlockBeamRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TileBook.class, new BlockBookRenderer());
			ClientRegistry.bindTileEntitySpecialRenderer(TilePedestal.class, new BlockPedestalRenderer());
			RenderingRegistry.registerBlockHandler(new BlockBloodRenderer());
		}
		Minecraft mc = Minecraft.getMinecraft();
		
		QuestLog = new KeyBinding("Quest Log", Keyboard.KEY_L, "key.categories.gameplay");
		
		ClientRegistry.registerKeyBinding(QuestLog);
		mc.gameSettings.loadOptions();
		
		new PresetController(CustomNpcs.Dir);
		
		if(CustomNpcs.EnableUpdateChecker){
			VersionChecker checker = new VersionChecker();
			checker.start();
		}
		
		ClientCloneController.Instance = new ClientCloneController();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        
        if(CustomNpcs.InventoryGuiEnabled){
	        MinecraftForge.EVENT_BUS.register(new TabRegistry());
	        
	        if (TabRegistry.getTabList().size() < 2){
	        	TabRegistry.registerTab(new InventoryTabVanilla());
	        }
        	TabRegistry.registerTab(new InventoryTabFactions());
        	TabRegistry.registerTab(new InventoryTabQuests());
        }
	}

	private void createFolders() {
		File file = new File(CustomNpcs.Dir,"assets/customnpcs");
		if(!file.exists())
			file.mkdirs();
		
		File check = new File(file,"sounds");
		if(!check.exists())
			check.mkdir();
		
		File json = new File(file, "sounds.json");
		if(!json.exists()){
			try {
				json.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(json));
				writer.write("{\n\n}");
				writer.close();
			} catch (IOException e) {
			}
		}

		check = new File(file,"textures");
		if(!check.exists())
			check.mkdir();
		
		File cache = new File(check,"cache");
		if(!cache.exists())
			cache.mkdir();
		((IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new CustomNpcResourceListener());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		if (ID > EnumGuiType.values().length)
			return null;
		EnumGuiType gui = EnumGuiType.values()[ID];
		EntityNPCInterface npc = NoppesUtil.getLastNpc();
		Container container =  this.getContainer(gui, player, x, y, z, npc);
		return getGui(npc, gui, container, x, y, z);
	}

	private GuiScreen getGui(EntityNPCInterface npc, EnumGuiType gui, Container container, int x, int y, int z) {
		if (gui == EnumGuiType.MainMenuDisplay){
			if(npc != null)
				return new GuiNpcDisplay(npc);
			else
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Unable to find npc"));
		}
		else if (gui == EnumGuiType.MainMenuStats)
			return new GuiNpcStats(npc);

		else if (gui == EnumGuiType.MainMenuInv)
			return new GuiNPCInv(npc, (ContainerNPCInv) container);

		else if (gui == EnumGuiType.MainMenuAdvanced)
			return new GuiNpcAdvanced(npc);

		else if(gui == EnumGuiType.QuestReward)
			return new GuiNpcQuestReward(npc, (ContainerNpcQuestReward) container);
		
		else if(gui == EnumGuiType.QuestItem)
			return new GuiNpcQuestTypeItem(npc, (ContainerNpcQuestTypeItem) container);
		
		else if (gui == EnumGuiType.MovingPath)
			return new GuiNpcPather(npc);

		else if (gui == EnumGuiType.ManageFactions)
			return new GuiNPCManageFactions(npc);
		
		else if (gui == EnumGuiType.ManageLinked)
			return new GuiNPCManageLinkedNpc(npc);

		else if (gui == EnumGuiType.ManageTransport)
			return new GuiNPCManageTransporters(npc);

		else if (gui == EnumGuiType.ManageRecipes)
			return new GuiNpcManageRecipes(npc,(ContainerManageRecipes) container);
		
		else if (gui == EnumGuiType.ManageDialogs)
			return new GuiNPCManageDialogs(npc);
		
		else if (gui == EnumGuiType.ManageQuests)
			return new GuiNPCManageQuest(npc);
		
		else if(gui == EnumGuiType.ManageBanks)
        	return new GuiNPCManageBanks(npc,(ContainerManageBanks) container);

		else if (gui == EnumGuiType.MainMenuGlobal)
			return new GuiNPCGlobalMainMenu(npc);

		else if (gui == EnumGuiType.MainMenuAI)
			return new GuiNpcAI(npc);

		else if (gui == EnumGuiType.PlayerFollowerHire)
			return new GuiNpcFollowerHire(npc, (ContainerNPCFollowerHire) container);

		else if (gui == EnumGuiType.PlayerFollower)
			return new GuiNpcFollower(npc, (ContainerNPCFollower) container);
		
		else if(gui == EnumGuiType.PlayerTrader)
			return new GuiNPCTrader(npc, (ContainerNPCTrader) container);
		
		else if(gui == EnumGuiType.PlayerBankSmall || gui == EnumGuiType.PlayerBankUnlock || gui == EnumGuiType.PlayerBankUprade || gui == EnumGuiType.PlayerBankLarge)
			return new GuiNPCBankChest(npc, (ContainerNPCBankInterface) container);

		else if(gui == EnumGuiType.PlayerTransporter)
			return new GuiTransportSelection(npc);
		
		else if(gui == EnumGuiType.Script)
			return new GuiScript(npc);
		
		else if(gui == EnumGuiType.PlayerAnvil)
			return new GuiNpcCarpentryBench((ContainerCarpentryBench) container);
		
		else if (gui == EnumGuiType.SetupFollower)
			return  new GuiNpcFollowerSetup(npc, (ContainerNPCFollowerSetup) container);
		
		else if (gui == EnumGuiType.SetupItemGiver)
			return new GuiNpcItemGiver(npc, (ContainerNpcItemGiver) container);
		
		else if(gui == EnumGuiType.SetupTrader)
			return new GuiNpcTraderSetup(npc, (ContainerNPCTraderSetup) container);

		else if(gui == EnumGuiType.SetupTransporter)
			return new GuiNpcTransporter(npc);
		
		else if(gui == EnumGuiType.SetupBank)
			return new GuiNpcBankSetup(npc);
		
		else if(gui == EnumGuiType.NpcRemote && Minecraft.getMinecraft().currentScreen == null)
			return new GuiNpcRemoteEditor();

		else if(gui == EnumGuiType.PlayerMailman)
			return new GuiMailmanWrite((ContainerMail) container, x == 1, y == 1);
		
		else if(gui == EnumGuiType.PlayerMailbox)
			return new GuiMailbox();
		
		else if(gui == EnumGuiType.MerchantAdd)
			return new GuiMerchantAdd();
		
		else if(gui == EnumGuiType.Crate)
			return new GuiCrate((ContainerCrate)container);

		else if(gui == EnumGuiType.NpcDimensions)
			return new GuiNpcDimension();
		
		else if(gui == EnumGuiType.Border)
			return new GuiBorderBlock(x, y, z);

		else if(gui == EnumGuiType.BigSign)
			return new GuiBigSign(x, y, z);
		
		else if (gui == EnumGuiType.RedstoneBlock)
			return new GuiNpcRedstoneBlock(x, y, z);
		
		else if(gui == EnumGuiType.MobSpawner)
			return new GuiNpcMobSpawner(x, y, z);
		
		else if(gui == EnumGuiType.MobSpawnerMounter)
			return new GuiNpcMobSpawnerMounter(x, y, z);

		else if (gui == EnumGuiType.Waypoint)
			return new GuiNpcWaypoint(x, y, z);
		
		else if (gui == EnumGuiType.Companion)
			return new GuiNpcCompanionStats(npc);
		
		else if (gui == EnumGuiType.CompanionTalent)
			return new GuiNpcCompanionTalents(npc);
		
		else if (gui == EnumGuiType.CompanionInv)
			return new GuiNpcCompanionInv(npc, (ContainerNPCCompanion) container);
		return null;
	}
	
	@Override
	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if(minecraft.thePlayer != player)
			return;

		GuiScreen guiscreen = getGui(null, gui, null, i, j, k);
		

		if (guiscreen != null) {
			minecraft.displayGuiScreen(guiscreen);
		}
	}

	@Override
	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
		openGui(npc, gui, 0, 0, 0);
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
		Minecraft minecraft = Minecraft.getMinecraft();

		Container container = this.getContainer(gui, minecraft.thePlayer, x, y, z, npc);
		GuiScreen guiscreen = getGui(npc, gui,container, x, y, z);

		if (guiscreen != null) {
			minecraft.displayGuiScreen(guiscreen);
		}
	}

	public void openGui(EntityPlayer player, Object guiscreen) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if(!player.worldObj.isRemote || !(guiscreen instanceof GuiScreen))
			return;

		if (guiscreen != null) {
			minecraft.displayGuiScreen((GuiScreen)guiscreen);
		}
		
	}

	@Override
	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
		if(string.equals("Spell")){
			int color = (Integer) ob[0];
			int number = (Integer) ob[1];
	    	for(int i = 0; i < number; i++){
		    	Random rand = player.worldObj.rand;
				double x = (rand.nextDouble() - 0.5D) * (double)player.width;
				double y = player.getEyeHeight();
				double z = (rand.nextDouble() - 0.5D) * (double)player.width;
		
		        double f = (rand.nextDouble() - 0.5D) * 2D;
		        double f1 =  -rand.nextDouble();
		        double f2 = (rand.nextDouble() - 0.5D) * 2D;
	
		        Minecraft.getMinecraft().effectRenderer.addEffect(new EntityElementalStaffFX(player, x, y, z, f, f1, f2, color));
		    }
		}
		else if(string.equals("ModelData")){
			ModelData data = (ModelData) ob[0];
			ModelPartData particles = (ModelPartData) ob[1];
			EntityCustomNpc npc = (EntityCustomNpc) player;
			Minecraft minecraft =  Minecraft.getMinecraft();
			double height = npc.getYOffset() + data.getBodyY();
			Random rand = npc.getRNG();
			if(particles.type == 0){
				for(int i = 0; i< 2; i++){
					EntityEnderFX fx = new EntityEnderFX(npc, (rand.nextDouble() - 0.5D) * (double)player.width, (rand.nextDouble() * (double)player.height) - height - 0.25D, (rand.nextDouble() - 0.5D) * (double)player.width, (rand.nextDouble() - 0.5D) * 2D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2D, particles);
					minecraft.effectRenderer.addEffect(fx);
				}
	    		
			}
			else if(particles.type == 1){
	        	for(int i = 0; i < 2; i++){
		            double x = player.posX + (rand.nextDouble() - 0.5D) * 0.9;
		            double y = (player.posY + rand.nextDouble() * 1.9) - 0.25D - height;
		            double z = player.posZ + (rand.nextDouble() - 0.5D) * 0.9;
		
		            
		            double f = (rand.nextDouble() - 0.5D) * 2D;
		            double f1 =  -rand.nextDouble();
		            double f2 = (rand.nextDouble() - 0.5D) * 2D;
		            
		            minecraft.effectRenderer.addEffect(new EntityRainbowFX(player.worldObj, x, y, z, f, f1, f2));
	        	}
			}
		}
	}
	private ModelSkirtArmor model = new ModelSkirtArmor();

	public ModelBiped getSkirtModel() {
		return model;
	}

	public boolean hasClient() {
		return true;
	}
	
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}


	@Override
	public void registerItem(Item item) {
		MinecraftForgeClient.registerItemRenderer(item, new NpcItemRenderer());
	}

	public static void bindTexture(ResourceLocation location) {
		try{
			if(location == null)
				return;
	        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
	        if(location != null)
	        	texturemanager.bindTexture((ResourceLocation) location);
		}
		catch(NullPointerException ex){
			
		}
		catch(ReportedException ex){
			
		}
	}

	@Override
	public void spawnParticle(String particle, double x, double y, double z,
			double motionX, double motionY, double motionZ, float scale) {
		
		RenderGlobal render = Minecraft.getMinecraft().renderGlobal;
		
		EntityFX fx = render.doSpawnParticle(particle, x, y, z, motionX, motionY, motionZ);
		if(fx == null)
			return;

        if (particle.equals("flame")){
        	ObfuscationReflectionHelper.setPrivateValue(EntityFlameFX.class, (EntityFlameFX)fx, scale, 0);
        }
        else if (particle.equals("smoke")){
        	ObfuscationReflectionHelper.setPrivateValue(EntitySmokeFX.class, (EntitySmokeFX)fx, scale, 0);
        }
	}

	
	public static class FontContainer {
		private StringCache textFont = null;
		public boolean useCustomFont = true;
		
		private FontContainer(){
			
		}
		
		public FontContainer(String fontType, int fontSize) {
			textFont = new StringCache();
			textFont.setDefaultFont("Arial", fontSize, true);
			useCustomFont = !fontType.equalsIgnoreCase("minecraft");
	    	try {
	    		if(!useCustomFont || fontType.isEmpty() || fontType.equalsIgnoreCase("default"))
	    			textFont.setCustomFont(new ResourceLocation("customnpcs","OpenSans.ttf"), fontSize, true);
	    		else
	    			textFont.setDefaultFont(fontType, fontSize, true);
			} catch (Exception e) {
				LogWriter.info("Failed loading font so using Arial");
			}
		}

		public int height(){
			if(useCustomFont)
				return textFont.fontHeight;
			return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
		}
		
		public int width(String text){
			if(useCustomFont)
				return textFont.getStringWidth(text);
			return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
		}

		public FontContainer copy() {
			FontContainer font = new FontContainer();
			font.textFont = textFont;
			font.useCustomFont = useCustomFont;
			return font;
		}

		public void drawString(String text, int x, int y, int color) {
			if(useCustomFont){
				textFont.renderString(text, x, y, color, true);
				textFont.renderString(text, x, y, color, false);
			}
			else{
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
			}
		}

		public String getName() {
			if(!useCustomFont)
				return "Minecraft";
			return textFont.usedFont().getFontName();
		}
	}
}
