package noppes.npcs.util;

import java.util.*;

public class CacheHashMap<K, V extends CacheHashMap.CachedObject<?>> extends HashMap<K, V> {
    private final long maxCacheTime;

    public CacheHashMap(long maxCacheTime) {
        this.maxCacheTime = maxCacheTime;
    }

    @Override
    public V get(Object key) {
        V object = super.get(key);
        object.updateTimeAccessed();

        Iterator<Map.Entry<K,V>> iterator = this.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K,V> entry = iterator.next();
            long timeDiff = entry.getValue().timeSinceAccessed();
            if (timeDiff > this.maxCacheTime) {
                iterator.remove();
            }
        }

        return object;
    }

    public static class CachedObject<T> {
        private long lastTimeAccessed;
        private final T object;

        public CachedObject(T object) {
            this.object = object;
            this.lastTimeAccessed = System.currentTimeMillis();
        }

        public long timeSinceAccessed() {
            return System.currentTimeMillis() - this.lastTimeAccessed;
        }

        public void updateTimeAccessed() {
            this.lastTimeAccessed = System.currentTimeMillis();
        }

        public T getObject() {
            return this.object;
        }
    }
}
