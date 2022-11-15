package noppes.npcs.api.jobs;

import noppes.npcs.roles.JobInterface;
import noppes.npcs.api.entity.ICustomNpc;

public interface IJob {

    int getType();

    JobInterface getJobInterface();

    ICustomNpc getNpc();
}
