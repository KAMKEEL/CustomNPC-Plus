package noppes.npcs.scripted.roles;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.scripted.constants.JobType;

public class ScriptJobItemGiver extends ScriptJobInterface {
    private JobItemGiver job;

    public ScriptJobItemGiver(EntityNPCInterface npc) {
        super(npc);
        this.job = (JobItemGiver) npc.jobInterface;
    }

    @Override
    public int getType() {
        return JobType.ITEMGIVER;
    }

}
