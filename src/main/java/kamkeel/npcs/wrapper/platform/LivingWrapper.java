package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IDamage;
import kamkeel.npcs.platform.entity.ILiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

/**
 * MC 1.7.10 implementation of {@link ILiving}.
 * Wraps a raw {@link EntityLivingBase} instance.
 */
public class LivingWrapper extends EntityWrapper implements ILiving {

    protected final EntityLivingBase living;

    public LivingWrapper(EntityLivingBase living) {
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
    public void damage(float amount, IDamage source) {
        DamageSource mcSource = source != null ? (DamageSource) source.getHandle() : DamageSource.generic;
        living.attackEntityFrom(mcSource, amount);
    }
}
