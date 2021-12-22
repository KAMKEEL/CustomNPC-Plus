package noppes.npcs.scripted.interfaces;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.ScriptItemStack;
import noppes.npcs.scripted.entity.ScriptLivingBase;

public interface IEntityLivingBase<T extends EntityLivingBase> extends IEntity<T> {
    /**
     * @return The entity's current health
     */
    public float getHealth();

    /**
     * @param health The new health of this entity
     */
    public void setHealth(float health);

    /**
     * @return Entity's max health
     */
    public double getMaxHealth();
    /**
     * @return Whether or not this entity is attacking something
     */
    public boolean isAttacking();

    /**
     * @param living Entity which this entity will attack
     */
    public void setAttackTarget(ScriptLivingBase living);

    /**
     * @return The entity which this entity is attacking
     */
    public ScriptLivingBase getAttackTarget();

    public int getType();

    public boolean typeOf(int type);

    /**
     * @param entity Entity to check
     * @return Whether or not this entity can see the given entity
     */
    public boolean canSeeEntity(ScriptEntity entity);

    /**
     * Expert use only
     * @return Returns the minecraft entity object
     */
    public T getMCEntity();

    /**
     * Makes the entity swing its hand
     */
    public void swingHand();

    /**
     * Works the same as the <a href="http://minecraft.gamepedia.com/Commands#effect">/effect command</a>
     * @param effect
     * @param duration The duration in seconds
     * @param strength The amplifier of the potion effect
     * @param hideParticles Whether or not you want to hide potion particles
     */
    public void addPotionEffect(int effect, int duration, int strength, boolean hideParticles);

    /**
     * Clears all potion effects
     */
    public void clearPotionEffects();

    /**
     * @since 1.7.10c
     * @param effect Potion effect to check
     * @return Returns -1 if its not active. Otherwise returns the strenght of the potion
     */
    public int getPotionEffect(int effect);

    /**
     * Note not all Living Entities support this
     * @since 1.7.10c
     * @return The item the entity is holding
     */
    public ScriptItemStack getHeldItem();

    /**
     * Note not all Living Entities support this
     * @since 1.7.10c
     * @param item The item to be set
     */
    public void setHeldItem(ScriptItemStack item);

    /**
     * Note not all Living Entities support this
     * @param slot Slot of what armor piece to get, 0:boots, 1:pants, 2:body, 3:head
     * @return The item in the given slot
     */
    public ScriptItemStack getArmor(int slot);

    /**
     * Note not all Living Entities support this
     * @since 1.7.10c
     * @param slot Slot of what armor piece to set, 0:boots, 1:pants, 2:body, 3:head
     * @param item Item to be set
     */
    public void setArmor(int slot, ScriptItemStack item);
}
