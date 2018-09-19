package org.aksw.ocelot.synonyms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Deprecated
public class SynonymsMain {

  protected final static Logger LOG = LogManager.getLogger(SynonymsMain.class);

  public static void main(final String[] args) {

    final String word = "ceo";

    // synonyms
    final ISynonyms synonyms = new Synonyms().setMax(10);
    final List<String> synonymsSet = synonyms.getSynonyms(word);
    LOG.info(synonymsSet);

    // synonyms of synonyms
    final Map<String, List<String>> map = new HashMap<>();
    synonymsSet.parallelStream()
        .forEach(synonym -> map.put(synonym, synonyms.getSynonyms(synonym)));

    map.entrySet().forEach(LOG::info);
  }
}
