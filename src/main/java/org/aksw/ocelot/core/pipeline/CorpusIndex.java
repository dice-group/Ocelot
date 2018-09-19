package org.aksw.ocelot.core.pipeline;

import java.util.Comparator;

/**
 * Comparator for Array indices of the words in the corpus.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class CorpusIndex implements Comparable<CorpusIndex>, Comparator<CorpusIndex> {

  /** beginning of the fist word */
  private final int start;
  /** beginning of the last word */
  private final int end;

  /**
   *
   * Constructor.
   *
   * @param start
   * @param end
   */
  public CorpusIndex(final int start, final int end) {
    if (start > end) {
      throw new UnsupportedOperationException(
          "CorpusIndex parameter start has to be smaller than end.");
    }
    this.start = start;
    this.end = end;
  }

  /**
   * beginning of the fist word
   *
   * @return the start
   */
  public int getStart() {
    return start;
  }

  /**
   * beginning of the last word
   *
   * @return the end
   */
  public int getEnd() {
    return end;
  }

  @Override
  public int compareTo(final CorpusIndex o) {
    int dif = start - o.start;
    if (dif == 0) {
      dif = end - o.end;
    }
    return dif;
  }

  @Override
  public int compare(final CorpusIndex o, final CorpusIndex oo) {
    return o.compareTo(oo);
  }

  @Override
  public String toString() {
    return "CorpusIndex [start=" + start + ", end=" + end + "]";
  }
}
