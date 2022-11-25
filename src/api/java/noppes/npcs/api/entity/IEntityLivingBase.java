package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IPos;
import noppes.npcs.api.item.IItemStack;

public interface IEntityLivingBase<T extends EntityLivingBase> extends IEntity<T> {
    /**
     * @return The entity's current health
     */
    float getHealth();

    /**
     * @param health The new health of this entity
     */
    void setHealth(float health);

    void hurt(float damage);

    void hurt(float damage, IEntity source);

    void hurt(float damage, IDamageSource damageSource);

    /**
     * @return Entity's max health
     */
    double getMaxHealth();
    /**
     * @return Whether or not this entity is attacking something
     */
    boolean isAttacking();

    /**
     * @param living Entity which this entity will attack
     */
    void setAttackTarget(IEntityLivingBase living);

    /**
     * @return The entity which this entity is attacking
     */
    IEntityLivingBase getAttackTarget();

    int getType();

    boolean typeOf(int type);

    /**
     * @param entity Entity to check
     * @return Whether or not this entity can see the given entity
     */
    boolean canSeeEntity(IEntity entity);

    /**
     *
     * @return Returns the look vector of this entity as an IPos object.
     */
    IPos getLookVector();

    /**
     *
     * @param maxDistance The max distance to perform checks before stopping
     * @return The first block found by following along the entity's look vector.
     */
    IBlock getLookingAtBlock(int maxDistance);

    /**
     * Returns an IPos object corresponding to the final obstructed point following along the
     * entity's look vector. If no obstructions are found (entity is looking in a straight path of
     * air with no blocks in the way), the final position is returned.
     *
     * @param maxDistance The max distance to perform checks before stopping
     * @return The position the entity is looking at.
     */
    IPos getLookingAtPos(int maxDistance);

    /**
     *
     * @param maxDistance The max distance to perform checks before stopping
     * @param range The radius to check for surrounding entities at every point on the vector
     * @return A list of entities, sorted first by closest to the entity along the look vector, then by which
     *         entities are closest to the vector.
     */
    IEntity[] getLookingAtEntities(int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision);

    IEntity[] getLookingAtEntities(int maxDistance, double offset, double range);

    /**
     * Expert use only
     * @return Returns the minecraft entity object
     */
    T getMCEntity();

    /**
     * Makes the entity swing its hand
     */
    void swingHand();

    /**
     * Works the same as the <a href="http://minecraft.gamepedia.com/Commands#effect">/effect command</a>
     * @param effect
     * @param duration The duration in seconds
     * @param strength The amplifier of the potion effect
     * @param hideParticles Whether or not you want to hide potion particles
     */
    void addPotionEffect(int effect, int duration, int strength, boolean hideParticles);

    /**
     * Clears all potion effects
     */
    void clearPotionEffects();

    /**
     * @since 1.7.10c
     * @param effect Potion effect to check
     * @return Returns -1 if its not active. Otherwise returns the strenght of the potion
     */
    int getPotionEffect(int effect);

    /**
     * Note not all Living Entities support this
     * @since 1.7.10c
     * @return The item the entity is holding
     */
    IItemStack getHeldItem();

    /**
     * Note not all Living Entities support this
     * @since 1.7.10c
     * @param item The item to be set
     */
    void setHeldItem(IItemStack item);

    /**
     * Note not all Living Entities support this
     * @param slot Slot of what armor piece to get, 0:boots, 1:pants, 2:body, 3:head
     * @return The item in the given slot
     */
    IItemStack getArmor(int slot);

    /**
     * Note not all Living Entities support this
     * @since 1.7.10c
     * @param slot Slot of what armor piece to set, 0:boots, 1:pants, 2:body, 3:head
     * @param item Item to be set
     */
    void setArmor(int slot, IItemStack item);

    void setAIMoveSpeed(float speed);

    float getAIMoveSpeed();

    void setAbsorptionAmount(float amount);

    float getAbsorptionAmount();
}
