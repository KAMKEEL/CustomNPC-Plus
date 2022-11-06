package noppes.npcs.scripted.interfaces.jobs;

import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public interface IJobHealer extends IJob {

    void heal(IEntityLivingBase entity, float amount);
}
