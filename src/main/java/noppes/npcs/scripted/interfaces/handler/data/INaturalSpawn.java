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

    void setMinHeight(int height);

    int getMinHeight();

    void setMaxHeight(int height);

    int getMaxHeight();

    void spawnsLikeAnimal(boolean spawns);

    boolean spawnsLikeAnimal();

    void spawnsLikeMonster(boolean spawns);

    boolean spawnsLikeMonster();

    void spawnsInLiquid(boolean spawns);

    boolean spawnsInLiquid();

    void spawnsInAir(boolean spawns);

    boolean spawnsInAir();

    String[] getBiomes();

    void setBiomes(String[] biomes);
}
