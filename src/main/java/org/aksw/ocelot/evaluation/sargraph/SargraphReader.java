package org.aksw.ocelot.evaluation.sargraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.GrammaticalRelationNode;
import org.aksw.ocelot.generalisation.graph.INode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode;
import org.aksw.ocelot.generalisation.graph.IndexedWordNode.GType;
import org.aksw.ocelot.generalisation.graph.RootNode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.trees.EnglishGrammaticalRelations;

// holds parsed data
class TmpNode {

  String node_id = null;

  String lemma = null;
  // pos
  String tag = null;
  // domain and range
  String re_argument = null;

  // ner type
  String ner = null;

  public TmpNode(final String lemma, final String node_id, final String tag,
      final String re_argument, final String ner) {

    this.lemma = lemma;
    this.node_id = node_id;
    this.tag = tag;

    // values are: person,parent, ceremony, organization, from, position, to, date, cause, place,...
    this.re_argument = re_argument;

    this.ner = ner;
  }
}


public class SargraphReader {

  protected static Logger LOG = LogManager.getLogger(SargraphReader.class);

  // xml elements
  protected String re_pattern = "re_pattern";
  protected String vertex = "vertex";
  protected String relation_edge = "relation_edge";
  protected String pattern_id = "id";

  // nodes
  protected String lemma = "lemma";
  protected String node_id = "node_id";
  protected String tag = "tag";
  protected String re_argument = "re_argument";

  // edges
  protected String label = "label";
  protected String source = "source";
  protected String target = "target";
  // end xml elements

  private String file = null;

  // parsed data
  private final Map<String, TmpNode> nodes = new HashMap<>();

  // parsed data
  private final List<AbstractMap.SimpleEntry<String, String>> edges = new ArrayList<>();

  // pattern id to parsed graphs
  protected final Map<String, ColoredDirectedGraph> graphs = new HashMap<>();

  /**
   * Test.
   */
  public static void main(final String[] a) {

    final String file;
    {
      final String xmlFile = "patterns/person_death.xml";
      file = Const.DATA_FOLDER.concat(File.separator)//
          .concat("eval").concat(File.separator).concat(xmlFile);
    }

    LOG.info(file);
    final SargraphReader readSargraph = new SargraphReader(file);
    readSargraph.getGraphs().forEach(LOG::info);
    LOG.info("graph size: " + readSargraph.getGraphs().size());

    final List<ColoredDirectedGraph> list = new ArrayList<>(readSargraph.getGraphs());

    list.retainAll(new HashSet<>(readSargraph.getGraphs()));

    for (int i = 0; i < list.size(); i++) {
      final ColoredDirectedGraph cdi = list.get(i);
      for (int ii = 1 + i; ii < list.size(); ii++) {
        final ColoredDirectedGraph cdii = list.get(ii);
        if (cdi.equals(cdii)) {
          LOG.info("e:" + i + " " + ii);
          LOG.info(cdi);
          LOG.info(cdii);
        }
      }
    }

    LOG.info("graph size without duplicates: " + new HashSet<>(readSargraph.getGraphs()).size());

    LOG.info(readSargraph.allreArgs);
  }

  /**
   *
   * Constructor.
   *
   * @throws FileNotFoundException
   * @throws XMLStreamException
   */
  public SargraphReader(final String file) {
    this.file = file;
    run();
  }

  protected void clear() {
    nodes.clear();
    edges.clear();
  }

