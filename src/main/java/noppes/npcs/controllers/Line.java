package noppes.npcs.controllers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;


public class Line {

	public Line(){
		
	}
	public Line(String text){
		this.text = text;
	}

	public String text = "";
	public String sound = "";
	public boolean hideText = false;
	
	public Line copy() {
		Line line = new Line(text);
		line.sound = sound;
		line.hideText = hideText;
		return line;
	}
	
	public Line formatTarget(EntityLivingBase entity) {
		if(entity == null)
			return this;
		Line line = copy();
		if(entity instanceof EntityPlayer)
			line.text = line.text.replace("@target", ((EntityPlayer) entity).getDisplayName());
		else
			line.text = line.text.replace("@target", entity.getCommandSenderName());
		return line;
	}
}
