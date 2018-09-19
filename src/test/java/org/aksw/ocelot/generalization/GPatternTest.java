package org.aksw.ocelot.generalization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.ocelot.application.ApplicationUtil;
import org.aksw.ocelot.generalisation.GPattern;
import org.aksw.ocelot.generalisation.LGGStore;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

// tests the generalization process
public class GPatternTest {
  final static Logger LOG = LogManager.getLogger(GPatternTest.class);

  ApplicationUtil util = new ApplicationUtil();
  GPattern pattern = new GPattern();

  // TODO: test fails because the DOMAION and RANGE nodes types are missing in the tree
  @Test
  public void testPattern() {
    generalizationTest(Arrays.asList(//
        // sentences
        "Alice plays with Bob soccer.", //
        "Paul plays with Kevin soccer."//

    ), // expected pattern
        "PERSON" + " play with " + "PERSON" + " soccer"//
    );
    generalizationTest(Arrays.asList(//
        // sentences
        "Paul plays with Kevin in the room.", //
        // "Alice plays with Bob soccer", //
        "Bob plays with Alice in the kitchen."//

    ), // expected pattern
        "PERSON" + " play with " + "PERSON" + " in the NN" //
    );

    generalizationTest(Arrays.asList(//
        // sentences
        "Alice and Bob are married and have two kids, Tom and Clara.", //
        "Paul and Paula are married and have 2 kids, Clara and Tomas."//

    ), // expected pattern
        "PERSON" + " and " + "PERSON" + " are married and have NUMBER kids , PERSON and PERSON" //
    );
  }

  // privates
  private void generalizationTest(final List<String> sentences, final String expectedPattern) {

    // get normal trees
    final List<ColoredDirectedGraph> list = new ArrayList<>();

    // TODO: FIX ME
    // sentences.forEach(sentence -> list.add(util.getColoredDirectedGraph(sentence)));

    // generalize trees
    final List<LGGStore> storeList = pattern.run(list);

    // test
    LOG.info(storeList);
    storeList.forEach(triple -> LOG.info(triple.getLeft()));
    Assert.assertEquals(1, storeList.size());
    Assert.assertEquals(expectedPattern, storeList.get(0).getLeft().printPattern());
  }
}
