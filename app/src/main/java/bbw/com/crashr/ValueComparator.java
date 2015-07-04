package bbw.com.crashr;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by Sam on 4/07/2015.
 */
public class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {
    Map<K, V> map;

    public ValueComparator(Map<K, V> base) {
        this.map = base;
    }

    @Override
    public int compare(K o1, K o2) {
        return map.get(o2).compareTo(map.get(o1));
    }
}
