package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.jobs.IJobBard;

public class ScriptJobBard extends ScriptJobInterface implements IJobBard {
	private JobBard job;
	public ScriptJobBard(EntityNPCInterface npc){
		super(npc);
		this.job = (JobBard) npc.jobInterface;
	}
	@Override
	public int getType(){
		return JobType.BARD;
	}
	
	/**
	 * @return The song the bard is playing
	 */
	public String getSong(){
		return job.song;
	}
	
	/**
	 * @param song The song you want the bard to play
	 */
	public void setSong(String song){
		job.song = song;
		npc.script.clientNeedsUpdate = true;
	}

	/**
	 *
	 * @param i The id of the instrument. IDs:
	 *          0 - None,
	 *          1 - Banjo,
	 *          2 - Violin,
	 *          3 - Guitar,
	 *          4 - Harp,
	 *          5 - FrenchHorn
	 */
	public void setInstrument(int i) {
		this.job.setInstrument(i);
	}

	public int getInstrumentId() {
		return this.job.getInstrument().ordinal();
	}

	public void setMinRange(int range) {
		this.job.minRange = range;
	}
	public int getMinRange() {
		return this.job.minRange;
	}

	public void setMaxRange(int range) {
		this.job.maxRange = range;
	}
	public int getMaxRange() {
		return this.job.maxRange;
	}

	public void setStreaming(boolean streaming) {
		this.job.isStreamer = streaming;
	}
	public boolean getStreaming() {
		return this.job.isStreamer;
	}

	public void hasOffRange(boolean value) {
		this.job.hasOffRange = value;
	}
	public boolean hasOffRange() {
		return this.job.hasOffRange;
	}
}
