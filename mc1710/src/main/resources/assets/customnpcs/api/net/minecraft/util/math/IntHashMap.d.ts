/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.util.math
 */

/**
 * @javaFqn net.minecraft.util.math.IntHashMap
 */
export class IntHashMap<V> {
    /**
     * The number of items stored in this map
     */
    private transient int count;
    /**
     * The grow threshold
     */
    private int threshold = 12;
    /**
     * The scale factor used to determine when to grow the table
     */
    private final float growFactor = 0.75F;
    
    /**
     * Makes the passed in integer suitable for hashing by a number of shifts
     */
    computeHash(integer: import('./int').int): import('./int').int;
    /**
     * Computes the index of the slot for the hash and slot count passed in.
     */
    getSlotIndex(hash: import('./int').int, slotCount: import('./int').int): import('./int').int;
    /**
     * Returns the object associated to a key
     *
     * @param key the integer key to look up
     * @return the value associated with the key, or null if not found
     */
    lookup(key: import('./int').int): V;
    /**
     * Returns true if this hash table contains the specified item.
     *
     * @param key the integer key to check
     * @return true if the map contains this key
     */
    containsItem(key: import('./int').int): import('./boolean').boolean;
    lookupEntry(p_76045_1_: import('./int').int): IntHashMap.Entry<V>;
    /**
     * Adds a key and associated value to this map
     *
     * @param key the integer key
     * @param value the value to associate with the key
     */
    addKey(key: import('./int').int, value: V): import('./void').void;
    /**
     * Increases the number of hash slots
     */
    grow(p_76047_1_: import('./int').int): import('./void').void;
    /**
     * Copies the hash slots to a new array
     */
    copyTo(p_76048_1_: IntHashMap.Entry<V>[]): import('./void').void;
    /**
     * Removes the specified object from the map and returns it
     *
     * @param key the integer key to remove
     */
    removeObject(key: import('./int').int): V;
    removeEntry(p_76036_1_: import('./int').int): IntHashMap.Entry<V>;
    /**
     * Removes all entries from the map
     */
    clearMap(): import('./void').void;
    /**
     * Adds an object to a slot
     */
    insert(p_76040_1_: import('./int').int, p_76040_2_: import('./int').int, p_76040_3_: V, p_76040_4_: import('./int').int): import('./void').void;
    /**
     * The number of items stored in this map
     */
    private transient int count;
    /**
     * The grow threshold
     */
    threshold: import('./int').int;
    /**
     * The scale factor used to determine when to grow the table
     */
    growFactor: import('./float').float;
}

export namespace IntHashMap {
    /**
     * @javaFqn net.minecraft.util.math.IntHashMap.Entry
     */
    export class Entry<V> {
        /**
         * The hash code of this entry
         */
        final int hashEntry;
        /**
         * The object stored in this entry
         */
        V valueEntry;
        IntHashMap.Entry<V> nextEntry;
        /**
         * The id of the hash slot computed from the hash
         */
        final int slotHash;
        
        Entry(int p_i1552_1_, int p_i1552_2_, V p_i1552_3_, IntHashMap.Entry<V> p_i1552_4_) {
        this.valueEntry = p_i1552_3_;
        this.nextEntry = p_i1552_4_;
        this.hashEntry = p_i1552_2_;
        this.slotHash = p_i1552_1_;
        }
        
        /**
         * Returns the hash code for this entry
         */
        getHash(): import('./int').int;
        /**
         * Returns the object stored in this entry
         */
        getValue(): import('./V').V;
        equals(p_equals_1_: Object): import('./boolean').boolean;
        hashCode(): import('./int').int;
        toString(): String;
    }
}
