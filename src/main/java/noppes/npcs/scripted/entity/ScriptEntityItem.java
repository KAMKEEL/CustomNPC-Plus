package noppes.npcs.scripted.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEntityItem<T extends EntityItem> extends ScriptEntity<T> implements IEntityItem {
    private final T entity;

    public ScriptEntityItem(T entityItem) {
        super(entityItem);
        this.entity = entityItem;
    }

    public String getOwner() {
        return this.entity.func_145798_i();
    }

    public void setOwner(String name) {
        this.entity.func_145797_a(name);
    }

    public String getThrower() {
        return this.entity.func_145800_j();
    }

    public void setThrower(String name) {
        this.entity.func_145799_b(name);
    }

    public int getPickupDelay() {
        return this.entity.delayBeforeCanPickup;
    }

    public void setPickupDelay(int delay) {
        this.entity.delayBeforeCanPickup = delay;
    }

    public int getType() {
        return EntityType.ITEM;
    }

    public long getAge() {
        return this.entity.age;
    }

    public void setAge(long age) {
        age = Math.max(Math.min(age, Integer.MAX_VALUE), Integer.MIN_VALUE);
        this.entity.age = (int) age;
    }

    public int getLifeSpawn() {
        return this.entity.lifespan;
    }

    public void setLifeSpawn(int age) {
        this.entity.lifespan = age;
    }

    public IItemStack getItem() {
        return NpcAPI.Instance().getIItemStack(this.entity.getEntityItem());
    }

    public void setItem(IItemStack item) {
        ItemStack stack = item == null ? null : item.getMCItemStack();
        this.entity.setEntityItemStack(stack);
    }
}
