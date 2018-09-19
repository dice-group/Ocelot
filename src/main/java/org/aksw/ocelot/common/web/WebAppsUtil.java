package org.aksw.ocelot.common.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;

public class WebAppsUtil {

  public static Logger LOG = Logger.getLogger(WebAppsUtil.class);

  /**
   * Gives the applications process id.
   *
   * @return applications process id
   */
  public static synchronized String getProcessId() {

    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    final int index = jvmName.indexOf('@');
    if (index < 1) {
      return null;
    }
    try {
      return Long.toString(Long.parseLong(jvmName.substring(0, index)));
    } catch (final NumberFormatException e) {
      return null;
    }
  }

  /**
   * Writes a system depended file to shut down the application with kill cmd and process id.
   *
   * @return true if the file was written
   */
  public static synchronized boolean writeShutDownFile(final String fileName) {

    // get process Id
    final String id = getProcessId();
    if (id == null) {
      return false;
    }

    String cmd = "";
    String fileExtension = "";

    cmd = "kill " + id + System.getProperty("line.separator") + "rm " + fileName + ".sh";
    fileExtension = "sh";
    LOG.info(fileName + "." + fileExtension);

    final File file = new File(fileName + "." + fileExtension);
    try {
      final BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(cmd);
      out.close();
    } catch (final Exception e) {
      LOG.error(e.getMessage());
    }
    file.setExecutable(true, false);
    file.deleteOnExit();
    return true;
  }

  public static synchronized boolean shutDown() {
    try {
      Runtime.getRuntime().exec("kill ".concat(getProcessId()));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return false;
    }
    return true;
  }
}
