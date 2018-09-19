package org.aksw.ocelot.core.measure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.common.lang.MapUtil;
import org.aksw.ocelot.core.measure.nlr.RootNLR;
import org.aksw.ocelot.core.pipeline.Drift;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.data.properties.PropertiesFactory;
import org.aksw.ocelot.share.CandidateTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class MeasurementExperiment {
  protected final static Logger LOG = LogManager.getLogger(MeasurementExperiment.class);

  private final Measurement measurement;

  private final Map<Triple, Set<Map<CandidateTypes, Object>>> data;
  private final Map<String, Double> specificity;
  private final Map<String, Map<String, Double>> supports;

  /**
   *
   * Constructor.
   *
   */
  public MeasurementExperiment() {
    data = Drift.readAllResults();
    Cleansing.cleanAll(data);

    measurement = new Measurement(new RootNLR(), data);

    supports = supports();
    specificity = specificity();
  }

  public Set<String> getallRoots() {
    return measurement.getallRoots();
  }

  /**
   * Gets the top n scores from {@link #scores()}.
   *
   * @param n limit
   * @return a map with relation to a map with n top nlr to scores.
   */
  public Map<String, Map<String, Double>> scores(final int n) {

    final Map<String, Map<String, Double>> scores = scores();

    final Map<String, Map<String, Double>> topN = new HashMap<>();

    // final Infos infos = new Infos(data);
    for (final String relation : supports.keySet()) {
      topN.put(relation, new HashMap<>());

      final Iterator<Entry<String, Double>> iter = MapUtil.reverseSortByValue(scores.get(relation))//
          .entrySet().stream().limit(n).iterator();
      while (iter.hasNext()) {
        final Entry<String, Double> e = iter.next();
        topN.get(relation).put(e.getKey(), e.getValue());
      }
    }
    return topN;
  }

  /**
   * Combines several scores to one. The score can be in [0,...,1].
   *
   * @return a map with relation to a map with nlr to scores.
   */
  public Map<String, Map<String, Double>> scores() {

    final Map<String, Map<String, Double>> combined = new HashMap<>();

    for (final String relation : supports.keySet()) {
      combined.put(relation, new HashMap<>());

      if (supports.get(relation).values().isEmpty()) {
        LOG.info("No support for: " + relation);
        continue;
      }

      final Double maxSu = Collections.max(supports.get(relation).values());
      // LOG.info("max su:" + maxSu);
      final Double maxSp = Collections.max(specificity.values());
      for (final String nlr : supports.get(relation).keySet()) {

        final Double su = supports.get(relation).get(nlr) / maxSu;
        final Double sp = specificity.get(nlr) / maxSp;

        // LOG.info("support: " + su);
        // LOG.info("specificity: " + sp);

        if ((su != null) && (sp != null)) {
          Double value = 0D;
          value = (su * sp);
          combined.get(relation).put(nlr, value);
        }
      }
    }
    return combined;
  }

  private Map<String, Double> specificity() {
    return measurement.specificity();
  }

  // predicate to support
  private Map<String, Map<String, Double>> supports() {
    final Map<String, Map<String, Double>> supports = new HashMap<>();
    for (final String pre : PropertiesFactory.getInstance().getPredicates()) {
      final Map<String, Double> support = measurement.support(pre);
      supports.put(pre, support);
    }
    return supports;
  }

  /**
   * For debug.
   * 
   * @param scores
   */
  public void print(final Map<String, Map<String, Double>> scores) {

    LOG.info("INFO");
    // print top
    final int top = 20;
    final Infos infos = new Infos(data);

    for (final String ps : supports.keySet()) {
      LOG.info("=====");
      infos.init(ps);
      LOG.info(infos);

      final Iterator<Entry<String, Double>> iter;
      iter = MapUtil.reverseSortByValue(scores.get(ps)).entrySet().stream().limit(top).iterator();

      while (iter.hasNext()) {
        final Entry<String, Double> e = iter.next();
        LOG.info(e);
      }
    }
  }
}
