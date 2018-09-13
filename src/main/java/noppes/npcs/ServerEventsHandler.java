package noppes.npcs;

import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import noppes.npcs.blocks.tiles.TileBanner;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.controllers.QuestData;
import noppes.npcs.controllers.RecipeCarpentry;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemExcalibur;
import noppes.npcs.items.ItemShield;
import noppes.npcs.items.ItemSoulstoneEmpty;
import noppes.npcs.quests.QuestKill;
import noppes.npcs.roles.RoleFollower;

public class ServerEventsHandler {

	public static EntityVillager Merchant;
	public static Entity mounted;
	
//	private HashMap<String, Integer> exps = new HashMap<String, Integer>();
//
//	@SubscribeEvent
//	public void onUpdate(LivingUpdateEvent event){
//		if(!(event.entityLiving instanceof EntityPlayer))
//			return;
//		EntityPlayer player = (EntityPlayer) event.entityLiving;
//		if(exps.containsKey(player.getCommandSenderName())){
//			int prevExp = exps.get(player.getCommandSenderName());
//			if(prevExp > player.experienceTotal){				
//				player.addChatMessage(new ChatComponentText("Omg exp was gotten:" + (player.experienceTotal - prevExp)));
//			}
//			else if(prevExp < player.experienceTotal){				
//				player.addChatMessage(new ChatComponentText("Omg exp was lost:" + (prevExp - player.experienceTotal)));
//			}
//		}
//		exps.put(player.getCommandSenderName(), player.experienceTotal);
//	}

