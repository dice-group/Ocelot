package org.aksw.ocelot.generalisation;

import java.io.NotSerializableException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Main class for the generalization process. Serializes gernal pattern for a predicate
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class GGeneralizeMain {

  final static Logger LOG = LogManager.getLogger(GGeneralizeMain.class);

  // how many trees we take into account
  protected static final Integer treeLimit = Integer.MAX_VALUE;

  // TODO: add to config s
  // serialization folder
  protected static String storeFolder = Const.TMP_FOLDER.concat("/ocelot/serial");

  private final LGGStoreUtil storeUtil = new LGGStoreUtil();

  public GGeneralizeMain() {}

  public GGeneralizeMain(final String storeFolder) {
    this();
    GGeneralizeMain.storeFolder = storeFolder;
  }

  /**
   * Reads graphs from model and gets the generalized pattern and serialises it.
   *
   * @param p
   * @param synonymes
   */
  public void createGeneralizedTrees(final String p, final List<String> synonymes) {

    List<ColoredDirectedGraph> graphs = new GModel(p).getGraphs(p, synonymes);

    if (treeLimit != Integer.MAX_VALUE) {
      LOG.warn("graph limit set to " + treeLimit + " so we do not use the whole data!");
      graphs = graphs.stream().limit(treeLimit).collect(Collectors.toList());
    }

    // Create generalized pattern
    final List<LGGStore> store = new GPattern().run(graphs);

    // serialize all generalized trees, that at least generals two trees.
    final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> map;
    map = storeUtil.generalizedToSpecialized(store);

    // remove other trees
    final Iterator<Entry<ColoredDirectedGraph, Set<ColoredDirectedGraph>>> iter;
    iter = map.entrySet().iterator();
    final int max = 1;
    while (iter.hasNext()) {
      if (iter.next().getValue().size() <= max) {
        iter.remove();
      }
    }
    serialize(p, map);
  }

  public static String getPredicateLabelFromURI(final String p) {
    return p.substring(p.lastIndexOf("/") + 1, p.length());
  }

  public static void serialize(final String p,
      final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> map) {

    SerializationUtil.setRootFolder(storeFolder);
    try {
      SerializationUtil.serialize(getPredicateLabelFromURI(p).concat(".bin"), map, true);
    } catch (final NotSerializableException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> deserialize(final String p) {
    SerializationUtil.setRootFolder(storeFolder);
    return SerializationUtil.deserialize(getPredicateLabelFromURI(p).concat(".bin"), HashMap.class);
  }
}
