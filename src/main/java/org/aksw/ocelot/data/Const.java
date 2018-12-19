package org.aksw.ocelot.data;

import java.io.File;
import java.nio.file.Paths;

import org.aksw.simba.knowledgeextraction.commons.config.CfgManager;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Reads config file.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Const {

  protected static Logger LOG = LogManager.getLogger(Const.class);
  // public static String CFG_FOLDER = "config";
  // public static String CFG_FOLDER = "data/ocelot/config";

  public static String baseFolder = "config";
  protected CfgManager cfgManager = null;
  protected XMLConfiguration CFG = null;

  public static String DATA_FOLDER = null;
  public static String TMP_FOLDER = null;
  public static String DBPEDIA_FOLDER = null;

  // properties
  public static String RELATION_DOMAIN = null;
  public static String RELATION_RANGE = null;
  public static String RELATION_FILE = null;
  public static String RELATION_DOMAIN_PLACEHOLDER = null;
  public static String RELATION_RANGE_PLACEHOLDER = null;

  // index
  public static int INDEX_FIXED_THREAD_POOL_SIZE = -1;
  public static int INDEX_NLP_TIMEOUT = -1;
  public static int sentenceLengthMin = -1;
  public static int sentenceLengthMax = -1;
  public static int searchThreadsSF = -1;
  public static int searchTimeoutSF = -1;

  // search limit of the wiki index results
  public static int LIMIT = -1;
  public static int MAX_PUNCT = -1;
  public static int TRIPLE_STEPS = -1;
  public static int maxTriplesperURI = -1;

  public static int minSFlength = -1;

  // word2ec
  public static String word2vecKey = null;
  public static String word2vecEndpoint = null;
  public static double SIM_THRESHOLD = -1;

  // corpus
  public static String CORPUS_FOLDER = null;

  // SOLR
  public static String SOLR_CORE_INDEX = null;
  public static String SOLR_CORE_SURFACEFORMS = null;
  public static int SOLR_TIMEOUT = -1;
  public static String SOLR_URL = null;
  public static int SOLR_THREADS = -1;
  public static int SOLR_QUEUE = -1;
  public static int SOLR_ROWS = -1;

  public Const(final String baseFolder) {
    Const.baseFolder = baseFolder;
    cfgManager = new CfgManager(baseFolder);
    CFG = cfgManager.getCfg(Const.class);

    DATA_FOLDER = CFG.getString("ocelot.dataFolder");
    TMP_FOLDER = CFG.getString("ocelot.tmpFolder");
    DBPEDIA_FOLDER = Const.DATA_FOLDER + File.separator + "dbpedia" + File.separator;

    RELATION_DOMAIN = CFG.getString("properties.domain");
    RELATION_RANGE = CFG.getString("properties.range");
    RELATION_FILE = Paths.get(baseFolder).normalize().resolve(//
        CFG.getString("properties.file")).toString();
    RELATION_DOMAIN_PLACEHOLDER = CFG.getString("properties.domainPlaceholder");
    RELATION_RANGE_PLACEHOLDER =

        CFG.getString("properties.rangePlaceholder");

    // index
    INDEX_FIXED_THREAD_POOL_SIZE = CFG.getInt("index.threads");
    INDEX_NLP_TIMEOUT = CFG.getInt("index.timeout");
    sentenceLengthMin = CFG.getInt("index.sentenceLengthMin");
    sentenceLengthMax = CFG.getInt("index.sentenceLengthMax");
    // sfSetSizeMin = CFG.getInt("index.sfSetSizeMin");
    // sfSetSizeMax = CFG.getInt("index.sfSetSizeMax");
    searchThreadsSF = CFG.getInt("index.searchThreadsSF");
    searchTimeoutSF = CFG.getInt("index.searchTimeoutSF");

    // search limit of the wiki index results
    LIMIT = getInt(CFG, "settings.limit");
    MAX_PUNCT = CFG.getInt("settings.maxPunct");
    TRIPLE_STEPS = getInt(CFG, "settings.tripleSteps");
    maxTriplesperURI = getInt(CFG, "settings.maxTriples");

    minSFlength = CFG.getInt("settings.minSurfaceformsLength");

    // word2ec
    word2vecKey = CFG.getString("word2vec.key");
    word2vecEndpoint = CFG.getString("word2vec.uri");
    SIM_THRESHOLD = CFG.getDouble("word2vec.threshold");

    // corpus
    CORPUS_FOLDER = CFG.getString("corpus.folder");

    // SOLR
    SOLR_CORE_INDEX = CFG.getString("solr.indexCore");
    SOLR_CORE_SURFACEFORMS = CFG.getString("solr.surfaceformsCore");
    SOLR_TIMEOUT = CFG.getInt("solr.timeout");
    SOLR_URL = CFG.getString("solr.url");
    SOLR_THREADS = CFG.getInt("solr.threads");
    SOLR_QUEUE = CFG.getInt("solr.queue");
    SOLR_ROWS = CFG.getInt("solr.rows");
  }

  public static final boolean NER_USE = true;
  // folders
  public static final String DBPEDIA_SURFACEFORMS_FILE = "_surface_forms.tsv";

  // public static final String SERIALISATION_SENTENCE_CANDIDATES = "sentenceCanidates.data";

  //
  public static final boolean useSurfaceforms = true;

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
  public static int getInt(final XMLConfiguration CFG, final String key) {
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
