package noppes.npcs.client;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.PacketHandlerServer;
import noppes.npcs.Server;
import noppes.npcs.ServerEventsHandler;
import noppes.npcs.client.ClientProxy.FontContainer;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;
import noppes.npcs.client.gui.player.GuiBook;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IGuiError;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.RecipeCarpentry;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;

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
			String message = "";
			String str;
			while((str = Server.readString(buffer)) != null && !str.isEmpty())
				message += StatCollector.translateToLocal(str);
			
			player.addChatMessage(new ChatComponentTranslation(message));
		}
		else if(type == EnumPacketClient.MESSAGE){
			String description = StatCollector.translateToLocal(Server.readString(buffer));
			String message = Server.readString(buffer);
			Achievement ach = new QuestAchievement(message, description);
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
            RecipeController.instance.anvilRecipes = RecipeController.syncRecipes;
            RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
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
					CustomNpcs.FontType = font; 
					CustomNpcs.FontSize = size;
					ClientProxy.Font = new FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
					CustomNpcs.Config.updateConfig();
					player.addChatMessage(new ChatComponentTranslation("Font set to %s", ClientProxy.Font.getName()));
				}
				else
					player.addChatMessage(new ChatComponentTranslation("Current font is %s", ClientProxy.Font.getName()));
			}
		}
	}



}
