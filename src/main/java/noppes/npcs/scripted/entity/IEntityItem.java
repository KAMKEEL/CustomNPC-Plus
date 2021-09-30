//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity;

import net.minecraft.entity.item.EntityItem;
import noppes.npcs.scripted.entity.IEntity;
import noppes.npcs.scripted.item.IItemStack;

public interface IEntityItem<T extends EntityItem> extends IEntity<T> {
    String getOwner();

    void setOwner(String var1);

    int getPickupDelay();

    void setPickupDelay(int var1);

    long getAge();

    void setAge(long var1);

    int getLifeSpawn();

    void setLifeSpawn(int var1);

    IItemStack getItem();

    void setItem(IItemStack var1);
}
