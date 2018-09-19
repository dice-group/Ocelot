package org.aksw.ocelot.data.kb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public final class DBpedia {
  public static final Logger LOG = LogManager.getLogger(DBpedia.class);

  // TODO: Add all to config
  public static final String url = "http://dbpedia.org/sparql";
  public static final String graph = "http://dbpedia.org";
  public static final int pagination = 1000;
  public static final int delay = 0;

  // TODO: add to resources
  public static final String queryPrefix = "../ocelot-data/sparql/prefix.txt";
  public static final String queryTriples = "../ocelot-data/sparql/triples.txt";
  public static final String queryLabels = "../ocelot-data/sparql/labels.txt";
  public static final String queryWikidata = "../ocelot-data/sparql/wikidatalink.txt";

  public static String PREFIX;

  static {
    try {
      PREFIX = new String(Files.readAllBytes(Paths.get(queryPrefix)));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      PREFIX = "";
    }
  }

  private DBpedia() {}

  /*
   * public static String queryformat(final String format, final Object... args) { return
   * DBpedia.PREFIX.concat(String.format(format, args)); }
   */
}
