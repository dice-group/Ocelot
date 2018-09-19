package org.aksw.ocelot.generalisation.graph.isomorphism;

import java.util.Comparator;

import org.aksw.ocelot.generalisation.graph.IColoredEdge;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class EdgeComp implements Comparator<IColoredEdge> {
  protected final static Logger LOG = LogManager.getLogger(EdgeComp.class);

  @Override
  public int compare(final IColoredEdge o1, final IColoredEdge o2) {
    return 0;
  }
}
