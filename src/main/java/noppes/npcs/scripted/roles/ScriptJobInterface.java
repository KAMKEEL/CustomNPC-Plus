package noppes.npcs.scripted.roles;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.jobs.IJob;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobInterface implements IJob {
    public final EntityNPCInterface npc;
    public final JobInterface jobInterface;

    public ScriptJobInterface(JobInterface jobInterface) {
        this.npc = null;
        this.jobInterface = jobInterface;
    }

    public ScriptJobInterface(EntityNPCInterface npc) {
        this.npc = npc;
        this.jobInterface = npc.jobInterface;
    }

    /**
     * @return Returns the JobType
     * @see noppes.npcs.scripted.constants.JobType
     */
    public int getType() {
        return JobType.UNKNOWN;
    }

    public JobInterface getJobInterface() {
        return jobInterface;
    }

    public ICustomNpc getNpc() {
        return (ICustomNpc) NpcAPI.Instance().getIEntity(npc);
    }

}
