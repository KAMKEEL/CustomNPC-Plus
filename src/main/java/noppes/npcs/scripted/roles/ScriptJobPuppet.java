package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobPuppet;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.jobs.IJobPuppet;

public class ScriptJobPuppet extends ScriptJobInterface implements IJobPuppet {
	private JobPuppet job;
	public ScriptJobPuppet(EntityNPCInterface npc){
		super(npc);
		this.job = (JobPuppet) npc.jobInterface;
	}

	public void setAnimated(boolean animated) {
		this.job.animate = animated;
	}

	public boolean isAnimated() {
		return this.job.animate;
	}

	public void setInterpolated(boolean interpolate) {
		this.job.interpolate = interpolate;
	}

	public boolean isInterpolated() {
		return this.job.interpolate;
	}

	public void setFullAngles(boolean limit) {
		job.fullAngles = limit;
	}

	public boolean fullAngles() {
		return job.fullAngles;
	}

	public void setAnimRate(float animRate) {
		if (animRate < 0)
			animRate = 0;
		this.job.animRate = animRate;
	}

	public float getAnimRate() {
		return this.job.animRate;
	}

	public void doWhileStanding(boolean whileStanding) {
		this.job.whileStanding = whileStanding;
	}

	public boolean doWhileStanding() {
		return this.job.whileStanding;
	}

	public void doWhileAttacking(boolean whileAttacking) {
		this.job.whileAttacking = whileAttacking;
	}

	public boolean doWhileAttacking() {
		return this.job.whileAttacking;
	}

	public void doWhileMoving(boolean whileMoving) {
		this.job.whileMoving = whileMoving;
	}

	public boolean doWhileMoving() {
		return this.job.whileMoving;
	}

	public void setRotation(int part, float rotationX, float rotationY, float rotationZ) {
		this.setRotationX(part,rotationX);
		this.setRotationY(part,rotationY);
		this.setRotationZ(part,rotationZ);
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @param rotation Rotation the of the body part
	 */
	public void setRotationX(int part, float rotation){
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
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @param rotation Rotation the of the body part
	 */
	public void setRotationY(int part, float rotation){
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
	public void setRotationZ(int part, float rotation){
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
	 * @return Returns X rotation in degrees (0-360)
	 */
	public float getRotationX(int part){
		if(part == 0)
			return job.head.rotationX;
		if(part == 1)
			return job.body.rotationX;
		if(part == 2)
			return job.larm.rotationX;
		if(part == 3)
			return job.rarm.rotationX;
		if(part == 4)
			return job.lleg.rotationX;
		if(part == 5)
			return job.rleg.rotationX;
		return 0;
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @return Returns Y rotation in degrees (0-360)
	 */
	public float getRotationY(int part){
		if(part == 0)
			return job.head.rotationY;
		if(part == 1)
			return job.body.rotationY;
		if(part == 2)
			return job.larm.rotationY;
		if(part == 3)
			return job.rarm.rotationY;
		if(part == 4)
			return job.lleg.rotationY;
		if(part == 5)
			return job.rleg.rotationY;
		return 0;
	}

	/**
	 * @since 1.7.10c
	 * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
	 * @return Returns Z rotation in degrees (0-360)
	 */
	public float getRotationZ(int part){
		if(part == 0)
			return job.head.rotationZ;
		if(part == 1)
			return job.body.rotationZ;
		if(part == 2)
			return job.larm.rotationZ;
		if(part == 3)
			return job.rarm.rotationZ;
		if(part == 4)
			return job.lleg.rotationZ;
		if(part == 5)
			return job.rleg.rotationZ;
		return 0;
	}

	public void setOffset(int part, float offsetX, float offsetY, float offsetZ) {
		this.setOffsetX(part,offsetX);
		this.setOffsetY(part,offsetY);
		this.setOffsetZ(part,offsetZ);
	}

	public void setOffsetX(int part, float offset) {
		if(getOffsetX(part) != offset)
			npc.script.clientNeedsUpdate = true;

		if(part == 0)
			job.head.pivotX = offset;
		if(part == 1)
			job.body.pivotX = offset;
		if(part == 2)
			job.larm.pivotX = offset;
		if(part == 3)
			job.rarm.pivotX = offset;
		if(part == 4)
			job.lleg.pivotX = offset;
		if(part == 5)
			job.rleg.pivotX = offset;
	}

	public void setOffsetY(int part, float offset) {
		if(getOffsetY(part) != offset)
			npc.script.clientNeedsUpdate = true;

		if(part == 0)
			job.head.pivotY = offset;
		if(part == 1)
			job.body.pivotY = offset;
		if(part == 2)
			job.larm.pivotY = offset;
		if(part == 3)
			job.rarm.pivotY = offset;
		if(part == 4)
			job.lleg.pivotY = offset;
		if(part == 5)
			job.rleg.pivotY = offset;
	}

	public void setOffsetZ(int part, float offset) {
		if(getOffsetZ(part) != offset)
			npc.script.clientNeedsUpdate = true;

		if(part == 0)
			job.head.pivotZ = offset;
		if(part == 1)
			job.body.pivotZ = offset;
		if(part == 2)
			job.larm.pivotZ = offset;
		if(part == 3)
			job.rarm.pivotZ = offset;
		if(part == 4)
			job.lleg.pivotZ = offset;
		if(part == 5)
			job.rleg.pivotZ = offset;
	}

	public float getOffsetX(int part) {
		if(part == 0)
			return job.head.pivotX;
		if(part == 1)
			return job.body.pivotX;
		if(part == 2)
			return job.larm.pivotX;
		if(part == 3)
			return job.rarm.pivotX;
		if(part == 4)
			return job.lleg.pivotX;
		if(part == 5)
			return job.rleg.pivotX;
		return 0;
	}

	public float getOffsetY(int part) {
		if(part == 0)
			return job.head.pivotY;
		if(part == 1)
			return job.body.pivotY;
		if(part == 2)
			return job.larm.pivotY;
		if(part == 3)
			return job.rarm.pivotY;
		if(part == 4)
			return job.lleg.pivotY;
		if(part == 5)
			return job.rleg.pivotY;
		return 0;
	}

	public float getOffsetZ(int part) {
		if(part == 0)
			return job.head.pivotZ;
		if(part == 1)
			return job.body.pivotZ;
		if(part == 2)
			return job.larm.pivotZ;
		if(part == 3)
			return job.rarm.pivotZ;
		if(part == 4)
			return job.lleg.pivotZ;
		if(part == 5)
			return job.rleg.pivotZ;
		return 0;
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
	@Override
	public int getType(){
		return JobType.PUPPET;
	}
	
}
