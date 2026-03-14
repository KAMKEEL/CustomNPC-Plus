package noppes.npcs.api.entity;

import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IPos;
import noppes.npcs.api.item.IItemStack;

/**
 * Platform-api version (MC-free). The src/api shadow adds generic type parameter
 * and IBlock-dependent methods.
 */
public interface IEntityLivingBase extends IEntity {

    float getHealth();

    void setHealth(float health);

    void hurt(float damage);

    void hurt(float damage, IEntity source);

    void hurt(float damage, IDamageSource damageSource);

    void setMaxHurtTime(int time);

    int getMaxHurtTime();

    double getMaxHealth();

    double getFollowRange();

    double getKnockbackResistance();

    double getSpeed();

    double getMeleeStrength();

    void setMaxHealth(double health);

    void setFollowRange(double range);

    void setKnockbackResistance(double knockbackResistance);

    void setSpeed(double speed);

    void setMeleeStrength(double attackDamage);

    boolean isAttacking();

    void setAttackTarget(IEntityLivingBase living);

    IEntityLivingBase getAttackTarget();

    int getAttackTargetTime();

    void setLastAttacker(IEntity entity);

    IEntity getLastAttacker();

    int getLastAttackerTime();

    boolean canBreatheUnderwater();

    int getType();

    boolean typeOf(int type);

    IPos getLookVector();

    // Note: getLookingAtBlock methods omitted — require IBlock (not in platform-api)

    IPos getLookingAtPos(int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IPos getLookingAtPos(int maxDistance);

    IEntity[] getLookingAtEntities(IEntity[] ignoreEntities, int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IEntity[] getLookingAtEntities(int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IEntity[] getLookingAtEntities(int maxDistance, double offset, double range);

    Object getMCEntity();

    void swingHand();

    void addPotionEffect(int effect, int duration, int strength, boolean hideParticles);

    void clearPotionEffects();

    int getPotionEffect(int effect);

    IItemStack getHeldItem();

    void setHeldItem(IItemStack item);

    IItemStack getArmor(int slot);

    void setArmor(int slot, IItemStack item);

    boolean isChild();

    void renderBrokenItemStack(IItemStack itemStack);

    boolean isOnLadder();

    int getTotalArmorValue();

    int getArrowCountInEntity();

    void setArrowCountInEntity(int count);

    void dismountEntity(IEntity entity);

    void setAIMoveSpeed(float speed);

    float getAIMoveSpeed();

    void setAbsorptionAmount(float amount);

    float getAbsorptionAmount();

    void setHurtTime(int time);

    void applyKnockback(float strength, IEntity source);

    void applyKnockback(float strength, double dirX, double dirZ);

    void forceKnockback(float strength, IEntity source);

    void forceKnockback(float strength, double dirX, double dirZ);
}
