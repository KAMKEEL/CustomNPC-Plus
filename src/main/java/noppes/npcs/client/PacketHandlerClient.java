package noppes.npcs.client;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.*;
import noppes.npcs.client.ClientProxy.FontContainer;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.ScriptClientSound;
import noppes.npcs.client.controllers.ScriptSoundController;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.player.GuiBook;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.SyncType;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;
import java.util.HashMap;

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
		if(type == EnumPacketClient.CHATBUBBLE){
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if(npc.messages == null)
				npc.messages = new RenderChatMessages();
			String text = NoppesStringUtils.formatText(Server.readString(buffer), player, npc);
			npc.messages.addMessage(text, npc);

			if(buffer.readBoolean())
				player.addChatMessage(new ChatComponentTranslation(npc.getCommandSenderName() + ": " + text));
		}
		else if(type == EnumPacketClient.CHAT){
            if(!ConfigClient.ChatAlerts)
                return;

			String message = "";
			String str;
			while((str = Server.readString(buffer)) != null && !str.isEmpty())
				message += StatCollector.translateToLocal(str);

			player.addChatMessage(new ChatComponentTranslation(message));
		}
		else if(type == EnumPacketClient.MESSAGE || type == EnumPacketClient.PARTY_MESSAGE){
            if(!ConfigClient.BannerAlerts)
                return;

			String description = StatCollector.translateToLocal(Server.readString(buffer));
			String message = Server.readString(buffer);
			Achievement ach = type == EnumPacketClient.MESSAGE ? new MessageAchievement(message, description) : new MessageAchievement(CustomItems.bag == null ? Items.paper : CustomItems.bag, message, description);
			Minecraft.getMinecraft().guiAchievement.func_146256_a(ach);
			ObfuscationReflectionHelper.setPrivateValue(GuiAchievement.class, Minecraft.getMinecraft().guiAchievement, ach.getDescription(), 4);
		}
		else if(type == EnumPacketClient.SYNCRECIPES_ADD){
			NBTTagList list = Server.readNBT(buffer).getTagList("recipes", 10);
	        if(list == null)
	        	return;
            for(int i = 0; i < list.tagCount(); i++)
            {
        		RecipeCarpentry recipe = RecipeCarpentry.read(list.getCompoundTagAt(i));
            	RecipeController.syncRecipes.put(recipe.id,recipe);
            }

		}
		else if(type == EnumPacketClient.SYNCRECIPES_WORKBENCH){
            RecipeController.reloadGlobalRecipes(RecipeController.syncRecipes);
            RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
		}
		else if(type == EnumPacketClient.SYNCRECIPES_CARPENTRYBENCH){
            RecipeController.Instance.anvilRecipes = RecipeController.syncRecipes;
            RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
		}
        else if(type == EnumPacketClient.SYNC_ADD || type == EnumPacketClient.SYNC_END){
            int synctype = buffer.readInt();
            NBTTagCompound compound = Server.readNBT(buffer);

            SyncController.clientSync(synctype, compound, type == EnumPacketClient.SYNC_END);

            if(synctype == SyncType.PLAYER_DATA){
                ClientCacheHandler.playerData.setNBT(compound);
            }
        }
        else if(type == EnumPacketClient.SYNC_UPDATE){
            int synctype = buffer.readInt();
            NBTTagCompound compound = Server.readNBT(buffer);
            SyncController.clientSyncUpdate(synctype, compound, buffer);
        }
        else if(type == EnumPacketClient.SYNC_REMOVE){
            int synctype = buffer.readInt();
            int id = buffer.readInt();

            SyncController.clientSyncRemove(synctype, id);
        }
        else if(type == EnumPacketClient.MARK_DATA){
            Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
            if(!(entity instanceof EntityNPCInterface))
                return;
            MarkData data = MarkData.get((EntityNPCInterface) entity);
            data.setNBT(Server.readNBT(buffer));
        }
		else if(type == EnumPacketClient.DIALOG){
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			NoppesUtil.openDialog(Server.readNBT(buffer), (EntityNPCInterface) entity, player);
		}
		else if(type == EnumPacketClient.DIALOG_DUMMY){
			EntityDialogNpc npc = new EntityDialogNpc(player.worldObj);
			npc.display.name = Server.readString(buffer);
			EntityUtil.Copy(player, npc);
			NoppesUtil.openDialog(Server.readNBT(buffer), npc, player);
		}
		else if(type == EnumPacketClient.QUEST_COMPLETION){
			NoppesUtil.guiQuestCompletion(player, Server.readNBT(buffer));
		}
		else if(type == EnumPacketClient.EDIT_NPC){
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			NoppesUtil.setLastNpc((EntityNPCInterface) entity);
		}
		else if(type == EnumPacketClient.PLAY_MUSIC){
			MusicController.Instance.playMusic(Server.readString(buffer), player);
		}
		else if(type == EnumPacketClient.PLAY_SOUND){
			MusicController.Instance.playSound(Server.readString(buffer),buffer.readFloat(),buffer.readFloat(),buffer.readFloat());
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
		else if(type == EnumPacketClient.SCRIPTED_PARTICLE){
			NoppesUtil.spawnScriptedParticle(player, buffer);
		}
		else if(type == EnumPacketClient.PARTICLE){
			NoppesUtil.spawnParticle(buffer);
		}
		else if(type == EnumPacketClient.DELETE_NPC){
			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(buffer.readInt());
			if(entity == null || !(entity instanceof EntityNPCInterface))
				return;
			((EntityNPCInterface)entity).delete();
		}
		else if(type == EnumPacketClient.SCROLL_LIST){
			NoppesUtil.setScrollList(buffer);
		}
		else if(type == EnumPacketClient.SCROLL_DATA){
			NoppesUtil.setScrollData(buffer);
		}
		else if(type == EnumPacketClient.SCROLL_DATA_PART){
			NoppesUtil.addScrollData(buffer);
		}
		else if(type == EnumPacketClient.SCROLL_GROUP){
			NoppesUtil.setScrollGroup(buffer);
		}
		else if(type == EnumPacketClient.SCROLL_GROUP_PART){
			NoppesUtil.addScrollGroup(buffer);
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
        else if(type == EnumPacketClient.CLONER){
            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();
            CustomNpcs.proxy.openGui(x, y, z, EnumGuiType.MobSpawner, player);
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
		else if(type == EnumPacketClient.GUI_ERROR){
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui == null || !(gui instanceof IGuiError))
				return;

			int i = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);

			((IGuiError)gui).setError(i, compound);
		}
		else if(type == EnumPacketClient.GUI_CLOSE){
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if(gui == null)
				return;

			if(gui instanceof IGuiClose){
				int i = buffer.readInt();
				NBTTagCompound compound = Server.readNBT(buffer);
					((IGuiClose)gui).setClose(i, compound);
			}

			Minecraft mc = Minecraft.getMinecraft();
	        mc.displayGuiScreen(null);
	        mc.setIngameFocus();
		}
		else if(type == EnumPacketClient.VILLAGER_LIST){
            MerchantRecipeList merchantrecipelist = MerchantRecipeList.func_151390_b(new PacketBuffer(buffer));
            ServerEventsHandler.Merchant.setRecipes(merchantrecipelist);
		}
		else if(type == EnumPacketClient.OPEN_BOOK){
			int x = buffer.readInt(), y = buffer.readInt(), z = buffer.readInt();

			NoppesUtil.openGUI(player, new GuiBook(player, ItemStack.loadItemStackFromNBT(Server.readNBT(buffer)), x, y, z));
		}
		else if(type == EnumPacketClient.CONFIG){
			int config = buffer.readInt();
			if(config == 0){//Font
				String font = Server.readString(buffer);
				int size = buffer.readInt();
				if(!font.isEmpty()){
					ConfigClient.FontType = font;
					ConfigClient.FontSize = size;
					ClientProxy.Font = new FontContainer(ConfigClient.FontType, ConfigClient.FontSize);

					ConfigClient.FontTypeProperty.set(ConfigClient.FontType);
					ConfigClient.FontSizeProperty.set(ConfigClient.FontSize);
					if(ConfigClient.config.hasChanged()){
						ConfigClient.config.save();
					}

					player.addChatMessage(new ChatComponentTranslation("Font set to %s", ClientProxy.Font.getName()));
				}
				else
					player.addChatMessage(new ChatComponentTranslation("Current font is %s", ClientProxy.Font.getName()));
			}
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
				animationData.viewReadFromNBT(compound);

                if (compound.hasKey("Frame")) {
                    animationData.animation.readFromNBT(compound.getCompoundTag("Animation"));
                    animationData.animation.jumpToFrameAtTime(compound.getInteger("Frame"), compound.getInteger("Time"));
                } else if (compound.hasKey("Animation")) {
                    Client.sendData(EnumPacketServer.CacheAnimation, animationId);
                }
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
		else if(type == EnumPacketClient.PLAY_SOUND_TO) {
			int id = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			ScriptClientSound sound = ScriptClientSound.fromScriptSound(compound, player.worldObj);

			ScriptSoundController.Instance.playSound(id,sound);
		}
		else if(type == EnumPacketClient.PLAY_SOUND_TO_NO_ID) {
			NBTTagCompound compound = Server.readNBT(buffer);
			ScriptClientSound sound = ScriptClientSound.fromScriptSound(compound, player.worldObj);
			ScriptSoundController.Instance.playSound(sound);
		}
		else if(type == EnumPacketClient.STOP_SOUND_FOR) {
			int id = buffer.readInt();
			ScriptSoundController.Instance.stopSound(id);
		}
		else if(type == EnumPacketClient.PAUSE_SOUNDS) {
			ScriptSoundController.Instance.pauseAllSounds();
		}
		else if(type == EnumPacketClient.CONTINUE_SOUNDS) {
			ScriptSoundController.Instance.continueAllSounds();
		}
		else if(type == EnumPacketClient.STOP_SOUNDS) {
			ScriptSoundController.Instance.stopAllSounds();
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
