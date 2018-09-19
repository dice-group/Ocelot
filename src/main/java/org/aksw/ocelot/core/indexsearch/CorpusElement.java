package org.aksw.ocelot.core.indexsearch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.ocelot.share.EnumSolrWikiIndex;
import org.apache.solr.common.SolrDocument;

/**
 * Holds a SolrDocument and makes it easy to get the data.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class CorpusElement {

  SolrDocument doc = null;

  /**
   *
   * Constructor.
   *
   * @param doc
   */
  public CorpusElement(final SolrDocument doc) {
    this.doc = doc;
  }

  public Map<EnumSolrWikiIndex, Object> toMap() {
    final Map<EnumSolrWikiIndex, Object> map = new HashMap<>();
    map.put(EnumSolrWikiIndex.SENTENCE, getSentence());
    map.put(EnumSolrWikiIndex.NER, getNER());
    map.put(EnumSolrWikiIndex.TOKEN, getToken());
    map.put(EnumSolrWikiIndex.LEMMA, getLemma());
    map.put(EnumSolrWikiIndex.POS, getPOS());
    map.put(EnumSolrWikiIndex.TOKENNER, getTokenNER());
    map.put(EnumSolrWikiIndex.INDEX, getIndex());
    return map;
  }

  public List<String> getPOS() {
    return Arrays.asList(((String) doc.getFieldValue(EnumSolrWikiIndex.POS.getName())).split(" "));
  }

  public List<Integer> getIndex() {
    return Arrays.asList(((String) doc.getFieldValue(EnumSolrWikiIndex.INDEX.getName())).split(" "))
        .stream().map(Integer::parseInt).collect(Collectors.toList());
  }

  public List<String> getTokenNER() {
    return Arrays
        .asList(((String) doc.getFieldValue(EnumSolrWikiIndex.TOKENNER.getName())).split(" "));
  }

  public List<String> getLemma() {
    return Arrays
        .asList(((String) doc.getFieldValue(EnumSolrWikiIndex.LEMMA.getName())).split(" "));
  }

  public List<String> getToken() {
    return Arrays
        .asList(((String) doc.getFieldValue(EnumSolrWikiIndex.TOKEN.getName())).split(" "));
  }

  public List<String> getNER() {
    return Arrays.asList(((String) doc.getFieldValue(EnumSolrWikiIndex.NER.getName())).split(" "));
  }

  public String getSentence() {
    return (String) doc.getFieldValue(EnumSolrWikiIndex.SENTENCE.getName());
  }
}
