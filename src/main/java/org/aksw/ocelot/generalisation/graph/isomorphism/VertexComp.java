package org.aksw.ocelot.generalisation.graph.isomorphism;

import java.util.Comparator;

import org.aksw.ocelot.generalisation.graph.GrammaticalRelationNode;
import org.aksw.ocelot.generalisation.graph.INode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode.GType;
import org.aksw.ocelot.generalisation.graph.RootNode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class VertexComp implements Comparator<INode> {
  protected final static Logger LOG = LogManager.getLogger(VertexComp.class);

  String domain = "";
  String range = "";

  public VertexComp(final String domain, final String range) {
    this.domain = domain;
    this.range = range;
  }

  @Override
  public int compare(final INode o, final INode oo) {

    int compare = -1;
    if ((o instanceof RootNode) && (oo instanceof RootNode)) {
      compare = compare((RootNode) o, (RootNode) oo);

    } else if ((o instanceof IndexedWordNode) && (oo instanceof IndexedWordNode)) {
      compare = compare((IndexedWordNode) o, (IndexedWordNode) oo);

    } else if ((o instanceof GrammaticalRelationNode) && (oo instanceof GrammaticalRelationNode)) {
      compare = compare((GrammaticalRelationNode) o, (GrammaticalRelationNode) oo);
    }
    LOG.trace("compare: " + compare);
    return compare;
  }

  protected int compare(final RootNode o, final RootNode oo) {
    LOG.trace("compare RootNode: " + o.getLabel() + " -> " + oo.getLabel());
    return o.getLabel().compareTo(oo.getLabel());
  }

  /**
   * Compares a default tree with a generalized.
   *
   * @param treeNode
   * @param generalTreeNode
   * @return
   */
  protected int compare(final IndexedWordNode treeNode, final IndexedWordNode generalTreeNode) {

    LOG.trace("compare IndexedWordNode..." + treeNode.type + " " + treeNode.getLabel() + " "
        + generalTreeNode.type + " " + generalTreeNode.getLabel());

    final GType treeType = treeNode.type;
    final GType generalType = generalTreeNode.type;

    if (generalType.equals(GType.ORIGINAL)) {
      if (treeType.equals(GType.ORIGINAL)) {
        return treeNode.getLabel().compareTo(generalTreeNode.getLabel());
      } else {
        return -1;
      }
    }
    /**
     * <code>
        if (generalType.equals(GType.DOMAIN)) {
          if (treeType.equals(GType.NER)) {
            if (domain.equals(treeNode.ner)) {
              return 0;
            }
          }
        }

        if (generalType.equals(GType.RANGE)) {
          if (treeType.equals(GType.NER)) {
            if (range.equals(treeNode.ner)) {
              return 0;
            }
          }
        }
    </code>
     */
    if (generalType.equals(GType.DOMAIN)) {
      if (treeType.equals(GType.DOMAIN)) {
        return 0;
      }
    }

    if (generalType.equals(GType.RANGE)) {
      if (treeType.equals(GType.RANGE)) {
        return 0;
      }
    }
    if (generalType.equals(GType.LEMMA)) {
      if (treeType.equals(GType.ORIGINAL)) {
        if (generalTreeNode.lemma.equals(treeNode.lemma)) {
          return 0;
        }
      }
    }

    if (generalType.equals(GType.POS)) {
      if (treeType.equals(GType.ORIGINAL)) {
        if (generalTreeNode.pos.equals(treeNode.pos)) {
          return 0;
        }
      }
    }

    if (generalType.equals(GType.NER)) {
      if (treeType.equals(GType.NER)) {
        if (generalTreeNode.ner.equals(treeNode.ner)) {
          return 0;
        }
      }
    }
    return -1;
  }

  /**
   * Compares GrammaticalRelationNode objects.
   *
   * @param treeNode
   * @param generalTreeNode
   * @return
   */
  protected int compare(final GrammaticalRelationNode o, final GrammaticalRelationNode oo) {
    final int i = o.getLabel().compareTo(oo.getLabel());
    LOG.trace("GrammaticalRelationNode compare " + i);
    return i;
  }
}
