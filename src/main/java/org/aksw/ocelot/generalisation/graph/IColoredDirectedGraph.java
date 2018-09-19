package org.aksw.ocelot.generalisation.graph;

import java.util.Set;

public interface IColoredDirectedGraph {

  public Set<INode> vertexSet();

  public boolean addVertex(INode a);

  public Set<IColoredEdge> getEdges();

  public IColoredEdge getEdge(INode a, INode b);

  // TODO: rename to getRoots() ??
  public Set<INode> getZeroIndegreeNodes();

  // TODO: rename to getLeafes() ??
  public Set<INode> getZeroOutdegreeNodes();
}
