package org.aksw.ocelot.core.indexsearch;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface ICorpus {
  /**
   * Search in for sentences containing the surfaceform sf and the domain and range types.
   *
   * @param sf surfaceform
   * @param domain NER type in sentence
   * @param range NER type in sentence
   * @return sentence ids
   */
  public Set<String> sentenceIDs(final String sf, String domain, String range);

  /**
   * Search in for sentences containing the surfaceforms in sfs.
   *
   * @param sfs surfaceforms
   * @return a map with the surfaceform as key and sentence ids as value
   */
  public Map<String, Set<String>> sentenceIDs(final Set<String> sfs, String domain, String range);

  /**
   * Gets a sentence to the given id.
   *
   * @param docid id
   * @return sentence
   */
  public String getSentence(String id);

  public CorpusElement getSolrDocument(final String id);
}
