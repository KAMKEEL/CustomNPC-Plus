package noppes.npcs.client;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.player.GuiQuestCompletion;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.Quest;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.Sys;

public class NoppesUtil {

	public static void requestOpenGUI(EnumGuiType gui) {
		requestOpenGUI(gui, 0, 0, 0);
	}

	public static void requestOpenGUI(EnumGuiType gui, int i, int j, int k) {
		Client.sendData(EnumPacketServer.Gui, gui.ordinal(), i, j, k);
	}

	public static void spawnParticle(ByteBuf buffer) throws IOException{
		double posX = buffer.readDouble();
		double posY = buffer.readDouble();
		double posZ = buffer.readDouble();
		float height = buffer.readFloat();
		float width = buffer.readFloat();
		float yOffset = buffer.readFloat();
		
		String particle = Server.readString(buffer);
		World worldObj = Minecraft.getMinecraft().theWorld;

		Random rand = worldObj.rand;
		if(particle.equals("heal")){
	        for (int k = 0; k < 6; k++)
	        {
	        	worldObj.spawnParticle("instantSpell", posX + (rand.nextDouble() - 0.5D) * (double)width, (posY + rand.nextDouble() * (double)height) - (double)yOffset, posZ + (rand.nextDouble() - 0.5D) * (double)width, 0, 0, 0);
	        	worldObj.spawnParticle("spell", posX + (rand.nextDouble() - 0.5D) * (double)width, (posY + rand.nextDouble() * (double)height) - (double)yOffset, posZ + (rand.nextDouble() - 0.5D) * (double)width, 0, 0, 0);
	        }
		}
	}

	public static void clickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));    	
	}

	private static EntityNPCInterface lastNpc;
	public static EntityNPCInterface getLastNpc() {
		return lastNpc;
	}
	public static void setLastNpc(EntityNPCInterface npc) {
		lastNpc = npc;
	}

	public static void openGUI(EntityPlayer player, Object guiscreen) {
		CustomNpcs.proxy.openGui(player, guiscreen);
	}
	
	public static void openFolder(File dir){
        String s = dir.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX)
        {
            try
            {
                //logger.info(s);
                Runtime.getRuntime().exec(new String[] {"/usr/bin/open", s});
                return;
            }
            catch (IOException ioexception1)
            {
                //logger.error("Couldn\'t open file", ioexception1);
            }
        }
        else if (Util.getOSType() == Util.EnumOS.WINDOWS)
        {
            String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[] {s});

            try
            {
                Runtime.getRuntime().exec(s1);
                return;
            }
            catch (IOException ioexception)
            {
                //logger.error("Couldn\'t open file", ioexception);
            }
        }

        boolean flag = false;

        try
        {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
            oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {dir.toURI()});
        }
        catch (Throwable throwable)
        {
            //logger.error("Couldn\'t open link", throwable);
            flag = true;
        }

        if (flag)
        {
            //logger.info("Opening via system class!");
            Sys.openURL("file://" + s);
        }
	}

	public static void setScrollList(ByteBuf buffer) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui instanceof GuiNPCInterface && ((GuiNPCInterface)gui).hasSubGui())
			gui = ((GuiNPCInterface)gui).getSubGui();
		if(gui == null || !(gui instanceof IScrollData))
			return;
		Vector<String> data = new Vector<String>();
		String line;
		
		try {
			int size = buffer.readInt();
			for(int i = 0; i < size; i++){
				data.add(Server.readString(buffer));
			}
		} catch (Exception e) {
			
		}
		
		((IScrollData)gui).setData(data,null);
	}
	
	private static HashMap<String,Integer> data = new HashMap<String,Integer>();

	public static void addScrollData(ByteBuf buffer) {
		try {
			int size = buffer.readInt();
			for(int i = 0; i < size; i++){
				int id = buffer.readInt();
				String name = Server.readString(buffer);
				data.put(name, id);
			}
		} catch (Exception e) {
		}
	}
	
	public static void setScrollData(ByteBuf buffer) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui == null)
			return;
		try {
			int size = buffer.readInt();
			for(int i = 0; i < size; i++){
				int id = buffer.readInt();
				String name = Server.readString(buffer);
				data.put(name, id);
			}
		} catch (Exception e) {
		}
		if(gui instanceof GuiNPCInterface && ((GuiNPCInterface)gui).hasSubGui()){
			gui = (GuiScreen) ((GuiNPCInterface)gui).getSubGui();
		}
		if(gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface)gui).hasSubGui()){
			gui = (GuiScreen) ((GuiContainerNPCInterface)gui).getSubGui();
		}
		if(gui instanceof IScrollData)
			((IScrollData)gui).setData(new Vector<String>(data.keySet()), data);
		data = new HashMap<String,Integer>();
	}

	public static void guiQuestCompletion(EntityPlayer player, NBTTagCompound read) {
		Quest quest = new Quest();
		quest.readNBT(read);
		if (!quest.completeText.equals(""))
			NoppesUtil.openGUI(player, new GuiQuestCompletion(quest));
		else
			NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletion, quest.id);
	}
	
	public static void openDialog(NBTTagCompound compound, EntityNPCInterface npc, EntityPlayer player){
		if(DialogController.instance == null)
			DialogController.instance = new DialogController();
		Dialog dialog = new Dialog();
		dialog.readNBT(compound);
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if(gui == null || !(gui instanceof GuiDialogInteract))
			CustomNpcs.proxy.openGui(player, new GuiDialogInteract(npc, dialog));
		else{
			GuiDialogInteract dia = (GuiDialogInteract) gui;
			dia.appendDialog(dialog);
		}
	}
	public static void saveRedstoneBlock(EntityPlayer player, NBTTagCompound compound){
		int x = compound.getInteger("x");
		int y = compound.getInteger("y");
		int z = compound.getInteger("z");
		
		TileEntity tile = player.worldObj.getTileEntity(x, y, z);
		tile.readFromNBT(compound);
		
		CustomNpcs.proxy.openGui(x, y, z, EnumGuiType.RedstoneBlock, player);
	}
	public static void saveWayPointBlock(EntityPlayer player, NBTTagCompound compound){
		int x = compound.getInteger("x");
		int y = compound.getInteger("y");
		int z = compound.getInteger("z");
		
		TileEntity tile = player.worldObj.getTileEntity(x, y, z);
		tile.readFromNBT(compound);
		
		CustomNpcs.proxy.openGui(x, y, z, EnumGuiType.Waypoint, player);
	}

}
