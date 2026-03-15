/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Entity-level interface for zone entities (Hazard and Trap).
 * Created via IEnergyHandler.createHazard() or createTrap().
 * Configure properties, then call spawn() to place in the world.
  * @javaFqn noppes.npcs.api.entity.IEnergyZone
*/
export interface IEnergyZone<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEntity').IEntity {
    /** @return Zone type: 0=Trap, 1=Hazard */
    getZoneType(): import('./int').int;
    /** @return Zone shape: 0=Circle, 1=Square */
    getZoneShape(): import('./int').int;
    setZoneShape(shape: import('./int').int): import('./void').void;
    getRadius(): import('./float').float;
    setRadius(radius: import('./float').float): import('./void').void;
    getZoneHeight(): import('./float').float;
    setZoneHeight(height: import('./float').float): import('./void').void;
    getDuration(): import('./int').int;
    setDuration(ticks: import('./int').int): import('./void').void;
    getInnerColor(): import('./int').int;
    setInnerColor(color: import('./int').int): import('./void').void;
    getOuterColor(): import('./int').int;
    setOuterColor(color: import('./int').int): import('./void').void;
    isOuterColorEnabled(): import('./boolean').boolean;
    setOuterColorEnabled(enabled: import('./boolean').boolean): import('./void').void;
    getParticleDensity(): import('./float').float;
    setParticleDensity(density: import('./float').float): import('./void').void;
    getParticleScale(): import('./float').float;
    setParticleScale(scale: import('./float').float): import('./void').void;
    getAnimSpeed(): import('./float').float;
    setAnimSpeed(speed: import('./float').float): import('./void').void;
    isIgnoreIFrames(): import('./boolean').boolean;
    setIgnoreIFrames(ignore: import('./boolean').boolean): import('./void').void;
    getDamagePerSecond(): import('./float').float;
    setDamagePerSecond(dps: import('./float').float): import('./void').void;
    getDamageInterval(): import('./int').int;
    setDamageInterval(ticks: import('./int').int): import('./void').void;
    isAffectsCaster(): import('./boolean').boolean;
    setAffectsCaster(affects: import('./boolean').boolean): import('./void').void;
    getTriggerRadius(): import('./float').float;
    setTriggerRadius(radius: import('./float').float): import('./void').void;
    getArmTime(): import('./int').int;
    setArmTime(ticks: import('./int').int): import('./void').void;
    getMaxTriggers(): import('./int').int;
    setMaxTriggers(max: import('./int').int): import('./void').void;
    getTriggerCooldown(): import('./int').int;
    setTriggerCooldown(ticks: import('./int').int): import('./void').void;
    getDamage(): import('./float').float;
    setDamage(damage: import('./float').float): import('./void').void;
    getKnockback(): import('./float').float;
    setKnockback(knockback: import('./float').float): import('./void').void;
    isVisible(): import('./boolean').boolean;
    setVisible(visible: import('./boolean').boolean): import('./void').void;
    /**
     * Get custom damage data for addon handler routing (e.g. DBC damage stats).
     * @return the zone's damage configuration as NBT
     */
    getDamageData(): import('../INbt').noppes.npcs.api.INbt;
    /**
     * Set custom damage data for addon handler routing.
     * @param data the damage configuration NBT
     */
    setDamageData(data: import('../INbt').noppes.npcs.api.INbt): import('./void').void;
    /** Spawn this zone entity into the world. */
    spawn(): import('./void').void;
}
