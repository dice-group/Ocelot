package org.aksw.ocelot.data;

import java.io.File;
import java.nio.file.Paths;

import org.aksw.ocelot.common.config.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Reads config files in {@link CFG_FOLDER}
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Const {
  protected static Logger LOG = LogManager.getLogger(Const.class);

  public static final boolean NER_USE = true;

  public static final XMLConfiguration CFG = CfgManager.getCfg(Const.class);

  // folders
  public static final String DATA_FOLDER = CFG.getString("ocelot.dataFolder");
  public static final String TMP_FOLDER = CFG.getString("ocelot.tmpFolder");
  public static final String DBPEDIA_FOLDER =
      Const.DATA_FOLDER + File.separator + "dbpedia" + File.separator;

  public static final String DBPEDIA_SURFACEFORMS_FILE = "_surface_forms.tsv";

  // public static final String SERIALISATION_SENTENCE_CANDIDATES = "sentenceCanidates.data";

  // properties
  public static final String RELATION_DOMAIN = CFG.getString("properties.domain");
  public static final String RELATION_RANGE = CFG.getString("properties.range");
  public static final String RELATION_FILE = Paths.get(CfgManager.CFG_FOLDER).normalize().resolve(//
      CFG.getString("properties.file")).toString();
  public static final String RELATION_DOMAIN_PLACEHOLDER =
      CFG.getString("properties.domainPlaceholder");
  public static final String RELATION_RANGE_PLACEHOLDER =
      CFG.getString("properties.rangePlaceholder");

  //
  public static final boolean useSurfaceforms = true;

  // index
  public static final int INDEX_FIXED_THREAD_POOL_SIZE = CFG.getInt("index.threads");
  public static final int INDEX_NLP_TIMEOUT = CFG.getInt("index.timeout");
  public static final int sentenceLengthMin = CFG.getInt("index.sentenceLengthMin");
  public static final int sentenceLengthMax = CFG.getInt("index.sentenceLengthMax");
  // public static final int sfSetSizeMin = CFG.getInt("index.sfSetSizeMin");
  // public static final int sfSetSizeMax = CFG.getInt("index.sfSetSizeMax");
  public static final int searchThreadsSF = CFG.getInt("index.searchThreadsSF");
  public static final int searchTimeoutSF = CFG.getInt("index.searchTimeoutSF");

  // search limit of the wiki index results
  public static final int LIMIT = getInt("settings.limit");
  public static final int MAX_PUNCT = CFG.getInt("settings.maxPunct");
  public static final int TRIPLE_STEPS = getInt("settings.tripleSteps");
  public static final int maxTriplesperURI = getInt("settings.maxTriples");

  public static final int minSFlength = CFG.getInt("settings.minSurfaceformsLength");

  // word2ec
  public static final String word2vecKey = CFG.getString("word2vec.key");
  public static final String word2vecEndpoint = CFG.getString("word2vec.uri");
  public static final double SIM_THRESHOLD = CFG.getDouble("word2vec.threshold");

  // corpus
  public static final String CORPUS_FOLDER = CFG.getString("corpus.folder");

  // SOLR
  public static final String SOLR_CORE_INDEX = CFG.getString("solr.indexCore");
  public static final String SOLR_CORE_SURFACEFORMS = CFG.getString("solr.surfaceformsCore");
  public static final int SOLR_TIMEOUT = CFG.getInt("solr.timeout");
  public static final String SOLR_URL = CFG.getString("solr.url");
  public static final int SOLR_THREADS = CFG.getInt("solr.threads");
  public static final int SOLR_QUEUE = CFG.getInt("solr.queue");
  public static final int SOLR_ROWS = CFG.getInt("solr.rows");

  // unsorted
  // index - sfSteps sfs per thread (1 sparql request for sfSteps sfs)
  public static final int sfSteps = 50; // sf index
  public static final int INDEX_SOLR_THREADS = 4;
  public static final int INDEX_SOLR_QUEUE = 10;
  // pagination size
  public static final int INDEX_SOLR_ROWS = 10000;

  /**
   * Gets the integer value assigned to the key.
   *
   * @param key
   * @return
   */
  public static int getInt(final String key) {
    return CFG.getInt(key) < 0 ? //
        Integer.MAX_VALUE : CFG.getInt(key);
  }

  // print informations
  static {
    LOG.info("==== Constants are loaded ====");
    LOG.info("max triples per URI: " + maxTriplesperURI);
    LOG.info("min SF length: " + minSFlength);
    LOG.info("Domain: " + RELATION_DOMAIN + "(" + RELATION_DOMAIN_PLACEHOLDER + ")");
    LOG.info("Range: " + RELATION_RANGE + "(" + RELATION_RANGE_PLACEHOLDER + ")");
    LOG.info("wiki search limit: " + LIMIT);
    LOG.info("use additional surfaceforms: " + (useSurfaceforms ? "on" : "off"));
    LOG.info("We " + (NER_USE ? "check" : "ignore") + "NER types in the corpus");
    LOG.info("==============================");
  }
}
