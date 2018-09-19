package org.aksw.ocelot.evaluation.sargraph;

import java.io.File;

import org.aksw.ocelot.application.ApplicationUtil;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.isomorphism.VF2SubgraphIsomorphism;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SargraphEval {

  final static Logger LOG = LogManager.getLogger(SargraphEval.class);

  protected ApplicationUtil util = new ApplicationUtil();
  protected SargraphReader reader;
  protected String predicate;

  /**
   * @param file Sargraph path,e.g.,patterns/marriage.xml
   * @param p predicate
   */
  public SargraphEval(final String file, final String p) {
    reader = new SargraphReader(file);
    predicate = p;
  }

  public void eval() {
    boolean found = false;
    for (final ColoredDirectedGraph generalizedTree : util.loadGeneralizedTrees(predicate)
        .keySet()) {
      LOG.info("----" + generalizedTree.printPattern());
      for (final ColoredDirectedGraph sargraph : reader.getGraphs()) {

        // TODO: update domain and range here !!!!
        final String domain = "";
        final String range = "";
        found = VF2SubgraphIsomorphism.isomorphismExists(sargraph, generalizedTree, domain, range);
        LOG.info(sargraph.printPattern());

        if (found) {
          LOG.info("--");
          LOG.info(sargraph.printPattern());
          LOG.info(generalizedTree.printPattern());
          break;
        }
      }
    }
  }

  /**
   * Test.
   */
  public static void main(final String[] a) {
    final String p = "http://dbpedia.org/ontology/spouse";
    final String xmlFile = "patterns/marriage.xml";
    final String file;
    file = Const.DATA_FOLDER.concat(File.separator)//
        .concat("eval").concat(File.separator).concat(xmlFile);

    final SargraphEval sargraphEval = new SargraphEval(file, p);
    sargraphEval.eval();
  }

}
