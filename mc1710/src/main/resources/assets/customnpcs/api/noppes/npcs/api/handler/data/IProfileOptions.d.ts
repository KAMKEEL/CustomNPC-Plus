/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IProfileOptions
 */
export interface IProfileOptions {
    hasProfileOptions(): import('./boolean').boolean;
    setProfileOptions(enable: import('./boolean').boolean): import('./void').void;
    /**
     * @param profileType 0:Individual, 1:Shared
     */
    setCooldownControl(profileType: import('./int').int): import('./void').void;
    /**
     * @return 0:Individual, 1:Shared
     */
    getCooldownControl(): import('./int').int;
    /**
     * @param profileType 0:Individual, 1:Shared
     */
    setCompleteControl(profileType: import('./int').int): import('./void').void;
    /**
     * @return 0:Individual, 1:Shared
     */
    getCompleteControl(): import('./int').int;
}
