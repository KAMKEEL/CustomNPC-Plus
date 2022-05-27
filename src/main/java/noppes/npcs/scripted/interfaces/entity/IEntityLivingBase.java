package noppes.npcs.scripted.interfaces.entity;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.scripted.interfaces.item.IItemStack;
import noppes.npcs.scripted.ScriptDamageSource;

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

    void hurt(float damage, ScriptDamageSource damageSource);

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
}
