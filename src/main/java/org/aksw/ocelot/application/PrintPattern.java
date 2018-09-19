package org.aksw.ocelot.application;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.classify.PredicateSurfaceformsVec;
import org.aksw.ocelot.common.lang.MapUtil;
import org.aksw.ocelot.common.lang.StringUtil;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

public class PrintPattern {
  final static Logger LOG = LogManager.getLogger(PrintPattern.class);

  final static int maxNodes = Integer.MAX_VALUE;
  final static boolean withSynonyms = false;
  final static String sep = " \t ";

  public static void main(final String[] args) throws IOException {

    final PredicateSurfaceformsVec predicateSurfaceformsVec = new PredicateSurfaceformsVec();
    final Map<String, Set<String>> labelsMap = predicateSurfaceformsVec.getLabels();

    final ApplicationUtil util = new ApplicationUtil();
    final CSVWriter writer = new CSVWriter(new FileWriter("test.csv"), '\t');
    // feed in your array (or convert your data to an array)

    // each predicate
    final StringBuilder sb = new StringBuilder();
    for (final String predicate : labelsMap.keySet()) {
      final String[] entries = new String[5];
      // load trees
      final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> trees;
      trees = util.loadGeneralizedTrees(predicate);
      LOG.info("==========: " + predicate + " generalizedTrees size: " + trees.size());
      LOG.info("synonyms: " + labelsMap.get(predicate));

      // generalizedTree to the number of generalized trees
      Map<ColoredDirectedGraph, Integer> numberOfGeneralizedTrees = new HashMap<>();
      for (final Entry<ColoredDirectedGraph, Set<ColoredDirectedGraph>> i : trees.entrySet()) {
        final ColoredDirectedGraph generalizedTree = i.getKey();
        final Set<ColoredDirectedGraph> generalizedTrees = i.getValue();

        if (generalizedTree.vertexSet().size() <= maxNodes) {
          // we do not use trees with more than maxNodes nodes
          numberOfGeneralizedTrees.put(generalizedTree, generalizedTrees.size());
        }
      }

      numberOfGeneralizedTrees = MapUtil.reverseSortByValue(numberOfGeneralizedTrees);
      for (final Entry<ColoredDirectedGraph, Integer> ii : numberOfGeneralizedTrees.entrySet()) {

        final ColoredDirectedGraph generalizedTree = ii.getKey();
        final Integer numberOfTrees = ii.getValue();

        if (withSynonyms) {

          final Set<String> labels = labelsMap.get(predicate);
          for (final String label : labels) {
            // find synonym in pattern
            if (StringUtil.indices(generalizedTree.printPattern(), label).size() > 0) {

              LOG.info(generalizedTree.printPattern() + " ; " + numberOfTrees + " ; " + label
                  + " ; " + generalizedTree.vertexSet().size());

              entries[0] = predicate;
              entries[1] = generalizedTree.printPattern();
              entries[2] = numberOfTrees + "";
              entries[3] = label;
              entries[4] = generalizedTree.vertexSet().size() + "";
              writer.writeNext(entries);

              sb//
                  .append(predicate).append(sep)//
                  .append(generalizedTree.printPattern()).append(sep)//
                  .append(numberOfTrees).append(sep)//
                  .append(label).append(sep)//
                  .append(generalizedTree.vertexSet().size())//
                  .append("\n");
              break;
            }
          } // end for
        } else {
          LOG.info(generalizedTree.printPattern() + " ; " + numberOfTrees + " ; "
              + generalizedTree.vertexSet().size());

          entries[0] = predicate;
          entries[1] = generalizedTree.printPattern();
          entries[2] = numberOfTrees + "";
          entries[3] = "";
          entries[4] = generalizedTree.vertexSet().size() + "";
          writer.writeNext(entries);

          sb//
              .append(predicate).append(sep)//
              .append(generalizedTree.printPattern()).append(sep)//
              .append(numberOfTrees).append(sep)//
              .append(generalizedTree.vertexSet().size())//
              .append("\n");
        }

      } // end for
    } // end for
    writer.close();
    LOG.info(sb.toString());
  }
}
