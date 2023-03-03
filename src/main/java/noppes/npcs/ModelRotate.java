package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelRotate {
	// Rotation
	public boolean whileStanding = true;
	public boolean whileAttacking = false;
	public boolean whileMoving = false;

	public ModelRotatePart head = new ModelRotatePart();
	public ModelRotatePart body = new ModelRotatePart();
	public ModelRotatePart larm = new ModelRotatePart();
	public ModelRotatePart rarm = new ModelRotatePart();
	public ModelRotatePart lleg = new ModelRotatePart();
	public ModelRotatePart rleg = new ModelRotatePart();

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("PuppetStanding", whileStanding);
		compound.setBoolean("PuppetAttacking", whileAttacking);
		compound.setBoolean("PuppetMoving", whileMoving);
		compound.setTag("PuppetHead", head.writeToNBT());
		compound.setTag("PuppetBody", body.writeToNBT());
		compound.setTag("PuppetRArm", rarm.writeToNBT());
		compound.setTag("PuppetLArm", larm.writeToNBT());
		compound.setTag("PuppetRLeg", rleg.writeToNBT());
		compound.setTag("PuppetLLeg", lleg.writeToNBT());
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		whileStanding = compound.getBoolean("PuppetStanding");
		whileAttacking = compound.getBoolean("PuppetAttacking");
		whileMoving = compound.getBoolean("PuppetMoving");
		head.readFromNBT(compound.getCompoundTag("PuppetHead"));
		body.readFromNBT(compound.getCompoundTag("PuppetBody"));
		rarm.readFromNBT(compound.getCompoundTag("PuppetRArm"));
		larm.readFromNBT(compound.getCompoundTag("PuppetLArm"));
		rleg.readFromNBT(compound.getCompoundTag("PuppetRLeg"));
		lleg.readFromNBT(compound.getCompoundTag("PuppetLLeg"));
	}

}
