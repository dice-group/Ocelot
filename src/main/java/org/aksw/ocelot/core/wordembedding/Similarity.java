package org.aksw.ocelot.core.wordembedding;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface Similarity {

  /**
   * Similarity of vectors.
   *
   * @param vectorA
   * @param vectorB
   * @return similarity
   */
  public double similarity(final float[] vectorA, final float[] vectorB);

}
