package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IModelPart;

public class AnimationPartConfig implements IModelPart {
	public float rotationX = 0f, rotationY = 0f, rotationZ = 0f;
	public float pivotX = 0f, pivotY = 0f, pivotZ = 0f;

	public boolean enablePart = false;
	public boolean fullAngles = false;
	public boolean animate = false;
	public float animRate = 1.0F;
	public boolean interpolate = true;

	// vvv Client-sided use vvv
	public float[] prevRotations = new float[]{0, 0, 0};
	public float[] prevPivots = new float[]{0, 0, 0};
	public float partialRotationTick = 0f;
	public float partialPivotTick = 0f;
	// ^^^ Client-sided use ^^^

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("EnablePart", enablePart);
		if(enablePart){
			compound.setFloat("RotationX", rotationX);
			compound.setFloat("RotationY", rotationY);
			compound.setFloat("RotationZ", rotationZ);
			compound.setFloat("PivotX", pivotX);
			compound.setFloat("PivotY", pivotY);
			compound.setFloat("PivotZ", pivotZ);

			compound.setBoolean("PuppetFullAngles", fullAngles);

			compound.setBoolean("PuppetInterpolate", interpolate);
			compound.setBoolean("PuppetAnimate", animate);
			compound.setFloat("PuppetAnimSpeed", animRate);
		}
		return compound;
	}

	public void readNBT(NBTTagCompound compound) {
		enablePart = compound.getBoolean("EnablePart");
		if(enablePart){
			rotationX = compound.getFloat("RotationX");
			rotationY = compound.getFloat("RotationY");
			rotationZ = compound.getFloat("RotationZ");
			pivotX = compound.getFloat("PivotX");
			pivotY = compound.getFloat("PivotY");
			pivotZ = compound.getFloat("PivotZ");

			fullAngles = compound.getBoolean("PuppetFullAngles");

			if (!compound.hasKey("PuppetInterpolate")) {
				interpolate = true;
			} else {
				interpolate = compound.getBoolean("PuppetInterpolate");
			}
			animate = compound.getBoolean("PuppetAnimate");
			animRate = compound.getFloat("PuppetAnimSpeed");
		}
	}

	public void setEnabled(boolean enabled) {
		this.enablePart = enabled;
	}

	public boolean isEnabled() {
		return this.enablePart;
	}

	public void setAnimated(boolean animated) {
		this.animate = animated;
	}

	public boolean isAnimated() {
		return this.animate;
	}

	public void setInterpolated(boolean interpolate) {
		this.interpolate = interpolate;
	}

	public boolean isInterpolated() {
		return this.interpolate;
	}

	public void setFullAngles(boolean fullAngles) {
		this.fullAngles = fullAngles;
	}

	public boolean fullAngles() {
		return this.fullAngles;
	}

	public void setAnimRate(float animRate) {
		this.animRate = animRate;
	}

	public float getAnimRate() {
		return this.animRate;
	}

	public void setRotation(float rotationX, float rotationY, float rotationZ) {
		this.setRotationX(rotationX);
		this.setRotationY(rotationY);
		this.setRotationZ(rotationZ);
	}

	public void setRotationX(float rotation) {
		float f = rotation / 360f - 0.5f;
		this.rotationX = f;
	}

	public void setRotationY(float rotation) {
		float f = rotation / 360f - 0.5f;
		this.rotationY = f;
	}

	public void setRotationZ(float rotation) {
		float f = rotation / 360f - 0.5f;
		this.rotationZ = f;
	}

	public float getRotationX() {
		return this.rotationX;
	}

	public float getRotationY() {
		return this.rotationY;
	}

	public float getRotationZ() {
		return this.rotationZ;
	}

	public void setOffset(float offsetX, float offsetY, float offsetZ) {
		this.setOffsetX(offsetX);
		this.setOffsetY(offsetY);
		this.setOffsetZ(offsetZ);
	}

	public void setOffsetX(float offset) {
		this.pivotX = offset;
	}

	public void setOffsetY(float offset) {
		this.pivotY = offset;
	}

	public void setOffsetZ(float offset) {
		this.pivotZ = offset;
	}

	public float getOffsetX() {
		return this.pivotX;
	}

	public float getOffsetY() {
		return this.pivotY;
	}

	public float getOffsetZ() {
		return this.pivotZ;
	}

}
