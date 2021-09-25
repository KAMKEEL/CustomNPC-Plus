package noppes.npcs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentTranslation;

public class VersionChecker extends Thread{
	private int revision = 15;
	public VersionChecker(){
		
	}
	public void run(){
		String name = '\u00A7'+ "2CustomNpcs" + '\u00A7' + "f";
		String link = '\u00A7'+"9"+'\u00A7' + "nClick here"; 
		String text =  name +" installed. For more info " + link;
		
        EntityPlayer player;
		try{
			player = Minecraft.getMinecraft().thePlayer;
		}
		catch(NoSuchMethodError e){
			return;
		}
        while((player = Minecraft.getMinecraft().thePlayer) == null){
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        ChatComponentTranslation message = new ChatComponentTranslation(text);
        message.getChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, "http://www.kodevelopment.nl/minecraft/customnpcs/"));
        player.addChatMessage(message);
	}
}