	@SubscribeEvent
	public void invoke(EntityInteractEvent event) {
		ItemStack item = event.entityPlayer.getCurrentEquippedItem();
		if(item == null)
			return;
		boolean isRemote = event.entityPlayer.worldObj.isRemote;
		boolean npcInteracted = event.target instanceof EntityNPCInterface;

		if(!isRemote && CustomNpcs.OpsOnly && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(event.entityPlayer.getGameProfile())){
			return;
		}
		
		if(!isRemote && item.getItem() == CustomItems.soulstoneEmpty && event.target instanceof EntityLivingBase) {
			((ItemSoulstoneEmpty)item.getItem()).store((EntityLivingBase)event.target, item, event.entityPlayer);			
		}
		
		if(item.getItem() == CustomItems.wand && npcInteracted && !isRemote){
			if (!CustomNpcsPermissions.Instance.hasPermission(event.entityPlayer, CustomNpcsPermissions.NPC_GUI)){
				return;
			}
			event.setCanceled(true);
			NoppesUtilServer.sendOpenGui(event.entityPlayer, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) event.target);
		}
		else if(item.getItem() == CustomItems.cloner && !isRemote && !(event.target instanceof EntityPlayer)){
			NBTTagCompound compound = new NBTTagCompound();
			if(!event.target.writeToNBTOptional(compound))
				return;
			PlayerData data = PlayerDataController.instance.getPlayerData(event.entityPlayer);
			ServerCloneController.Instance.cleanTags(compound);
			if(!Server.sendData((EntityPlayerMP)event.entityPlayer, EnumPacketClient.CLONE, compound))
				event.entityPlayer.addChatMessage(new ChatComponentText("Entity too big to clone"));
			data.cloned = compound;
			event.setCanceled(true);
		}
		else if(item.getItem() == CustomItems.scripter && !isRemote && npcInteracted){
			if(!CustomNpcsPermissions.Instance.hasPermission(event.entityPlayer, CustomNpcsPermissions.NPC_GUI))
				return;
	    	NoppesUtilServer.setEditingNpc(event.entityPlayer, (EntityNPCInterface)event.target);
			event.setCanceled(true);
        	Server.sendData((EntityPlayerMP)event.entityPlayer, EnumPacketClient.GUI, EnumGuiType.Script.ordinal());
		}
		else if(item.getItem() == CustomItems.mount){
			if(!CustomNpcsPermissions.Instance.hasPermission(event.entityPlayer, CustomNpcsPermissions.TOOL_MOUNTER))
				return;
			event.setCanceled(true);
	    	mounted = event.target;
	    	if(isRemote)
	    		CustomNpcs.proxy.openGui(MathHelper.floor_double(mounted.posX), MathHelper.floor_double(mounted.posY), MathHelper.floor_double(mounted.posZ), EnumGuiType.MobSpawnerMounter, event.entityPlayer);
		}
		else if(item.getItem() == CustomItems.wand && event.target instanceof EntityVillager){
			if(!CustomNpcsPermissions.Instance.hasPermission(event.entityPlayer, CustomNpcsPermissions.EDIT_VILLAGER))
				return;
			event.setCanceled(true);
			Merchant = (EntityVillager)event.target;
			
			if(!isRemote){
				EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;
				player.openGui(CustomNpcs.instance, EnumGuiType.MerchantAdd.ordinal(), player.worldObj, 0, 0, 0);
		        MerchantRecipeList merchantrecipelist = Merchant.getRecipes(player);
	
		        if (merchantrecipelist != null)
		        {
	            	Server.sendData(player, EnumPacketClient.VILLAGER_LIST, merchantrecipelist);
		        }
			}
		}
		
	}
	
	@SubscribeEvent
	public void invoke(LivingHurtEvent event) {
		if(!(event.entityLiving instanceof EntityPlayer))
			return;
		
		EntityPlayer player = (EntityPlayer) event.entityLiving;
		if(event.source.isUnblockable() || event.source.isFireDamage())
			return;
		if(!player.isBlocking())
			return;
		ItemStack item = player.getCurrentEquippedItem();
		if(item == null || !(item.getItem() instanceof ItemShield))
			return;
		if(((ItemShield)item.getItem()).material.getDamageVsEntity() < player.getRNG().nextInt(9))
			return;
		float damage = item.getItemDamage() + event.ammount;
		
		item.damageItem((int) event.ammount, player);

		if(damage > item.getMaxDamage())
			event.ammount = damage - item.getMaxDamage();
		else{
			event.ammount = 0;
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void invoke(PlayerInteractEvent event) {
		EntityPlayer player = event.entityPlayer;
		Block block = player.worldObj.getBlock(event.x, event.y, event.z);
		if(event.action == Action.LEFT_CLICK_BLOCK && player.getHeldItem() != null && player.getHeldItem().getItem() == CustomItems.teleporter){
			event.setCanceled(true);
		}
		
		if(block == Blocks.crafting_table && event.action == Action.RIGHT_CLICK_BLOCK && !player.worldObj.isRemote){
			RecipeController controller = RecipeController.instance;
	        NBTTagList list = new NBTTagList();
	        int i = 0;
	        for(RecipeCarpentry recipe : controller.globalRecipes.values()){
	        	list.appendTag(recipe.writeNBT());
	        	i++;
	        	if(i % 10 == 0){
	    	        NBTTagCompound compound = new NBTTagCompound();
	    	        compound.setTag("recipes", list);
	    	        Server.sendData((EntityPlayerMP)player, EnumPacketClient.SYNCRECIPES_ADD, compound);
	    	        list = new NBTTagList();
	        	}
	        }

        	if(i % 10 != 0){
    	        NBTTagCompound compound = new NBTTagCompound();
    	        compound.setTag("recipes", list);
    	        Server.sendData((EntityPlayerMP)player, EnumPacketClient.SYNCRECIPES_ADD, compound);
        	}
	        Server.sendData((EntityPlayerMP)player, EnumPacketClient.SYNCRECIPES_WORKBENCH);
		}		
		if(block == CustomItems.carpentyBench && event.action == Action.RIGHT_CLICK_BLOCK && !player.worldObj.isRemote){
			RecipeController controller = RecipeController.instance;
	        NBTTagList list = new NBTTagList();
	        int i = 0;
	        for(RecipeCarpentry recipe : controller.anvilRecipes.values()){
	        	list.appendTag(recipe.writeNBT());
	        	i++;
	        	if(i % 10 == 0){
	    	        NBTTagCompound compound = new NBTTagCompound();
	    	        compound.setTag("recipes", list);
	    	        Server.sendData((EntityPlayerMP)player, EnumPacketClient.SYNCRECIPES_ADD, compound);
	    	        list = new NBTTagList();
	        	}
	        }

        	if(i % 10 != 0){
    	        NBTTagCompound compound = new NBTTagCompound();
    	        compound.setTag("recipes", list);
    	        Server.sendData((EntityPlayerMP)player, EnumPacketClient.SYNCRECIPES_ADD, compound);
        	}
	        Server.sendData((EntityPlayerMP)player, EnumPacketClient.SYNCRECIPES_CARPENTRYBENCH);
		}
		if((block == CustomItems.banner || block == CustomItems.wallBanner || block == CustomItems.sign)  && event.action == Action.RIGHT_CLICK_BLOCK){
			ItemStack item = player.inventory.getCurrentItem();
			if(item == null || item.getItem() == null)
				return;
			int y = event.y;
			int meta = player.worldObj.getBlockMetadata(event.x, event.y, event.z);
			if(meta >= 7)
				y--;
			TileBanner tile = (TileBanner)player.worldObj.getTileEntity(event.x, y, event.z);
			if(!tile.canEdit()){
		        if(item.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)){
		        	tile.time = System.currentTimeMillis();
		        	if(player.worldObj.isRemote)
		        		player.addChatComponentMessage(new ChatComponentTranslation("availability.editIcon"));
		        }
				return;
			}

        	if(!player.worldObj.isRemote){
				tile.icon = item.copy();
		    	player.worldObj.markBlockForUpdate(event.x, y, event.z);
		    	event.setCanceled(true);
        	}
	    	
		}
	}

	@SubscribeEvent
	public void invoke(LivingDeathEvent event) {
		if(event.entityLiving.worldObj.isRemote)
			return;
		if(event.source.getEntity() != null){
			if(event.source.getEntity() instanceof EntityPlayer){
				doExcalibur((EntityPlayer) event.source.getEntity(),event.entityLiving);
			}
			
			if(event.source.getEntity() instanceof EntityNPCInterface && event.entityLiving != null){
				EntityNPCInterface npc = (EntityNPCInterface) event.source.getEntity();
				Line line = npc.advanced.getKillLine();
				if(line != null)
					npc.saySurrounding(line.formatTarget(event.entityLiving));

				npc.script.callScript(EnumScriptType.KILLS, "target", event.entityLiving);
			}
			
			EntityPlayer player = null;
			if(event.source.getEntity() instanceof EntityPlayer)
				player = (EntityPlayer) event.source.getEntity();
			else if(event.source.getEntity() instanceof EntityNPCInterface && ((EntityNPCInterface)event.source.getEntity()).advanced.role == EnumRoleType.Follower)
				player = ((RoleFollower)((EntityNPCInterface)event.source.getEntity()).roleInterface).owner;
			if(player != null){
				doQuest(player, event.entityLiving, true);
		
				if(event.entityLiving instanceof EntityNPCInterface)
					doFactionPoints(player, (EntityNPCInterface)event.entityLiving);
			}
		}
		if(event.entityLiving instanceof EntityPlayer){
			PlayerData data = PlayerDataController.instance.getPlayerData((EntityPlayer)event.entityLiving);
			data.saveNBTData(null);
		}
	}

	private void doExcalibur(EntityPlayer player, EntityLivingBase entity) {
		ItemStack item = player.getCurrentEquippedItem();
		if(item == null || item.getItem() != CustomItems.excalibur)
			return;
		Server.sendData((EntityPlayerMP)player, EnumPacketClient.PLAY_MUSIC, "customnpcs:songs.excalibur");
		player.addChatMessage(new ChatComponentTranslation("<" + StatCollector.translateToLocal(item.getItem().getUnlocalizedName() + ".name") + "> " + ItemExcalibur.quotes[player.getRNG().nextInt(ItemExcalibur.quotes.length)]));
	}

	private void doFactionPoints(EntityPlayer player, EntityNPCInterface npc) {
		npc.advanced.factions.addPoints(player);
	}

	private void doQuest(EntityPlayer player, EntityLivingBase entity, boolean all) {
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
		boolean change = false;
		String entityName = EntityList.getEntityString(entity);		
		
		for(QuestData data : playerdata.activeQuests.values()){
			if(data.quest.type != EnumQuestType.Kill && data.quest.type != EnumQuestType.AreaKill)
				continue;
			if(data.quest.type == EnumQuestType.AreaKill && all){
				List<EntityPlayer> list = player.worldObj.getEntitiesWithinAABB(EntityPlayer.class, entity.boundingBox.expand(10, 10, 10));
				for(EntityPlayer pl : list)
					if(pl != player)
						doQuest(pl, entity, false);
			
			}
			String name = entityName;
			QuestKill quest = (QuestKill) data.quest.questInterface;
			if(quest.targets.containsKey(entity.getCommandSenderName()))
				name = entity.getCommandSenderName();
			else if(!quest.targets.containsKey(name))
				continue;
			HashMap<String, Integer> killed = quest.getKilled(data);
			if(killed.containsKey(name) && killed.get(name) >= quest.targets.get(name))
				continue;
			int amount = 0;
			if(killed.containsKey(name))
				amount = killed.get(name);
			killed.put(name, amount + 1);
			quest.setKilled(data, killed);
			change = true;
		}
		if(!change)
			return;
		
		playerdata.checkQuestCompletion(player,EnumQuestType.Kill);
	}

	@SubscribeEvent
	public void pickUp(EntityItemPickupEvent event){
		if(event.entityPlayer.worldObj.isRemote)
			return;
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(event.entityPlayer).questData;
		playerdata.checkQuestCompletion(event.entityPlayer, EnumQuestType.Item);
	}

	@SubscribeEvent
	public void world(EntityJoinWorldEvent event){
		if(event.world.isRemote || !(event.entity instanceof EntityPlayer))
			return;
		PlayerData data = PlayerDataController.instance.getPlayerData((EntityPlayer) event.entity);
		data.updateCompanion(event.world);
	}

	@SubscribeEvent
	public void populateChunk(PopulateChunkEvent.Post event){
		NPCSpawning.performWorldGenSpawning(event.world, event.chunkX, event.chunkZ, event.rand);
	}
}
