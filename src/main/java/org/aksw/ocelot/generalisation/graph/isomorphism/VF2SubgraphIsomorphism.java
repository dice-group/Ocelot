package org.aksw.ocelot.generalisation.graph.isomorphism;

import java.util.Comparator;
import java.util.Iterator;

import org.aksw.ocelot.generalisation.graph.IColoredEdge;
import org.aksw.ocelot.generalisation.graph.INode;
import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;

public class VF2SubgraphIsomorphism {

  private VF2SubgraphIsomorphismInspector<INode, IColoredEdge> vfs1 = null;

  /**
   *
   * Constructor.
   *
   * @param graph1
   * @param graph2
   * @param vertexComparator
   * @param edgeComparators
   */
  public VF2SubgraphIsomorphism(//
      final Graph<INode, IColoredEdge> graph1, final Graph<INode, IColoredEdge> graph2, //
      final Comparator<INode> vertexComparator, final Comparator<IColoredEdge> edgeComparators) {

    vfs1 = new VF2SubgraphIsomorphismInspector<>(graph1, graph2, vertexComparator, edgeComparators);
  }

  public Iterator<GraphMapping<INode, IColoredEdge>> getMappings() {
    return vfs1.getMappings();
  }

  public boolean isomorphismExists() {
    return vfs1.isomorphismExists();
  }

  public static VF2SubgraphIsomorphism getVF2SubgraphIsomorphism( //
      final Graph<INode, IColoredEdge> graph1, final Graph<INode, IColoredEdge> graph2, //
      final Comparator<INode> vertexComparator, final Comparator<IColoredEdge> edgeComparators) {
    return new VF2SubgraphIsomorphism(graph1, graph2, vertexComparator, edgeComparators);
  }

  public static boolean isomorphismExists(final Graph<INode, IColoredEdge> g,
      final Graph<INode, IColoredEdge> gg, final String domain, final String range) {
    return getVF2SubgraphIsomorphism(g, gg, new VertexComp(domain, range), new EdgeComp())
        .isomorphismExists();
  }
}
