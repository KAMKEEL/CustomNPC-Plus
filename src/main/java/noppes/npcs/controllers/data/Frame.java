package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumAnimationPart;

import java.util.ArrayList;


public class Frame {

	public ArrayList<FramePart> frameParts = new ArrayList<FramePart>();
	public int duration = 0;
	boolean customized = false;
	public float speed = 1.0F;
	public boolean smooth = false;

	public Frame(){}

	public Frame(ArrayList<FramePart> parts, int duration){
		this.frameParts = parts;
		this.duration = duration;
	}

	public Frame(ArrayList<FramePart> parts, int duration, float speed, boolean smooth){
		this.frameParts = parts;
		this.duration = duration;
		this.speed = speed;
		this.smooth = smooth;
		this.customized = true;
	}

	public ArrayList<FramePart> getFrameParts() {
		return frameParts;
	}

	public void setFrameParts(ArrayList<FramePart> frameParts) {
		this.frameParts = frameParts;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isCustomized() {
		return customized;
	}

	public void setCustomized(boolean customized) {
		this.customized = customized;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean isSmooth() {
		return smooth;
	}

	public void setSmooth(boolean smooth) {
		this.smooth = smooth;
	}

	public void readFromNBT(NBTTagCompound compound){
		duration = compound.getInteger("Duration");

		// Customized = TRUE if Speed or Smooth Exist
		if(compound.hasKey("Speed")){
			customized = true;
			speed = compound.getFloat("Speed");
		}
		if(compound.hasKey("Smooth")){
			customized = true;
			smooth = compound.getBoolean("Smooth");
		}

		ArrayList<FramePart> frameParts = new ArrayList<FramePart>();
		NBTTagList list = compound.getTagList("FrameParts", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			FramePart framePart = new FramePart();
			framePart.readFromNBT(item);
			frameParts.add(framePart);
		}
		this.frameParts = frameParts;
	}

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("Duration", duration);
		if(customized){
			compound.setFloat("Speed", speed);
			compound.setBoolean("Smooth", smooth);
		}

		NBTTagList list = new NBTTagList();
		for(FramePart framePart : frameParts){
			NBTTagCompound item = framePart.writeToNBT();
			list.appendTag(item);
		}
		compound.setTag("FrameParts", list);
		return compound;
	}
}
