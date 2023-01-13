package noppes.npcs.controllers.data;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.EnumAnimationPart;

import javax.vecmath.Vector3f;
import java.util.Vector;


public class FramePart {

	public EnumAnimationPart part;
	public int[] rotation = {0, 0, 0};
	public int[] pivot = {0, 0, 0};

	boolean customized = false;

	public float speed = 1.0F;
	public boolean smooth = false;

	public FramePart(EnumAnimationPart part){
		this.part = part;
	}

	public FramePart(EnumAnimationPart part, int[] rotation, int[] pivot){
		this.part = part;
		this.rotation = rotation;
		this.pivot = pivot;
	}

	public FramePart(EnumAnimationPart part, int[] rotation, int[] pivot, float speed, boolean smooth){
		this.part = part;
		this.rotation = rotation;
		this.pivot = pivot;
		this.speed = speed;
		this.smooth = smooth;
		this.customized = true;
	}

	public EnumAnimationPart getPart() {
		return part;
	}

	public void setPart(EnumAnimationPart part) {
		this.part = part;
	}

	public int[] getRotation() {
		return rotation;
	}

	public void setRotation(int[] rotation) {
		this.rotation = rotation;
	}

	public int[] getPivot() {
		return pivot;
	}

	public void setPivot(int[] pivot) {
		this.pivot = pivot;
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
