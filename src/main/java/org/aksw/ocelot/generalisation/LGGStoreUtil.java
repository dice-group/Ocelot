package org.aksw.ocelot.generalisation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.RootNode;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LGGStoreUtil {
  final static Logger LOG = LogManager.getLogger(LGGStoreUtil.class);

  /**
   * Returns generalized trees to all its specialized trees in the given store.
   *
   * @param store object
   * @return map with generalized trees to specialized trees.
   */
  protected Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> generalizedToSpecialized(
      final List<LGGStore> store) {

    // generalized trees to all its specialized trees
    final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> map;
    map = new HashMap<>();

    for (final LGGStore storeEntry : store) {
      if (map.get(storeEntry.getLeft()) == null) {
        map.put(storeEntry.getLeft(), new HashSet<>());
      }
      map.get(storeEntry.getLeft()).addAll(storeEntry.getMiddle());
      // gtoall.get(s.getLeft()).addAll(s.getRight());
    }
    return map;
  }

  public void printDebug(final List<LGGStore> store) {
    LOG.info("====PRINT DEBUG INFO");
    LOG.info("store: " + store.size());

    // generalized trees to all its specialized trees
    final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> generalizedToSpecialized;
    generalizedToSpecialized = generalizedToSpecialized(store);

    final Set<String> usedRoots = new HashSet<>();
    generalizedToSpecialized.entrySet().forEach(entry -> {
      LOG.info("-----");
      // prints the generalized tree
      LOG.info(entry.getKey().printPattern());

      // find all root labels of the specialized trees
      entry.getValue().forEach(v -> {

        v.getZeroIndegreeNodes().forEach(root -> {
          if (root instanceof RootNode) {
            usedRoots.add(((RootNode) root).getLabel());
          } else {
            LOG.warn(//
                "Node without indegree edges but not an instance of a RootNode class: "
                    + root.getClass().getSimpleName());
          }
        });
        // prints the specialized trees
        LOG.info("\t" + v.printPattern());
      });
    });
    LOG.info("usedRoots in the specialized trees: " + usedRoots);

    // finds duplicate pattern. The nlr pattern are more general than the trees
    final Map<String, LGGStore> map = new HashMap<>();
    for (final LGGStore s : store) {
      final String nlr = s.getLeft().printPattern();

      if (map.containsKey(nlr)) {
        LOG.info("----found duplicate");
        LOG.info(nlr);
        LOG.info(map.get(nlr).getLeft());
        LOG.info(s.getLeft());
      } else {
        map.put(nlr, s);
      }
    }
  }

  public void printinfo(final List<ColoredDirectedGraph> graphs) {
    final Set<String> nlr = new HashSet<>();
    graphs.forEach(g -> nlr.add(g.printPattern()));

    LOG.info("All pattern of the graphs: ");
    nlr.forEach(LOG::info);
    LOG.info("graphs size: " + graphs.size());
    LOG.info("nlr size: " + nlr.size());
  }
}
