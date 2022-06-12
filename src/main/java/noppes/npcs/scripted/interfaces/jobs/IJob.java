package noppes.npcs.scripted.interfaces.jobs;

import noppes.npcs.roles.JobInterface;
import noppes.npcs.scripted.interfaces.entity.ICustomNpc;

public interface IJob {

    int getType();

    JobInterface getJobInterface();

    ICustomNpc getNpc();
}
