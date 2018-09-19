package org.aksw.ocelot.application;

import java.util.Set;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IOcelot {

  /**
   * Extracts predicates in the sentences holding between the given domain and range type which
   * start and end by the given index.
   *
   * @param sentence
   * @param domain
   * @param range
   * @param domainBegin
   * @param domainEnd
   * @param rangeBegin
   * @param rangeEnd
   * @return set of predicate uris
   */
  public Set<String> run(String sentence, String domain, String range, //
      int domainBegin, final int domainEnd, final int rangeBegin, final int rangeEnd);

  /**
   * 
   * @return
   */
  public Set<String> getSupportedPredicates();
}
