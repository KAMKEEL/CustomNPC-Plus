package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IPlatformEntity;
import kamkeel.npcs.platform.entity.IPlatformWorld;
import net.minecraft.entity.Entity;

/**
 * MC 1.7.10 implementation of {@link IPlatformEntity}.
 * Wraps a raw {@link Entity} instance.
 */
public class MC1710PlatformEntity implements IPlatformEntity {

    protected final Entity entity;

    public MC1710PlatformEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public int getEntityId() {
        return entity.getEntityId();
    }

    @Override
    public String getUniqueID() {
        return entity.getUniqueID().toString();
    }

    @Override
    public double getX() {
        return entity.posX;
    }

    @Override
    public double getY() {
        return entity.posY;
    }

    @Override
    public double getZ() {
        return entity.posZ;
    }

    @Override
    public float getYaw() {
        return entity.rotationYaw;
    }

    @Override
    public float getPitch() {
        return entity.rotationPitch;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        entity.setPosition(x, y, z);
    }

    @Override
    public boolean isAlive() {
        return !entity.isDead;
    }

    @Override
    public IPlatformWorld getWorld() {
        return new MC1710PlatformWorld(entity.worldObj);
    }

    @Override
    public Object getHandle() {
        return entity;
    }
}
