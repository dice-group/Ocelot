package org.aksw.ocelot.common.io;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Memory {
  protected final static Logger LOG = LogManager.getLogger(Memory.class);

  public long maxValue = -1;

  /**
   *
   * Constructor.
   *
   * @param thread
   */
  public Memory(final Thread thread) {
    this(thread, 10 * 1000);
  }

  /**
   *
   * Constructor.
   *
   * @param thread
   * @param sleep in ms
   */
  public Memory(final Thread thread, final int sleep) {
    final Runtime runtime = Runtime.getRuntime();
    new Thread(() -> {
      while (thread.isAlive()) {
        try {
          Thread.sleep(sleep);
        } catch (final Exception e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
        System.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        memoryAfter = memoryAfter / (1000 * 1000);
        if (memoryAfter > maxValue) {
          maxValue = memoryAfter;
        }
        LOG.info(maxValue + "MB /" + (runtime.totalMemory() / (1000 * 1000)) + "MB");
      }
    }).start();
  }
}
