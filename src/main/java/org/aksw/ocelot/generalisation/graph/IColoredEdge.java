package org.aksw.ocelot.generalisation.graph;

public interface IColoredEdge {

  public String getColor();

  public String getLabel();

  public IColoredEdge setEdge(final String color, final String label);

  public IColoredEdge setEdge(IColoredEdge edge);
}
