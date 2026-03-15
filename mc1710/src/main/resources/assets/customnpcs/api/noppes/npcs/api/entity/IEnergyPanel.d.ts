/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents an energy panel barrier - a flat rectangular shield (Wall or Shield mode).
  * @javaFqn noppes.npcs.api.entity.IEnergyPanel
*/
export interface IEnergyPanel<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyBarrier').IEnergyBarrier {
    getPanelWidth(): import('./float').float;
    setPanelWidth(width: import('./float').float): import('./void').void;
    getPanelHeight(): import('./float').float;
    setPanelHeight(height: import('./float').float): import('./void').void;
    getPanelYaw(): import('./float').float;
    setPanelYaw(yaw: import('./float').float): import('./void').void;
    /**
     * Panel mode: 0=PLACED, 1=HELD, 2=LAUNCHED
     * @return the panel mode ordinal
     */
    getPanelMode(): import('./int').int;
    setPanelMode(mode: import('./int').int): import('./void').void;
    isLaunched(): import('./boolean').boolean;
    /** Spawn this panel entity into the world. */
    spawn(): import('./void').void;
}
