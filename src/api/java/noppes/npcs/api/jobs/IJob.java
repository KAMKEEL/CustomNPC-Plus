package noppes.npcs.api.jobs;

import noppes.npcs.api.entity.ICustomNpc;

public interface IJob {

    int getType();

    ICustomNpc getNpc();
}
