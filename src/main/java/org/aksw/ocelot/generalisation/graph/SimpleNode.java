package org.aksw.ocelot.generalisation.graph;

import java.io.Serializable;

/**
 * A simple node with an unique {@link #id} and a {@link #label}.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class SimpleNode implements INode, Serializable {
  // TODO: make SimpleNode abstract

  protected static final long serialVersionUID = 8590203007037980742L;

  protected String id;

  protected String label;

  /**
   *
   * Constructor.
   *
   * @param id
   * @param label
   */
  public SimpleNode(final String id, final String label) {
    this.id = id;
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public void setLabel(final String label) {
    this.label = label;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /**
   * Is equals if objects of class SimpleNode and id are equals.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleNode other = (SimpleNode) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Node [id=" + id + ", label=" + label + "]";
  }
}
