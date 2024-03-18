package noppes.npcs.util;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public LRUHashMap(int size) {
        super(size, 0.75F, true);
        this.maxSize = size;
    }

    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return this.size() > this.maxSize;
    }
}
