package noppes.npcs;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.*;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.io.IOException;

public class PacketHandlerServer{

	@SubscribeEvent
	public void onServerPacket(ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer)event.handler).playerEntity;
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
					if (ConfigScript.canScript(player, CustomNpcsPermissions.SCRIPT)) {
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

    private void npcEventScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc) throws Exception {
		DataScript data = npc.script;
		if(type == EnumPacketServer.EventScriptDataGet) {
			PacketUtil.getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.EventScriptDataSave) {
			PacketUtil.saveScripts(data,buffer,player);
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
			PacketUtil.getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.ScriptPlayerSave) {
            int tab = buffer.getInt(buffer.readerIndex());
			PacketUtil.saveScripts(data,buffer,player);
			ScriptController.Instance.lastPlayerUpdate = System.currentTimeMillis();
            if(tab == -1)
                ScriptController.Instance.savePlayerScriptsSync();
		}
	}

	private void forgeScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		ForgeDataScript data = ScriptController.Instance.forgeScripts;
		if (type == EnumPacketServer.ScriptForgeGet) {
			PacketUtil.getScripts(data,buffer,player);
		} else if (type == EnumPacketServer.ScriptForgeSave) {
            int tab = buffer.getInt(buffer.readerIndex());
			PacketUtil.saveScripts(data,buffer,player);
			ScriptController.Instance.lastForgeUpdate = System.currentTimeMillis();
            if(tab == -1)
                ScriptController.Instance.saveForgeScriptsSync();
		}
	}

	private void npcGlobalScriptPackets(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player) throws Exception {
		GlobalNPCDataScript data = ScriptController.Instance.globalNpcScripts;
		if(type == EnumPacketServer.ScriptGlobalNPCGet) {
			PacketUtil.getScripts(data,buffer,player);
		} else if(type == EnumPacketServer.ScriptGlobalNPCSave) {
            int tab = buffer.getInt(buffer.readerIndex());
			PacketUtil.saveScripts(data,buffer,player);
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

	private void warn(EntityPlayer player, String warning){
		MinecraftServer.getServer().logWarning(player.getCommandSenderName() + ": " + warning);
	}
}
