/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Teleport abilities.
 * Instant repositioning with various modes.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityTeleport
*/
export interface IAbilityTeleport extends import('../IAbility').IAbility {
    /** @return Teleport mode ordinal (0=BLINK, 1=BEHIND, 2=SINGLE). */
    getMode(): import('./int').int;
    /** @param mode Teleport mode ordinal (0=BLINK, 1=BEHIND, 2=SINGLE). */
    setMode(mode: import('./int').int): import('./void').void;
    /** @return Number of rapid blink teleports in BLINK mode. */
    getBlinkCount(): import('./int').int;
    /** @param count Number of blink teleports. */
    setBlinkCount(count: import('./int').int): import('./void').void;
    /** @return Delay in ticks between blink teleports. */
    getBlinkDelayTicks(): import('./int').int;
    /** @param ticks Delay in ticks between blinks. */
    setBlinkDelayTicks(ticks: import('./int').int): import('./void').void;
    /** @return Maximum random offset radius for blink teleports in blocks. */
    getBlinkRadius(): import('./float').float;
    /** @param radius Blink offset radius in blocks. */
    setBlinkRadius(radius: import('./float').float): import('./void').void;
    /** @return Distance behind the target to teleport to in BEHIND mode. */
    getBehindDistance(): import('./float').float;
    /** @param distance Behind-target distance in blocks. */
    setBehindDistance(distance: import('./float').float): import('./void').void;
    /** @return Whether the teleport requires clear line of sight to the destination. */
    isRequireLineOfSight(): import('./boolean').boolean;
    /** @param require Whether line of sight is required. */
    setRequireLineOfSight(require: import('./boolean').boolean): import('./void').void;
    /** @return Whether damage is dealt at the departure location. */
    isDamageAtStart(): import('./boolean').boolean;
    /** @param damage Whether to deal damage at departure. */
    setDamageAtStart(damage: import('./boolean').boolean): import('./void').void;
    /** @return Whether damage is dealt at the arrival location. */
    isDamageAtEnd(): import('./boolean').boolean;
    /** @param damage Whether to deal damage at arrival. */
    setDamageAtEnd(damage: import('./boolean').boolean): import('./void').void;
    /** @return Damage dealt at teleport locations. */
    getDamage(): import('./float').float;
    /** @param damage Teleport damage amount. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Radius of the damage area at teleport locations in blocks. */
    getDamageRadius(): import('./float').float;
    /** @param radius Damage area radius in blocks. */
    setDamageRadius(radius: import('./float').float): import('./void').void;
    RANDOM: import('./enum TeleportMode private static final Random').enum TeleportMode private static final Random;
    getDisplayDamage: import('./float').float;
}
