package org.aksw.ocelot.synonyms;

import java.util.List;

/**
 * ISynonyms interface.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface ISynonyms {

  /**
   * Returns a sorted (depending on the similarity function) list of synonyms.
   */
  public List<String> getSynonyms(String word);

  /**
   * Returns true, in case the synonym set of wordA and the synonym set of wordB contain at least
   * one same element.
   **/
  public boolean checkSynonyms(String wordA, String wordB);

}
