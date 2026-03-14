package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IDamage;
import kamkeel.npcs.platform.entity.IMob;
import net.minecraft.util.DamageSource;

/**
 * MC 1.7.10 implementation of {@link IDamage}.
 * Wraps a raw {@link DamageSource} instance.
 */
public class DamageWrapper implements IDamage {

    private final DamageSource source;

    public DamageWrapper(DamageSource source) {
        this.source = source;
    }

    @Override
    public String getType() {
        return source.getDamageType();
    }

    @Override
    public IMob getSourceEntity() {
        if (source.getEntity() == null) return null;
        return new EntityWrapper(source.getEntity());
    }

    @Override
    public Object getHandle() {
        return source;
    }
}
