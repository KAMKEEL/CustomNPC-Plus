package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IPlatformEntity;
import kamkeel.npcs.platform.entity.IPlatformWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * MC 1.7.10 implementation of {@link IPlatformWorld}.
 * Wraps a raw {@link World} instance.
 */
public class MC1710PlatformWorld implements IPlatformWorld {

    private final World world;

    public MC1710PlatformWorld(World world) {
        this.world = world;
    }

    @Override
    public int getDimensionId() {
        return world.provider.dimensionId;
    }

    @Override
    public boolean isClient() {
        return world.isRemote;
    }

    @Override
    public IPlatformEntity getEntityById(int id) {
        Entity entity = world.getEntityByID(id);
        if (entity == null) return null;
        return new MC1710PlatformEntity(entity);
    }

    @Override
    public Object getHandle() {
        return world;
    }
}
