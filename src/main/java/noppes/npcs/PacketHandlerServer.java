package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.relauncher.Side;
import foxz.utils.Market;
import io.netty.buffer.ByteBuf;
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
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.*;
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
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.gui.ScriptGui;
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
			} else if (type == EnumPacketServer.QuestLogToServer) {
				NoppesUtilPlayer.updateQuestLogData(buffer, player);
				return;
			} else if(type == EnumPacketServer.DimensionsGet){
				HashMap<String,Integer> map = new HashMap<String,Integer>();
				for(int id : DimensionManager.getStaticDimensionIDs()){
					WorldProvider provider = DimensionManager.createProviderFor(id);
					map.put(provider.getDimensionName(), id);
				}
				NoppesUtilServer.sendScrollData(player, map);
			} else if(type == EnumPacketServer.TagsGet){
				NoppesUtilServer.sendTagDataAll(player);
			} else if (type == EnumPacketServer.NpcTagsGet) {
				NBTTagCompound compound = new NBTTagCompound();
				NBTTagList tagList = new NBTTagList();
				for (UUID uuid : npc.advanced.tagUUIDs) {
					Tag tag = TagController.getInstance().getTagFromUUID(uuid);
					if (tag != null) {
						tagList.appendTag(new NBTTagString(tag.name));
					}
				}
				compound.setTag("TagNames",tagList);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			} else if (type == EnumPacketServer.CacheAnimation) {
				PlayerDataController.Instance.getPlayerData(player).animationData.cacheAnimation(buffer.readInt());
				return;
			} else if (type == EnumPacketServer.GetPartyData || type == EnumPacketServer.CreateParty) {
				if (type == EnumPacketServer.CreateParty) {
					Party party = PartyController.Instance().createParty();
					party.addPlayer(player);
					party.setLeader(player);
				}
				sendPartyData(player);
			} else if (type == EnumPacketServer.DisbandParty) {
				PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
				if (playerData.partyUUID != null) {
					PartyController.Instance().disbandParty(playerData.partyUUID);
				}

				NBTTagCompound compound = new NBTTagCompound();
				compound.setBoolean("Disband", true);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			} else if (type == EnumPacketServer.KickPlayer) {
				EntityPlayer kickPlayer = NoppesUtilServer.getPlayerByName(Server.readString(buffer));
				if (kickPlayer != null) {
					PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
					if (playerData.partyUUID != null) {
						Party party = PartyController.Instance().getParty(playerData.partyUUID);
                        if (!party.getIsLocked()) {
                            party.removePlayer(kickPlayer);
                            if (kickPlayer != player) {
                                sendPartyData(player);
                            } else {
                                sendInviteData((EntityPlayerMP) kickPlayer);
                            }
                        }
					}
				}
			} else if (type == EnumPacketServer.SavePartyData) {
				PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
				if (playerData.partyUUID != null) {
					Party party = PartyController.Instance().getParty(playerData.partyUUID);
					party.readClientNBT(Server.readNBT(buffer));
				}
			} else if (type == EnumPacketServer.SetPartyLeader) {
				PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
				if (playerData.partyUUID != null) {
					Party party = PartyController.Instance().getParty(playerData.partyUUID);
                    if (!party.getIsLocked()) {
                        party.setLeader(NoppesUtilServer.getPlayerByName(Server.readString(buffer)));
                        Server.sendData(player, EnumPacketClient.GUI_DATA, party.writeToNBT());
                    }
				}
			} else if (type == EnumPacketServer.PartyInvite) {
				EntityPlayer invitedPlayer = NoppesUtilServer.getPlayerByName(Server.readString(buffer));
				if (invitedPlayer != null) {
					PlayerData senderData = PlayerDataController.Instance.getPlayerData(player);
					PlayerData invitedData = PlayerDataController.Instance.getPlayerData(invitedPlayer);
					if (senderData.partyUUID != null && invitedData.partyUUID == null) {//only send invite if player is not in a party
                        Party party = PartyController.Instance().getParty(senderData.partyUUID);
                        if (!party.getIsLocked()) {
                            invitedData.inviteToParty(party);
                        }
					}
				}
			} else if (type == EnumPacketServer.GetPartyInviteList) {
				sendInviteData(player);
			} else if (type == EnumPacketServer.AcceptInvite) {
				PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
				String uuidString = Server.readString(buffer);
				if (uuidString != null) {
					UUID uuid = UUID.fromString(uuidString);
					playerData.acceptInvite(uuid);
				}
			} else if (type == EnumPacketServer.IgnoreInvite) {
				PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
				String uuidString = Server.readString(buffer);
				if (uuidString != null) {
					UUID uuid = UUID.fromString(uuidString);
					playerData.ignoreInvite(uuid);
				}
			} else if (type == EnumPacketServer.SetPartyQuest) {
				PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
				Party party = PartyController.Instance().getParty(playerData.partyUUID);
				if (party != null) {
					if (party.getPartyLeader() != player) {
						return;
					}

					String questCategory = Server.readString(buffer);
					String questName = Server.readString(buffer);
					party.setQuest(null);
					for (Quest quest : QuestController.Instance.quests.values()) {
						if (quest.partyOptions.allowParty && quest.getCategory().getName().equals(questCategory) && quest.getName().equals(questName)) {
							party.setQuest(quest);
							break;
						}
					}
					Server.sendData(player, EnumPacketClient.GUI_DATA, party.writeToNBT());
				}
			}

			if(type.needsNpc && npc == null){

			}
			else if(type.hasPermission() && !CustomNpcsPermissions.hasPermission(player, type.permission)){
				//player doesnt have permission to do this
			}
			else if(item == null && (type == EnumPacketServer.ScriptPlayerGet || type == EnumPacketServer.ScriptPlayerSave || type == EnumPacketServer.ScriptGlobalNPCGet || type == EnumPacketServer.ScriptGlobalNPCSave || type == EnumPacketServer.ScriptForgeGet || type == EnumPacketServer.ScriptForgeSave))
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
					else if (ConfigScript.isScriptDev(player)) {
						if (type == EnumPacketServer.EventScriptDataGet || type == EnumPacketServer.EventScriptDataSave)
							npcEventScriptPackets(type, buffer, player, npc);
						else if (type == EnumPacketServer.ScriptPlayerGet || type == EnumPacketServer.ScriptPlayerSave)
							playerScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptGlobalNPCGet || type == EnumPacketServer.ScriptGlobalNPCSave)
							npcGlobalScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptForgeGet || type == EnumPacketServer.ScriptForgeSave)
							forgeScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptItemDataGet || type == EnumPacketServer.ScriptItemDataSave)
							itemScriptPackets(type, buffer, player);
						else if (type == EnumPacketServer.ScriptBlockDataGet || type == EnumPacketServer.ScriptBlockDataSave)
							blockScriptPackets(type, buffer, player);
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

	public static void sendPartyData(EntityPlayerMP player) {
		PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);

		if (playerData.partyUUID != null) {
			Party party = PartyController.Instance().getParty(playerData.partyUUID);
            NBTTagCompound compound = party.writeToNBT();
            if (party.getQuest() != null) {
                Quest quest = (Quest) party.getQuest();
                Vector<String> vector = quest.questInterface.getQuestLogStatus(player);
                NBTTagList list = new NBTTagList();
                for (String s : vector) {
                    list.appendTag(new NBTTagString(s));
                }
                compound.setTag("QuestProgress", list);
                if(quest.completion == EnumQuestCompletion.Npc && quest.questInterface.isCompleted(playerData)) {
                    compound.setString("QuestCompleteWith", quest.completerNpc);
                }
            }
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.ScriptGlobalGuiDataSave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			ConfigScript.ScriptingEnabled = compound.getBoolean("ScriptsEnabled");
			ConfigScript.GlobalPlayerScripts = compound.getBoolean("PlayerScriptsEnabled");
			ConfigScript.GlobalNPCScripts = compound.getBoolean("GlobalNPCScriptsEnabled");
			ConfigScript.GlobalForgeScripts = compound.getBoolean("ForgeScriptsEnabled");
		}
	}

	private void scriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws Exception {
		if(type == EnumPacketServer.ScriptDataSave){
			npc.script.readFromNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.script.hasInited = false;
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s SAVED NPC %s (%s, %s, %s) [%s]", "SCRIPTER", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)(npc).posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
		}
		else if(type == EnumPacketServer.ScriptDataGet){
			NBTTagCompound compound = npc.script.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
	}

	public static void getScripts(IScriptHandler data, ByteBuf buffer, EntityPlayerMP player) {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("ScriptEnabled", data.getEnabled());
		compound.setString("ScriptLanguage", data.getLanguage());
		compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
		compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(data.getConsoleText()));
		Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		List<ScriptContainer> containers = data.getScripts();
		for (int i = 0; i < containers.size(); i++) {
			ScriptContainer container = containers.get(i);
			NBTTagCompound tabCompound = new NBTTagCompound();
			tabCompound.setInteger("Tab",i);
			tabCompound.setTag("Script",container.writeToNBT(new NBTTagCompound()));
			tabCompound.setInteger("TotalScripts",containers.size());
			Server.sendData(player, EnumPacketClient.GUI_DATA, tabCompound);
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
			NBTTagCompound tabCompound = Server.readNBT(buffer);
			ScriptContainer script = new ScriptContainer(data);
			script.readFromNBT(tabCompound);
			data.getScripts().set(tab,script);
		} else {
			NBTTagCompound compound = Server.readNBT(buffer);
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
			saveScripts(data,buffer,player);
			ScriptController.Instance.lastPlayerUpdate = System.currentTimeMillis();
		}
	}

	private void forgeScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		ForgeDataScript data = ScriptController.Instance.forgeScripts;
		if (type == EnumPacketServer.ScriptForgeGet) {
			getScripts(data,buffer,player);
		} else if (type == EnumPacketServer.ScriptForgeSave) {
			saveScripts(data,buffer,player);
			ScriptController.Instance.lastForgeUpdate = System.currentTimeMillis();
		}
	}

	private void npcGlobalScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		GlobalNPCDataScript data = ScriptController.Instance.globalNpcScripts;
		if(type == EnumPacketServer.ScriptGlobalNPCGet) {
			getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.ScriptGlobalNPCSave) {
			saveScripts(data,buffer,player);
			ScriptController.Instance.lastGlobalNpcUpdate = System.currentTimeMillis();
		}
	}

	private void itemScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		if (type == EnumPacketServer.ScriptItemDataGet) {
			ScriptCustomItem iw = (ScriptCustomItem) NpcAPI.Instance().getIItemStack(player.getHeldItem());
			iw.loadScriptData();
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
			wrapper.saveScriptData();
			wrapper.loaded = false;
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptBlockDataSave) {
			if (!player.capabilities.isCreativeMode) {
				return;
			}
			TileEntity tile = player.worldObj.getTileEntity(buffer.readInt(), buffer.readInt(), buffer.readInt());
			if(!(tile instanceof TileScripted))
				return;
			TileScripted script = (TileScripted) tile;
			script.setNBT(Server.readNBT(buffer));
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
			NoppesUtilServer.sendScrollData(player, DialogController.Instance.getScroll());
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
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			}
		}
		else if(type == EnumPacketServer.QuestGet){
			Quest quest = QuestController.Instance.quests.get(buffer.readInt());
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
		else if(type == EnumPacketServer.TagGet){
			NBTTagCompound compound = new NBTTagCompound();
			Tag tag = TagController.getInstance().get(buffer.readInt());
			tag.writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
	}

	private void wandPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws IOException{
		if(type == EnumPacketServer.Delete){
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
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
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
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
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s OPEN NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), ((EntityNPCInterface)entity).display.getName(), entity.posX, entity.posY, entity.posZ,  entity.worldObj.getWorldInfo().getWorldName()));
			}
		}
        else if(type == EnumPacketServer.RemoteGlobalMenu){
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.GlobalRemote, null);
            if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
                LogWriter.script(String.format("[%s] (Player) %s OPEN GLOBAL MENU", "WAND", player.getCommandSenderName()));
            }
        }
		else if(type == EnumPacketServer.RemoteDelete){
			Entity entity = player.worldObj.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			npc = (EntityNPCInterface) entity;
			if(ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
				LogWriter.script(String.format("[%s] (Player) %s DELETE NPC %s (%s, %s, %s) [%s]", "WAND", player.getCommandSenderName(), npc.display.getName(), (int)npc.posX, (int)npc.posY, (int)npc.posZ,  npc.worldObj.getWorldInfo().getWorldName()));
			}
			npc.delete();
			NoppesUtilServer.deleteNpc(npc,player);
			NoppesUtilServer.sendNearbyNpcs(player);
		}
		else if(type == EnumPacketServer.RemoteNpcsGet){
			NoppesUtilServer.sendNearbyNpcs(player);
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
			RecipeCarpentry recipe = RecipeController.Instance.getRecipe(buffer.readInt());
			NoppesUtilServer.setRecipeGui(player,recipe);
		}
		else if(type == EnumPacketServer.RecipeRemove){
			RecipeCarpentry recipe = RecipeController.Instance.delete(buffer.readInt());
			NoppesUtilServer.sendRecipeData(player, recipe.isGlobal?3:4);
			NoppesUtilServer.setRecipeGui(player,new RecipeCarpentry(""));
		}
		else if(type == EnumPacketServer.RecipeSave){
			RecipeCarpentry recipe = RecipeController.Instance.saveRecipe(Server.readNBT(buffer));
			NoppesUtilServer.sendRecipeData(player, recipe.isGlobal?3:4);
			NoppesUtilServer.setRecipeGui(player,recipe);
		}
		else if(type == EnumPacketServer.NaturalSpawnGetAll){
			NoppesUtilServer.sendScrollData(player, SpawnController.Instance.getScroll());
		}
		else if(type == EnumPacketServer.NaturalSpawnGet){
			SpawnData spawn = SpawnController.Instance.getSpawnData(buffer.readInt());
			if(spawn != null){
				Server.sendData(player, EnumPacketClient.GUI_DATA, spawn.writeNBT(new NBTTagCompound()));
			}
		}
		else if(type == EnumPacketServer.NaturalSpawnSave){
			SpawnData data = new SpawnData();
			data.readNBT(Server.readNBT(buffer));
			SpawnController.Instance.saveSpawnData(data);

			NoppesUtilServer.sendScrollData(player, SpawnController.Instance.getScroll());
		}
		else if(type == EnumPacketServer.NaturalSpawnRemove){
			SpawnController.Instance.removeSpawnData(buffer.readInt());
			NoppesUtilServer.sendScrollData(player, SpawnController.Instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogCategorySave){
			DialogCategory category = new DialogCategory();
			category.readNBT(Server.readNBT(buffer));
			DialogController.Instance.saveCategory(category);
			NoppesUtilServer.sendScrollData(player, DialogController.Instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogCategoryRemove){
			DialogController.Instance.removeCategory(buffer.readInt());
			NoppesUtilServer.sendScrollData(player, DialogController.Instance.getScroll());
		}
		else if(type == EnumPacketServer.DialogCategoryGet){
			DialogCategory category = DialogController.Instance.categories.get(buffer.readInt());
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
			boolean sendGroup = buffer.readBoolean();
			DialogController.Instance.saveDialog(category,dialog);
			if(dialog.category != null){
				if(sendGroup){
					NoppesUtilServer.sendDialogGroup(player,dialog.category);
				}
				else {
					NoppesUtilServer.sendDialogData(player,dialog.category);
				}
			}
		}
		else if(type == EnumPacketServer.QuestOpenGui){
			Quest quest = new Quest();
			int gui = buffer.readInt();
			quest.readNBT(Server.readNBT(buffer));
			NoppesUtilServer.setEditingQuest(player,quest);
			player.openGui(CustomNpcs.instance, gui , player.worldObj, 0, 0, 0);
		}
		else if(type == EnumPacketServer.DialogRemove){
			int diagId = buffer.readInt();
			boolean sendGroup = buffer.readBoolean();
			Dialog dialog = DialogController.Instance.dialogs.get(diagId);
			if(dialog != null && dialog.category != null){
				DialogController.Instance.removeDialog(dialog);
				if(sendGroup){
					NoppesUtilServer.sendDialogGroup(player,dialog.category);
				}
				else {
					NoppesUtilServer.sendDialogData(player,dialog.category);
				}
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
			QuestCategory category = QuestController.Instance.categories.get(buffer.readInt());
			if(category != null){
				NBTTagCompound comp = category.writeNBT(new NBTTagCompound());
				comp.removeTag("Dialogs");
				Server.sendData(player, EnumPacketClient.GUI_DATA, comp);
			}
		}
		else if(type == EnumPacketServer.QuestCategorySave){
			QuestCategory category = new QuestCategory();
			category.readNBT(Server.readNBT(buffer));
			QuestController.Instance.saveCategory(category);
			NoppesUtilServer.sendQuestCategoryData(player);
		}
		else if(type == EnumPacketServer.QuestCategoryRemove){
			QuestController.Instance.removeCategory(buffer.readInt());
			NoppesUtilServer.sendQuestCategoryData(player);
		}
		else if(type == EnumPacketServer.QuestSave){
			int category = buffer.readInt();
			Quest quest = new Quest();
			quest.readNBT(Server.readNBT(buffer));
			boolean sendGroup = buffer.readBoolean();
			QuestController.Instance.saveQuest(category, quest);
			if(quest.category != null){
				if(sendGroup){
					NoppesUtilServer.sendQuestGroup(player, quest.category);
				}
				else {
					NoppesUtilServer.sendQuestData(player, quest.category);
				}
			}
		}
		else if(type == EnumPacketServer.QuestDialogGetTitle){
			Dialog quest = DialogController.Instance.dialogs.get(buffer.readInt());
			Dialog quest2 = DialogController.Instance.dialogs.get(buffer.readInt());
			Dialog quest3 = DialogController.Instance.dialogs.get(buffer.readInt());
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
			Quest quest = QuestController.Instance.quests.get(buffer.readInt());
			boolean sendGroup = buffer.readBoolean();
			if(quest != null){
				QuestController.Instance.removeQuest(quest);
				if(sendGroup){
					NoppesUtilServer.sendQuestGroup(player,quest.category);
				}
				else {
					NoppesUtilServer.sendQuestData(player,quest.category);
				}
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
		else if(type == EnumPacketServer.TagSet){
			this.setTags(npc,buffer);
		}
		else if(type == EnumPacketServer.TagSave){
			Tag tag = new Tag();
			tag.readNBT(Server.readNBT(buffer));
			TagController.getInstance().saveTag(tag);
			NoppesUtilServer.sendTagDataAll(player);
			NBTTagCompound compound = new NBTTagCompound();
			tag.writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.TagRemove){
			TagController.getInstance().delete(buffer.readInt());
			NoppesUtilServer.sendTagDataAll(player);
			NBTTagCompound compound = new NBTTagCompound();
			(new Tag()).writeNBT(compound);
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
		else if(type == EnumPacketServer.PlayerDataMapRegen){
			NoppesUtilServer.regenPlayerData(player);
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
			JobSpawner job = (JobSpawner) npc.jobInterface;
			job.setJobCompound(buffer.readInt(), null);
			Server.sendData(player, EnumPacketClient.GUI_DATA, job.getTitles());
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.AnimationRemove){
			AnimationController.getInstance().delete(buffer.readInt());
			NoppesUtilServer.sendAnimationDataAll(player);
			NBTTagCompound compound = (new Animation()).writeToNBT();
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if(type == EnumPacketServer.AnimationSave){
			Animation animation = new Animation();
			animation.readFromNBT(Server.readNBT(buffer));
			AnimationController.getInstance().saveAnimation(animation);
			NoppesUtilServer.sendAnimationDataAll(player);
			Server.sendData(player, EnumPacketClient.GUI_DATA, animation.writeToNBT());
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
			int tab = buffer.readInt();
			for(String name : ServerCloneController.Instance.getClones(tab))
				list.appendTag(new NBTTagString(name));

			NBTTagList listDate = new NBTTagList();
			for(String name : ServerCloneController.Instance.getClonesDate(tab))
				listDate.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			compound.setTag("ListDate", listDate);

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
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
			PlayerData data = PlayerDataController.Instance.getPlayerData(player);
			if(data.cloned == null)
				return;
			String name = Server.readString(buffer);
			int tab = buffer.readInt();
			NBTTagCompound tagExtra = Server.readNBT(buffer);
			ServerCloneController.Instance.addClone(data.cloned, name, tab, tagExtra);
		}
		else if(type == EnumPacketServer.CloneRemove){
			int tab = buffer.readInt();
			ServerCloneController.Instance.removeClone(Server.readString(buffer), tab);

			NBTTagList list = new NBTTagList();

			for(String name : ServerCloneController.Instance.getClones(tab))
				list.appendTag(new NBTTagString(name));

			NBTTagList listDate = new NBTTagList();
			for(String name : ServerCloneController.Instance.getClonesDate(tab))
				listDate.appendTag(new NBTTagString(name));

			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			compound.setTag("ListDate", listDate);

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
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

			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.CloneTagList) {
			int tab = buffer.readInt();
			TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("CloneTags", tagMap.writeNBT());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.TagSet) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
			this.setTags(npc,buffer);
		}
		else if (type != EnumPacketServer.NpcTagsGet && type != EnumPacketServer.TagsGet) {
			warn(player, "WE 2 tried todo something with the wrong tool, probably a hacker");
		}
	}

	private void setTags(EntityNPCInterface npc, ByteBuf buffer) throws IOException {
		npc.advanced.tagUUIDs.removeIf(uuid -> TagController.getInstance().getTagFromUUID(uuid) != null);
		NBTTagCompound compound = Server.readNBT(buffer);
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
