package org.aksw.ocelot.common.lang;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class UniqueQueue<T> {

  private final Queue<T> queue = new LinkedList<T>();
  private final Set<T> set = new HashSet<T>();

  public boolean add(final T t) {
    if (set.add(t)) {
      queue.add(t);
      return true;
    }
    return false;
  }

  public T remove() {
    final T ret = queue.remove();
    set.remove(ret);
    return ret;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    return queue.size();
  }
}
