package noppes.npcs.api.jobs;

import noppes.npcs.api.entity.IEntityLivingBase;

public interface IJobHealer extends IJob {

    void heal(IEntityLivingBase entity, float amount);

    void setRange(int range);
    int getRange();

    void setSpeed(int speed);
    int getSpeed();
}
