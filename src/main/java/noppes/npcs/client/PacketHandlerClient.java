package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.*;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.player.GuiBook;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public class PacketHandlerClient extends PacketHandlerServer{

	@SubscribeEvent
	public void onPacketData(ClientCustomPacketEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		ByteBuf buffer = event.packet.payload();
		try {
			client(buffer,(EntityPlayer) player,EnumPacketClient.values()[buffer.readInt()]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void client(ByteBuf buffer, final EntityPlayer player, EnumPacketClient type) throws IOException{
        if(type == EnumPacketClient.MARK_DATA){
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
            if(!(entity instanceof EntityNPCInterface))
                return;
            MarkData data = MarkData.get((EntityNPCInterface) entity);
            data.setNBT(Server.readNBT(buffer));
        }
		else if(type == EnumPacketClient.QUEST_COMPLETION){
			NoppesUtil.guiQuestCompletion(player, Server.readNBT(buffer));
		}
		else if(type == EnumPacketClient.UPDATE_NPC){
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			((EntityNPCInterface)entity).readSpawnData(compound);
		}
		else if(type == EnumPacketClient.ROLE){
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			((EntityNPCInterface)entity).advanced.setRole(compound.getInteger("Role"));
			((EntityNPCInterface)entity).roleInterface.readFromNBT(compound);
			NoppesUtil.setLastNpc((EntityNPCInterface) entity);
		}
		else if(type == EnumPacketClient.GUI){
			EnumGuiType gui = EnumGuiType.values()[buffer.readInt()];
			CustomNpcs.proxy.openGui(NoppesUtil.getLastNpc(), gui, buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
		else if(type == EnumPacketClient.SCROLL_SELECTED){
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui == null || !(gui instanceof IScrollData))
				return;
			String selected = Server.readString(buffer);

			((IScrollData)gui).setSelected(selected);
		}
		else if(type == EnumPacketClient.GUI_REDSTONE){
			NoppesUtil.saveRedstoneBlock(player, Server.readNBT(buffer));
		}
		else if(type == EnumPacketClient.GUI_WAYPOINT){
			NoppesUtil.saveWayPointBlock(player, Server.readNBT(buffer));
		}
		else if(type == EnumPacketClient.CLONE){
			NBTTagCompound compound = Server.readNBT(buffer);
			NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(compound));
		}
        else if(type == EnumPacketClient.TELEPORTER){
            CustomNpcs.proxy.openGui((EntityNPCInterface)null,EnumGuiType.NpcDimensions);
        }
		else if(type == EnumPacketClient.GUI_DATA){
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui == null)
				return;

			if(gui instanceof GuiNPCInterface && ((GuiNPCInterface)gui).hasSubGui()){
				gui = (GuiScreen) ((GuiNPCInterface)gui).getSubGui();
			}
			else if(gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface)gui).hasSubGui()){
				gui = (GuiScreen) ((GuiContainerNPCInterface)gui).getSubGui();
			}
			if(gui instanceof IGuiData)
				((IGuiData)gui).setGuiData(Server.readNBT(buffer));
		}
		else if(type == EnumPacketClient.VILLAGER_LIST){
            MerchantRecipeList merchantrecipelist = MerchantRecipeList.func_151390_b(new PacketBuffer(buffer));
            ServerEventsHandler.Merchant.setRecipes(merchantrecipelist);
		}
		else if(type == EnumPacketClient.OPEN_BOOK){
			int x = buffer.readInt(), y = buffer.readInt(), z = buffer.readInt();

			NoppesUtil.openGUI(player, new GuiBook(player, ItemStack.loadItemStackFromNBT(Server.readNBT(buffer)), x, y, z));
		}
		else if(type == EnumPacketClient.ISGUIOPEN){
			boolean isGUIOpen = Minecraft.getMinecraft().currentScreen != null;

			NoppesUtil.isGUIOpen(isGUIOpen);
		}
		else if(type == EnumPacketClient.SCRIPT_OVERLAY_DATA){
			OverlayCustom overlayCustom = new OverlayCustom(Minecraft.getMinecraft());
			overlayCustom.setOverlayData(Server.readNBT(buffer));

			ClientCacheHandler.customOverlays.put(overlayCustom.overlay.getID(),overlayCustom);
		}
		else if(type == EnumPacketClient.SCRIPT_OVERLAY_CLOSE){
			int id = buffer.readInt();
			ClientCacheHandler.customOverlays.remove(id);
		}
		else if(type == EnumPacketClient.OVERLAY_QUEST_TRACKING){
			try {
				NBTTagCompound compound = Server.readNBT(buffer);
				ClientCacheHandler.questTrackingOverlay = new OverlayQuestTracking(Minecraft.getMinecraft());
				ClientCacheHandler.questTrackingOverlay.setOverlayData(compound);
			} catch (IOException e) {
				ClientCacheHandler.questTrackingOverlay = null;
			}
		}
		else if(type == EnumPacketClient.SWING_PLAYER_ARM){
			Minecraft.getMinecraft().thePlayer.swingItem();
		}
		else if(type == EnumPacketClient.PLAYER_UPDATE_SKIN_OVERLAYS) {
			EntityPlayer sendingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(Server.readString(buffer));
			NBTTagCompound compound = Server.readNBT(buffer);
			if (sendingPlayer != null) {
				NoppesUtil.updateSkinOverlayData(sendingPlayer, compound);
			}
		}
		else if(type == EnumPacketClient.UPDATE_ANIMATIONS) {
			NBTTagCompound compound = Server.readNBT(buffer);
			AnimationData animationData = null;
			if (compound.hasKey("EntityId")) {
				Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(compound.getInteger("EntityId"));
				if (entity instanceof EntityNPCInterface) {
					animationData = ((EntityNPCInterface) entity).display.animationData;
				}
			} else {
				EntityPlayer sendingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(Server.readString(buffer));
				if (sendingPlayer != null) {
					if (!ClientCacheHandler.playerAnimations.containsKey(sendingPlayer.getUniqueID())) {
						ClientCacheHandler.playerAnimations.put(sendingPlayer.getUniqueID(), new AnimationData(sendingPlayer));
					}
					animationData = ClientCacheHandler.playerAnimations.get(sendingPlayer.getUniqueID());
                    animationData.parent = sendingPlayer;
				}
			}

			if (animationData != null) {
                int animationId;
                if (compound.hasKey("AnimationID")) {
                    animationId = compound.getInteger("AnimationID");
                } else {
                    Animation animation = new Animation();
                    animation.readFromNBT(compound.getCompoundTag("Animation"));
                    ClientCacheHandler.animationCache.put(animation.getID(), animation);
                    animationId = animation.getID();
                }

                animationData.setAnimation(ClientCacheHandler.animationCache.get(animationId));
				animationData.readFromNBT(compound);
                Client.sendData(EnumPacketServer.CacheAnimation, animationId);
			}
		}
		else if(type == EnumPacketClient.DISABLE_MOUSE_INPUT) {
			long length = buffer.readLong();
			try {
				String parsedButtons = Server.readString(buffer);
				if (parsedButtons == null || parsedButtons.isEmpty()) {
					ClientEventHandler.disabledButtonTimes.put(-1, length);
					return;
				}

				String[] splitIds = parsedButtons.split(";");
				for (String s : splitIds) {
					ClientEventHandler.disabledButtonTimes.put(Integer.parseInt(s), length);
				}
			} catch (Exception ignored) {}
		}
		else if(type == EnumPacketClient.SYNC_WEAPON) {
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
			if(!(entity instanceof EntityNPCInterface))
				return;
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			int weaponSlotIndex = buffer.readInt();
			ItemStack stack = ItemStack.loadItemStackFromNBT(Server.readNBT(buffer));
			npc.inventory.weapons.put(weaponSlotIndex,stack);
		}
        else if(type == EnumPacketClient.PARTY_DATA){
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if(gui == null)
                return;

            if(gui instanceof GuiNPCInterface && ((GuiNPCInterface)gui).hasSubGui()){
                gui = (GuiScreen) ((GuiNPCInterface)gui).getSubGui();
            }
            else if(gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface)gui).hasSubGui()){
                gui = (GuiScreen) ((GuiContainerNPCInterface)gui).getSubGui();
            }
            if(gui instanceof IPartyData)
                ((IPartyData)gui).setPartyData(Server.readNBT(buffer));
        }
	}
}
