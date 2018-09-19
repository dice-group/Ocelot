package org.aksw.ocelot.share;

/**
 * Enums for the solr wiki index. <br>
 * <br>
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public enum EnumSolrWikiIndex {

  DOCNR("doc"),

  SENTENCENR("sentenceNr"),

  SECTION("section"),

  ID("id"),

  SENTENCE("sentence"),

  SENTENCE_SIZE("sentenceSize"),

  TOKEN("token"),

  NER("ner"),

  TOKENNER("tokenNer"),

  LEMMA("lemma"),

  POS("pos"),

  INDEX("index");

  private String name;

  private EnumSolrWikiIndex(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
