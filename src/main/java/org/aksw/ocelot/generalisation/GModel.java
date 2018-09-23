package org.aksw.ocelot.generalisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.util.Pair;
import org.aksw.ocelot.core.measure.Cleansing;
import org.aksw.ocelot.core.pipeline.Drift;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.share.CandidateTypes;
import org.aksw.simba.knowledgeextraction.commons.lang.StringUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * Reads and holds data.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class GModel {

  final static Logger LOG = LogManager.getLogger(GModel.class);

  // sentence to predicates
  private final Map<String, Set<String>> sentenceToPredicates = new HashMap<>();

  // sentence to candidates
  private final Map<String, Set<Map<CandidateTypes, Object>>> sentenceToCandidates =
      new HashMap<>();

  public GModel() {
    LOG.info("GModel ...");
    Drift.readAllResults();
    // initData(Drift.readAllResults());
  }

  public GModel(final String predicate) {
    LOG.info("GModel ...");
    Drift.readResults(predicate);
    // initData(Drift.readResults(predicate));
  }

  public GModel(final Set<String> predicates) {
    LOG.info("GModel ...");
    Drift.readResults(predicates);
    // initData(Drift.readResults(predicates));
  }

  /**
   * Reads data for a given predicate or reads all data at once and fills
   * {@link #sentenceToCandidates} and {@link #sentenceToPredicates} with it. <br>
   * <br>
   * Replaces domain and range with placeholders.
   */
  protected void initData(Map<Triple, Set<Map<CandidateTypes, Object>>> data) {

    if (data == null || data.isEmpty()) {
      LOG.warn("no data given!!");
      data = new HashMap<>();
    }

    LOG.info("Cleans all data ...");
    Cleansing.cleanAll(data);

    // store data in this class
    data.entrySet().forEach(entry -> entry.getValue().forEach(candidate -> {

      final String sentence = getSentence(candidate);

      if (!sentenceToCandidates.containsKey(sentence)) {
        sentenceToCandidates.put(sentence, new HashSet<>());
        sentenceToPredicates.put(sentence, new HashSet<>());
      }
      sentenceToPredicates.get(sentence).add(entry.getKey().getP());

      preprocessData(candidate);

      sentenceToCandidates.get(sentence).add(candidate);
    }));
  }

  public Map<Triple, Set<Map<CandidateTypes, Object>>> getData(final String predicate) {
    final Map<Triple, Set<Map<CandidateTypes, Object>>> map = new HashMap<>();

    Cleansing.cleanAll(Drift.data);

    for (final Entry<Triple, Set<Map<CandidateTypes, Object>>> entry : Drift.data.entrySet()) {
      if (entry.getKey().getP().equals(predicate)) {
        map.put(entry.getKey(), entry.getValue());
      }
    }
    return map;
  }

  public List<ColoredDirectedGraph> getGraphs(final String predicate, final List<String> synonyms) {
    final Map<Triple, Set<Map<CandidateTypes, Object>>> map = getData(predicate);

    final List<ColoredDirectedGraph> graphs = new ArrayList<>();
    for (final Entry<Triple, Set<Map<CandidateTypes, Object>>> entry : map.entrySet()) {
      for (final Map<CandidateTypes, Object> ee : entry.getValue()) {

        final String sentence = getSentence(ee);

        boolean synonymsFound = synonyms.isEmpty();
        for (final String label : synonyms) {
          synonymsFound = !StringUtil.indices(sentence, label).isEmpty();
          if (synonymsFound) {
            break;
          }
        }
        if (synonymsFound) {
          preprocessData(ee);
          final SemanticGraph sg = getSemanticGraph(ee);
          graphs.add(SemanticGraphToColoredDirectedGraph.semanticGraphToColoredDirectedGraph(sg));
        }
      }
    }
    return graphs;
  }

  /**
   * Gets all graphs from {@link #sentenceToCandidates} that are based on sentences for the given
   * predicate and the root of the graph is in synonyms. In case synonyms are empty, all will be
   * used.
   *
   * @param predicate
   * @param synonyms
   *
   * @return graphs
   */
  @Deprecated
  public List<ColoredDirectedGraph> getGraphsOLD(final String predicate,
      final List<String> synonyms) {

    final List<ColoredDirectedGraph> graphs = new ArrayList<>();

    for (final String sentence : sentenceToCandidates.keySet()) {
      if (sentenceToPredicates.get(sentence).contains(predicate)) {

        boolean found = false;
        for (final String label : synonyms) {
          found = !StringUtil.indices(sentence, label).isEmpty();
          if (found) {
            break;
          }
        }

        if (!found) {
          LOG.info("---");
          LOG.info("not found: " + sentence);
          LOG.info(synonyms);

        } else {

          final Set<Map<CandidateTypes, Object>> candidateset = sentenceToCandidates.get(sentence);

          // TODO:
          final Map<CandidateTypes, Object> candidate = candidateset.iterator().next();

          final SemanticGraph sg = getSemanticGraph(candidate);
          graphs.add(SemanticGraphToColoredDirectedGraph.semanticGraphToColoredDirectedGraph(sg));
        }
      }
    }
    return graphs;
  }

  /**
   * Replaces nodes with Domain and Range placeholder and removes compount.
   *
   * @param candidate
   */
  protected void preprocessData(final Map<CandidateTypes, Object> candidate) {

    final SemanticGraph sg = getSemanticGraph(candidate);
    final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> indices;
    indices = Cleansing.getIndices(candidate);

    SemanticGraphToColoredDirectedGraph.replaceDomainAndRange(indices, sg);
    SemanticGraphToColoredDirectedGraph.removeCompount(indices, sg);

    candidate.put(CandidateTypes.SEM_GRAPH, sg);
  }

  public SemanticGraph getSemanticGraph(final Map<CandidateTypes, Object> candidate) {
    return (SemanticGraph) candidate.get(CandidateTypes.SEM_GRAPH);
  }

  public IndexedWord getRoot(final Map<CandidateTypes, Object> candidate) {
    return (IndexedWord) candidate.get(CandidateTypes.ROOT);
  }

  public String getSentence(final Map<CandidateTypes, Object> candidate) {
    return (String) candidate.get(CandidateTypes.SENTENCE);
  }
}
