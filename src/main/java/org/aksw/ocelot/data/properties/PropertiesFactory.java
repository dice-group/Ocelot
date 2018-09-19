package org.aksw.ocelot.data.properties;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.aksw.ocelot.data.Const;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class PropertiesFactory {
  protected static Logger LOG = LogManager.getLogger(PropertiesFactory.class);

  static BackgroundKnowledge properties = null;

  public static final Path file = Paths.get(Const.RELATION_FILE).normalize();

  public static BackgroundKnowledge getInstance() {
    if (properties == null) {
      final int pagination = 1000;
      final int delay = 2000;
      final String url = "http://dbpedia.org/sparql";
      final String graph = "http://dbpedia.org";

      LOG.info("file: " + file.getFileName());
      properties = new Properties(url, graph, pagination, delay, file);
    }
    return properties;
  }

  public static BackgroundKnowledge getInstance(final Path file) {
    if (properties == null) {
      final int pagination = 1000;
      final int delay = 2000;
      final String url = "http://dbpedia.org/sparql";
      final String graph = "http://dbpedia.org";

      properties = new Properties(url, graph, pagination, delay, file);
    }
    return properties;
  }
}
