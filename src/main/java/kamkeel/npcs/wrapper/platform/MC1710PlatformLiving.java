package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IPlatformDamageSource;
import kamkeel.npcs.platform.entity.IPlatformLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

/**
 * MC 1.7.10 implementation of {@link IPlatformLiving}.
 * Wraps a raw {@link EntityLivingBase} instance.
 */
public class MC1710PlatformLiving extends MC1710PlatformEntity implements IPlatformLiving {

    protected final EntityLivingBase living;

    public MC1710PlatformLiving(EntityLivingBase living) {
        super(living);
        this.living = living;
    }

    @Override
    public float getHealth() {
        return living.getHealth();
    }

    @Override
    public void setHealth(float hp) {
        living.setHealth(hp);
    }

    @Override
    public float getMaxHealth() {
        return living.getMaxHealth();
    }

    @Override
    public void damage(float amount, IPlatformDamageSource source) {
        DamageSource mcSource = source != null ? (DamageSource) source.getHandle() : DamageSource.generic;
        living.attackEntityFrom(mcSource, amount);
    }
}
