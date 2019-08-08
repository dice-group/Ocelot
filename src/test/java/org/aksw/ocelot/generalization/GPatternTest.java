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

// tests the generalization process
public class GPatternTest {
  final static Logger LOG = LogManager.getLogger(GPatternTest.class);

  ApplicationUtil util = new ApplicationUtil();
  GPattern pattern = new GPattern();

  // TODO: test fails because the DOMAION and RANGE node types are missing in the tree
  // TODO: set up config to find folder with pattern,but thats not possible within just the source
  // @Test
  public void testPattern() {
    generalizationTest(Arrays.asList(//
        // sentences
        "Alice plays with Bobbb soccer.", //
        "Paull plays with Kevin soccer."//

    ), // expected pattern
        "PERSON" + " play with " + "PERSON" + " soccer"//
        // "?D" + " plays with " + "?R" + " soccer"//
        , 0, "Alice".length(), //
        "Alice plays with ".length(), "Alice plays with Bobbb".length()//
    );

    generalizationTest(Arrays.asList(//
        // sentences
        "Paul plays with Kevin in the room.", //
        // "Alice plays with Bob soccer", //
        "Bobb plays with Alice in the kitchen."//

    ), // expected pattern
        "PERSON" + " play with " + "PERSON" + " in the NN" //
        // "?D" + " plays with " + "?R" + " in the NN" //
        , 0, "Paul".length(), //
        "Paul plays with  ".length(), "Paul plays with Kevin".length()//
    );

    /**
     * <code>
     generalizationTest(Arrays.asList(//
         // sentences
         "Alice and Bobbb are married and have two kids, Tom   and Clara.", //
         "Paull and Paula are married and have 2 kids,   Clara and Tomas."//


     ), // expected pattern
         "PERSON" + " and " + "PERSON" + " are married and have NUMBER kids , PERSON and PERSON" //
         , 0, "Paul".length(), //
         "Paul plays with  ".length(), "Paul plays with Kevin".length()//
     );
     </code>
     */
  }

  // privates
  private void generalizationTest(final List<String> sentences, final String expectedPattern,
      final int domainBegin, final int domainEnd, final int rangeBegin, final int rangeEnd) {

    // get normal trees
    final List<ColoredDirectedGraph> list = new ArrayList<>();

    sentences.forEach(sentence -> list
        .add(util.getColoredDirectedGraph(sentence, domainBegin, domainEnd, rangeBegin, rangeEnd)));

    Assert.assertNotNull(list);

    // generalize trees
    final List<LGGStore> storeList = pattern.run(list);

    // test
    LOG.info(storeList);
    storeList.forEach(triple -> LOG.info(triple.getLeft()));
    Assert.assertEquals(1, storeList.size());
    Assert.assertEquals(expectedPattern, storeList.get(0).getLeft().printPattern());
  }
}
