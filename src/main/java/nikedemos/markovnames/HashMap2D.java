package nikedemos.markovnames;

import java.util.HashMap;
import java.util.Map;

/**
 * A two-dimensional HashMap.
 *
 * @param <T1> the type of the first key
 * @param <T2> the type of the second key
 * @param <T3> the type of the value
 */
public class HashMap2D<T1, T2, T3> {

    public final Map<T1, Map<T2, T3>> mMap;

    /**
     * <a href="https://stackoverflow.com/a/10299689/9355344">Constructs an empty HashMap2D.</a>
     */
    public HashMap2D() {
        mMap = new HashMap<>();
    }

    /**
     * Associates the specified value with the specified keys in this map (optional operation). If the map previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * @param key1  the first key
     * @param key2  the second key
     * @param value the value to be set
     * @return the value previously associated with (key1, key2), or <code>null</code> if none
     */
    public T3 put(T1 key1, T2 key2, T3 value) {
        Map<T2, T3> map = mMap.computeIfAbsent(key1, k -> new HashMap<>());
        return map.put(key2, value);
    }

    /**
     * Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
     * the key.
     *
     * @param key1 the first key whose associated value is to be returned
     * @param key2 the second key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
     * the key
     * @see Map#get(Object)
     */
    public T3 get(T1 key1, T2 key2) {
        Map<T2, T3> map = mMap.get(key1);
        return map != null ? map.get(key2) : null;
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key.
     *
     * @param key1 the first key whose presence in this map is to be tested
     * @param key2 the second key whose presence in this map is to be tested
     * @return <code>true</code> if this map contains a mapping for the specified key
     * @see Map#containsKey(Object)
     */
    public boolean containsKeys(T1 key1, T2 key2) {
        Map<T2, T3> map = mMap.get(key1);
        return map != null && map.containsKey(key2);
    }

    /**
     * Removes all the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        mMap.clear();
    }
}