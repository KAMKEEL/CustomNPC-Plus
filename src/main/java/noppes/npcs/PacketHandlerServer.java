package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.relauncher.Side;
import foxz.utils.Market;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.packets.data.ScrollSelectedPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.network.packets.data.large.PartyDataPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import kamkeel.npcs.network.packets.data.large.ScrollListPacket;
import kamkeel.npcs.util.ByteBufUtils;
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
import net.minecraft.world.WorldServer;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.*;
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
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.io.IOException;
import java.util.*;

public class PacketHandlerServer{

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
		if(ConfigMain.OpsOnly && !NoppesUtilServer.isOp(player)){
			warn(player, "tried to use custom npcs without being an op");
			return;
		}
		ByteBuf in = event.packet.payload();
		EnumPacketServer type = null;
		try {
			type = EnumPacketServer.values()[in.readInt()];

			ItemStack item = player.inventory.getCurrentItem();

			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			if(item == null && (type == EnumPacketServer.ScriptPlayerGet || type == EnumPacketServer.ScriptPlayerSave || type == EnumPacketServer.ScriptGlobalNPCGet || type == EnumPacketServer.ScriptGlobalNPCSave || type == EnumPacketServer.ScriptForgeGet || type == EnumPacketServer.ScriptForgeSave))
				warn(player, "tried to use custom npcs without a tool in hand, probably a hacker");
			else {
				if (item != null) {
					if (item.getItem() == CustomItems.wand)
						wandPackets(type, in, player, npc);
					else if (item.getItem() == CustomItems.moving)
						movingPackets(type, in, player, npc);
					else if (item.getItem() == CustomItems.mount)
						mountPackets(type, in, player);
					else if (item.getItem() == CustomItems.cloner)
						clonePackets(type, in, player);
					else if (item.getItem() == CustomItems.teleporter)
						featherPackets(type, in, player);
					else if (item.getItem() == Item.getItemFromBlock(CustomItems.waypoint) || item.getItem() == Item.getItemFromBlock(CustomItems.border) || item.getItem() == Item.getItemFromBlock(CustomItems.redstoneBlock))
						blockPackets(type, in, player);
					else if (ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT)) {
						if (type == EnumPacketServer.EventScriptDataGet || type == EnumPacketServer.EventScriptDataSave)
							npcEventScriptPackets(type, in, player, npc);
						else if (type == EnumPacketServer.ScriptPlayerGet || type == EnumPacketServer.ScriptPlayerSave)
							playerScriptPackets(type, in, player);
						else if (type == EnumPacketServer.ScriptGlobalNPCGet || type == EnumPacketServer.ScriptGlobalNPCSave)
							npcGlobalScriptPackets(type, in, player);
						else if (type == EnumPacketServer.ScriptForgeGet || type == EnumPacketServer.ScriptForgeSave)
							forgeScriptPackets(type, in, player);
						else if (type == EnumPacketServer.ScriptItemDataGet || type == EnumPacketServer.ScriptItemDataSave)
							itemScriptPackets(type, in, player);
						else if (type == EnumPacketServer.ScriptBlockDataGet || type == EnumPacketServer.ScriptBlockDataSave)
							blockScriptPackets(type, in, player);
						else if (type == EnumPacketServer.ScriptGlobalGuiDataGet || type == EnumPacketServer.ScriptGlobalGuiDataSave)
							getScriptsEnabled(type, in, player);
						else if (item.getItem() == CustomItems.scripter)
							scriptPackets(type, in, player, npc);
					}
				}
			}
		} catch (Exception e) {
			LogWriter.error("Error with EnumPacketServer." + type, e);
		}
	}

	public static void sendPartyData(EntityPlayerMP player) {
		PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
		if (playerData.partyUUID != null) {
			Party party = PartyController.Instance().getParty(playerData.partyUUID);
            NBTTagCompound compound = party.writeToNBT();
            if (party.getQuest() != null) {
                Quest quest = (Quest) party.getQuest();
                compound.setString("QuestName", quest.getCategory().getName() + ":" + quest.getName());
                Vector<String> vector = quest.questInterface.getPartyQuestLogStatus(party);
                NBTTagList list = new NBTTagList();
                for (String s : vector) {
                    list.appendTag(new NBTTagString(s));
                }
                compound.setTag("QuestProgress", list);
                if(quest.completion == EnumQuestCompletion.Npc && quest.questInterface.isPartyCompleted(party)) {
                    compound.setString("QuestCompleteWith", quest.completerNpc);
                }
            }
            PartyDataPacket.sendPartyData(player, compound);
        } else {
			sendInviteData(player);
		}
	}

	public static void sendInviteData(EntityPlayerMP player) {
		PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
		if (playerData.partyUUID == null) {
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			HashSet<UUID> partyInvites = playerData.getPartyInvites();
			for (UUID uuid : partyInvites) {
				Party party = PartyController.Instance().getParty(uuid);
				NBTTagCompound partyCompound = new NBTTagCompound();
				partyCompound.setString("PartyLeader", party.getPartyLeaderName());
				partyCompound.setString("PartyUUID", party.getPartyUUID().toString());
				list.appendTag(partyCompound);
			}
			compound.setTag("PartyInvites", list);
            PartyDataPacket.sendPartyData(player, compound);
        }
	}

	private void isGuiOpenPacket(ByteBuf buffer, EntityPlayerMP player) throws IOException {
		NoppesUtilServer.isGUIOpen(buffer, player);
	}

	private void getScriptsEnabled(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if (type == EnumPacketServer.ScriptGlobalGuiDataGet) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("ScriptsEnabled", ConfigScript.ScriptingEnabled);
			compound.setBoolean("PlayerScriptsEnabled", ConfigScript.GlobalPlayerScripts);
			compound.setBoolean("GlobalNPCScriptsEnabled", ConfigScript.GlobalNPCScripts);
			compound.setBoolean("ForgeScriptsEnabled", ConfigScript.GlobalForgeScripts);
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if (type == EnumPacketServer.ScriptGlobalGuiDataSave) {
			NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
			ConfigScript.ScriptingEnabled = compound.getBoolean("ScriptsEnabled");
			ConfigScript.GlobalPlayerScripts = compound.getBoolean("PlayerScriptsEnabled");
			ConfigScript.GlobalNPCScripts = compound.getBoolean("GlobalNPCScriptsEnabled");
			ConfigScript.GlobalForgeScripts = compound.getBoolean("ForgeScriptsEnabled");
		}
	}

	private void scriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws Exception {
		if(type == EnumPacketServer.ScriptDataSave){
			npc.script.readFromNBT(ByteBufUtils.readNBT(buffer));
			npc.updateAI = true;
			npc.script.hasInited = false;
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s SAVED NPC %s (%s, %s, %s) [%s]", "SCRIPTER", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)(npc).posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
		}
		else if(type == EnumPacketServer.ScriptDataGet){
			NBTTagCompound compound = npc.script.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			GuiDataPacket.sendGuiData(player, compound);
		}
	}

	public static void getScripts(IScriptHandler data, ByteBuf buffer, EntityPlayerMP player) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("ScriptEnabled", data.getEnabled());
		compound.setString("ScriptLanguage", data.getLanguage());
		compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
		compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(data.getConsoleText()));
		GuiDataPacket.sendGuiData(player, compound);
		List<ScriptContainer> containers = data.getScripts();
		for (int i = 0; i < containers.size(); i++) {
			ScriptContainer container = containers.get(i);
			NBTTagCompound tabCompound = new NBTTagCompound();
			tabCompound.setInteger("Tab",i);
			tabCompound.setTag("Script",container.writeToNBT(new NBTTagCompound()));
			tabCompound.setInteger("TotalScripts",containers.size());
			GuiDataPacket.sendGuiData(player, tabCompound);
		}
	}

	public static void saveScripts(IScriptHandler data, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		int tab = buffer.readInt();
		int totalScripts = buffer.readInt();
		if (totalScripts == 0) {
			data.getScripts().clear();
		}

		if (tab >= 0) {
			if (data.getScripts().size() > totalScripts) {
				data.setScripts(data.getScripts().subList(0,totalScripts));
			} else while (data.getScripts().size() < totalScripts) {
				data.getScripts().add(new ScriptContainer(data));
			}
			NBTTagCompound tabCompound = ByteBufUtils.readNBT(buffer);
			ScriptContainer script = new ScriptContainer(data);
			script.readFromNBT(tabCompound);
			data.getScripts().set(tab,script);
		} else {
			NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
			data.setLanguage(compound.getString("ScriptLanguage"));
			if (!ScriptController.Instance.languages.containsKey(data.getLanguage())) {
				if (!ScriptController.Instance.languages.isEmpty()) {
					data.setLanguage((String) ScriptController.Instance.languages.keySet().toArray()[0]);
				} else {
					data.setLanguage("ECMAScript");
				}
			}
			data.setEnabled(compound.getBoolean("ScriptEnabled"));
		}
	}

	private void npcEventScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws Exception {
		DataScript data = npc.script;
		if(type == EnumPacketServer.EventScriptDataGet) {
			getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.EventScriptDataSave) {
			saveScripts(data,buffer,player);
			npc.updateAI = true;
			npc.script.hasInited = false;
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s SAVED NPC %s (%s, %s, %s) [%s]", "SCRIPTER", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)(npc).posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
		}
	}

	private void playerScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		PlayerDataScript data = ScriptController.Instance.playerScripts;
		if(type == EnumPacketServer.ScriptPlayerGet) {
			getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.ScriptPlayerSave) {
            int tab = buffer.getInt(buffer.readerIndex());
			saveScripts(data,buffer,player);
			ScriptController.Instance.lastPlayerUpdate = System.currentTimeMillis();
            if(tab == -1)
                ScriptController.Instance.savePlayerScriptsSync();
		}
	}

	private void forgeScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		ForgeDataScript data = ScriptController.Instance.forgeScripts;
		if (type == EnumPacketServer.ScriptForgeGet) {
			getScripts(data,buffer,player);
		} else if (type == EnumPacketServer.ScriptForgeSave) {
            int tab = buffer.getInt(buffer.readerIndex());
			saveScripts(data,buffer,player);
			ScriptController.Instance.lastForgeUpdate = System.currentTimeMillis();
            if(tab == -1)
                ScriptController.Instance.saveForgeScriptsSync();
		}
	}

	private void npcGlobalScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		GlobalNPCDataScript data = ScriptController.Instance.globalNpcScripts;
		if(type == EnumPacketServer.ScriptGlobalNPCGet) {
			getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.ScriptGlobalNPCSave) {
            int tab = buffer.getInt(buffer.readerIndex());
			saveScripts(data,buffer,player);
			ScriptController.Instance.lastGlobalNpcUpdate = System.currentTimeMillis();
            if(tab == -1)
                ScriptController.Instance.saveGlobalScriptsSync();
		}
	}

	private void itemScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		if (type == EnumPacketServer.ScriptItemDataGet) {
			ScriptCustomItem iw = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
			iw.loadScriptData();
			NBTTagCompound compound = iw.getMCNbt();
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			GuiDataPacket.sendGuiData(player, compound);
		} else if (type == EnumPacketServer.ScriptItemDataSave) {
			if (!player.capabilities.isCreativeMode) {
				return;
			}

			NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
			ScriptCustomItem wrapper = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
			wrapper.setMCNbt(compound);
			wrapper.saveScriptData();
			wrapper.loaded = false;
            wrapper.lastInited = -1;
			player.sendContainerToPlayer(player.inventoryContainer);
		}
	}

	private void blockScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		if (type == EnumPacketServer.ScriptBlockDataGet) {
			TileEntity tile = player.worldObj.getTileEntity(buffer.readInt(), buffer.readInt(), buffer.readInt());
			if(!(tile instanceof TileScripted))
				return;
			NBTTagCompound compound = ((TileScripted) tile).getNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			GuiDataPacket.sendGuiData(player, compound);
		} else if (type == EnumPacketServer.ScriptBlockDataSave) {
			if (!player.capabilities.isCreativeMode) {
				return;
			}
			TileEntity tile = player.worldObj.getTileEntity(buffer.readInt(), buffer.readInt(), buffer.readInt());
			if(!(tile instanceof TileScripted))
				return;
			TileScripted script = (TileScripted) tile;
			script.setNBT(ByteBufUtils.readNBT(buffer));
			script.lastInited = -1;
		}
	}

	private void featherPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if(type == EnumPacketServer.DimensionTeleport){
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
			GuiDataPacket.sendGuiData(player, npc.ai.writeToNBT(new NBTTagCompound()));
		}
		else if(type == EnumPacketServer.MovingPathSave){
			npc.ai.setMovingPath(NBTTags.getIntegerArraySet(ByteBufUtils.readNBT(buffer).getTagList("MovingPathNew",10)));
		}
	}

	private void blockPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if (type == EnumPacketServer.SaveTileEntity) {
			NoppesUtilServer.saveTileEntity(player, ByteBufUtils.readNBT(buffer));
		}
		else if(type == EnumPacketServer.GetTileEntity){
			TileEntity tile = player.worldObj.getTileEntity(buffer.readInt(), buffer.readInt(), buffer.readInt());
			NBTTagCompound compound = new NBTTagCompound();
			tile.writeToNBT(compound);
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if(type == EnumPacketServer.DialogCategoriesGet){
			ScrollDataPacket.sendScrollData(player, DialogController.Instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogsGetFromDialog){
			Dialog dialog = DialogController.Instance.dialogs.get(buffer.readInt());
			if(dialog == null)
				return;
			NoppesUtilServer.sendDialogData(player,dialog.category);
		}
		else if(type == EnumPacketServer.DialogsGet){
			int catID = buffer.readInt();
			boolean sendGroup = buffer.readBoolean();
			if(sendGroup){
				NoppesUtilServer.sendDialogGroup(player,DialogController.Instance.categories.get(catID));
			}
			else {
				NoppesUtilServer.sendDialogData(player,DialogController.Instance.categories.get(catID));
			}
		}
		else if(type == EnumPacketServer.QuestsGetFromQuest){
			Quest quest = QuestController.Instance.quests.get(buffer.readInt());
			if(quest == null)
				return;
			NoppesUtilServer.sendQuestData(player,quest.category);
		}
		else if(type == EnumPacketServer.QuestCategoriesGet){
			NoppesUtilServer.sendQuestCategoryData(player);
		}
		else if(type == EnumPacketServer.QuestsGet){
			QuestCategory category = QuestController.Instance.categories.get(buffer.readInt());
			boolean sendGroup = buffer.readBoolean();
			if(sendGroup){
				NoppesUtilServer.sendQuestGroup(player,category);
			}
			else {
				NoppesUtilServer.sendQuestData(player,category);
			}
		}
		else if(type == EnumPacketServer.FactionsGet){
			NoppesUtilServer.sendFactionDataAll(player);
		}
		else if(type == EnumPacketServer.DialogGet){
			Dialog dialog = DialogController.Instance.dialogs.get(buffer.readInt());
			if(dialog != null){
				NBTTagCompound compound = dialog.writeToNBT(new NBTTagCompound());
				Quest quest = QuestController.Instance.quests.get(dialog.quest);
				if(quest != null)
					compound.setString("DialogQuestName", quest.title);
				GuiDataPacket.sendGuiData(player, compound);
			}
		}
		else if(type == EnumPacketServer.QuestGet){
			Quest quest = QuestController.Instance.quests.get(buffer.readInt());
			if(quest != null){
				NBTTagCompound compound = new NBTTagCompound();
				if(quest.hasNewQuest())
					compound.setString("NextQuestTitle", quest.getNextQuest().title);
				GuiDataPacket.sendGuiData(player, quest.writeToNBT(compound));
			}
		}
		else if(type == EnumPacketServer.FactionGet){
			NBTTagCompound compound = new NBTTagCompound();
			Faction faction = FactionController.getInstance().get(buffer.readInt());
			faction.writeNBT(compound);
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if(type == EnumPacketServer.TagGet){
			NBTTagCompound compound = new NBTTagCompound();
			Tag tag = TagController.getInstance().get(buffer.readInt());
			tag.writeNBT(compound);
			GuiDataPacket.sendGuiData(player, compound);
		}
	}

	private void wandPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws IOException{
       if(type == EnumPacketServer.QuestOpenGui){
			Quest quest = new Quest();
			int gui = buffer.readInt();
			quest.readNBT(ByteBufUtils.readNBT(buffer));
			NoppesUtilServer.setEditingQuest(player,quest);
			player.openGui(CustomNpcs.instance, gui , player.worldObj, 0, 0, 0);
		}
		else if(type == EnumPacketServer.TraderMarketSave){
			String market = ByteBufUtils.readString(buffer);
            if(market == null)
                return;
			boolean bo = buffer.readBoolean();
			if(npc.roleInterface instanceof RoleTrader){
				if(bo)
					Market.setMarket(npc, market);
				else
					Market.save((RoleTrader)npc.roleInterface, market);
			}
		}
		else if(type == EnumPacketServer.AnimationsGet){
			NoppesUtilServer.sendAnimationDataAll(player);
		}
		else if(type == EnumPacketServer.AnimationGet){
			Animation animation = (Animation) AnimationController.getInstance().get(buffer.readInt());
			NBTTagCompound compound = animation.writeToNBT();
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if(type == EnumPacketServer.AnimationRemove){
			AnimationController.getInstance().delete(buffer.readInt());
			NoppesUtilServer.sendAnimationDataAll(player);
			NBTTagCompound compound = (new Animation()).writeToNBT();
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if(type == EnumPacketServer.AnimationSave){
			Animation animation = new Animation();
			animation.readFromNBT(ByteBufUtils.readNBT(buffer));
			AnimationController.getInstance().saveAnimation(animation);
			NoppesUtilServer.sendAnimationDataAll(player);
			GuiDataPacket.sendGuiData(player, animation.writeToNBT());
		}
		else
			blockPackets(type, buffer, player);
	}
	private void mountPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException{
		if(type == EnumPacketServer.SpawnRider){
			Entity entity = EntityList.createEntityFromNBT(ByteBufUtils.readNBT(buffer), player.worldObj);
			player.worldObj.spawnEntityInWorld(entity);
			entity.mountEntity(ServerEventsHandler.mounted);
		}
		else if(type == EnumPacketServer.PlayerRider){
			player.mountEntity(ServerEventsHandler.mounted);
		}
		else if(type == EnumPacketServer.CloneList){
			NBTTagList list = new NBTTagList();
			int tab = buffer.readInt();
			for(String name : ServerCloneController.Instance.getClones(tab))
				list.appendTag(new NBTTagString(name));

			NBTTagList listDate = new NBTTagList();
			for(String name : ServerCloneController.Instance.getClonesDate(tab))
				listDate.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			compound.setTag("ListDate", listDate);

			GuiDataPacket.sendGuiData(player, compound);
		}
		else
			warn(player,"WE 1 tried todo something with the wrong tool, probably a hacker");
	}

	private void clonePackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws IOException {
		if(type == EnumPacketServer.SpawnMob){
			boolean server = buffer.readBoolean();
			int x = buffer.readInt();
			int y = buffer.readInt();
			int z = buffer.readInt();
			NBTTagCompound compound;
			if(server)
				compound = ServerCloneController.Instance.getCloneData(player, ByteBufUtils.readString(buffer), buffer.readInt());
			else
				compound = ByteBufUtils.readNBT(buffer);
			if(compound == null)
				return;
			Entity entity = NoppesUtilServer.spawnClone(compound, x, y, z, player.worldObj);
			if(entity == null){
				player.addChatMessage(new ChatComponentText("Failed to create an entity out of your clone"));
				return;
			}

            if (entity instanceof EntityNPCInterface && !ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT)) {
                EntityNPCInterface npc = (EntityNPCInterface) entity;
                npc.script.setEnabled(false);
            }

			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
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
				compound = ServerCloneController.Instance.getCloneData(player, ByteBufUtils.readString(buffer), buffer.readInt());
			else
				compound = ByteBufUtils.readNBT(buffer);

            if (!ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT)) {
                return;
            }

			if(compound != null)
				NoppesUtilServer.createMobSpawner(x, y, z, compound, player);
		}
		else if(type == EnumPacketServer.ClonePreSave){
			boolean bo = ServerCloneController.Instance.getCloneData(null, ByteBufUtils.readString(buffer), buffer.readInt()) != null;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("NameExists", bo);
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if(type == EnumPacketServer.CloneSave){
			PlayerData data = PlayerDataController.Instance.getPlayerData(player);
			if(data.cloned == null)
				return;
			String name = ByteBufUtils.readString(buffer);
			int tab = buffer.readInt();
			NBTTagCompound tagExtra = ByteBufUtils.readNBT(buffer);
            NBTTagCompound tagCompound = ByteBufUtils.readNBT(buffer);

            NBTTagList tagList = tagCompound.getTagList("TagUUIDs", 8);
            data.cloned.setTag("TagUUIDs", tagList);
			ServerCloneController.Instance.addClone(data.cloned, name, tab, tagExtra);
		}
		else if(type == EnumPacketServer.CloneRemove){
			int tab = buffer.readInt();
			ServerCloneController.Instance.removeClone(ByteBufUtils.readString(buffer), tab);

			NBTTagList list = new NBTTagList();

			for(String name : ServerCloneController.Instance.getClones(tab))
				list.appendTag(new NBTTagString(name));

			NBTTagList listDate = new NBTTagList();
			for(String name : ServerCloneController.Instance.getClonesDate(tab))
				listDate.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			compound.setTag("ListDate", listDate);

			GuiDataPacket.sendGuiData(player, compound);
		}
		else if(type == EnumPacketServer.CloneList){
			NBTTagList list = new NBTTagList();
			int tab = buffer.readInt();
			for(String name : ServerCloneController.Instance.getClones(tab))
				list.appendTag(new NBTTagString(name));

			NBTTagList listDate = new NBTTagList();
			for(String name : ServerCloneController.Instance.getClonesDate(tab))
				listDate.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			compound.setTag("ListDate", listDate);

			GuiDataPacket.sendGuiData(player, compound);
		}
		else if (type == EnumPacketServer.CloneTagList) {
			int tab = buffer.readInt();
			TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("CloneTags", tagMap.writeNBT());
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if (type == EnumPacketServer.CloneAllTags) {
			NBTTagCompound compound = new NBTTagCompound();
			HashSet<Tag> validTags = TagController.getInstance().getAllTags();
			NBTTagList validTagList = new NBTTagList();
			for(Tag tag : validTags){
				NBTTagCompound tagCompound = new NBTTagCompound();
				tag.writeNBT(tagCompound);
				validTagList.appendTag(tagCompound);
			}
			compound.setTag("AllTags", validTagList);
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if (type == EnumPacketServer.CloneAllTagsShort) {
			NBTTagCompound compound = new NBTTagCompound();
			HashSet<Tag> validTags = TagController.getInstance().getAllTags();
			NBTTagList validTagList = new NBTTagList();
			for(Tag tag : validTags){
				NBTTagCompound tagCompound = new NBTTagCompound();
				tag.writeShortNBT(tagCompound);
				validTagList.appendTag(tagCompound);
			}
			compound.setTag("ShortTags", validTagList);
			GuiDataPacket.sendGuiData(player, compound);
		}
		else if (type != EnumPacketServer.NpcTagsGet && type != EnumPacketServer.TagsGet) {
			warn(player, "WE 2 tried todo something with the wrong tool, probably a hacker");
		}
	}

	private void setTags(EntityNPCInterface npc, ByteBuf buffer) throws IOException {
		npc.advanced.tagUUIDs.removeIf(uuid -> TagController.getInstance().getTagFromUUID(uuid) != null);
		NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
		NBTTagList list = compound.getTagList("TagNames",8);
		for (int i = 0; i < list.tagCount(); i++) {
			String tagName = list.getStringTagAt(i);
			npc.advanced.tagUUIDs.add(((Tag)TagController.getInstance().getTagFromName(tagName)).uuid);
		}
	}

	private void warn(EntityPlayer player, String warning){
		MinecraftServer.getServer().logWarning(player.getCommandSenderName() + ": " + warning);
	}
}
