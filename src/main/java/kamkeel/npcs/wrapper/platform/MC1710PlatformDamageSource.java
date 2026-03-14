package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IPlatformDamageSource;
import kamkeel.npcs.platform.entity.IPlatformEntity;
import net.minecraft.util.DamageSource;

/**
 * MC 1.7.10 implementation of {@link IPlatformDamageSource}.
 * Wraps a raw {@link DamageSource} instance.
 */
public class MC1710PlatformDamageSource implements IPlatformDamageSource {

    private final DamageSource source;

    public MC1710PlatformDamageSource(DamageSource source) {
        this.source = source;
    }

    @Override
    public String getType() {
        return source.getDamageType();
    }

    @Override
    public IPlatformEntity getSourceEntity() {
        if (source.getEntity() == null) return null;
        return new MC1710PlatformEntity(source.getEntity());
    }

    @Override
    public Object getHandle() {
        return source;
    }
}
