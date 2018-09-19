package org.aksw.ocelot.core.measure;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.core.pipeline.Drift;
import org.aksw.ocelot.data.kb.DBpediaKB;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.share.CandidateTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Stores infos about the triples and results.
 *
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Infos {
  static final Logger LOG = LogManager.getLogger(Infos.class);

  private Map<Triple, Set<Map<CandidateTypes, Object>>> data = null;

  // triples from KB
  int triplesSize = 0;

  // triples in the final results
  int triplesUsed = 0;

  // sentences containing NLR
  int sentencesUsed = 0;

  // used roots
  Map<String, Integer> roots = new HashMap<>();
  final DBpediaKB kb = new DBpediaKB();
  String predicate = null;

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {

    final Infos infos = new Infos(Drift.readAllResults());
    infos.init("http://dbpedia.org/ontology/significantBuilding");
    LOG.info(infos.toString());
  }

  /**
   *
   * Constructor initializes the data.
   *
   * @param predicate
   */
  public Infos(final Map<Triple, Set<Map<CandidateTypes, Object>>> data) {
    this.data = data;
  }

  /**
   *
   * @param predicate
   */
  public void init(final String predicate) {
    this.predicate = predicate;

    final Set<Triple> triples = kb.getTriples(predicate, Integer.MAX_VALUE);
    triplesSize = triples.size();

    final Map<Triple, Set<Map<CandidateTypes, Object>>> datap = new HashMap<>();
    datap.putAll(data);

    datap.keySet().retainAll(triples);
    triplesUsed = datap.keySet().size();

    /**
     * Prints all triples we don't have <code>
     {
       // all unused triples
       triples.removeAll(datap.keySet());
       // print them
       int i = 0;
       final int max = 1000;
       for (final Triple triple : triples) {
         LOG.info(triple.getS().replace("http://dbpedia.org/resource/", "") + "/"
             + triple.getO().replace("http://dbpedia.org/resource/", ""));
         if (i++ == max) {
           break;
         }
       }
     }</code>
     */
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Infos [triplesSize=" + triplesSize + ", triplesUsed=" + triplesUsed + ", sentencesUsed="
        + sentencesUsed + ", roots=" + roots + ", predicate=" + predicate + "]";
  }
}
