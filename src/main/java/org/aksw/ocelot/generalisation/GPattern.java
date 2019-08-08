package org.aksw.ocelot.generalisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.IColoredEdge;
import org.aksw.ocelot.generalisation.graph.INode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode;
import org.aksw.ocelot.generalisation.graph.RootNode;
import org.aksw.simba.knowledgeextraction.commons.lang.UniqueQueue;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Runs the generalization process.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
// TODO:Rename
public class GPattern {

  final static Logger LOG = LogManager.getLogger(GPattern.class);

  /* generalizes nodes */
  NodeGeneralization generalize = new NodeGeneralization();

  /**
   * Generalizes the trees as long as its possible.
   *
   * @param list
   */
  public List<LGGStore> run(final List<ColoredDirectedGraph> list) {

    final int setSize = new HashSet<>(list).size();
    if (setSize != list.size()) {
      LOG.info("The parameter contains duplicate trees.");
    }

    // add trees to store
    List<LGGStore> store = removeDuplicates(toStore(list));

    // runs as long as the size of the store changes
    int size = Integer.MAX_VALUE;
    do {
      size = store.size();
      LOG.info("store : " + size);

      // generalization
      store = general(store);

    } while (size > store.size());
    LOG.info("final store : " + store.size());

    // count results
    int total = 0;

    //
    for (final LGGStore s : store) {
      total += s.getMiddle().size();
      // total += s.getRight().size();
      if (s.getMiddle().isEmpty()) {
        total++;
      }
    }
    LOG.info("Should be the same");
    LOG.info(setSize + " /" + total);

    return store;
  }

  private List<LGGStore> general(final List<LGGStore> trees) {
    final List<LGGStore> general = new ArrayList<>();

    while (trees.size() > 0) {
      final LGGStore treeA = trees.remove(0);

      LGGStore bestmatch = null;
      int bestindex = -1;
      for (int i = 0; i < trees.size(); i++) {
        final LGGStore treeB = trees.get(i);

        final ColoredDirectedGraph lgg = lgg(treeA.getLeft(), treeB.getLeft());
        if (!validGeneralisation(lgg)) {
          LOG.trace("Not a valid generalisation, missing domain or range type node.");
        } else {
          if (lgg.vertexSet().size() > 0) {

            if (bestmatch == null
                || lgg.vertexSet().size() > bestmatch.getLeft().vertexSet().size()) {

              final boolean originalEmptyA = treeA.getMiddle().isEmpty();
              final boolean originalEmptyB = treeB.getMiddle().isEmpty();

              bestmatch = new LGGStore(lgg);
              bestindex = i;

              if (originalEmptyA) {
                bestmatch.getMiddle().add(treeA.getLeft());
              } else {
                bestmatch.getMiddle().addAll(treeA.getMiddle());
              }

              if (originalEmptyB) {
                bestmatch.getMiddle().add(treeB.getLeft());
              } else {
                bestmatch.getMiddle().addAll(treeB.getMiddle());
              }
            }
          }
        }
      } // end inner for

      if (bestmatch != null) {
        general.add(bestmatch);
        trees.remove(bestindex);
      } else {
        general.add(treeA);
      }
    } // while
    return general;
  }

  /**
   * Puts all generalized graphs in a set and ignores duplicates. At the moment the middle and right
   * side if the store is will be lose.
   *
   * @param stores
   * @return stores with no duplicates.
   */
  protected List<LGGStore> removeDuplicates(final List<LGGStore> stores) {
    final Set<ColoredDirectedGraph> set = new HashSet<>();
    final List<LGGStore> l = new ArrayList<>();
    stores.forEach(store -> {
      if (set.add(store.getLeft())) {
        l.add(store);
      }
    });
    return l;
  }

  // List<ColoredDirectedGraph> to List<LGGStore>
  protected List<LGGStore> toStore(final List<ColoredDirectedGraph> list) {
    final List<LGGStore> store = new ArrayList<>();
    list.forEach(tree -> {
      store.add(new LGGStore(tree, new ArrayList<>()));
    });
    return store;
  }

  /**
   * 1. compare roots <br>
   * 2. get all outgoing edges of root in graph A <br>
   * 2. add targets to unseen node list <br>
   * 2. start with graph a <br>
   * 3. get a
   *
   * @param a
   * @param b
   * @return
   */
  public ColoredDirectedGraph lgg(final ColoredDirectedGraph a, final ColoredDirectedGraph b) {

    final ColoredDirectedGraph lgg = new ColoredDirectedGraph();

    // maps lgg nodes to the graph a and b
    final Map<INode, INode> lggToA = new HashMap<>();
    final Map<INode, INode> lggToB = new HashMap<>();

    // roots
    final RootNode rootA = a.getRoot();
    final RootNode rootB = b.getRoot();

    if (rootA != null && rootB != null && rootA.getLabel().equals(rootB.getLabel())) {

      // root label a and b
      // final Set<String> allRootLabels = new HashSet<>();
      // final String split = ";";
      // allRootLabels.addAll(Arrays.asList(rootA.getLabel().split(split)));
      // allRootLabels.addAll(Arrays.asList(rootB.getLabel().split(split)));

      // new root
      // final String labelRootLgg = String.join(split, new ArrayList<>(allRootLabels));
      final RootNode rootLgg = new RootNode(rootA.getId(), rootA.getLabel());

      // add root
      if (lgg.addVertex(rootLgg)) {
        lggToA.put(rootLgg, rootA);
        lggToB.put(rootLgg, rootB);
      }
      final UniqueQueue<INode> whitelist = new UniqueQueue<>();
      whitelist.add(rootLgg);

      while (!whitelist.isEmpty()) {
        // new
        final INode sourceLgg = whitelist.remove();

        // mapped nodes of A and B
        final INode sourceA = lggToA.get(sourceLgg);
        final INode sourceB = lggToB.get(sourceLgg);

        // outgoing edges A
        for (final IColoredEdge outEdgeA : a.outgoingEdgesOf(sourceA)) {
          final INode targetA = a.getEdgeTarget(outEdgeA);
          // outgoing edges B
          for (final IColoredEdge outEdgeB : b.outgoingEdgesOf(sourceB)) {
            final INode targetB = b.getEdgeTarget(outEdgeB);

            // try to generalize nodes
            final INode targetLgg = generalize.generalizes(targetA, targetB);

            if (targetLgg != null) {
              lgg.addVertex(targetLgg);
              lgg.addEdge(sourceLgg, targetLgg);

              lggToA.put(targetLgg, targetA);
              lggToB.put(targetLgg, targetB);

              whitelist.add(targetLgg);

              // TODO: Update me, do not break here. Another node could be a general one!
              // put all in a candidate set and choose the best one
              break;
            }
          } // end inner loop
        } // end outer loop
      }
    }
    return lgg;
  }

  /**
   * Checks if domain and range node types are in the vertex set.
   *
   * @param g ColoredDirectedGraph instance
   * @return true if present
   */
  public boolean validGeneralisation(final ColoredDirectedGraph g) {
    boolean foundDomain = false;
    boolean foundRange = false;

    for (final INode node : g.vertexSet()) {
      if (node instanceof IndexedWordNode) {
        if (((IndexedWordNode) node).type.equals(IndexedWordNode.GType.DOMAIN)) {
          foundDomain = true;
        } else if (((IndexedWordNode) node).type.equals(IndexedWordNode.GType.RANGE)) {
          foundRange = true;
        }
      }
      if (foundDomain && foundRange) {
        break;
      }
    }
    return foundDomain && foundRange;
  }
}
