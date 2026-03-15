/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * MC shadow of the unified INbt interface.
 * This version shadows the platform-api INbt at compile time for mc1710,
 * adding the typed {@code getMCNBT()} method and scripting convenience methods.
 *
 * <p>Compound tags contain uniquely-keyed tags of various types:
 * <pre>
 * 1: Byte    2: Short   3: Int      4: Long
 * 5: Float   6: Double  7: Byte[]   8: String
 * 9: List   10: Compound 11: Int[]
 * </pre>
  * @javaFqn noppes.npcs.api.INbt
*/
export interface INbt {
    setString(key: String, value: String): import('./void').void;
    setInteger(key: String, value: import('./int').int): import('./void').void;
    setBoolean(key: String, value: import('./boolean').boolean): import('./void').void;
    setDouble(key: String, value: import('./double').double): import('./void').void;
    setFloat(key: String, value: import('./float').float): import('./void').void;
    setLong(key: String, value: import('./long').long): import('./void').void;
    setShort(key: String, value: import('./short').short): import('./void').void;
    setByte(key: String, value: import('./byte').byte): import('./void').void;
    setByteArray(key: String, value: import('./byte').byte[]): import('./void').void;
    setIntArray(key: String, value: import('./int').int[]): import('./void').void;
    setIntegerArray(key: String, value: import('./int').int[]): import('./void').void;
    setCompound(key: String, compound: import('./INbt').INbt): import('./void').void;
    setTag(key: String, compound: import('./INbt').INbt): import('./void').void;
    /**
     * Sets a list from an Object array. Elements can be: INbt, String, Double, Float, Integer, int[].
     */
    setList(key: String, value: Object[]): import('./void').void;
    /**
     * Sets a typed tag list on this compound.
     */
    setTagList(key: String, list: import('./INbtList').INbtList): import('./void').void;
    getString(key: String): String;
    getInteger(key: String): import('./int').int;
    getBoolean(key: String): import('./boolean').boolean;
    getDouble(key: String): import('./double').double;
    getFloat(key: String): import('./float').float;
    getLong(key: String): import('./long').long;
    getShort(key: String): import('./short').short;
    getByte(key: String): import('./byte').byte;
    getByteArray(key: String): import('./byte').byte[];
    getIntArray(key: String): import('./int').int[];
    getIntegerArray(key: String): import('./int').int[];
    getIntArray(): import('./return').return;
    getCompound(key: String): import('./INbt').INbt;
    /**
     * Returns a tag list as an Object array. Elements are typed based on list type:
     * INbt (compounds), String, Double, Float, Integer, int[].
     */
    getList(key: String, type: import('./int').int): Object[];
    /**
     * Gets a typed tag list from this compound.
     *
     * @param key  the key
     * @param type the element type ID (e.g. 10 for compounds, 8 for strings)
     * @return the list, or an empty list if the key doesn't exist
     */
    getTagList(key: String, type: import('./int').int): import('./INbtList').INbtList;
    /**
     * Returns the type of the tag list with this key.
     */
    getListType(key: String): import('./int').int;
    hasKey(key: String): import('./boolean').boolean;
    has(key: String): import('./boolean').boolean;
    hasKey(): import('./return').return;
    hasKey(key: String, type: import('./int').int): import('./boolean').boolean;
    getKeySet(): String[];
    getKeys(): String[];
    removeTag(key: String): import('./void').void;
    remove(key: String): import('./void').void;
    getTagType(key: String): import('./int').int;
    getType(key: String): import('./int').int;
    getTagType(): import('./return').return;
    isEmpty(): import('./boolean').boolean;
    merge(other: import('./INbt').INbt): import('./void').void;
    copy(): import('./INbt').INbt;
    /**
     * Returns the underlying MC NBTTagCompound.
     * Expert use only.
     */
    getMCNBT(): import('../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
    toJsonString(): String;
    isEqual(nbt: import('./INbt').INbt): import('./boolean').boolean;
    clear(): import('./void').void;
}
