package noppes.npcs.util;

import java.util.*;

public class CacheHashMap<K, V extends CacheHashMap.CachedObject<?>> extends HashMap<K, V> {
    private final long maxCacheTime;

    public CacheHashMap(long maxCacheTime) {
        this.maxCacheTime = maxCacheTime;
    }

    @Override
    public V get(Object key) {
        synchronized (this) {
            List<K> keysToRemove = new ArrayList<>();
            for (Map.Entry<K,V> entry : this.entrySet()) {
                if (!Objects.equals(key,entry.getKey())) {
                    long timeDiff = entry.getValue().timeSinceAccessed();
                    if (timeDiff > this.maxCacheTime) {
                        keysToRemove.add(entry.getKey());
                    }
                }
            }
            for (K k : keysToRemove) {
                this.remove(k);
            }
        }

        V object = super.get(key);
        object.updateTimeAccessed();

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
