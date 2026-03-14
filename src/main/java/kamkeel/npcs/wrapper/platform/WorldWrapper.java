package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IMob;
import kamkeel.npcs.platform.entity.IGameWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * MC 1.7.10 implementation of {@link IGameWorld}.
 * Wraps a raw {@link World} instance.
 */
public class WorldWrapper implements IGameWorld {

    private final World world;

    public WorldWrapper(World world) {
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
    public IMob getEntityById(int id) {
        Entity entity = world.getEntityByID(id);
        if (entity == null) return null;
        return new EntityWrapper(entity);
    }

    @Override
    public Object getHandle() {
        return world;
    }
}
