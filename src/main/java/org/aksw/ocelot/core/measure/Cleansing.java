package org.aksw.ocelot.core.measure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.Pair;
import org.aksw.ocelot.core.indexsearch.CorpusElement;
import org.aksw.ocelot.core.indexsearch.ICorpus;
import org.aksw.ocelot.core.indexsearch.WikipediaCorpus;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.share.CandidateTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;

public class Cleansing {
  protected final static Logger LOG = LogManager.getLogger(Cleansing.class);

  protected ICorpus wiki = new WikipediaCorpus();

  @Deprecated
  // examples :
  // ?D , ?R
  //
  //
  // check if there is a more than stopwods between sub and obj
  public void updateRoot(final String p,
      final Map<Triple, Set<Map<CandidateTypes, Object>>> tripleToCandidates) {

    // all triples
    for (final Iterator<Triple> i = tripleToCandidates.keySet().iterator(); i.hasNext();) {

      final Triple triple = i.next();
      final Set<Map<CandidateTypes, Object>> candidatesSet = tripleToCandidates.get(triple);

      // with p
      if (triple.getP().equals(p) || (p == null)) {
        for (final Iterator<Map<CandidateTypes, Object>> ii = candidatesSet.iterator(); ii
            .hasNext();) {
          final Map<CandidateTypes, Object> candidate = ii.next();

          final String sentenceId = ((String) candidate.get(CandidateTypes.ID));
          final CorpusElement doc = wiki.getSolrDocument(sentenceId);
          final List<String> token = new ArrayList<>(doc.getToken());
          final IndexedWord root = Cleansing.getRoot(candidate);
          final List<Integer> indices = getIndicesList(candidate);

          final int rootIndex = root.index() - 1;
          final int nlrBegin = indices.get(0);
          final int nlrEnd = indices.get(3);
          if ((rootIndex < nlrBegin) || (rootIndex > nlrEnd)) {
            // root outside
            // check if there is something else than stopwords
            LOG.info(token.subList(indices.get(1), indices.get(2)));

          }
        }

      }
    }
  }

  /**
   * Removes surface forms that are in several resources.
   *
   * @param data
   */
  public static void cleanTriples(final Map<Triple, Set<Map<CandidateTypes, Object>>> data) {

    if ((data == null) || data.isEmpty()) {
      LOG.warn("No data given!");
      return;
    }

    // keys to list
    final List<Triple> list = Arrays.asList(data.keySet().toArray( //
        new Triple[data.keySet().size()]//
    ));

    final ListIterator<Triple> i = list.listIterator();
    while (i.hasNext()) {
      final Triple ti = i.next();
      final Set<String> si = ti.getSubjectSFs();
      final Set<String> oi = ti.getObjectSFs();

      final ListIterator<Triple> ii = list.listIterator(i.nextIndex());
      while (ii.hasNext()) {
        final Triple tii = ii.next();
        if (!tii.getS().equals(ti.getS())) {
          removeIntersection(si, tii.getSubjectSFs());
        }
        if (!tii.getO().equals(ti.getO())) {
          removeIntersection(oi, tii.getObjectSFs());
        }
      }
    }
  }

  /**
   * Removes the intersection of two sets a and b from the sets a and b.
   *
   * @param a set a
   * @param b set b
   * @return intersection, removed elements
   */
  private static Set<String> removeIntersection(final Set<String> a, final Set<String> b) {
    final Set<String> intersection = new HashSet<>(a);
    if (intersection.retainAll(b)) {
      a.removeAll(intersection);
      b.removeAll(intersection);
    }
    return intersection;
  }

  /**
   * Calls {@link #cleanTriples(Map) and {@link #cleanData(Map)}
   *
   * @param tripleToCandidates
   */
  public static void cleanAll(
      final Map<Triple, Set<Map<CandidateTypes, Object>>> tripleToCandidates) {

    cleanTriples(tripleToCandidates);
    cleanData(tripleToCandidates);
  }

  /**
   * Cleans data, so that each sentence has only one relation. Sentences with multiple relations
   * will be deleted. <br>
   * <br>
   *
   * @param tripleToCandidates all the data
   */
  public static void cleanData(
      final Map<Triple, Set<Map<CandidateTypes, Object>>> tripleToCandidates) {
    cleanData(null, tripleToCandidates);
  }

