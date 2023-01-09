package noppes.npcs.scripted.roles;

import noppes.npcs.api.handler.data.IModelPart;
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

	public IModelPart getPart(int part) {
		switch (part) {
			case 0:
				return job.head;
			case 1:
				return job.body;
			case 2:
				return job.larm;
			case 3:
				return job.rarm;
			case 4:
				return job.lleg;
			case 5:
				return job.rleg;
		}
		return null;
	}

	public void allEnabled(boolean enabled) {
		for (int i = 0; i < 6; i++) {
			this.getPart(i).setEnabled(enabled);
		}
	}

	public void allAnimated(boolean animated) {
		for (int i = 0; i < 6; i++) {
			this.getPart(i).setAnimated(animated);
		}
	}

	public void allInterpolated(boolean interpolate) {
		for (int i = 0; i < 6; i++) {
			this.getPart(i).setInterpolated(interpolate);
		}
	}

	public void allFullAngles(boolean fullAngles) {
		for (int i = 0; i < 6; i++) {
			this.getPart(i).setFullAngles(fullAngles);
		}
	}

	public void allAnimRate(float animRate) {
		if (animRate < 0)
			animRate = 0;
		for (int i = 0; i < 6; i++) {
			this.getPart(i).setAnimRate(animRate);
		}
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

	@Deprecated
	public void setRotationX(int part, float rotation) {
		this.getPart(part).setRotationX(rotation);
	}

	@Deprecated
	public void setRotationY(int part, float rotation) {
		this.getPart(part).setRotationY(rotation);
	}

	@Deprecated
	public void setRotationZ(int part, float rotation) {
		this.getPart(part).setRotationZ(rotation);
	}

	@Deprecated
	public float getRotationX(int part) {
		return this.getPart(part).getRotationX();
	}

	@Deprecated
	public float getRotationY(int part) {
		return this.getPart(part).getRotationY();
	}

	@Deprecated
	public float getRotationZ(int part) {
		return this.getPart(part).getRotationZ();
	}

	@Deprecated
	public boolean isEnabled(int part) {
		return this.getPart(part).isEnabled();
	}

	@Deprecated
	public void setEnabled(int part, boolean bo) {
		this.getPart(part).setEnabled(bo);
	}

	@Override
	public int getType(){
		return JobType.PUPPET;
	}
	
}
