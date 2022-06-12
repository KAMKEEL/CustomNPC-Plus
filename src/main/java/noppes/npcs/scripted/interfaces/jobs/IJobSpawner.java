package noppes.npcs.scripted.interfaces.jobs;

import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public interface IJobSpawner extends IJob {

    IEntityLivingBase spawnEntity(int number);

    void removeAllSpawned();
}
