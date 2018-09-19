package org.aksw.ocelot.generalisation;

import org.aksw.ocelot.generalisation.graph.GrammaticalRelationNode;
import org.aksw.ocelot.generalisation.graph.INode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode.GType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class NodeGeneralization {

  final static Logger LOG = LogManager.getLogger(NodeGeneralization.class);

  /**
   * Generalizes Nodes, calls {{@link #generaliseGrammaticalRelationNodes(INode, INode)} and
   * {@link #generalisesIndexedNodes(IndexedWordNode, IndexedWordNode)}}.
   *
   * @param targetA
   * @param targetB
   *
   * @return generalized node
   */
  public INode generalizes(final INode targetA, final INode targetB) {

    final boolean a = (targetA instanceof IndexedWordNode);
    final boolean b = (targetB instanceof IndexedWordNode);
    if (a && b) {
      return generalisesIndexedNodes((IndexedWordNode) targetA, (IndexedWordNode) targetB);
    }

    final boolean ag = (targetA instanceof GrammaticalRelationNode);
    final boolean bg = (targetB instanceof GrammaticalRelationNode);
    if (ag && bg) {
      return generaliseGrammaticalRelationNodes(targetA, targetB);
    }

    return null;
  }

  protected INode generaliseGrammaticalRelationNodes(final INode targetA, final INode targetB) {
    return (targetA.getLabel().equals(targetB.getLabel())) ? //
        new GrammaticalRelationNode(targetA.getId(), targetA.getLabel()) : null;
  }

  protected INode generalisesIndexedNodes(//
      final IndexedWordNode nodeA, final IndexedWordNode nodeB) {

    final IndexedWordNode nA = nodeA;
    final IndexedWordNode nB = nodeB;
    final GType tA = nA.type;
    final GType tB = nB.type;

    if (tA.equals(GType.DOMAIN) && tB.equals(GType.DOMAIN)) {
      return node(nA.getLabel(), nA, GType.DOMAIN);
    }
    if (tA.equals(GType.RANGE) && tB.equals(GType.RANGE)) {
      return node(nA.getLabel(), nA, GType.RANGE);
    }
    if (tA.equals(GType.NER) && tB.equals(GType.NER) && nA.ner.equals(nB.ner)) {
      return node(nA.ner, nA, GType.NER);
    }

    if (tA.equals(GType.ORIGINAL) && tB.equals(GType.ORIGINAL)) {
      return toOriginal(nA, nB);
    }

    if (tB.equals(GType.ORIGINAL) && tA.equals(GType.LEMMA)) {
      return toLemma(nB, nA);
    }
    if (tA.equals(GType.ORIGINAL) && tB.equals(GType.LEMMA)) {
      return toLemma(nA, nB);
    }
    if (tB.equals(GType.ORIGINAL) && tA.equals(GType.POS)) {
      toPOS(nB, nA);
    }
    if (tA.equals(GType.ORIGINAL) && tB.equals(GType.POS)) {
      toPOS(nA, nB);
    }

    if (tA.equals(GType.LEMMA) && tB.equals(GType.LEMMA)) {
      toLemma(nA, nB);
    }

    if ((tA.equals(GType.LEMMA) && tB.equals(GType.POS))
        || (tB.equals(GType.LEMMA) && tA.equals(GType.POS))
        || (tA.equals(GType.POS) && tB.equals(GType.POS))) {
      toPOS(nA, nB);
    }

    return null;
  }

  protected INode toOriginal(//
      final IndexedWordNode targetA, final IndexedWordNode targetB) {
    LOG.trace(targetA + " " + targetB);
    if (targetA.pos.equals(targetB.pos)) {
      if (targetA.getLabel().equals(targetB.getLabel())) {
        return node(targetA.getLabel(), targetA, GType.ORIGINAL);
      } else {
        return toLemma(targetA, targetB);
      }
    }
    return null;
  }

  protected INode toLemma(//
      final IndexedWordNode targetA, final IndexedWordNode targetB) {
    if (targetA.pos.equals(targetB.pos)) {
      if (targetA.lemma.equals(targetB.lemma)) {
        return node(targetA.lemma, targetA, GType.LEMMA);
      } else {
        return toPOS(targetA, targetB);
      }
    }
    return null;
  }

  protected INode toPOS(//
      final IndexedWordNode targetA, final IndexedWordNode targetB) {
    if (targetA.pos.equals(targetB.pos)) {
      return node(targetA.pos, targetA, GType.POS);
    }
    return null;
  }

  /**
   * Creates a new type depending node.
   *
   * @param label
   * @param targetA
   * @param type
   *
   * @return
   */
  private IndexedWordNode node(//
      final String label, final IndexedWordNode targetA, final GType type) {
    switch (type) {
      case ORIGINAL:
        return new IndexedWordNode(targetA.getId(), label, targetA.lemma, targetA.pos,
            targetA.originalText, targetA.ner, type);
      case LEMMA:
        return new IndexedWordNode(targetA.getId(), label, targetA.lemma, targetA.pos, "", "",
            type);
      case POS:
        return new IndexedWordNode(targetA.getId(), label, "", targetA.pos, "", "", type);
      case DOMAIN:
      case RANGE:
        return new IndexedWordNode(targetA.getId(), label, "", "", "", "", type);
      case NER:
        return new IndexedWordNode(targetA.getId(), label, "", "", "", targetA.ner, type);
      default:
        return null;
    }
  }
}
