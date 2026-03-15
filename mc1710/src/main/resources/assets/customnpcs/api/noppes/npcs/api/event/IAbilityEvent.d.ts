/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events related to NPC ability execution.
 * These events are fired during the ability lifecycle.
  * @javaFqn noppes.npcs.api.event.IAbilityEvent
*/
export interface IAbilityEvent extends import('./INpcEvent').INpcEvent {
    /**
     * Get the entity executing the ability (NPC or Player).
     * @return the entity executing the ability
     */
    getEntity(): import('../entity/IEntityLivingBase').IEntityLivingBase;
    /**
     * Get the player executing the ability, or null if the caster is an NPC.
     * @return the player executing the ability, or null if NPC
     */
    getPlayer(): import('../entity/IPlayer').IPlayer;
    /**
     * Whether the entity executing the ability is an NPC.
     * @return true if the executor is an NPC
     */
    isNPC(): import('./boolean').boolean;
    /**
     * Get the ability involved in this event.
     * @return the ability being executed
     */
    getAbility(): import('../ability/IAbility').IAbility;
    /**
     * Get the target of the ability, or null if no target.
     * @return the current target entity
     */
    getTarget(): import('../entity/IEntityLivingBase').IEntityLivingBase;
    readonly player: import('../entity/IPlayer').IPlayer;
    readonly npc: import('../entity/ICustomNpc').ICustomNpc;
}

export namespace IAbilityEvent {
    
    
    
    
    
    /**
     * Fired when an ability starts executing (enters WINDUP phase).
     * Canceling prevents the ability from starting.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.StartEvent
*/
    export interface StartEvent extends IAbilityEvent {
    }
    /**
     * Fired when an ability enters the ACTIVE phase and performs its effect.
     * Canceling prevents the ability effect from occurring.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.ExecuteEvent
*/
    export interface ExecuteEvent extends IAbilityEvent {
    }
    /**
     * Fired when an ability is interrupted by damage.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.InterruptEvent
*/
    export interface InterruptEvent extends IAbilityEvent {
        /**
         * Get the damage source that caused the interruption.
         * @return the damage source
         */
        getDamageSource(): import('../IDamageSource').IDamageSource;
        /**
         * Get the amount of damage that caused the interruption.
         * @return the final damage dealt
         */
        getDamage(): import('./float').float;
    }
    /**
     * Fired when an ability completes its full execution cycle.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.CompleteEvent
*/
    export interface CompleteEvent extends IAbilityEvent {
    }
    /**
     * Fired when a toggle ability is switched ON or OFF.
     * Canceling prevents the toggle from changing state.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.ToggleEvent
*/
    export interface ToggleEvent extends IAbilityEvent {
        isTogglingOn(): import('./boolean').boolean;
        getOldState(): import('./int').int;
        getNewState(): import('./int').int;
    }
    /**
     * Fired every 10 ticks for each active toggle ability.
     * Scripts can call setEnabled(false) to force-deactivate.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.ToggleUpdateEvent
*/
    export interface ToggleUpdateEvent extends IAbilityEvent {
        getTick(): import('./int').int;
        getState(): import('./int').int;
        isEnabled(): import('./boolean').boolean;
        setEnabled(enabled: import('./boolean').boolean): import('./void').void;
    }
    /**
     * Fired when an ability hits an entity with damage.
     * Canceling this event prevents the damage from being applied.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.HitEvent
*/
    export interface HitEvent extends IAbilityEvent {
        /**
         * Get the entity that was hit by the ability.
         * @return the entity that was hit
         */
        getHitEntity(): import('../entity/IEntityLivingBase').IEntityLivingBase;
        /**
         * Get the damage amount.
         * @return the damage amount
         */
        getDamage(): import('./float').float;
        /**
         * Set the damage amount. Allows scripts to modify damage.
         * @param damage the new damage amount
         */
        setDamage(damage: import('./float').float): import('./void').void;
        /**
         * Get the horizontal knockback amount.
         * @return the knockback strength
         */
        getKnockback(): import('./float').float;
        /**
         * Set the horizontal knockback amount.
         * @param knockback the new knockback strength
         */
        setKnockback(knockback: import('./float').float): import('./void').void;
        /**
         * Get the vertical knockback (upward force) amount.
         * @return the upward knockback component
         */
        getKnockbackUp(): import('./float').float;
        /**
         * Set the vertical knockback (upward force) amount.
         * @param knockbackUp the new upward knockback component
         */
        setKnockbackUp(knockbackUp: import('./float').float): import('./void').void;
    }
    /**
     * Fired every tick while an ability is executing.
     * Provides the current phase and tick count.
          * @javaFqn noppes.npcs.api.event.IAbilityEvent.TickEvent
*/
    export interface TickEvent extends IAbilityEvent {
        /**
         * Get the current phase of the ability.
         * 0 = IDLE, 1 = WINDUP, 2 = ACTIVE, 3 = DAZED, 4 = BURST_DELAY
         * @return the current ability phase (0=IDLE, 1=WINDUP, 2=ACTIVE, 3=DAZED, 4=BURST_DELAY)
         */
        getAbilityPhase(): import('./int').int;
        /**
         * Get the current tick count within the phase.
         * @return the current tick within the phase
         */
        getTick(): import('./int').int;
    }
}
