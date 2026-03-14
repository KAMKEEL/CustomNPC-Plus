package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IMob;
import kamkeel.npcs.platform.entity.IGameWorld;
import net.minecraft.entity.Entity;

/**
 * MC 1.7.10 implementation of {@link IMob}.
 * Wraps a raw {@link Entity} instance.
 */
public class EntityWrapper implements IMob {

    protected final Entity entity;

    public EntityWrapper(Entity entity) {
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
    public IGameWorld getWorld() {
        return new WorldWrapper(entity.worldObj);
    }

    @Override
    public Object getHandle() {
        return entity;
    }
}
