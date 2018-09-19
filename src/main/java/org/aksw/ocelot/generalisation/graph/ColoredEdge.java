package org.aksw.ocelot.generalisation.graph;

import org.jgrapht.graph.DefaultEdge;

public class ColoredEdge extends DefaultEdge implements IColoredEdge {

  private static final long serialVersionUID = -3403322707754719490L;

  private String color = "";
  private String label = "";

  /**
   * 
   * Constructor.
   *
   */
  public ColoredEdge() {
    // don't remove me, used by ClassBasedEdgeFactory
  }

  /**
   * 
   * Constructor.
   *
   * @param edge
   */
  public ColoredEdge(final IColoredEdge edge) {
    this(edge.getColor(), edge.getLabel());
  }

  /**
   * 
   * Constructor.
   *
   * @param color
   * @param label
   */
  public ColoredEdge(final String color, final String label) {
    this.color = color;
    this.label = label;
  }

  @Override
  public String getColor() {
    return color;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public IColoredEdge setEdge(final String color, final String label) {
    this.color = color;
    this.label = label;
    return this;
  }

  @Override
  public IColoredEdge setEdge(final IColoredEdge edge) {
    return setEdge(edge.getColor(), edge.getLabel());
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((color == null) ? 0 : color.hashCode());
    result = (prime * result) + ((label == null) ? 0 : label.hashCode());
    result = (prime * result) + ((getSource() == null) ? 0 : getSource().hashCode());
    result = (prime * result) + ((getTarget() == null) ? 0 : getTarget().hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ColoredEdge)) {
      return false;
    }
    final ColoredEdge other = (ColoredEdge) obj;
    if (color == null) {
      if (other.color != null) {
        return false;
      }
    } else if (!color.equals(other.color)) {
      return false;
    }
    if (label == null) {
      if (other.label != null) {
        return false;
      }
    } else if (!label.equals(other.label)) {
      return false;
    }
    if (getSource() == null) {
      if (other.getSource() != null) {
        return false;
      }
    } else if (!getSource().equals(other.getSource())) {
      return false;
    }
    if (getTarget() == null) {
      if (other.getTarget() != null) {
        return false;
      }
    } else if (!getTarget().equals(other.getTarget())) {
      return false;
    }
    return true;
  }
}