  /**
   * Cleans data, so that a triple has a sentence only once. Sometimes a triple has a sentence
   * multiple times because of the surface forms.
   *
   * @param p a predicate
   * @param tripleToCandidates all the data
   */
  public static void cleanData(//
      final String p, final Map<Triple, Set<Map<CandidateTypes, Object>>> tripleToCandidates) {

    if ((tripleToCandidates == null) || tripleToCandidates.isEmpty()) {
      LOG.warn("No data given!");
      return;
    } else {
      LOG.info("clean data");
    }

    // all triples
    for (final Iterator<Triple> i = tripleToCandidates.keySet().iterator(); i.hasNext();) {

      final Triple triple = i.next();
      final Set<Map<CandidateTypes, Object>> candidatesSet = tripleToCandidates.get(triple);

      // with p
      if (triple.getP().equals(p) || (p == null)) {

        // store sentence id to candidates
        final Map<String, Set<Map<CandidateTypes, Object>>> sentenceToCandidates = new HashMap<>();
        // all results
        for (final Iterator<Map<CandidateTypes, Object>> ii = candidatesSet.iterator(); ii
            .hasNext();) {
          final Map<CandidateTypes, Object> candidate = ii.next();

          final String sentenceId = ((String) candidate.get(CandidateTypes.ID));
          if (sentenceToCandidates.get(sentenceId) == null) {
            sentenceToCandidates.put(sentenceId, new HashSet<>());
          }
          sentenceToCandidates.get(sentenceId).add(candidate);
        } // end candidatesSet

        // cleaning results for a triple and the candidate sentences
        for (final Iterator<String> id = sentenceToCandidates.keySet().iterator(); id.hasNext();) {
          final Set<Map<CandidateTypes, Object>> candidates = sentenceToCandidates.get(id.next());

          if (candidates.size() > 1) {
            // subject and object indices of each candidate
            final Set<List<Integer>> indicesSet = new HashSet<>();

            // removes duplicate candidates
            for (final Iterator<Map<CandidateTypes, Object>> iter = candidates.iterator(); iter
                .hasNext();) {
              final Map<CandidateTypes, Object> c = iter.next();

              if (!indicesSet.add(getIndicesList(c))) {
                candidatesSet.remove(c);
                iter.remove();
              }
            } // end candidates

            // FIXME:
            // sentence with multiple occurrence for sub or obj. for the moment we delete them
            if (candidates.size() > 1) {
              // LOG.info( "Same sentences for a triple with multiple indicies for object and
              // subject:");
              // candidates.forEach(c -> LOG.info(c.get(CandidateTypes.SENTENCE)));
              // LOG.info(triple);
              candidatesSet.removeAll(candidates);
            }
          }
        }
      } // not p
    } // triples
  }

  /**
   *
   * @param result
   * @return
   */
  public static IndexedWord getRoot(final Map<CandidateTypes, Object> result) {
    final IndexedWord root = (IndexedWord) result.get(CandidateTypes.ROOT);
    return root;
  }

  /**
   *
   * @param result
   * @return
   */
  public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getIndices(
      final Map<CandidateTypes, Object> result) {

    final Pair<Integer, Integer> domain =
        new Pair<>((((int) result.get(CandidateTypes.SUBJECT_INDEX_BEGIN))),
            (((int) result.get(CandidateTypes.SUBJECT_INDEX_END))));
    final Pair<Integer, Integer> range =
        new Pair<>((((int) result.get(CandidateTypes.OBJECT_INDEX_BEGIN))),
            (((int) result.get(CandidateTypes.OBJECT_INDEX_END))));
    return new Pair<>(domain, range);
  }

  /**
   *
   * @param result
   * @return
   */
  public static List<Integer> getIndicesList(final Map<CandidateTypes, Object> result) {

    final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> indices = getIndices(result);
    final List<Integer> indicesList = new ArrayList<>();
    indicesList.add(indices.getKey().getKey());
    indicesList.add(indices.getKey().getValue());
    indicesList.add(indices.getValue().getKey());
    indicesList.add(indices.getValue().getValue());
    return indicesList;
  }

  /**
   *
   * @param result
   * @return
   */
  @SuppressWarnings("unchecked")
  public static String getSP(final Map<CandidateTypes, Object> result) {
    return ((ArrayList<IndexedWord>) result.get(CandidateTypes.SP)).stream().map(IndexedWord::word)
        .collect(Collectors.joining(", "));
  }
}
