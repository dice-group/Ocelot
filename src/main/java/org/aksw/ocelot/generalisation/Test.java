package org.aksw.ocelot.generalisation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.core.pipeline.Drift;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.share.CandidateTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Test {
  final static Logger LOG = LogManager.getLogger(Test.class);

  public static void main(final String[] a) {

    final String predicate = "http://dbpedia.org/ontology/spouse";

    final Map<Triple, Set<Map<CandidateTypes, Object>>> map = Drift.readResults(predicate);

    for (final Entry<Triple, Set<Map<CandidateTypes, Object>>> entry : map.entrySet()) {

      final Triple t = entry.getKey();
      final Set<Map<CandidateTypes, Object>> set = entry.getValue();

      if (set.size() > 1) {
        LOG.info(t);
        LOG.info(set);

        // System.exit(0);
      }
    }
  }
}
