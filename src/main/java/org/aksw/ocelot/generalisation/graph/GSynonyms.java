package org.aksw.ocelot.generalisation.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.aksw.ocelot.common.lang.MapUtil;
import org.aksw.ocelot.core.measure.MeasurementExperiment;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * TODO: find for each predicate the link to wikidata. Retrieve all the alias labels and use them as
 * seeds
 *
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class GSynonyms {
  protected final static Logger LOG = LogManager.getLogger(GSynonyms.class);

  MeasurementExperiment measurement = new MeasurementExperiment();

  public static void main(final String[] a) {
    final GSynonyms synonyms = new GSynonyms();

    final Map<String, Set<String>> synonymsResults = synonyms.findSeeds(10);

    LOG.info(synonymsResults);

  }

  /**
   *
   * Finds predicateToSeedlabel (top scored root) to be used per predicate. Searches in the other
   * predicates values to find the same label and compares the scores, in case the other score is
   * better, set the current score to -1, to ignore the root for the current predicate.
   *
   * @param seedsLimit how many seed to be returned
   * @return map with predicate to seed labels.
   */
  protected Map<String, Set<String>> findSeeds(final int seedsLimit) {
    return _findSeeds(seedsLimit, 1);
  }

  /**
   * Finds predicateToSeedlabel (top scored root) to be used per predicate. Searches in the other
   * predicates values to find the same label and compares the scores, in case the other score is
   * better, set the current score to -1, to ignore the root for the current predicate.
   *
   * @param seedsLimit how many seed to be returned
   * @param maxroots how many top roots are considered
   * @return map with predicate to seed labels.
   */
  protected Map<String, Set<String>> _findSeeds(final int seedsLimit, final int maxroots) {

    // return predicateToSeedlabel to be used per predicate
    final Map<String, Set<String>> seeds = new HashMap<>();

    // top n scored roots per predicate
    final Map<String, Map<String, Double>> pToRootsScores = measurement.scores(maxroots);

    // for each p and all scored roots
    for (final Entry<String, Map<String, Double>> outer : pToRootsScores.entrySet()) {
      final String p = outer.getKey();
      final Map<String, Double> scoredRoots = outer.getValue();

      seeds.put(p, new HashSet<>());
      final AtomicInteger top = new AtomicInteger(seedsLimit);

      // loop over sorted scored roots
      for (final Entry<String, Double> e : MapUtil.reverseSortByValue(scoredRoots).entrySet()) {
        if (top.get() < 1) {
          break;
        }

        // searches in the other predicates values to find the same label and compares the scores
        // in case the other score is better, set the current score to -1.
        final String rootOuter = e.getKey();
        Double scoreOuter = e.getValue();
        for (final Entry<String, Map<String, Double>> inner : pToRootsScores.entrySet()) {
          if (!inner.getKey().equals(p)) {
            final Map<String, Double> rootsInner = inner.getValue();
            if (rootsInner.containsKey(rootOuter)) {
              if (rootsInner.get(rootOuter) > scoreOuter) {
                scoreOuter = -1D;
              }
            }
          }
        } // end

        if (scoreOuter > 0) {
          // the current label has the best score
          top.decrementAndGet();
          seeds.get(p).add(rootOuter);
        }
      }
    } // end outer
    return seeds;
  }
}
