package org.aksw.ocelot.core.measure.nlr;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.ocelot.share.CandidateTypes;

import edu.stanford.nlp.ling.IndexedWord;

public class ShortestPathNLR implements INLR {

  @SuppressWarnings("unchecked")
  @Override
  public String getNLR(final Map<CandidateTypes, Object> result) {
    return String.join(" ", ((ArrayList<IndexedWord>) result.get(CandidateTypes.SP)).stream()
        .map(IndexedWord::lemma).collect(Collectors.toList()));
  }
}
