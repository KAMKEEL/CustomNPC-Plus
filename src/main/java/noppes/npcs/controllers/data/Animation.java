package noppes.npcs.controllers.data;

import java.util.ArrayList;


public class Animation {

	public ArrayList<Frame> frames = new ArrayList<Frame>();
	public int id = 0;
	public String name;
	public float speed = 1.0F;
	public boolean smooth = false;

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

	// MAKE WRITE NBT
	// MAKE READ NBT
}
