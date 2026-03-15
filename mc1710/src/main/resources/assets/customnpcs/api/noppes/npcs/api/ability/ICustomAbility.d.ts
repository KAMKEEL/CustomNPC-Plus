/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * A custom (script-driven) ability whose behavior is entirely defined by script event hooks.
 * Supports both instant execution mode and toggle mode.
  * @javaFqn noppes.npcs.api.ability.ICustomAbility
*/
export interface ICustomAbility extends import('./IAbility').IAbility {
    /**
     * Get the duration in ticks for instant mode (non-toggle).
     * @return duration in ticks
     */
    getDurationTicks(): import('./int').int;
    /**
     * Set the duration in ticks for instant mode.
     * @param ticks duration in ticks
     */
    setDurationTicks(ticks: import('./int').int): import('./void').void;
    /**
     * Get the telegraph shape type ordinal.
     * @return ordinal of TelegraphType: 0=CIRCLE, 1=RING, 2=LINE, 3=CONE, 4=POINT, 5=SQUARE, 6=NONE
     */
    getTelegraphShapeType(): import('./int').int;
    /**
     * Set the telegraph shape type by ordinal.
     * @param type ordinal of TelegraphType
     */
    setTelegraphShapeType(type: import('./int').int): import('./void').void;
    /**
     * Get the targeting mode ordinal.
     * @return ordinal of TargetingMode: 0=AGGRO_TARGET, 1=SELF, 2=AOE_SELF, 3=AOE_TARGET
     */
    getTargetingModeType(): import('./int').int;
    /**
     * Set the targeting mode by ordinal.
     * @param type ordinal of TargetingMode
     */
    setTargetingModeType(type: import('./int').int): import('./void').void;
    /**
     * Get how many ticks the telegraph persists during the active phase.
     * When syncTelegraphWithDuration is true, this returns the duration ticks.
     * @return telegraph active ticks
     */
    getTelegraphActiveTicks(): import('./int').int;
    /**
     * Set how many ticks the telegraph persists during the active phase.
     * Only used when syncTelegraphWithDuration is false.
     * @param ticks telegraph active ticks
     */
    setTelegraphActiveTicks(ticks: import('./int').int): import('./void').void;
    /**
     * Whether telegraph active ticks are synced to the ability duration.
     * @return true if synced
     */
    isSyncTelegraphWithDuration(): import('./boolean').boolean;
    /**
     * Set whether telegraph active ticks should sync to the ability duration.
     * @param sync whether to sync
     */
    setSyncTelegraphWithDuration(sync: import('./boolean').boolean): import('./void').void;
    /**
     * Get the telegraph radius (used by CIRCLE, RING, SQUARE shapes).
     * @return the telegraph radius
     */
    getTelegraphRadius(): import('./float').float;
    /**
     * Set the telegraph radius.
     * @param radius the telegraph radius
     */
    setTelegraphRadius(radius: import('./float').float): import('./void').void;
    /**
     * Get the telegraph inner radius (used by RING, CONE shapes).
     * @return the inner radius
     */
    getTelegraphInnerRadius(): import('./float').float;
    /**
     * Set the telegraph inner radius.
     * @param innerRadius the inner radius
     */
    setTelegraphInnerRadius(innerRadius: import('./float').float): import('./void').void;
    /**
     * Get the telegraph length (used by LINE, CONE shapes).
     * @return the telegraph length
     */
    getTelegraphLength(): import('./float').float;
    /**
     * Set the telegraph length.
     * @param length the telegraph length
     */
    setTelegraphLength(length: import('./float').float): import('./void').void;
    /**
     * Get the telegraph width (used by LINE shape).
     * @return the telegraph width
     */
    getTelegraphWidth(): import('./float').float;
    /**
     * Set the telegraph width.
     * @param width the telegraph width
     */
    setTelegraphWidth(width: import('./float').float): import('./void').void;
    /**
     * Get the telegraph angle in degrees (used by CONE shape).
     * @return the telegraph angle in degrees
     */
    getTelegraphAngle(): import('./float').float;
    /**
     * Set the telegraph angle in degrees.
     * @param angle the angle in degrees
     */
    setTelegraphAngle(angle: import('./float').float): import('./void').void;
}
