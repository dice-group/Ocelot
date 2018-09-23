package org.aksw.ocelot.common.config;

import java.io.File;
import java.nio.file.Paths;

import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * CfgManager with default configuration folder 'config'.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class CfgManager {
  protected final static Logger LOG = LogManager.getLogger(CfgManager.class);

  public static String CFG_FOLDER = "config";

  /**
   * Overwrites the default configuration folder value 'config'.
   *
   * @param folder
   */
  public static String setFolder(final String folder) {
    CFG_FOLDER = Paths.get(folder).normalize().toString();
    return CFG_FOLDER;
  }

  /**
   * Reads the configuration for the given class.
   *
   * @param className
   *
   * @return cfg XMLConfiguration or null
   */
  public static XMLConfiguration getCfg(final String className) {

    final String file = CFG_FOLDER.concat(File.separator).concat(className).concat(".xml");
    XMLConfiguration cfg = null;
    if (FileUtil.fileExists(file)) {
      try {
        LOG.info("Loading: ".concat(file));
        cfg = new XMLConfiguration(file);
      } catch (final ConfigurationException e) {
        LOG.error(e.getLocalizedMessage(), e);
        LOG.error("Could not load XML configuration file.");
      }
    } else {
      LOG.error("Could not find file(" + className + ").");
    }
    return cfg;
  }

  /**
   * Calls {@link #getCfg(String)} with the given class name.
   *
   * @param classs
   *
   * @return cfg XMLConfiguration
   */
  public static XMLConfiguration getCfg(final Class<?> classs) {
    return CfgManager.getCfg(classs.getName());
  }
}
