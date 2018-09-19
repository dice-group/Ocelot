package org.aksw.ocelot.evaluation.ocelot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.ocelot.application.ApplicationUtil;
import org.aksw.ocelot.evaluation.boa.ReadBoaIndex;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Compares boa and ocelot.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Test {
  final static Logger LOG = LogManager.getLogger(Test.class);

  protected ApplicationUtil util = new ApplicationUtil();

  protected List<String> boaPattern = new ArrayList<>();
  protected List<String> ocelotPattern = new ArrayList<>();

  public void runBoa(final String p) {
    final String file = "/media/rspeck/store1/Data/boa_backup/solr/data/boa/en/index";
    final ReadBoaIndex index = new ReadBoaIndex(file);
    try {
      index.searcher(p);
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    // TODO: update me
    // boaPattern = index.getPattern().getField(ReadBoaEnum.NLR_VAR.getLabel()).stringValue();

    boaPattern.forEach(LOG::info);
  }

  public void runOcelot(final String p) {
    final Set<ColoredDirectedGraph> generalizedTrees = util.loadGeneralizedTrees(p).keySet();
    for (final ColoredDirectedGraph gTree : generalizedTrees) {
      ocelotPattern.add(gTree.printPattern());
    }
    ocelotPattern.forEach(LOG::info);
  }

  public void comapre() {
    if (boaPattern.size() > ocelotPattern.size()) {
      boaPattern.retainAll(ocelotPattern);
      LOG.info(boaPattern);
    } else {
      ocelotPattern.retainAll(boaPattern);
      LOG.info(ocelotPattern);
    }

  }

  public static void main(final String[] args) throws IOException {
    final String p = "http://dbpedia.org/ontology/spouse";
    final Test test = new Test();
    test.runOcelot(p);
    test.runBoa(p);
    test.comapre();

  }
}
