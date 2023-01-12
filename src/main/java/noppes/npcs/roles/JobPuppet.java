package noppes.npcs.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;

public class JobPuppet extends JobInterface{
	public PartConfig head = new PartConfig(this);
	public PartConfig larm = new PartConfig(this);
	public PartConfig rarm = new PartConfig(this);
	public PartConfig body = new PartConfig(this);
	public PartConfig lleg = new PartConfig(this);
	public PartConfig rleg = new PartConfig(this);

	public boolean enabled = false;
	public boolean whileStanding = true;
	public boolean whileAttacking = false;
	public boolean whileMoving = false;

	public JobPuppet(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("PuppetHead", head.writeNBT());
		compound.setTag("PuppetLArm", larm.writeNBT());
		compound.setTag("PuppetRArm", rarm.writeNBT());
		compound.setTag("PuppetBody", body.writeNBT());
		compound.setTag("PuppetLLeg", lleg.writeNBT());
		compound.setTag("PuppetRLeg", rleg.writeNBT());

		compound.setBoolean("PuppetEnabled", enabled);
		compound.setBoolean("PuppetStanding", whileStanding);
		compound.setBoolean("PuppetAttacking", whileAttacking);
		compound.setBoolean("PuppetMoving", whileMoving);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		head.readNBT(compound.getCompoundTag("PuppetHead"));
		larm.readNBT(compound.getCompoundTag("PuppetLArm"));
		rarm.readNBT(compound.getCompoundTag("PuppetRArm"));
		body.readNBT(compound.getCompoundTag("PuppetBody"));
		lleg.readNBT(compound.getCompoundTag("PuppetLLeg"));
		rleg.readNBT(compound.getCompoundTag("PuppetRLeg"));

		enabled = compound.getBoolean("PuppetEnabled");
		whileStanding = compound.getBoolean("PuppetStanding");
		whileAttacking = compound.getBoolean("PuppetAttacking");
		whileMoving = compound.getBoolean("PuppetMoving");
	}
	
	@Override
	public boolean aiShouldExecute() {
		return false;
	}

	@Override
	public void reset() {
	}
	public void delete() {
	}

	public boolean isActive() {
		if (!this.enabled)
			return false;

		if(!npc.isEntityAlive())
			return false;
		
		if(whileAttacking && npc.isAttacking() || whileMoving && npc.isWalking() || whileStanding && !npc.isWalking())
			return true;
		return false;
	}

}
