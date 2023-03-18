package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.IModelRotate;
import noppes.npcs.api.entity.data.IModelRotatePart;
import noppes.npcs.util.ValueUtil;

public class ModelRotate implements IModelRotate {
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

	public boolean whileStanding() {
		return this.whileStanding;
	}

	public void whileStanding(boolean whileStanding) {
		this.whileStanding = whileStanding;
	}

	public boolean whileAttacking() {
		return this.whileAttacking;
	}

	public void whileAttacking(boolean whileAttacking) {
		this.whileAttacking = whileAttacking;
	}

	public boolean whileMoving() {
		return this.whileMoving;
	}

	public void whileMoving(boolean whileMoving) {
		this.whileMoving = whileMoving;
	}

	public IModelRotatePart getPart(int part) {
		switch (ValueUtil.clamp(part,0,5)) {
			case 0:
				return head;
			case 1:
				return body;
			case 2:
				return larm;
			case 3:
				return rarm;
			case 4:
				return lleg;
			case 5:
				return rleg;
		}
		return null;
	}
}
