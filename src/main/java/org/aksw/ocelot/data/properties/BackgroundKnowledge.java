package org.aksw.ocelot.data.properties;

import java.util.Map;
import java.util.Set;

public interface BackgroundKnowledge {

  // Set<String> getPredicates(Path file);

  Set<String> getPredicates();

  Map<String, Set<String>> getTriples(final String predicate, final int max);

  Set<String> getLabels(final String uri);

}
