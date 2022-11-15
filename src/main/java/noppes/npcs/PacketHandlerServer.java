package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import foxz.utils.Market;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.LinkedNpcController.LinkedData;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.scripted.item.ScriptCustomItem;

public class PacketHandlerServer{

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
		if(CustomNpcs.OpsOnly && !NoppesUtilServer.isOp(player)){
			warn(player, "tried to use custom npcs without being an op");
			return;
		}
		ByteBuf buffer = event.packet.payload();
		EnumPacketServer type = null;
		try {
			type = EnumPacketServer.values()[buffer.readInt()];

			ItemStack item = player.inventory.getCurrentItem();

			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);

			if (type == EnumPacketServer.IsGuiOpen) {
				isGuiOpenPacket(buffer, player);
				return;
			} else if (type == EnumPacketServer.CustomGuiButton && player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiButton((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui) player.openContainer).customGui, buffer.readInt());
				return;
			} else if (type == EnumPacketServer.CustomGuiSlotChange && player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiSlot((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui) player.openContainer).customGui, buffer.readInt());
				return;
			} else if (type == EnumPacketServer.CustomGuiUnfocused && player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiUnfocused((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui) player.openContainer).customGui, buffer.readInt());
				return;
			} else if (type == EnumPacketServer.CustomGuiScrollClick && player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiScrollClick((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui) player.openContainer).customGui, buffer.readInt(), buffer.readInt(), CustomGuiController.readScrollSelection(buffer), buffer.readBoolean());
				return;
			} else if (type == EnumPacketServer.CustomGuiClose) {
				EventHooks.onCustomGuiClose((IPlayer) NpcAPI.Instance().getIEntity(player), (new ScriptGui()).fromNBT(Server.readNBT(buffer)));
				return;
			} else if (type == EnumPacketServer.UpdateTrackedQuest) {
				updateTrackedQuest(buffer, player);
				return;
			}

			if(type.needsNpc && npc == null){

			}
			else if(type.hasPermission() && !CustomNpcsPermissions.hasPermission(player, type.permission)){
				//player doesnt have permission to do this
			}
			else if(item == null && (type == EnumPacketServer.ScriptPlayerGet || type == EnumPacketServer.ScriptPlayerSave || type == EnumPacketServer.ScriptNPCGet || type == EnumPacketServer.ScriptNPCSave || type == EnumPacketServer.ScriptForgeGet || type == EnumPacketServer.ScriptForgeSave))
				warn(player, "tried to use custom npcs without a tool in hand, probably a hacker");
			else {
				if (item != null) {
					if (item.getItem() == CustomItems.wand)
						wandPackets(type, buffer, player, npc);
					else if (item.getItem() == CustomItems.moving)
						movingPackets(type, buffer, player, npc);
					else if (item.getItem() == CustomItems.mount)
						mountPackets(type, buffer, player);
					else if (item.getItem() == CustomItems.cloner)
						clonePackets(type, buffer, player);
					else if (item.getItem() == CustomItems.teleporter)
						featherPackets(type, buffer, player);
					else if (item.getItem() == Item.getItemFromBlock(CustomItems.waypoint) || item.getItem() == Item.getItemFromBlock(CustomItems.border) || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock))
						blockPackets(type, buffer, player);
					else if (CustomNpcs.isScriptDev(player)) {
						if (type == EnumPacketServer.ScriptPlayerGet || type == EnumPacketServer.ScriptPlayerSave)
							playerScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptNPCGet || type == EnumPacketServer.ScriptNPCSave)
							npcScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptForgeGet || type == EnumPacketServer.ScriptForgeSave)
							forgeScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptItemDataGet || type == EnumPacketServer.ScriptItemDataSave)
							itemScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptGlobalGuiDataGet || type == EnumPacketServer.ScriptGlobalGuiDataSave)
							getScriptsEnabled(type, buffer, player);
						else if (item.getItem() == CustomItems.scripter)
							scriptPackets(type, buffer, player, npc);
					}
				}
			}
		} catch (Exception e) {
			LogWriter.error("Error with EnumPacketServer." + type, e);
		}
	}

	private void updateTrackedQuest(ByteBuf buffer, EntityPlayerMP player) {
		String trackedQuestString = Server.readString(buffer);
		if (trackedQuestString != null && trackedQuestString.contains(":")) {
			String[] splitString = trackedQuestString.split(":");
			if(splitString.length < 2){
				return;
			}
			String categoryName = splitString[0];
			String questName = splitString[1];

			for (QuestCategory category : QuestController.instance.categories.values()) {
				if (category.title.equals(categoryName)) {
					for (Quest quest : category.quests.values()) {
						if (quest.title.equals(questName)) {
							PlayerDataController.instance.getPlayerData(player).questData.trackedQuest = quest;
							NoppesUtilPlayer.sendTrackedQuestData(player, quest);
							return;
						}
					}
				}
			}
		}

		PlayerDataController.instance.getPlayerData(player).questData.trackedQuest = null;
		Server.sendData(player, EnumPacketClient.OVERLAY_QUEST_TRACKING);
	}

	private void isGuiOpenPacket(ByteBuf buffer, EntityPlayerMP player) throws IOException {
		NoppesUtilServer.isGUIOpen(buffer, player);
	}

	private void getScriptsEnabled(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if (type == EnumPacketServer.ScriptGlobalGuiDataGet) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("ScriptsEnabled", CustomNpcs.ScriptingEnabled);
			compound.setBoolean("PlayerScriptsEnabled", CustomNpcs.GlobalPlayerScripts);
			compound.setBoolean("GlobalNPCScriptsEnabled", CustomNpcs.GlobalNPCScripts);
			compound.setBoolean("ForgeScriptsEnabled", CustomNpcs.GlobalForgeScripts);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.ScriptGlobalGuiDataSave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			CustomNpcs.ScriptingEnabled = compound.getBoolean("ScriptsEnabled");
			CustomNpcs.GlobalPlayerScripts = compound.getBoolean("PlayerScriptsEnabled");
			CustomNpcs.GlobalNPCScripts = compound.getBoolean("GlobalNPCScriptsEnabled");
			CustomNpcs.GlobalForgeScripts = compound.getBoolean("ForgeScriptsEnabled");
		}
	}

	private void scriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws Exception {
		if(type == EnumPacketServer.ScriptDataSave){
			npc.script.readFromNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.script.hasInited = false;
			if(CustomNpcs.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s SAVED NPC %s (%s, %s, %s) [%s]", "SCRIPTER", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)(npc).posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
		}
		else if(type == EnumPacketServer.ScriptDataGet){
			NBTTagCompound compound = npc.script.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
	}

	private void playerScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		NBTTagCompound compound;
		if(type == EnumPacketServer.ScriptPlayerGet) {
			PlayerDataScript data = ScriptController.Instance.playerScripts;
			compound = data.writeToNBT(new NBTTagCompound());
			compound.setBoolean("ScriptEnabled", ScriptController.Instance.playerScripts.getEnabled());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if(type == EnumPacketServer.ScriptPlayerSave) {
			ScriptController.Instance.setPlayerScripts(Server.readNBT(buffer));
		}
	}

	private void forgeScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		NBTTagCompound compound;
		if (type == EnumPacketServer.ScriptForgeGet) {
			ForgeDataScript data = ScriptController.Instance.forgeScripts;
			compound = data.writeToNBT(new NBTTagCompound());
			compound.setBoolean("ScriptEnabled", ScriptController.Instance.forgeScripts.getEnabled());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, new Object[]{compound});
		} else if (type == EnumPacketServer.ScriptForgeSave) {
			ScriptController.Instance.setForgeScripts(Server.readNBT(buffer));
		}
	}

	private void npcScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		NBTTagCompound compound;
		if(type == EnumPacketServer.ScriptNPCGet) {
			NPCDataScript data = ScriptController.Instance.npcScripts;
			compound = data.writeToNBT(new NBTTagCompound());
			compound.setBoolean("ScriptEnabled", ScriptController.Instance.npcScripts.getEnabled());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if(type == EnumPacketServer.ScriptNPCSave) {
			ScriptController.Instance.setNPCScripts(Server.readNBT(buffer));
		}
	}

	private void itemScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		if (type == EnumPacketServer.ScriptItemDataGet) {
			ScriptCustomItem iw = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
			NBTTagCompound compound = iw.getMCNbt();
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, new Object[]{compound});
		} else if (type == EnumPacketServer.ScriptItemDataSave) {
			if (!player.capabilities.isCreativeMode) {
				return;
			}

			NBTTagCompound compound = Server.readNBT(buffer);
			ScriptCustomItem wrapper = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
			wrapper.setMCNbt(compound);
			wrapper.lastInited = -1L;
			wrapper.saveScriptData();
			player.sendContainerToPlayer(player.inventoryContainer);
		}
	}

	private void featherPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if(type == EnumPacketServer.DimensionsGet){
			HashMap<String,Integer> map = new HashMap<String,Integer>();
			for(int id : DimensionManager.getStaticDimensionIDs()){
				WorldProvider provider = DimensionManager.createProviderFor(id);
				map.put(provider.getDimensionName(), id);
			}
			NoppesUtilServer.sendScrollData(player, map);
		}
		else if(type == EnumPacketServer.DimensionTeleport){
			int dimension = buffer.readInt();
			WorldServer world = MinecraftServer.getServer().worldServerForDimension(dimension);
			ChunkCoordinates coords = world.getEntrancePortalLocation();
			if(coords == null){
				coords = world.getSpawnPoint();
				if(!world.isAirBlock(coords.posX, coords.posY, coords.posZ))
					coords.posY = world.getTopSolidOrLiquidBlock(coords.posX, coords.posZ);
				else{
					while(world.isAirBlock(coords.posX, coords.posY - 1, coords.posZ) && coords.posY > 0){
						coords.posY--;
					}
					if(coords.posY == 0)
						coords.posY = world.getTopSolidOrLiquidBlock(coords.posX, coords.posZ);
				}
			}
			NoppesUtilPlayer.teleportPlayer(player, coords.posX, coords.posY, coords.posZ, dimension);
		}
	}

	private void movingPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws IOException {
		if(type == EnumPacketServer.MovingPathGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.ai.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MovingPathSave){
			npc.ai.setMovingPath(NBTTags.getIntegerArraySet(Server.readNBT(buffer).getTagList("MovingPathNew",10)));
		}
	}

	private void blockPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if (type == EnumPacketServer.SaveTileEntity) {
			NoppesUtilServer.saveTileEntity(player, Server.readNBT(buffer));
		}
		else if(type == EnumPacketServer.GetTileEntity){
			TileEntity tile = player.worldObj.getTileEntity(buffer.readInt(), buffer.readInt(), buffer.readInt());
			NBTTagCompound compound = new NBTTagCompound();
			tile.writeToNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.DialogCategoriesGet){
			NoppesUtilServer.sendScrollData(player, DialogController.instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogsGetFromDialog){
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if(dialog == null)
				return;
			NoppesUtilServer.sendDialogData(player,dialog.category);
		}
		else if(type == EnumPacketServer.DialogsGet){
			NoppesUtilServer.sendDialogData(player,DialogController.instance.categories.get(buffer.readInt()));
		}
		else if(type == EnumPacketServer.QuestsGetFromQuest){
			Quest quest = QuestController.instance.quests.get(buffer.readInt());
			if(quest == null)
				return;
			NoppesUtilServer.sendQuestData(player,quest.category);
		}
		else if(type == EnumPacketServer.QuestCategoriesGet){
			NoppesUtilServer.sendQuestCategoryData(player);
		}
		else if(type == EnumPacketServer.QuestsGet){
			QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
			NoppesUtilServer.sendQuestData(player,category);
		}
		else if(type == EnumPacketServer.FactionsGet){
			NoppesUtilServer.sendFactionDataAll(player);
		}
		else if(type == EnumPacketServer.DialogGet){
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if(dialog != null){
				NBTTagCompound compound = dialog.writeToNBT(new NBTTagCompound());
				Quest quest = QuestController.instance.quests.get(dialog.quest);
				if(quest != null)
					compound.setString("DialogQuestName", quest.title);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			}
		}
		else if(type == EnumPacketServer.QuestGet){
			Quest quest = QuestController.instance.quests.get(buffer.readInt());
			if(quest != null){
				NBTTagCompound compound = new NBTTagCompound();
				if(quest.hasNewQuest())
					compound.setString("NextQuestTitle", quest.getNextQuest().title);
				Server.sendData(player, EnumPacketClient.GUI_DATA, quest.writeToNBT(compound));
			}
		}
		else if(type == EnumPacketServer.FactionGet){
			NBTTagCompound compound = new NBTTagCompound();
			Faction faction = FactionController.getInstance().get(buffer.readInt());
			faction.writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
	}

	private void wandPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws IOException{
		if(type == EnumPacketServer.Delete){
			if(CustomNpcs.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s DELETE NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)npc.posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
			npc.delete();
			NoppesUtilServer.deleteNpc(npc,player);
		}
		else if(type == EnumPacketServer.LinkedAdd){
			LinkedNpcController.Instance.addData(Server.readString(buffer));

			List<String> list = new ArrayList<String>();
			for(LinkedData data : LinkedNpcController.Instance.list)
				list.add(data.name);
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.SCROLL_LIST, list);
		}
		else if(type == EnumPacketServer.LinkedRemove){
			LinkedNpcController.Instance.removeData(Server.readString(buffer));

			List<String> list = new ArrayList<String>();
			for(LinkedData data : LinkedNpcController.Instance.list)
				list.add(data.name);
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.SCROLL_LIST, list);
		}
		else if(type == EnumPacketServer.LinkedGetAll){
			List<String> list = new ArrayList<String>();
			for(LinkedData data : LinkedNpcController.Instance.list)
				list.add(data.name);
			Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
			if(npc != null)
				Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, npc.linkedName);
		}
		else if(type == EnumPacketServer.LinkedSet){
			npc.linkedName = Server.readString(buffer);
			LinkedNpcController.Instance.loadNpcData(npc);
		}
		else if(type == EnumPacketServer.NpcMenuClose){
			npc.reset();
			if(npc.linkedData != null)
				LinkedNpcController.Instance.saveNpcData(npc);
			if(CustomNpcs.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s CLOSE NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)npc.posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
			NoppesUtilServer.setEditingNpc(player, null);
		}
		else if(type == EnumPacketServer.BanksGet){
			NoppesUtilServer.sendBankDataAll(player);
		}
		else if(type == EnumPacketServer.BankGet){
			Bank bank = BankController.getInstance().getBank(buffer.readInt());
			NoppesUtilServer.sendBank(player,bank);
		}
		else if(type == EnumPacketServer.BankSave){
			Bank bank = new Bank();
			bank.readEntityFromNBT(Server.readNBT(buffer));
			BankController.getInstance().saveBank(bank);
			NoppesUtilServer.sendBankDataAll(player);
			NoppesUtilServer.sendBank(player,bank);
		}
		else if(type == EnumPacketServer.BankRemove){
			BankController.getInstance().removeBank(buffer.readInt());
			NoppesUtilServer.sendBankDataAll(player);
			NoppesUtilServer.sendBank(player,new Bank());
		}
		else if(type == EnumPacketServer.RemoteMainMenu){
			Entity entity = player.worldObj.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) entity);
			if(CustomNpcs.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s OPEN NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), ((EntityNPCInterface)entity).display.getName(), entity.posX, entity.posY, entity.posZ,  entity.worldObj.getWorldInfo().getWorldName()));
			}
		}
		else if(type == EnumPacketServer.RemoteDelete){
			Entity entity = player.worldObj.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			npc = (EntityNPCInterface) entity;
			if(CustomNpcs.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s DELETE NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)npc.posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
			npc.delete();
			NoppesUtilServer.deleteNpc(npc,player);
			NoppesUtilServer.sendNearbyNpcs(player);
		}
		else if(type == EnumPacketServer.RemoteNpcsGet){
			NoppesUtilServer.sendNearbyNpcs(player);
			Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, CustomNpcs.FreezeNPCs?"Unfreeze Npcs":"Freeze Npcs");
		}
		else if(type == EnumPacketServer.RemoteFreeze){
			CustomNpcs.FreezeNPCs = !CustomNpcs.FreezeNPCs;
			Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, CustomNpcs.FreezeNPCs?"Unfreeze Npcs":"Freeze Npcs");
		}
		else if(type == EnumPacketServer.RemoteReset){
			Entity entity = player.worldObj.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			npc = (EntityNPCInterface) entity;
			npc.reset();
		}
		else if(type == EnumPacketServer.RemoteTpToNpc){
			Entity entity = player.worldObj.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			npc = (EntityNPCInterface) entity;
			player.playerNetServerHandler.setPlayerLocation(npc.posX, npc.posY, npc.posZ, 0, 0);
		}
		else if(type == EnumPacketServer.Gui){
			EnumGuiType gui = EnumGuiType.values()[buffer.readInt()];
			int i = buffer.readInt();
			int j = buffer.readInt();
			int k = buffer.readInt();
			NoppesUtilServer.sendOpenGui(player, gui, npc, i, j, k);
		}
		else if(type == EnumPacketServer.RecipesGet){
			NoppesUtilServer.sendRecipeData(player,buffer.readInt());
		}
		else if(type == EnumPacketServer.RecipeGet){
			RecipeCarpentry recipe = RecipeController.instance.getRecipe(buffer.readInt());
			NoppesUtilServer.setRecipeGui(player,recipe);
		}
		else if(type == EnumPacketServer.RecipeRemove){
			RecipeCarpentry recipe = RecipeController.instance.delete(buffer.readInt());
			NoppesUtilServer.sendRecipeData(player, recipe.isGlobal?3:4);
			NoppesUtilServer.setRecipeGui(player,new RecipeCarpentry(""));
		}
		else if(type == EnumPacketServer.RecipeSave){
			RecipeCarpentry recipe = RecipeController.instance.saveRecipe(Server.readNBT(buffer));
			NoppesUtilServer.sendRecipeData(player, recipe.isGlobal?3:4);
			NoppesUtilServer.setRecipeGui(player,recipe);
		}
		else if(type == EnumPacketServer.NaturalSpawnGetAll){
			NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
		}
		else if(type == EnumPacketServer.NaturalSpawnGet){
			SpawnData spawn = SpawnController.instance.getSpawnData(buffer.readInt());
			if(spawn != null){
				Server.sendData(player, EnumPacketClient.GUI_DATA, spawn.writeNBT(new NBTTagCompound()));
			}
		}
		else if(type == EnumPacketServer.NaturalSpawnSave){
			SpawnData data = new SpawnData();
			data.readNBT(Server.readNBT(buffer));
			SpawnController.instance.saveSpawnData(data);

			NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
		}
		else if(type == EnumPacketServer.NaturalSpawnRemove){
			SpawnController.instance.removeSpawnData(buffer.readInt());
			NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogCategorySave){
			DialogCategory category = new DialogCategory();
			category.readNBT(Server.readNBT(buffer));
			DialogController.instance.saveCategory(category);
			NoppesUtilServer.sendScrollData(player, DialogController.instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogCategoryRemove){
			DialogController.instance.removeCategory(buffer.readInt());
			NoppesUtilServer.sendScrollData(player, DialogController.instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogCategoryGet){
			DialogCategory category = DialogController.instance.categories.get(buffer.readInt());
			if(category != null){
				NBTTagCompound comp = category.writeNBT(new NBTTagCompound());
				comp.removeTag("Dialogs");
				Server.sendData(player, EnumPacketClient.GUI_DATA, comp);
			}
		}
		else if(type == EnumPacketServer.DialogSave){
			int category = buffer.readInt();
			Dialog dialog = new Dialog();
			dialog.readNBT(Server.readNBT(buffer));
			DialogController.instance.saveDialog(category,dialog);
			if(dialog.category != null)
				NoppesUtilServer.sendDialogData(player,dialog.category);
		}
		else if(type == EnumPacketServer.QuestOpenGui){
			Quest quest = new Quest();
			int gui = buffer.readInt();
			quest.readNBT(Server.readNBT(buffer));
			NoppesUtilServer.setEditingQuest(player,quest);
			player.openGui(CustomNpcs.instance, gui , player.worldObj, 0, 0, 0);
		}
		else if(type == EnumPacketServer.DialogRemove){
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if(dialog != null && dialog.category != null){
				DialogController.instance.removeDialog(dialog);
				NoppesUtilServer.sendDialogData(player,dialog.category);
			}
		}
		else if(type == EnumPacketServer.DialogNpcGet){
			NoppesUtilServer.sendNpcDialogs(player);
		}
		else if(type == EnumPacketServer.DialogNpcSet){
			int slot = buffer.readInt();
			int dialog = buffer.readInt();
			DialogOption option = NoppesUtilServer.setNpcDialog(slot,dialog,player);
			if(option != null && option.hasDialog()){
				NBTTagCompound compound = option.writeNBT();
				compound.setInteger("Position", slot);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			}
		}
		else if(type == EnumPacketServer.DialogNpcRemove){
			npc.dialogs.remove(buffer.readInt());
		}
		else if(type == EnumPacketServer.QuestCategoryGet){
			QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
			if(category != null){
				NBTTagCompound comp = category.writeNBT(new NBTTagCompound());
				comp.removeTag("Dialogs");
				Server.sendData(player, EnumPacketClient.GUI_DATA, comp);
			}
		}
		else if(type == EnumPacketServer.QuestCategorySave){
			QuestCategory category = new QuestCategory();
			category.readNBT(Server.readNBT(buffer));
			QuestController.instance.saveCategory(category);
			NoppesUtilServer.sendQuestCategoryData(player);
		}
		else if(type == EnumPacketServer.QuestCategoryRemove){
			QuestController.instance.removeCategory(buffer.readInt());
			NoppesUtilServer.sendQuestCategoryData(player);
		}
		else if(type == EnumPacketServer.QuestSave){
			int category = buffer.readInt();
			Quest quest = new Quest();
			quest.readNBT(Server.readNBT(buffer));
			QuestController.instance.saveQuest(category, quest);
			if(quest.category != null)
				NoppesUtilServer.sendQuestData(player,quest.category);
		}
		else if(type == EnumPacketServer.QuestDialogGetTitle){
			Dialog quest = DialogController.instance.dialogs.get(buffer.readInt());
			Dialog quest2 = DialogController.instance.dialogs.get(buffer.readInt());
			Dialog quest3 = DialogController.instance.dialogs.get(buffer.readInt());
			NBTTagCompound compound = new NBTTagCompound();
			if(quest != null)
				compound.setString("1", quest.title);
			if(quest2 != null)
				compound.setString("2", quest2.title);
			if(quest3 != null)
				compound.setString("3", quest3.title);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.QuestRemove){
			Quest quest = QuestController.instance.quests.get(buffer.readInt());
			if(quest != null){
				QuestController.instance.removeQuest(quest);
				NoppesUtilServer.sendQuestData(player,quest.category);
			}
		}
		else if(type == EnumPacketServer.TransportCategoriesGet){
			NoppesUtilServer.sendTransportCategoryData(player);
		}
		else if(type == EnumPacketServer.TransportCategorySave){
			TransportController.getInstance().saveCategory(Server.readString(buffer), buffer.readInt());
		}
		else if(type == EnumPacketServer.TransportCategoryRemove){
			TransportController.getInstance().removeCategory(buffer.readInt());
			NoppesUtilServer.sendTransportCategoryData(player);
		}
		else if(type == EnumPacketServer.TransportRemove){
			int id = buffer.readInt();
			TransportLocation loc = TransportController.getInstance().removeLocation(id);
			if(loc != null)
				NoppesUtilServer.sendTransportData(player,loc.category.id);
		}
		else if(type == EnumPacketServer.TransportsGet){
			NoppesUtilServer.sendTransportData(player,buffer.readInt());
		}
		else if(type == EnumPacketServer.TransportSave){
			int cat = buffer.readInt();
			TransportLocation location = TransportController.getInstance().saveLocation(cat,Server.readNBT(buffer), npc);
			if(location != null){
				if(npc.advanced.role != EnumRoleType.Transporter)
					return;
				RoleTransporter role = (RoleTransporter) npc.roleInterface;
				role.setTransport(location);
			}
		}
		else if(type == EnumPacketServer.TransportGetLocation){
			if(npc.advanced.role != EnumRoleType.Transporter)
				return;
			RoleTransporter role = (RoleTransporter) npc.roleInterface;
			if(role.hasTransport()){
				Server.sendData(player,EnumPacketClient.GUI_DATA,role.getLocation().writeNBT());
				Server.sendData(player, EnumPacketClient.SCROLL_SELECTED,role.getLocation().category.title);
			}
		}
		else if(type == EnumPacketServer.FactionSet){
			npc.setFaction(buffer.readInt());
		}
		else if(type == EnumPacketServer.FactionSave){
			Faction faction = new Faction();
			faction.readNBT(Server.readNBT(buffer));
			FactionController.getInstance().saveFaction(faction);
			NoppesUtilServer.sendFactionDataAll(player);
			NBTTagCompound compound = new NBTTagCompound();
			faction.writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.FactionRemove){
			FactionController.getInstance().delete(buffer.readInt());
			NoppesUtilServer.sendFactionDataAll(player);
			NBTTagCompound compound = new NBTTagCompound();
			(new Faction()).writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.PlayerDataGet){
			int id = buffer.readInt();
			if(EnumPlayerData.values().length <= id)
				return;
			String name = null;
			EnumPlayerData datatype = EnumPlayerData.values()[id];
			if(datatype != EnumPlayerData.Players)
				name = Server.readString(buffer);
			NoppesUtilServer.sendPlayerData(datatype,player,name);
		}
		else if(type == EnumPacketServer.PlayerDataRemove){
			NoppesUtilServer.removePlayerData(buffer,player);
		}
		else if(type == EnumPacketServer.MainmenuDisplayGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.display.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MainmenuDisplaySave){
			npc.display.readToNBT(Server.readNBT(buffer));
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.MainmenuStatsGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.stats.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MainmenuStatsSave){
			npc.stats.readToNBT(Server.readNBT(buffer));
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.MainmenuInvGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MainmenuInvSave){
			npc.inventory.readEntityFromNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.MainmenuAIGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.ai.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MainmenuAISave){
			npc.ai.readToNBT(Server.readNBT(buffer));
			npc.setHealth(npc.getMaxHealth());
			npc.updateAI = true;
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.MainmenuAdvancedGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.advanced.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MainmenuAdvancedSave){
			npc.advanced.readToNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.JobSave){
			NBTTagCompound original = npc.jobInterface.writeToNBT(new NBTTagCompound());
			NBTTagCompound compound = Server.readNBT(buffer);
			Set<String> names = compound.func_150296_c();
			for(String name : names)
				original.setTag(name, compound.getTag(name));
			npc.jobInterface.readFromNBT(original);
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.JobGet){
			if(npc.jobInterface == null)
				return;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.jobInterface.writeToNBT(compound);

			if(npc.advanced.job == EnumJobType.Spawner)
				((JobSpawner)npc.jobInterface).cleanCompound(compound);

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);

			if(npc.advanced.job == EnumJobType.Spawner)
				Server.sendData(player, EnumPacketClient.GUI_DATA, ((JobSpawner)npc.jobInterface).getTitles());
		}
		else if(type == EnumPacketServer.JobSpawnerAdd){
			if(npc.advanced.job != EnumJobType.Spawner)
				return;
			JobSpawner job = (JobSpawner) npc.jobInterface;
			if(buffer.readBoolean()){
				NBTTagCompound compound = ServerCloneController.Instance.getCloneData(null, Server.readString(buffer), buffer.readInt());

				job.setJobCompound(buffer.readInt(), compound);
			}
			else{
				job.setJobCompound(buffer.readInt(), Server.readNBT(buffer));
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, job.getTitles());
		}
		else if(type == EnumPacketServer.RoleCompanionUpdate){
			if(npc.advanced.role != EnumRoleType.Companion)
				return;
			((RoleCompanion)npc.roleInterface).matureTo(EnumCompanionStage.values()[buffer.readInt()]);
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.JobSpawnerRemove){
			if(npc.advanced.job != EnumJobType.Spawner)
				return;
		}
		else if(type == EnumPacketServer.RoleSave){
			npc.roleInterface.readFromNBT(Server.readNBT(buffer));
			npc.updateClient = true;
		}
		else if(type == EnumPacketServer.RoleGet){
			if(npc.roleInterface == null)
				return;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("RoleData", true);
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.roleInterface.writeToNBT(compound));
		}
		else if(type == EnumPacketServer.MerchantUpdate){
			Entity entity = player.worldObj.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityVillager))
				return;
			MerchantRecipeList list = MerchantRecipeList.func_151390_b(new PacketBuffer(buffer));
			((EntityVillager)entity).setRecipes(list);
		}
		else if(type == EnumPacketServer.ModelDataSave){
			if(npc instanceof EntityCustomNpc)
				((EntityCustomNpc)npc).modelData.readFromNBT(Server.readNBT(buffer));
		}
		else if(type == EnumPacketServer.MailOpenSetup){
			PlayerMail mail = new PlayerMail();
			mail.readNBT(Server.readNBT(buffer));
			ContainerMail.staticmail = mail;
			player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.worldObj, 1, 0, 0);
		}
		else if(type == EnumPacketServer.TransformSave){
			boolean isValid = npc.transform.isValid();
			npc.transform.readOptions(Server.readNBT(buffer));
			if(isValid != npc.transform.isValid())
				npc.updateAI = true;
		}
		else if(type == EnumPacketServer.TransformGet){
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.transform.writeOptions(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.TransformLoad){
			if(npc.transform.isValid())
				npc.transform.transform(buffer.readBoolean());
		}
		else if(type == EnumPacketServer.TraderMarketSave){
			String market = Server.readString(buffer);
			boolean bo = buffer.readBoolean();
			if(npc.roleInterface instanceof RoleTrader){
				if(bo)
					Market.setMarket(npc, market);
				else
					Market.save((RoleTrader)npc.roleInterface, market);
				//NoppesUtilServer.sendRoleData(player, npc);
			}
		}
		else
			blockPackets(type, buffer, player);

	}
	private void mountPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException{
		if(type == EnumPacketServer.SpawnRider){
			Entity entity = EntityList.createEntityFromNBT(Server.readNBT(buffer), player.worldObj);
			player.worldObj.spawnEntityInWorld(entity);
			entity.mountEntity(ServerEventsHandler.mounted);
		}
		else if(type == EnumPacketServer.PlayerRider){
			player.mountEntity(ServerEventsHandler.mounted);
		}
		else if(type == EnumPacketServer.CloneList){
			NBTTagList list = new NBTTagList();

			for(String name : ServerCloneController.Instance.getClones(buffer.readInt()))
				list.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else
			warn(player,"tried todo something with the wrong tool, probably a hacker");
	}

	private void clonePackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if(type == EnumPacketServer.SpawnMob){
			boolean server = buffer.readBoolean();
			int x = buffer.readInt();
			int y = buffer.readInt();
			int z = buffer.readInt();
			NBTTagCompound compound;
			if(server)
				compound = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer), buffer.readInt());
			else
				compound = Server.readNBT(buffer);
			if(compound == null)
				return;
			Entity entity = NoppesUtilServer.spawnClone(compound, x, y, z, player.worldObj);
			if(entity == null){
				player.addChatMessage(new ChatComponentText("Failed to create an entity out of your clone"));
				return;
			}
			if(CustomNpcs.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s SPAWNED ENTITY %s", "CLONER", player.getCommandSenderName(), entity));
			}
		}
		else if(type == EnumPacketServer.MobSpawner){
			boolean server = buffer.readBoolean();
			int x = buffer.readInt();
			int y = buffer.readInt();
			int z = buffer.readInt();
			NBTTagCompound compound;
			if(server)
				compound = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer), buffer.readInt());
			else
				compound = Server.readNBT(buffer);
			if(compound != null)
				NoppesUtilServer.createMobSpawner(x, y, z, compound, player);
		}
		else if(type == EnumPacketServer.ClonePreSave){
			boolean bo = ServerCloneController.Instance.getCloneData(null, Server.readString(buffer), buffer.readInt()) != null;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("NameExists", bo);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.CloneSave){
			PlayerData data = PlayerDataController.instance.getPlayerData(player);
			if(data.cloned == null)
				return;
			ServerCloneController.Instance.addClone(data.cloned, Server.readString(buffer), buffer.readInt());
		}
		else if(type == EnumPacketServer.CloneRemove){
			int tab = buffer.readInt();
			ServerCloneController.Instance.removeClone(Server.readString(buffer), tab);

			NBTTagList list = new NBTTagList();

			for(String name : ServerCloneController.Instance.getClones(tab))
				list.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.CloneList){
			NBTTagList list = new NBTTagList();

			for(String name : ServerCloneController.Instance.getClones(buffer.readInt()))
				list.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else
			warn(player,"tried todo something with the wrong tool, probably a hacker");
	}

	private void warn(EntityPlayer player, String warning){
		MinecraftServer.getServer().logWarning(player.getCommandSenderName() + ": " + warning);
	}
}