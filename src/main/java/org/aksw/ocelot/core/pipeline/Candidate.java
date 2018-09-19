package org.aksw.ocelot.core.pipeline;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Data class holds a set of Strings {@link #sentenceIDs} representing sentence ids of sentences
 * that containing a subject String {@link #subjectSF} and a object String {@link #objectSF} for the
 * surfaceforms.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Candidate implements Serializable {
  private static final long serialVersionUID = 4146712728710027781L;

  protected String subjectSF = "";
  protected String objectSF = "";
  protected Set<String> sentenceIDs = new HashSet<>();

  /**
   *
   * Constructor.
   *
   * @param subjectSF
   * @param objectSF
   * @param candidates
   */
  public Candidate(final String subjectSF, final String objectSF, final Set<String> candidates) {
    this.subjectSF = subjectSF;
    this.objectSF = objectSF;
    sentenceIDs = candidates;
  }

  /**
   * Subject surfacefrom.
   *
   * @return {@link #subjectSF}
   */
  public String getSubjectSF() {
    return subjectSF;
  }

  /**
   * Object surfaceform.
   *
   * @return {@link #objectSF}
   */
  public String getObjectSF() {
    return objectSF;
  }

  /**
   * All sentence ids of sentences containing the surfaceforms {@link #subjectSF} and
   * {@link #objectSF}.
   *
   * @return the sentenceIDs
   */
  public Set<String> getSentenceIDs() {
    return sentenceIDs;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Candidate [ subjectSF=" + subjectSF + ", objectSF=" + objectSF + ", sentenceIDs="
        + sentenceIDs + "]";
  }
}
