package org.aksw.ocelot.generalisation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Extends a {@link org.apache.commons.lang3.tuple.Triple} to hold an object of
 * {@link org.aksw.legacy.ocelot.generalisation.graph.impl.ColoredDirectedGraph} and two list of
 * {@link org.aksw.legacy.ocelot.generalisation.graph.impl.ColoredDirectedGraph} object.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class LGGStore
    extends Triple<ColoredDirectedGraph, List<ColoredDirectedGraph>, List<ColoredDirectedGraph>> {

  private static final long serialVersionUID = -1738384161575219733L;

  ColoredDirectedGraph left = null;
  List<ColoredDirectedGraph> middle = new ArrayList<>();

  /**
   *
   * Constructor.
   *
   * @param left
   * @param middle
   * @param right
   */
  public LGGStore(final ColoredDirectedGraph left) {
    this.left = left;
  }

  /**
   *
   * Constructor.
   *
   * @param left
   * @param middle
   * @param right
   */
  public LGGStore(final ColoredDirectedGraph left, final List<ColoredDirectedGraph> middle) {
    this.left = left;
    if (middle != null) {
      this.middle.addAll(middle);
    }
  }

  /**
   *
   * Constructor.
   *
   * @param left
   * @param middle
   * @param right
   */
  public LGGStore(final ColoredDirectedGraph left, final ColoredDirectedGraph middle) {
    this.left = left;
    if (middle != null) {
      this.middle.add(middle);
    }
  }

  @Override
  public ColoredDirectedGraph getLeft() {
    return left;
  }

  @Override
  public List<ColoredDirectedGraph> getMiddle() {
    return middle;
  }

  // TODO: delete me
  @Override
  public List<ColoredDirectedGraph> getRight() {
    throw new UnsupportedOperationException("We don't need this one.");
  }

  @Override
  public String toString() {
    final String left = this.left.printPattern();
    final String middle =
        this.middle.stream().map(tree -> tree.printPattern()).collect(Collectors.joining(";"));

    return "left: ".concat(left).concat(" middle: ").concat(middle);
  }
}
