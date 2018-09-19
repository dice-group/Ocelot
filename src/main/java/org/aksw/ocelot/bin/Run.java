package org.aksw.ocelot.bin;

import org.aksw.ocelot.common.io.Memory;
import org.aksw.ocelot.common.web.WebAppsUtil;
import org.aksw.ocelot.core.pipeline.Drift;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Run {
  public static final Logger LOG = LogManager.getLogger(Run.class);

  /**
   * Starts the pipe.
   */
  public static void start() {
    new Drift();
  }

  public static void main(final String[] args) {
    LOG.info("Start ...");

    WebAppsUtil.writeShutDownFile("bin/close");

    final int msec = 10 * 1000;
    final Memory memory = new Memory(Thread.currentThread(), msec);

    // run pipe
    start();

    LOG.info("Max memory in use  " + memory.maxValue);
    LOG.info("End ...");
  }
}
