package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;

/**
 * Events related to NPC ability execution.
 * These events are fired during the ability lifecycle.
 */
public interface IAbilityEvent extends INpcEvent {

    /**
     * Get the entity executing the ability (NPC or Player).
     * @return the entity executing the ability
     */
    IEntityLivingBase getEntity();

    /**
     * Get the player executing the ability, or null if the caster is an NPC.
     * @return the player executing the ability, or null if NPC
     */
    IPlayer getPlayer();

    /**
     * Whether the entity executing the ability is an NPC.
     * @return true if the executor is an NPC
     */
    boolean isNPC();

    /**
     * Get the ability involved in this event.
     * @return the ability being executed
     */
    IAbility getAbility();

    /**
     * Get the target of the ability, or null if no target.
     * @return the current target entity
     */
    IEntityLivingBase getTarget();

    /**
     * Fired when an ability starts executing (enters WINDUP phase).
     * Canceling prevents the ability from starting.
     */
    @Cancelable
    interface StartEvent extends IAbilityEvent {
    }

    /**
     * Fired when an ability enters the ACTIVE phase and performs its effect.
     * Canceling prevents the ability effect from occurring.
     */
    @Cancelable
    interface ExecuteEvent extends IAbilityEvent {
    }

    /**
     * Fired when an ability is interrupted by damage.
     */
    interface InterruptEvent extends IAbilityEvent {
        /**
         * Get the damage source that caused the interruption.
         * @return the damage source
         */
        IDamageSource getDamageSource();

        /**
         * Get the amount of damage that caused the interruption.
         * @return the final damage dealt
         */
        float getDamage();
    }

    /**
     * Fired when an ability completes its full execution cycle.
     */
    interface CompleteEvent extends IAbilityEvent {
    }

    /**
     * Fired when a toggle ability is switched ON or OFF.
     * Canceling prevents the toggle from changing state.
     */
    @Cancelable
    interface ToggleEvent extends IAbilityEvent {
        boolean isTogglingOn();
        int getOldState();
        int getNewState();
    }

    /**
     * Fired every 10 ticks for each active toggle ability.
     * Scripts can call setEnabled(false) to force-deactivate.
     */
    interface ToggleUpdateEvent extends IAbilityEvent {
        int getTick();
        int getState();
        boolean isEnabled();
        void setEnabled(boolean enabled);
    }

    /**
     * Fired when an ability hits an entity with damage.
     * Canceling this event prevents the damage from being applied.
     */
    @Cancelable
    interface HitEvent extends IAbilityEvent {
        /**
         * Get the entity that was hit by the ability.
         * @return the entity that was hit
         */
        IEntityLivingBase getHitEntity();

        /**
         * Get the damage amount.
         * @return the damage amount
         */
        float getDamage();

        /**
         * Set the damage amount. Allows scripts to modify damage.
         * @param damage the new damage amount
         */
        void setDamage(float damage);

        /**
         * Get the horizontal knockback amount.
         * @return the knockback strength
         */
        float getKnockback();

        /**
         * Set the horizontal knockback amount.
         * @param knockback the new knockback strength
         */
        void setKnockback(float knockback);

        /**
         * Get the vertical knockback (upward force) amount.
         * @return the upward knockback component
         */
        float getKnockbackUp();

        /**
         * Set the vertical knockback (upward force) amount.
         * @param knockbackUp the new upward knockback component
         */
        void setKnockbackUp(float knockbackUp);
    }

    /**
     * Fired every tick while an ability is executing.
     * Provides the current phase and tick count.
     */
    interface TickEvent extends IAbilityEvent {
        /**
         * Get the current phase of the ability.
         * 0 = IDLE, 1 = WINDUP, 2 = ACTIVE, 3 = DAZED, 4 = BURST_DELAY
         * @return the current ability phase (0=IDLE, 1=WINDUP, 2=ACTIVE, 3=DAZED, 4=BURST_DELAY)
         */
        int getAbilityPhase();

        /**
         * Get the current tick count within the phase.
         * @return the current tick within the phase
         */
        int getTick();
    }
}
