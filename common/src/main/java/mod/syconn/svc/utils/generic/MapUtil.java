package mod.syconn.svc.utils.generic;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    public static <K, V> Map<K, V> of(K key, V value) {
        var map = new HashMap<K, V>();
        map.put(key, value);
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> add(K key, V value, Map<K, V>... maps) {
        var map = join(maps);
        map.put(key, value);
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> add(Map.Entry<K, V> add, Map<K, V>... maps) {
        var map = join(maps);
        map.put(add.getKey(), add.getValue());
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> remove(K key, Map<K, V>... maps) {
        var map = join(maps);
        map.remove(key);
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> join(Map<K, V>... maps) {
        var joined = new HashMap<K, V>();
        for (var map : maps) {
            if (map != null) joined.putAll(map);
        }
        return joined;
    }
}
