package org.aksw.ocelot.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.generalisation.GGeneralizeMain;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.ColoredEdge;
import org.aksw.ocelot.generalisation.graph.GrammaticalRelationNode;
import org.aksw.ocelot.generalisation.graph.INode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode;
import org.aksw.ocelot.generalisation.graph.RootNode;
import org.aksw.ocelot.generalisation.graph.SimpleNode;
import org.aksw.simba.knowledgeextraction.commons.cache.InMemoryCache;
import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class ApplicationUtil {

  protected final static Logger LOG = LogManager.getLogger(ApplicationUtil.class);

  protected StanfordPipe stanfordPipe = null;

  protected InMemoryCache<String, Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>>> cache =
      null;

  public ApplicationUtil() {

    // TODO: Add cache as optional parameter.
    final long timeToLive = Long.MAX_VALUE;
    final long timerInterval = Long.MAX_VALUE;
    // final int maxItems = Integer.MAX_VALUE;

    cache = new InMemoryCache<>(timeToLive, timerInterval);
  }

  /**
   *
   * @param sentence
   * @param domainBegin
   * @param domainEnd
   * @param rangeBegin
   * @param rangeEnd
   * @return
   */
  public ColoredDirectedGraph getColoredDirectedGraph(final String sentence, final int domainBegin,
      final int domainEnd, final int rangeBegin, final int rangeEnd) {

    // Stanford
    if (stanfordPipe == null) {
      stanfordPipe = StanfordPipe.instance();
    }
    final SemanticGraph sg = stanfordPipe.getSemanticGraph(sentence);

    sg.vertexListSorted().forEach(node -> {
      if (node.beginPosition() >= domainBegin && node.beginPosition() < domainEnd) {
        node.setOriginalText(Const.RELATION_DOMAIN_PLACEHOLDER);
      }
      if (node.beginPosition() >= rangeBegin && node.beginPosition() < rangeEnd) {
        node.setOriginalText(Const.RELATION_RANGE_PLACEHOLDER);
      }
    });
    LOG.trace(sg.toString());

    final ColoredDirectedGraph gg = getGraph(sg);
    removeSentenceEnding(gg);
    return gg;
  }

  /**
   * Creates a {@link ColoredDirectedGraph} object, for each relation a node holding the tag of the
   * relation. The edges have the black color.
   *
   * @param SemanticGraph
   * @return ColoredDirectedGraph
   */
  public static ColoredDirectedGraph getGraph(final SemanticGraph dependencyGraph) {
    final ColoredDirectedGraph graph = new ColoredDirectedGraph();

    // root
    final IndexedWord root = dependencyGraph.getFirstRoot();
    final String nodeId = new Integer(root.index()).toString();
    // all edges
    for (final SemanticGraphEdge edge : dependencyGraph.edgeListSorted()) {
      final IndexedWord sourceWord = edge.getSource();
      final IndexedWord targetWord = edge.getTarget();

      SimpleNode source = null, target = null, relationNode = null;

      // is edge source the root
      source = sourceWord.equals(root) ? //
          new RootNode(nodeId, sourceWord.originalText()) : getIndexedWordNode(edge.getSource());
      target = getIndexedWordNode(edge.getTarget());

      // relation
      final String id = sourceWord.index() + "/" + targetWord.index();
      final String relation = edge.getRelation().getShortName();
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
   * Creates a IndexedWordNode instance with ner or lemma as label. Some nodes are modified with
   * placeholders for domain and range of the relation.
   *
   * @param w IndexedWord
   * @return IndexedWordNode
   */
  private static IndexedWordNode getIndexedWordNode(final IndexedWord w) {
    final String id = new Integer(w.index()).toString();

    final String o = w.originalText();
    final boolean isDomain = o.equals(Const.RELATION_DOMAIN_PLACEHOLDER);
    final boolean isRange = o.equals(Const.RELATION_RANGE_PLACEHOLDER);
    if (isDomain) {
      return new IndexedWordNode(id, o, w, IndexedWordNode.GType.DOMAIN);
    } else if (isRange) {
      return new IndexedWordNode(id, o, w, IndexedWordNode.GType.RANGE);
    } else {
      final boolean isNER = !w.ner().equals(StanfordPipeExtended.NO_NER);

      IndexedWordNode.GType type = IndexedWordNode.GType.ORIGINAL;
      String label = w.originalText();
      if (isNER) {
        type = IndexedWordNode.GType.NER;
        label = w.ner();
      }
      return new IndexedWordNode(id, label, w, type);
    }
  }

  /**
   * Sends each map entry to {@link #removeSentenceEnding(ColoredDirectedGraph)}.
   *
   * @param map
   */
  public void removeSentenceEnding(final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> map) {
    for (final Entry<ColoredDirectedGraph, Set<ColoredDirectedGraph>> entry : map.entrySet()) {
      removeSentenceEnding(entry.getKey());
    }
  }

  /**
   * removes sentence endings by removing the nodes and edges
   *
   * @param coloredDirectedGraph
   */
  public void removeSentenceEnding(final ColoredDirectedGraph coloredDirectedGraph) {

    final Set<INode> remove = new HashSet<>();
    coloredDirectedGraph.getZeroOutdegreeNodes().forEach(leaf -> {
      // TODO: add ?! and !!??!!! and !!!!!
      if (leaf.getLabel().equals(".")) {
        remove.add(leaf);
      }
    });

    coloredDirectedGraph.removeAllVertices(remove);
    coloredDirectedGraph.getZeroOutdegreeNodes().forEach(leaf -> {
      if (leaf.getClass().getSimpleName().equals(GrammaticalRelationNode.class.getSimpleName())) {
        remove.add(leaf);
      }
    });

    coloredDirectedGraph.removeAllVertices(remove);
  }

  /**
   * Loads trees from serialization folder and replaces placeholders.
   *
   * @param p predicate
   *
   * @return
   */
  public Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> loadGeneralizedTrees(final String p) {
    Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> generalizedTrees = cache.get(p);
    if (generalizedTrees == null) {
      generalizedTrees = GGeneralizeMain.deserialize(p);
      // replacePlaceHolders(generalizedTrees);
      removeSentenceEnding(generalizedTrees);
      cache.put(p, generalizedTrees);
    }
    return generalizedTrees;
  }

  /**
   * replaces place holders with entity types
   *
   * @param generalizedTrees
   */
  protected void replacePlaceHolders(final Set<ColoredDirectedGraph> generalizedTrees) {
    generalizedTrees.forEach(g -> {
      g.vertexSet().forEach(node -> {
        if (node.getLabel().equals(Const.RELATION_DOMAIN_PLACEHOLDER)) {
          node.setLabel(Const.RELATION_DOMAIN);
        }
        if (node.getLabel().equals(Const.RELATION_RANGE_PLACEHOLDER)) {
          node.setLabel(Const.RELATION_RANGE);
        }
      });
    });
  }

  /**
   * It is a wrapper and calls {@link #getAllPredicateFiles(String) and
   * {@link #getAllPredicates(String)}} in this class.
   *
   * @param folder
   * @return all predicates in the folder.
   */
  public static Set<String> getAllPredicates(final String folder) {
    return getAllPredicates(getAllPredicateFiles(folder));
  }

  /**
   * Gets all files in folder with 'properties' file extension.
   *
   * @return
   */
  public static Set<Path> getAllPredicateFiles(final String folder) {
    final Set<Path> predicates = new HashSet<>();
    try {
      predicates.addAll(Files.walk(Paths.get(folder))//
          .filter(p -> FilenameUtils.getExtension(p.toFile().getName()).equals("properties"))
          .collect(Collectors.toSet()));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return predicates;
  }

  /**
   * Reads all lines/predicates in the given files.
   *
   * @param files
   * @return
   */
  public static Set<String> getAllPredicates(final Set<Path> files) {
    final Set<String> predicates = new HashSet<>();
    for (final Path file : files) {
      predicates.addAll(FileUtil.fileToList(file, "#"));
    }
    return predicates;
  }
}
