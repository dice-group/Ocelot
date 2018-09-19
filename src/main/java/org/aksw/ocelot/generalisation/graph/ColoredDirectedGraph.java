package org.aksw.ocelot.generalisation.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultDirectedGraph;

public class ColoredDirectedGraph extends DefaultDirectedGraph<INode, IColoredEdge>
    implements IColoredDirectedGraph {

  private static final long serialVersionUID = -1804419775738533850L;
  final static Logger LOG = LogManager.getLogger(ColoredDirectedGraph.class);

  /**
   *
   * Constructor.
   *
   * @param edgeClass
   */
  public ColoredDirectedGraph(final Class<? extends IColoredEdge> edgeClass) {
    super(edgeClass);
  }

  /**
   *
   * Constructor.
   *
   */
  public ColoredDirectedGraph() {
    this(ColoredEdge.class);
  }

  public IColoredEdge addEdge(final INode sourceVertex, final INode targetVertex,
      final String color, final String label) {
    return addEdge(sourceVertex, targetVertex).setEdge(color, label);
  }

  @Override
  public Set<IColoredEdge> getEdges() {
    return super.edgeSet();
  }

  // TODO: rename to getRoots()
  @Override
  public Set<INode> getZeroIndegreeNodes() {
    final Set<INode> set = new HashSet<>();
    for (final INode n : vertexSet()) {
      if (inDegreeOf(n) == 0) {
        set.add(n);
      }
    }
    return set;
  }

  @Override
  public Set<INode> getZeroOutdegreeNodes() {
    final Set<INode> set = new HashSet<>();
    for (final INode n : vertexSet()) {
      if (outDegreeOf(n) == 0) {
        set.add(n);
      }
    }
    return set;
  }

  @Override
  public int hashCode() {
    int hash = vertexSet().hashCode();

    for (final IColoredEdge e : edgeSet()) {
      int part = e.hashCode();

      final int source = getEdgeSource(e).hashCode();
      final int target = getEdgeTarget(e).hashCode();

      // Pairing function
      final int pairing = (((source + target) * (source + target + 1)) / 2) + target;
      part = (27 * part) + pairing;

      final long weight = (long) getEdgeWeight(e);
      part = (27 * part) + (int) (weight ^ (weight >>> 32));

      hash += part;
    }

    return hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!ColoredDirectedGraph.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ColoredDirectedGraph other = (ColoredDirectedGraph) obj;
    if (vertexSet().size() != other.vertexSet().size()) {
      return false;
    }

    if (edgeSet().size() != other.edgeSet().size()) {
      return false;
    }

    for (final IColoredEdge thisEdge : edgeSet()) {

      final INode thisS = getEdgeSource(thisEdge);
      final INode thisT = getEdgeTarget(thisEdge);

      boolean foundEdge = false;
      for (final IColoredEdge otherEedge : other.edgeSet()) {
        final INode otherS = getEdgeSource(otherEedge);
        final INode otherT = getEdgeTarget(otherEedge);

        // conditions
        final boolean cA = thisS.getLabel().equals(otherS.getLabel());
        final boolean cB = thisT.getLabel().equals(otherT.getLabel());

        if (cA && cB) {
          foundEdge = true;
          break;
        }
      } // end inner
      if (!foundEdge) {
        return false;
      }
    } // end outer
    return true;
  }

  /**
   * Prints the graph labels
   *
   * @param cdg
   * @return
   */
  public String printPattern() {
    final Map<Integer, INode> nodeIndex = new HashMap<>();
    vertexSet().forEach(node -> {
      // filters grammar nodes
      if (StringUtils.isNumeric(node.getId())) {
        nodeIndex.put(Integer.valueOf(node.getId()), node);
      }
    });
    // sorted ids get labels
    final StringBuilder rtn = new StringBuilder();
    // for ech node
    nodeIndex.keySet().stream().sorted().forEach(id -> {

      final INode node = nodeIndex.get(id);

      if (node instanceof IndexedWordNode) {
        final IndexedWordNode n = (IndexedWordNode) node;
        if (n.type.toString().equals(IndexedWordNode.GType.POS.toString())) {
          rtn.append("[");
          rtn.append(nodeIndex.get(id).getLabel());//
          rtn.append("]");
        } else if (n.type.equals(IndexedWordNode.GType.LEMMA)) {
          rtn.append("{");
          rtn.append(nodeIndex.get(id).getLabel());//
          rtn.append("}");
        } else if (n.type.equals(IndexedWordNode.GType.NER)) {
          rtn.append("(");
          rtn.append(nodeIndex.get(id).getLabel());//
          rtn.append(")");
        } else {
          rtn.append(nodeIndex.get(id).getLabel());//
        }

      } else if (node instanceof RootNode) {
        rtn.append(nodeIndex.get(id).getLabel());//
      }
      rtn.append(" ");
    });
    return rtn.toString().trim();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Nodes:\n");
    vertexSet().forEach(v -> sb.append(v).append("\n"));
    sb.append("Edges:\n");
    edgeSet().forEach(e -> {
      sb.append(getEdgeSource(e)).append("->").append(getEdgeTarget(e));
      sb.append("\n");
    });
    return sb.toString();
  }

  /**
   * Gets the first node that has zero indegree nodes and is a RootNode instance.
   *
   * @param coloredDirectedGraph
   * @return INode object
   */
  public RootNode getRoot() {
    final RootNode node = _getRoot();
    if (node == null) {
      LOG.warn("No root found for: " + toString());
    }
    return node;
  }

  protected RootNode _getRoot() {
    RootNode node = null;
    final Iterator<INode> zeroIndegreeNodesIter;
    zeroIndegreeNodesIter = getZeroIndegreeNodes().iterator();
    while (zeroIndegreeNodesIter.hasNext()) {
      final INode n = zeroIndegreeNodesIter.next();
      if (n instanceof RootNode) {
        node = (RootNode) n;
        break;
      } else {
        LOG.trace("node instance of " + n.getClass().getSimpleName() + ": " + n.toString() //
            + " hash:" + hashCode());
      }
    }
    return node;
  }
}
