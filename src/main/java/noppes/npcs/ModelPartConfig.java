package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelPartConfig {
	public float scaleX = 1, scaleY = 1, scaleZ = 1;
	public float transX = 0, transY = 0, transZ = 0;
	

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setFloat("ScaleX", scaleX);
		compound.setFloat("ScaleY", scaleY);
		compound.setFloat("ScaleZ", scaleZ);
		
		compound.setFloat("TransX", transX);
		compound.setFloat("TransY", transY);
		compound.setFloat("TransZ", transZ);
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		scaleX = ValueUtil.correctFloat(compound.getFloat("ScaleX"), 0.5f, 1.5f);
		scaleY = ValueUtil.correctFloat(compound.getFloat("ScaleY"), 0.5f, 1.5f);
		scaleZ = ValueUtil.correctFloat(compound.getFloat("ScaleZ"), 0.5f, 1.5f);

		transX = compound.getFloat("TransX");
		transY = compound.getFloat("TransY");
		transZ = compound.getFloat("TransZ");
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
