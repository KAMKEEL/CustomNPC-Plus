package noppes.npcs.scripted.interfaces.handler.data;

import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public interface INaturalSpawn {

    void setName(String name);

    String getName();

    void setEntity(IEntity entity);

    IEntity getEntity(IWorld world);

    void setWeight(int weight);

    int getWeight();

    void spawnsInLiquid(boolean spawns);

    boolean spawnsInLiquid();

    String[] getBiomes();

    void setBiomes(String[] biomes);
}
