package org.aksw.ocelot.core.measure.nlr;

import java.util.Map;

import org.aksw.ocelot.share.CandidateTypes;

import edu.stanford.nlp.ling.IndexedWord;

/**
 * Uses the lemma of the root node.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class RootNLR implements INLR {

  @Override
  public String getNLR(final Map<CandidateTypes, Object> result) {
    return ((IndexedWord) result.get(CandidateTypes.ROOT)).lemma();
  }
}
