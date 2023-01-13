package noppes.npcs.controllers.data;

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

	// MAKE WRITE NBT
	// MAKE READ NBT
}
