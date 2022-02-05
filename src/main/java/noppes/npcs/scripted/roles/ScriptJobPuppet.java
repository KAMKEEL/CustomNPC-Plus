package noppes.npcs.scripted.roles;

import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.JobPuppet;
import noppes.npcs.scripted.ScriptLivingBase;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobPuppet extends ScriptJobInterface{
	private JobPuppet job;
	public ScriptJobPuppet(EntityNPCInterface npc){
		super(npc);
		this.job = (JobPuppet) npc.jobInterface;
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @return Returns X rotation in degrees (0-360)
	 */
	public int getRotationX(int part){
		if(part == 0)
			return floatToInt(job.head.rotationX);
		if(part == 1)
			return floatToInt(job.body.rotationX);
		if(part == 2)
			return floatToInt(job.larm.rotationX);
		if(part == 3)
			return floatToInt(job.rarm.rotationX);
		if(part == 4)
			return floatToInt(job.lleg.rotationX);
		if(part == 5)
			return floatToInt(job.rleg.rotationX);
		return 0;
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @return Returns Y rotation in degrees (0-360)
	 */
	public int getRotationY(int part){
		if(part == 0)
			return floatToInt(job.head.rotationY);
		if(part == 1)
			return floatToInt(job.body.rotationY);
		if(part == 2)
			return floatToInt(job.larm.rotationY);
		if(part == 3)
			return floatToInt(job.rarm.rotationY);
		if(part == 4)
			return floatToInt(job.lleg.rotationY);
		if(part == 5)
			return floatToInt(job.rleg.rotationY);
		return 0;
	}
	
	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @return Returns Z rotation in degrees (0-360)
	 */
	public int getRotationZ(int part){
		if(part == 0)
			return floatToInt(job.head.rotationZ);
		if(part == 1)
			return floatToInt(job.body.rotationZ);
		if(part == 2)
			return floatToInt(job.larm.rotationZ);
		if(part == 3)
			return floatToInt(job.rarm.rotationZ);
		if(part == 4)
			return floatToInt(job.lleg.rotationZ);
		if(part == 5)
			return floatToInt(job.rleg.rotationZ);
		return 0;
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @param rotation Rotation the of the body part
	 */
	public void setRotationX(int part, int rotation){
		float f = rotation / 360f - 0.5f;
		if(getRotationX(part) != f)
			npc.script.clientNeedsUpdate = true;
		
		if(part == 0)
			job.head.rotationX = f;
		if(part == 1)
			job.body.rotationX = f;
		if(part == 2)
			job.larm.rotationX = f;
		if(part == 3)
			job.rarm.rotationX = f;
		if(part == 4)
			job.lleg.rotationX = f;
		if(part == 5)
			job.rleg.rotationX = f;
	}

	/**
	 * @since 1.7.10c
	 * @param type Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @param rotation Rotation the of the body part
	 */
	public void setRotationY(int part, int rotation){
		float f = rotation / 360f - 0.5f;
		if(getRotationY(part) != f)
			npc.script.clientNeedsUpdate = true;
		
		if(part == 0)
			job.head.rotationY = f;
		if(part == 1)
			job.body.rotationY = f;
		if(part == 2)
			job.larm.rotationY = f;
		if(part == 3)
			job.rarm.rotationY = f;
		if(part == 4)
			job.lleg.rotationY = f;
		if(part == 5)
			job.rleg.rotationY = f;
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @param rotation Rotation the of the body part
	 */
	public void setRotationZ(int part, int rotation){
		float f = rotation / 360f - 0.5f;
		if(getRotationZ(part) != f)
			npc.script.clientNeedsUpdate = true;
		
		if(part == 0)
			job.head.rotationZ = f;
		if(part == 1)
			job.body.rotationZ = f;
		if(part == 2)
			job.larm.rotationZ = f;
		if(part == 3)
			job.rarm.rotationZ = f;
		if(part == 4)
			job.lleg.rotationZ = f;
		if(part == 5)
			job.rleg.rotationZ = f;
	}
	
	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @return Returns whether or not the body part is enabled
	 */
	public boolean isEnabled(int part){
		if(part == 0)
			return !job.head.disabled;
		if(part == 1)
			return !job.body.disabled;
		if(part == 2)
			return !job.larm.disabled;
		if(part == 3)
			return !job.rarm.disabled;
		if(part == 4)
			return !job.lleg.disabled;
		if(part == 5)
			return !job.rleg.disabled;
		
		return false;
	}
	
	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @param bo Whether or not the body part is enabled
	 */
	public void setEnabled(int part, boolean bo){
		if(isEnabled(part) != bo)
			npc.script.clientNeedsUpdate = true;
		
		if(part == 0)
			job.head.disabled = !bo;
		if(part == 1)
			job.body.disabled = !bo;
		if(part == 2)
			job.larm.disabled = !bo;
		if(part == 3)
			job.rarm.disabled = !bo;
		if(part == 4)
			job.lleg.disabled = !bo;
		if(part == 5)
			job.rleg.disabled = !bo;
	}
	
	private int floatToInt(float f){
		return (int)((f + 0.5) * 360);
	}
	
	@Override
	public int getType(){
		return JobType.PUPPET;
	}
	
}
