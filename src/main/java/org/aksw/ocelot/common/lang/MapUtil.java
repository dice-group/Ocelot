package org.aksw.ocelot.common.lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapUtil {

  /**
   * Gets a value sorted LinkedList object.
   *
   * @param map
   * @return sorted LinkedList
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map) {
    return _sortByValue(new LinkedList<Map.Entry<K, V>>(map.entrySet()), false);
  }

  /**
   * Gets a value sorted LinkedList object.
   *
   * @param map
   * @return sorted LinkedList
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> reverseSortByValue(
      final Map<K, V> map) {
    return _sortByValue(new LinkedList<Map.Entry<K, V>>(map.entrySet()), true);
  }

  private static <K, V extends Comparable<? super V>> Map<K, V> _sortByValue(
      final List<Map.Entry<K, V>> mapEntries, final boolean reverse) {
    Collections.sort(mapEntries, (o1, o2) -> {
      if (reverse) {
        return (o2.getValue()).compareTo(o1.getValue());
      } else {
        return (o1.getValue()).compareTo(o2.getValue());
      }
    });
    final Map<K, V> result = new LinkedHashMap<K, V>();
    mapEntries.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }
}
