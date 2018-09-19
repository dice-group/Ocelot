package org.aksw.ocelot.core.measure.nlr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.aksw.ocelot.core.indexsearch.CorpusElement;
import org.aksw.ocelot.core.indexsearch.ICorpus;
import org.aksw.ocelot.core.indexsearch.WikipediaCorpus;
import org.aksw.ocelot.core.measure.Cleansing;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.share.CandidateTypes;

import edu.stanford.nlp.ling.IndexedWord;

public class TestNLR implements INLR {
  protected ICorpus wiki = new WikipediaCorpus();

  @Override
  public String getNLR(final Map<CandidateTypes, Object> result) {

    final String sentenceId = ((String) result.get(CandidateTypes.ID));

    final List<Integer> indices = Cleansing.getIndicesList(result);
    final IndexedWord root = Cleansing.getRoot(result);

    final CorpusElement doc = wiki.getSolrDocument(sentenceId);
    List<String> token = new ArrayList<>(doc.getToken());
    final List<String> pos = new ArrayList<>(doc.getPOS());

    // find nlr
    final int rootIndex = root.index() - 1;
    int nlrBegin = indices.get(0);
    int nlrEnd = indices.get(3);

    boolean rootOutside = false;
    if (rootIndex < nlrBegin) {
      nlrBegin = rootIndex;
      rootOutside = true;
    }

    if (rootIndex > nlrEnd) {
      nlrEnd = rootIndex + 1;
      rootOutside = true;
    }

    // replace subject tokens with one placeholder
    for (int l = indices.get(1) - indices.get(0); l > -1; l--) {
      token.remove(indices.get(0) + l);
      token.add(indices.get(0) + l, Const.RELATION_DOMAIN_PLACEHOLDER);
    }

    // replace objects tokens with one placeholder
    for (int l = indices.get(3) - indices.get(2); l > -1; l--) {
      token.remove(indices.get(2) + l);
      token.add(indices.get(2) + l, Const.RELATION_RANGE_PLACEHOLDER);
    }

    token.remove(rootIndex);
    token.add(rootIndex, "?".concat(root.lemma()));

    final List<String> tokenTmp = new ArrayList<>();
    tokenTmp.addAll(token);

    token = token.subList(nlrBegin, nlrEnd + 1);

    List<String> sub = null;
    if (rootOutside) {
      sub = (pos.subList(indices.get(1) + 1, indices.get(2)));
      // TODO: check words between s and o

      if (sub.size() == 1) {
        if (sub.contains(",")) {
          sub = Arrays.asList("DELETE");
        } else if (sub.contains("CC")) {
          sub = Arrays.asList("GOOD");
        }
      } else if (sub.size() > 1) {
        final String tmpsub = String.join(" ", sub);
        if (tmpsub.contains("NN IN")) {
          sub = Arrays.asList("UPDATE ROOT");
        }
      }

    }

    // remove leading and ending placeholder
    String t = String.join(" ", token).concat(" ")//
        .replaceAll("(\\".concat(Const.RELATION_DOMAIN_PLACEHOLDER).concat(" )+"),
            Const.RELATION_DOMAIN_PLACEHOLDER.concat(" "))//
        .replaceAll("(\\".concat(Const.RELATION_RANGE_PLACEHOLDER).concat(" )+"),
            Const.RELATION_RANGE_PLACEHOLDER.concat(" "));

    if (sub != null) {
      t = t + "// " + String.join(" ", sub) + " //";
    }
    return t.trim();
  }

}
