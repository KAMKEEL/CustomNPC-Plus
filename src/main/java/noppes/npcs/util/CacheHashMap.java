package noppes.npcs.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CacheHashMap<K, V extends CacheHashMap.CachedObject<?>> extends HashMap<K, V> {
    private final long maxCacheTime;
    private long saveInterval;

    public CacheHashMap(long maxCacheTime) {
        this.maxCacheTime = maxCacheTime;
    }

    public CacheHashMap(long maxCacheTime, long saveInterval) {
        this.maxCacheTime = maxCacheTime;
        this.saveInterval = saveInterval;
    }

    @Override
    public V get(Object key) {
        V object = super.get(key);
        object.updateTimeAccessed();

        Iterator<Map.Entry<K,V>> iterator = this.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K,V> entry = iterator.next();
            long lifeTimeDiff = entry.getValue().timeSinceAccessed();
            if (lifeTimeDiff > this.maxCacheTime) {
                entry.getValue().save();
                iterator.remove();
            } else if (this.saveInterval > 0) {
                long saveTimeDiff = entry.getValue().timeSinceSaved();
                if (saveTimeDiff > this.saveInterval) {
                    entry.getValue().save();
                    entry.getValue().updateSaveTime();
                }
            }
        }

        return object;
    }

    public static class CachedObject<T> {
        private long timeSaved;
        private long lastTimeAccessed;
        private final T object;

        public CachedObject(T object) {
            this.object = object;
            this.lastTimeAccessed = System.currentTimeMillis();
            this.timeSaved = System.currentTimeMillis();
        }

        public long timeSinceAccessed() {
            return System.currentTimeMillis() - this.lastTimeAccessed;
        }

        public long timeSinceSaved() {
            return System.currentTimeMillis() - this.timeSaved;
        }

        public void updateTimeAccessed() {
            this.lastTimeAccessed = System.currentTimeMillis();
        }

        public void updateSaveTime() {
            this.timeSaved = System.currentTimeMillis();
        }

        public void save(){}

        public T getObject() {
            return this.object;
        }
    }
}
