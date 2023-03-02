package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelPartConfig {
	//
	public float scaleX = 1, scaleY = 1, scaleZ = 1;
	public boolean shared = false;

	public float rotationX = 0f, rotationY = 0f, rotationZ = 0f;
	public boolean disabled = false;

	public boolean hide = false;
	// Limbwear [0: None, 1: Normal, 2: Solid]
	public byte limbwear = 0;

	public ModelPartConfig(){}

	public ModelPartConfig(byte limbDefault){
		this.limbwear = limbDefault;
	}

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setFloat("ScaleX", scaleX);
		compound.setFloat("ScaleY", scaleY);
		compound.setFloat("ScaleZ", scaleZ);
		compound.setBoolean("Shared", shared);

		compound.setBoolean("Disabled", disabled);
		compound.setFloat("RotationX", rotationX);
		compound.setFloat("RotationY", rotationY);
		compound.setFloat("RotationZ", rotationZ);

//		compound.setByte("Limbwear", limbwear);
//		compound.setBoolean("Hide", hide);
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		scaleX = ValueUtil.correctFloat(compound.getFloat("ScaleX"), 0.5f, 1.5f);
		scaleY = ValueUtil.correctFloat(compound.getFloat("ScaleY"), 0.5f, 1.5f);
		scaleZ = ValueUtil.correctFloat(compound.getFloat("ScaleZ"), 0.5f, 1.5f);

		disabled = compound.getBoolean("Disabled");
		rotationX = ValueUtil.correctFloat(compound.getFloat("RotationX"), -0.5f, 0.5f);
		rotationY = ValueUtil.correctFloat(compound.getFloat("RotationY"), -0.5f, 0.5f);
		rotationZ = ValueUtil.correctFloat(compound.getFloat("RotationZ"), -0.5f, 0.5f);

//		limbwear = compound.getByte("Limbwear");
//		hide = compound.getBoolean("Hide");
	}
	
	public String toString(){
		return "ScaleX: " + scaleX + " - ScaleY: " + scaleY + " - ScaleZ: " + scaleZ;
	}

	public void setScale(float x, float y, float z) {
		scaleX = x;
		scaleY = y;
		scaleZ = z;
	}
	public void setScale(float x, float y) {
		scaleZ = scaleX = x;
		scaleY = y;
	}

}
