package org.aksw.ocelot.bin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.classify.PredicateSurfaceformsVec;
import org.aksw.ocelot.generalisation.GGeneralizeMain;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Generalize and stores the trees.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Generalize {

  final static Logger LOG = LogManager.getLogger(Generalize.class);

  public static void main(final String[] args) {

    final PredicateSurfaceformsVec predicateSurfaceformsVec = new PredicateSurfaceformsVec();
    final Map<String, Set<String>> labels = predicateSurfaceformsVec.getLabels();

    labels.entrySet().forEach(LOG::info);

    for (final String predicate : labels.keySet()) {
      final List<String> synonymes = new ArrayList<>(labels.get(predicate));
      run(predicate, synonymes);
    }
  }

  public static void run(final String p, final List<String> synonymes) {
    new GGeneralizeMain().createGeneralizedTrees(p, synonymes);
  }
}
