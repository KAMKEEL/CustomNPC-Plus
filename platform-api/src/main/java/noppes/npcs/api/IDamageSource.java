package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

/**
 * Platform-api version (MC-free). The src/api shadow adds typed getMCDamageSource().
 */
public interface IDamageSource {

    String getType();

    boolean isUnblockable();

    boolean isProjectile();

    IEntity getTrueSource();

    IEntity getImmediateSource();

    /**
     * Returns the underlying MC DamageSource object.
     * Expert use only. Returns Object in platform-api; shadow narrows to DamageSource.
     */
    Object getMCDamageSource();
}
