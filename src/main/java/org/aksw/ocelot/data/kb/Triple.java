package org.aksw.ocelot.data.kb;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.aksw.ocelot.core.pipeline.Candidate;

/**
 * Data class holds URIs and surfaceforms for a triple.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Triple implements Serializable {

  private static final long serialVersionUID = -8386824328230564077L;

  protected String s = "";
  protected String o = "";
  protected String p = "";

  protected Set<String> subjectSFs = new HashSet<>();
  protected Set<String> objectSFs = new HashSet<>();

  public Set<Candidate> candidate = new HashSet<>();

  /**
   * @param s uri
   * @param predicate uri
   * @param o uri
   */
  public Triple(final String s, final String p, final String o) {
    this.s = s;
    this.p = p;
    this.o = o;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Triple [s=" + s + ", predicate=" + p + ", o=" + o + ", subjectSFs=" + subjectSFs
        + ", objectSFs=" + objectSFs + ", candidate=" + candidate + "]";
  }

  /**
   * @return the s
   */
  public String getS() {
    return s;
  }

  /**
   * @return the predicate
   */
  public String getP() {
    return p;
  }

  /**
   * @return the o
   */
  public String getO() {
    return o;
  }

  /**
   * @return the subjectSFs
   */
  public Set<String> getSubjectSFs() {
    return subjectSFs;
  }

  /**
   * @return the objectSFs
   */
  public Set<String> getObjectSFs() {
    return objectSFs;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((o == null) ? 0 : o.hashCode());
    result = (prime * result) + ((p == null) ? 0 : p.hashCode());
    result = (prime * result) + ((s == null) ? 0 : s.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Triple other = (Triple) obj;
    if (o == null) {
      if (other.o != null) {
        return false;
      }
    } else if (!o.equals(other.o)) {
      return false;
    }
    // if (objectSFs == null) {
    // if (other.objectSFs != null) {
    // return false;
    // }
    // } else if (!objectSFs.equals(other.objectSFs)) {
    // return false;
    // }
    if (p == null) {
      if (other.p != null) {
        return false;
      }
    } else if (!p.equals(other.p)) {
      return false;
    }
    if (s == null) {
      if (other.s != null) {
        return false;
      }
    } else if (!s.equals(other.s)) {
      return false;
    }
    /*
     * if (subjectSFs == null) { if (other.subjectSFs != null) { return false; } } else if
     * (!subjectSFs.equals(other.subjectSFs)) { return false; }
     */
    return true;
  }
  // ----

  /**
   * Gets all surfaceforms of all triples.
   *
   * @return set of all sfs
   */
  public static Set<String> getAllSurfaceforms(final Set<Triple> tripleSet) {
    final Set<String> all = new HashSet<>();
    for (final Triple triple : tripleSet) {
      all.addAll(triple.getSubjectSFs());
      all.addAll(triple.getObjectSFs());
    }
    return all;
  }
}
