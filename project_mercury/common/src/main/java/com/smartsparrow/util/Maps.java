package com.smartsparrow.util;

import java.util.HashMap;
import java.util.Map;

public class Maps {

    /**
     * Creates a Map with an entry
     *
     * @param k the entry key
     * @param v the entry value
     * @param <K> the key type
     * @param <V> the value type
     * @return a new map with an entry
     */
    public static <K, V> Map<K, V> of(K k, V v) {
        return new HashMap<K, V>(){
            private static final long serialVersionUID = -2055333167083236344L;

            { put(k, v); }
        };
    }

    /**
     * Creates a map with an entry where the value is an object
     *
     * @param k the entry key
     * @param v the entry value
     * @param <K> the key type
     * @return a new map with an entry
     */
    public static <K> Map<K, Object> ofObject(K k, Object v) {
        return of(k, v);
    }
}
