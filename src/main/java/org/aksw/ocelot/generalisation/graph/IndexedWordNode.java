package org.aksw.ocelot.generalisation.graph;

import edu.stanford.nlp.ling.IndexedWord;

/**
 * A node holding the data with the current label {@link SimpleNode#label} and type {@link #type}.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class IndexedWordNode extends SimpleNode {

  private static final long serialVersionUID = -2348817209187400896L;

  public static enum GType {
    POS, NER, LEMMA, ORIGINAL, DOMAIN, RANGE;
  }

  public GType type = GType.ORIGINAL;
  public String lemma, pos, originalText, ner;

  /**
   *
   * Constructor.
   *
   * @param node
   *
   *
   */
  public IndexedWordNode(final IndexedWordNode node, final GType type) {
    this(node.getId(), node.getLabel(), node.lemma, node.pos, node.originalText, node.ner, type);
    this.type = type;
  }

  /**
   *
   * Parses a Stanford node to an Ocelot node.
   *
   * @param id
   * @param label
   * @param indexedWord
   */
  public IndexedWordNode(final String id, final String label, final IndexedWord indexedWord,
      final GType type) {
    this(id, label, indexedWord.lemma(), indexedWord.tag(), indexedWord.originalText(),
        indexedWord.ner(), type);
  }

  /**
   *
   * Constructor.
   *
   * @param id
   * @param label
   * @param lemma
   * @param pos
   * @param originalText
   * @param ner
   */
  public IndexedWordNode(final String id, final String label, final String lemma, final String pos,
      final String originalText, final String ner, final GType type) {

    super(id, label);

    this.lemma = lemma;
    this.pos = pos;
    this.originalText = originalText;
    this.ner = ner;
    this.type = type;
  }

  @Override
  public String toString() {
    final String rtn = super.toString();

    return rtn.concat("[ IndexedWordNode: ").concat(type.toString()).concat("(")//
        .concat(" original:").concat(originalText)//
        .concat(" lemma:").concat(lemma)//
        .concat(" pos:").concat(pos)//
        .concat(" ner:").concat(ner)//
        .concat(")").concat(" ]");
  }
}
