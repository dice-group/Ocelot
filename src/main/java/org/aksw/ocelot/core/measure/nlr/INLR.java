package org.aksw.ocelot.core.measure.nlr;

import java.util.Map;

import org.aksw.ocelot.share.CandidateTypes;

public interface INLR {

  /**
   * Gets a NLR from a result set.
   *
   * @param result
   * @return
   */
  public String getNLR(final Map<CandidateTypes, Object> result);
}
