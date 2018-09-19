package org.aksw.ocelot.core.pipeline;

public class Stats {

  int total_sentences = 0;
  int removed = 0;
  int max_token = 0;
  int max_punct = 0;

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Stats [total_sentences=" + total_sentences + ", removed=" + removed + ", max_token="
        + max_token + ", max_punct=" + max_punct + "]";
  }

}
