/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * MC shadow of the INbtList interface.
 * Shadows the platform-api version at compile time for mc1710.
  * @javaFqn noppes.npcs.api.INbtList
*/
export interface INbtList {
    size(): import('./int').int;
    getCompound(index: import('./int').int): import('./INbt').INbt;
    getString(index: import('./int').int): String;
    getInt(index: import('./int').int): import('./int').int;
    getDouble(index: import('./int').int): import('./double').double;
    getFloat(index: import('./int').int): import('./float').float;
    getIntArray(index: import('./int').int): import('./int').int[];
    getElementType(): import('./int').int;
    addCompound(compound: import('./INbt').INbt): import('./void').void;
    addString(value: String): import('./void').void;
    addInt(value: import('./int').int): import('./void').void;
    addDouble(value: import('./double').double): import('./void').void;
    remove(index: import('./int').int): import('./void').void;
    getMCTagList(): Object;
}
