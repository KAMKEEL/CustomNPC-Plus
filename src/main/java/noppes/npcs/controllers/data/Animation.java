package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;


public class Animation {

	public ArrayList<Frame> frames = new ArrayList<Frame>();
	public int id = 0;
	public String name;
	public float speed = 1.0F;
	public boolean smooth = false;

	public Animation(){}

	public Animation(ArrayList<Frame> parts, String name){
		this.frames = parts;
		this.name = name;
	}

	public Animation(ArrayList<Frame> parts, String name, float speed, boolean smooth){
		this.frames = parts;
		this.name = name;
		this.speed = speed;
		this.smooth = smooth;
	}

	public ArrayList<Frame> getFrames() {
		return frames;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float getSpeed() {
		return speed;
	}

	public boolean isSmooth() {
		return smooth;
	}

	public void readFromNBT(NBTTagCompound compound){
		name = compound.getString("Name");
		id = compound.getInteger("ID");
		speed = compound.getFloat("Speed");
		smooth = compound.getBoolean("Smooth");

		ArrayList<Frame> frames = new ArrayList<Frame>();
		NBTTagList list = compound.getTagList("Frames", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			Frame frame = new Frame();
			frame.readFromNBT(item);
			frames.add(frame);
		}
		this.frames = frames;
	}

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("Name", name);
		compound.setInteger("ID", id);
		compound.setFloat("Speed", speed);
		compound.setBoolean("Smooth", smooth);

		NBTTagList list = new NBTTagList();
		for(Frame frame : frames){
			NBTTagCompound item = frame.writeToNBT();
			list.appendTag(item);
		}
		compound.setTag("Frames", list);
		return compound;
	}

}
