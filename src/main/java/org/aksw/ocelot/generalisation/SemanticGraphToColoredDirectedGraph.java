package org.aksw.ocelot.generalisation;

import java.util.List;
import java.util.ListIterator;

import org.aksw.commons.util.Pair;
import org.aksw.ocelot.core.nlp.StanfordPipeExtended;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.ColoredEdge;
import org.aksw.ocelot.generalisation.graph.GrammaticalRelationNode;
import org.aksw.ocelot.generalisation.graph.INode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode.GType;
import org.aksw.ocelot.generalisation.graph.RootNode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class SemanticGraphToColoredDirectedGraph {

  protected final static Logger LOG =
      LogManager.getLogger(SemanticGraphToColoredDirectedGraph.class);

  /**
   * Replaces all characters that are not in {A-Za-z\-}.
   *
   * @param string
   * @return cleaned string <code>
  
   public static String clean(final String string) {
     return string.replaceAll("[^A-Za-z\\-]", "").trim();
   }
   </code>
   */

  /**
   * Creates a {@link ColoredDirectedGraph} object, for each relation a node holding the tag of the
   * relation. The edges have the black color. <br>
   * <br>
   * Nodes in the graph are {@link GrammaticalRelationNode}, {@link RootNode},
   * {@link IndexedWordNode},
   *
   * @param SemanticGraph
   * @return ColoredDirectedGraph
   */
  public static ColoredDirectedGraph semanticGraphToColoredDirectedGraph(final SemanticGraph sg) {

    final ColoredDirectedGraph graph = new ColoredDirectedGraph();

    // root
    final IndexedWord root = sg.getFirstRoot();
    // all edges
    for (final SemanticGraphEdge edge : sg.edgeListSorted()) {
      final IndexedWord sourceWord = edge.getSource();
      final IndexedWord targetWord = edge.getTarget();
      final String relation = edge.getRelation().getShortName();

      INode source = null, target = null, relationNode = null;
      if ((sourceWord.equals(root))) {

        source = new RootNode(new Integer(sourceWord.index()).toString(),
            sourceWord.originalText().toString());
      } else {
        source = indexedWordToINode(sourceWord);
      }

      target = indexedWordToINode(targetWord);

      // relation
      final String id = sourceWord.index() + "/" + targetWord.index();
      relationNode = new GrammaticalRelationNode(id, relation);

      // add nodes
      graph.addVertex(source);
      graph.addVertex(target);
      graph.addVertex(relationNode);

      // add edges
      final String color = "black", label = "edge";
      graph.addEdge(source, relationNode, new ColoredEdge(color, label));
      graph.addEdge(relationNode, target, new ColoredEdge(color, label));
    }
    return graph;
  }

  /**
   * Creates a IndexedWordNode instance with ner or lemma as label.
   *
   * @param w IndexedWord
   * @return IndexedWordNode
   */
  protected static INode indexedWordToINode(final IndexedWord w) {

    final String id = new Integer(w.index()).toString();
    String label = w.originalText();

    final boolean isDomain = label.equals(Const.RELATION_DOMAIN_PLACEHOLDER);
    final boolean isRange = label.equals(Const.RELATION_RANGE_PLACEHOLDER);
    final boolean isNER = !w.ner().equals(StanfordPipeExtended.NO_NER);

    INode node;
    if (!isDomain && !isRange && isNER) {
      label = w.ner();
    }

    GType type = GType.ORIGINAL;
    // set node type
    if (isDomain) {

      type = IndexedWordNode.GType.DOMAIN;
    } else if (isRange) {
      type = IndexedWordNode.GType.RANGE;
    } else if (isNER) {
      type = IndexedWordNode.GType.NER;
    }

    // TODO: !!!!support more types!!!!
    node = new IndexedWordNode(id, label, w, type);
    return node;
  }

  /**
   * <code>
   public static synchronized void replaceDomainAndRange(final List<Integer> indices,
       final SemanticGraph semanticGraph) {

     final Pair<Integer, Integer> domain = new Pair<>(indices.get(0), indices.get(1));
     final Pair<Integer, Integer> range = new Pair<>(indices.get(2), indices.get(3));
     replaceDomainAndRange(new Pair<>(domain, range), semanticGraph);
   }</code>
   */

  public static synchronized void replaceDomainAndRange(
      final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> indices,
      final SemanticGraph semanticGraph) {

    final Pair<Integer, Integer> domain = indices.getKey();
    final Pair<Integer, Integer> range = indices.getValue();

    for (int i = domain.getKey() + 1; i <= (domain.getValue() + 1); i++) {
      setPlaceholder(semanticGraph.getNodeByIndexSafe(i), Const.RELATION_DOMAIN_PLACEHOLDER);
    }
    for (int i = range.getKey() + 1; i <= (range.getValue() + 1); i++) {
      setPlaceholder(semanticGraph.getNodeByIndexSafe(i), Const.RELATION_RANGE_PLACEHOLDER);
    }
  }

  public static synchronized void setPlaceholder(final IndexedWord w, final String placeholder) {
    w.setOriginalText(placeholder);
    w.setValue(placeholder);
    w.setLemma(placeholder);
    w.setTag(placeholder);
    w.setNER(placeholder);
  }

  /**
   * Removes target nodes connected with 'compound modifier'.
   *
   * @param indices domain and range indices
   * @param semanticGraph
   */
  public static synchronized void removeCompount(
      final Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> indices,
      final SemanticGraph semanticGraph) {

    final String tag = "compound modifier";

    if (indices != null) {
      final Pair<Integer, Integer> domain = indices.getKey();
      final Pair<Integer, Integer> range = indices.getValue();

      // domain
      for (int i = domain.getKey() + 1; i <= (domain.getValue() + 1); i++) {
        final IndexedWord w = semanticGraph.getNodeByIndexSafe(i);
        for (final SemanticGraphEdge edge : semanticGraph.outgoingEdgeList(w)) {
          if (edge.getRelation().getLongName().equals(tag)) {
            semanticGraph.removeVertex(edge.getTarget());
            semanticGraph.removeEdge(edge);
          }
        }
      }
      // range
      for (int i = range.getKey() + 1; i <= (range.getValue() + 1); i++) {
        final IndexedWord w = semanticGraph.getNodeByIndexSafe(i);
        for (final SemanticGraphEdge edge : semanticGraph.outgoingEdgeList(w)) {
          if (edge.getRelation().getLongName().equals(tag)) {
            semanticGraph.removeVertex(edge.getTarget());
            semanticGraph.removeEdge(edge);
          }
        }
      }
    }
    // NER
    for (final IndexedWord word : semanticGraph.vertexListSorted()) {
      if (!word.ner().equals(StanfordPipeExtended.NO_NER)) {
        for (final SemanticGraphEdge edge : semanticGraph.outgoingEdgeList(word)) {
          if (edge.getRelation().getLongName().equals(tag)) {
            semanticGraph.removeVertex(edge.getTarget());
            semanticGraph.removeEdge(edge);
          }
        }
      }
    }

    // removes 'punct' at the end
    final List<IndexedWord> list = semanticGraph.vertexListSorted();
    final ListIterator<IndexedWord> li = list.listIterator(list.size());
    while (li.hasPrevious()) {
      final IndexedWord word = li.previous();
      if (word.tag().equals(".")) {
        final List<SemanticGraphEdge> edges = semanticGraph.getIncomingEdgesSorted(word);
        semanticGraph.removeVertex(word);
        edges.forEach(semanticGraph::removeEdge);
        break;
      }
    }
  }

}
