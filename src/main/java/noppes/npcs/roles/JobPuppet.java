package noppes.npcs.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class JobPuppet extends JobInterface{
	public PartConfig head = new PartConfig();
	public PartConfig larm = new PartConfig();
	public PartConfig rarm = new PartConfig();
	public PartConfig body = new PartConfig();
	public PartConfig lleg = new PartConfig();
	public PartConfig rleg = new PartConfig();
	
	public boolean whileStanding = true;
	public boolean whileAttacking = false;
	public boolean whileMoving = false;
	public boolean fullAngles = false;

	public boolean animate = false;
	public float animRate = 1.0F;
	public boolean interpolate = true;

	//Client-sided use
	public float[] bipedRotsHead = {0,0,0};
	public float[] bipedRotsBody = {0,0,0};
	public float[] bipedRotsLeftArm = {0,0,0};
	public float[] bipedRotsRightArm = {0,0,0};
	public float[] bipedRotsLeftLeg = {0,0,0};
	public float[] bipedRotsRightLeg = {0,0,0};

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

		compound.setBoolean("PuppetStanding", whileStanding);
		compound.setBoolean("PuppetAttacking", whileAttacking);
		compound.setBoolean("PuppetMoving", whileMoving);
		compound.setBoolean("PuppetFullAngles", fullAngles);

		compound.setBoolean("PuppetInterpolate", interpolate);
		compound.setBoolean("PuppetAnimate", animate);
		compound.setFloat("PuppetAnimSpeed", animRate);
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

		whileStanding = compound.getBoolean("PuppetStanding");
		whileAttacking = compound.getBoolean("PuppetAttacking");
		whileMoving = compound.getBoolean("PuppetMoving");
		fullAngles = compound.getBoolean("PuppetFullAngles");

		if (!compound.hasKey("PuppetInterpolate")) {
			interpolate = true;
		} else {
			interpolate = compound.getBoolean("PuppetInterpolate");
		}
		animate = compound.getBoolean("PuppetAnimate");
		animRate = compound.getFloat("PuppetAnimSpeed");
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
		if(!npc.isEntityAlive())
			return false;
		
		if(whileAttacking && npc.isAttacking() || whileMoving && npc.isWalking() || whileStanding && !npc.isWalking())
			return true;
		return false;
	}
	
	public static class PartConfig {
		public float rotationX = 0f;
		public float rotationY = 0f;
		public float rotationZ = 0f;
		public float pivotX = 0f;
		public float pivotY = 0f;
		public float pivotZ = 0f;

		//Client-sided use
		public float prevPivotX = 0f;
		public float prevPivotY = 0f;
		public float prevPivotZ = 0f;

		public float partialRotationTick = 0f;
		public float partialPivotTick = 0f;
		public float destPivotX = 0f;
		public float destPivotY = 0f;
		public float destPivotZ = 0f;
		public boolean setOriginalPivot = false;
		public float originalPivotX = 0f;
		public float originalPivotY = 0f;
		public float originalPivotZ = 0f;

		public boolean disabled = false;
		
		public NBTTagCompound writeNBT(){
			NBTTagCompound compound = new NBTTagCompound();
			compound.setFloat("RotationX", rotationX);
			compound.setFloat("RotationY", rotationY);
			compound.setFloat("RotationZ", rotationZ);
			compound.setFloat("PivotX", pivotX);
			compound.setFloat("PivotY", pivotY);
			compound.setFloat("PivotZ", pivotZ);

			compound.setBoolean("Disabled", disabled);
			return compound;
		}
		
		public void readNBT(NBTTagCompound compound){
			rotationX = compound.getFloat("RotationX");
			rotationY = compound.getFloat("RotationY");
			rotationZ = compound.getFloat("RotationZ");
			pivotX = compound.getFloat("PivotX");
			pivotY = compound.getFloat("PivotY");
			pivotZ = compound.getFloat("PivotZ");

			disabled = compound.getBoolean("Disabled");
		}
	}
}
