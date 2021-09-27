package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobInterface {
    protected EntityNPCInterface npc;

    public ScriptJobInterface(EntityNPCInterface npc) {
        this.npc = npc;
    }

    /**
     * @return Returns the JobType
     * @see noppes.npcs.scripted.constants.JobType
     */
    public int getType() {
        return JobType.UNKNOWN;
    }
}
