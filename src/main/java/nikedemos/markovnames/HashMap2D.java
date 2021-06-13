package nikedemos.markovnames;

import java.util.HashMap;
import java.util.Map;

public class HashMap2D<T1, T2, T3> {

public final Map<T1, Map<T2, T3>> mMap;

/*
 https://stackoverflow.com/a/10299689/9355344
 */

public HashMap2D() {
    mMap = new HashMap<T1, Map<T2, T3>>();
}

/**
 * Associates the specified value with the specified keys in this map (optional operation). If the map previously
 * contained a mapping for the key, the old value is replaced by the specified value.
 * 
 * @param key1
 *            the first key
 * @param key2
 *            the second key
 * @param value
 *            the value to be set
 * @return the value previously associated with (key1,key2), or <code>null</code> if none
 * @see Map#put(Object, Object)
 */
public T3 put(T1 key1, T2 key2, T3 value) {
    Map<T2, T3> map;
    if (mMap.containsKey(key1)) {
        map = mMap.get(key1);
    } else {
        map = new HashMap<T2, T3>();
        mMap.put(key1, map);
    }

    return map.put(key2, value);
}

/**
 * Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
 * the key.
 * 
 * @param key1
 *            the first key whose associated value is to be returned
 * @param key2
 *            the second key whose associated value is to be returned
 * @return the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for
 *         the key
 * @see Map#get(Object)
 */
public T3 get(T1 key1, T2 key2) {
    if (mMap.containsKey(key1)) {
        return mMap.get(key1).get(key2);
    } else {
        return null;
    }
}

/**
 * Returns <code>true</code> if this map contains a mapping for the specified key
 * 
 * @param key1
 *            the first key whose presence in this map is to be tested
 * @param key2
 *            the second key whose presence in this map is to be tested
 * @return Returns true if this map contains a mapping for the specified key
 * @see Map#containsKey(Object)
 */
public boolean containsKeys(T1 key1, T2 key2) {
    return mMap.containsKey(key1) && mMap.get(key1).containsKey(key2);
}

public void clear() {
    mMap.clear();
}

}