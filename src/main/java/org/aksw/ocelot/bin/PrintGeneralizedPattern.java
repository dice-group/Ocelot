package org.aksw.ocelot.bin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.application.ApplicationUtil;
import org.aksw.ocelot.common.io.SerializationUtil;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Deprecated
public class PrintGeneralizedPattern {
  final static Logger LOG = LogManager.getLogger(PrintGeneralizedPattern.class);
  static {
    SerializationUtil.setRootFolder(
        "/media/rspeck/store/GitRepos/Relation-Extraction-Ocelot/ocelot/serial_syn/");
  }
  protected static ApplicationUtil util = new ApplicationUtil();
  protected List<String> ocelotPattern = new ArrayList<>();

  public static void main(final String[] args) {

    final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> e =
        util.loadGeneralizedTrees("http://dbpedia.org/ontology/spouse");

    for (final Entry<ColoredDirectedGraph, Set<ColoredDirectedGraph>> ee : e.entrySet()) {
      LOG.info("=====");
      LOG.info(ee.getKey());
      LOG.info(ee.getValue());

    }

  }

  /*
   * public void runOcelot(final String p) { final Set<ColoredDirectedGraph> generalizedTrees =
   * util.loadGeneralizedTrees(p).keySet(); for (final ColoredDirectedGraph gTree :
   * generalizedTrees) { ocelotPattern.add(gTree.printPattern()); }
   * ocelotPattern.forEach(LOG::info); }
   */

}