  public void run() {
    final int numberOfUsedDocuments = Integer.MAX_VALUE;
    clear();
    graphs.clear();
    try {
      final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      final InputStream in = new FileInputStream(file);

      final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);

      int currentDoc = 0;
      while (streamReader.hasNext() && (currentDoc < numberOfUsedDocuments)) {

        final int event = streamReader.getEventType();

        if ((event == XMLStreamConstants.START_ELEMENT)) {
          // start re_pattern
          if (streamReader.getLocalName().equals(re_pattern)) {

            // parse
            currentDoc++;

            final String patternId = getValue(streamReader, pattern_id);
            rePattern(streamReader);

            final ColoredDirectedGraph current = new ColoredDirectedGraph();

            // find root
            final Set<String> nodeIDs = new HashSet<>(nodes.keySet());

            // remove all edge targets
            edges.forEach(p -> nodeIDs.remove(p.getValue()));

            final Map<String, INode> map = new HashMap<>();
            // add root
            if (nodeIDs.size() == 1) {
              final String rootId = nodeIDs.iterator().next();

              final INode root = new RootNode(rootId, nodes.get(rootId).lemma);
              current.addVertex(root);
              map.put(rootId, root);

              nodes.entrySet().forEach(entry -> {
                final String id = entry.getKey();
                final TmpNode tn = entry.getValue();

                INode node;
                if (!map.containsKey(id)) {
                  if (id.contains("/")) {
                    node = new GrammaticalRelationNode(id, tn.lemma);
                  } else {

                    // TODO: update with NER
                    // update type of the node
                    node = new IndexedWordNode(//
                        id, tn.lemma, tn.lemma, tn.tag, tn.lemma, null, GType.LEMMA);
                  }
                  current.addVertex(node);
                  map.put(id, node);
                }
              });

              edges.forEach(p -> {
                final INode s = map.get(p.getKey());
                final INode t = map.get(p.getValue());

                current.addEdge(s, t);

              });
            }

            // TODO:
            graphs.put(patternId, current);
            clear();
          }
        }
        // end re_pattern
        streamReader.next();
      }
    } catch (final FileNotFoundException | XMLStreamException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  // TODO:
  static Map<String, String> typesMap = new HashMap<>();
  static {
    typesMap.put("person", "PERSON");
    typesMap.put("place", "LOCATION");
    typesMap.put("organization", "ORGANIZATION");
    // typesMap.put("member", "ORGANIZATION","PERSON");
  }

  Set<String> allreArgs = new HashSet<>();

  protected void rePattern(final XMLStreamReader streamReader) throws XMLStreamException {

    while (streamReader.hasNext()) {
      final int event = streamReader.getEventType();

      if (event == XMLStreamConstants.START_ELEMENT) {
        // start nodes
        if (streamReader.getLocalName().equals(vertex)) {

          final String nodeID = getValue(streamReader, node_id);
          String lemmaValue = getValue(streamReader, lemma);
          final String tagValue = getValue(streamReader, tag);
          final String re_argumentValue = getValue(streamReader, re_argument);
          allreArgs.add(re_argumentValue);
          if ((re_argumentValue != null) && !re_argumentValue.trim().isEmpty()) {

          }

          boolean ner = false;
          switch (tagValue) {
            case "C_person":
              lemmaValue = "PERSON";
              ner = true;
              break;
            case "C_date":
              lemmaValue = "DATE";
              ner = true;
              break;
            case "C_location":
              lemmaValue = "LOCATION";
              ner = true;
              break;

            case "C_organization":
              lemmaValue = "ORGANIZATION";
              ner = true;
              break;

          }

          if (ner) {
            // TODO: add ner to tmp node
            LOG.info(ner);
          }

          nodes.put(nodeID, new TmpNode(lemmaValue, nodeID, tagValue, re_argumentValue, ""));

        }

        if (streamReader.getLocalName().equals(relation_edge)) {

          String elabel = getValue(streamReader, label);

          if (elabel.equals("partmod") || elabel.equals("infmod")) {
            elabel = "vmod";
          }

          // ids
          final String esource = getValue(streamReader, source);
          final String etarget = getValue(streamReader, target);

          final String id = esource.concat("/").concat(etarget);

          if (!elabel.equals("null") && (EnglishGrammaticalRelations.valueOf(elabel) != null)) {

            final String relLabel = EnglishGrammaticalRelations.valueOf(elabel).toString();

            nodes.put(id, new TmpNode(relLabel, id, relLabel, relLabel, ""));

            edges.add(new SimpleEntry<String, String>(esource, id));
            edges.add(new SimpleEntry<String, String>(id, etarget));

          } else {
            LOG.warn("---");
            LOG.warn("NULL");
            LOG.warn(elabel);
            LOG.warn(esource);
            LOG.warn(etarget);
            LOG.warn("---");
          }
        }
      }

      if ((event == XMLStreamConstants.END_ELEMENT)
          && streamReader.getLocalName().equals(re_pattern)) {
        break;
      }
      streamReader.next();
    }

  }

  // -----
  public List<ColoredDirectedGraph> getGraphs() {
    return new ArrayList<>(graphs.values());
  }

  protected String getValue(final XMLStreamReader streamReader, final String name) {
    final int i = getIndexOfAttributeName(streamReader, name);
    if (i > -1) {
      return streamReader.getAttributeValue(i);
    } else {
      return null;
    }
  }

  protected int getIndexOfAttributeName(final XMLStreamReader streamReader, final String name) {
    for (int i = 0; i < streamReader.getAttributeCount(); i++) {
      if (streamReader.getAttributeLocalName(i).equals(name)) {
        return i;
      }
    }
    return -1;
  }
}
