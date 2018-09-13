package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import noppes.npcs.controllers.ChunkController;
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
	
	public static class PartConfig{
		public float rotationX = 0f;
		public float rotationY = 0f;
		public float rotationZ = 0f;
		
		public boolean disabled = false;
		
		public NBTTagCompound writeNBT(){
			NBTTagCompound compound = new NBTTagCompound();
			compound.setFloat("RotationX", rotationX);
			compound.setFloat("RotationY", rotationY);
			compound.setFloat("RotationZ", rotationZ);	
			
			compound.setBoolean("Disabled", disabled);
			return compound;
		}
		
		public void readNBT(NBTTagCompound compound){
			rotationX = ValueUtil.correctFloat(compound.getFloat("RotationX"), -0.5f, 0.5f);
			rotationY = ValueUtil.correctFloat(compound.getFloat("RotationY"), -0.5f, 0.5f);
			rotationZ = ValueUtil.correctFloat(compound.getFloat("RotationZ"), -0.5f, 0.5f);
			
			disabled = compound.getBoolean("Disabled");
		}
	}
}
