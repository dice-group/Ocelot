package org.aksw.ocelot.share;

/**
 * Enums for candidate types. <br>
 * <br>
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public enum CandidateTypes {

  ID, //

  SENTENCE, //

  SUBJECT_SF, OBJECT_SF, //

  SEM_GRAPH, SP, ROOT, //

  // indices of the surface forms
  SUBJECT_INDEX_BEGIN, SUBJECT_INDEX_END, //
  OBJECT_INDEX_BEGIN, OBJECT_INDEX_END;
}
