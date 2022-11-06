package noppes.npcs.scripted.interfaces.jobs;

import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public interface IJobHealer extends IJob {

    void heal(IEntityLivingBase entity, float amount);

    void setRange(int range);
    int getRange();

    void setSpeed(int speed);
    int getSpeed();
}
