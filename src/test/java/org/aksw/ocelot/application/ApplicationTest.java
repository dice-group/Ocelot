package org.aksw.ocelot.application;

import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApplicationTest {

  final static Logger LOG = LogManager.getLogger(ApplicationTest.class);

  String predicatesFolder = "config/predicates/";

  IOcelot app = null;

  @Before
  public void before() {
    app = new Application(predicatesFolder);
  }

  /**
   * Tests a pattern for "http://dbpedia.org/ontology/spouse" from config on a sentence.
   */
  @Test
  public void test() {

    Assert.assertFalse(app.getSupportedPredicates().isEmpty());

    final String p = "http://dbpedia.org/ontology/spouse";
    if (app.getSupportedPredicates().contains(p)) {

      final String domainEntity = "A. Alice";
      final String rangeEntity = "B. Bob";
      final String sentence = domainEntity.concat(" married ").concat(rangeEntity).concat(".");

      final String domain = Application.P;
      final String range = Application.P;

      final int domainBegin = sentence.indexOf(domainEntity);
      final int domainEnd = domainBegin + domainEntity.length();

      final int rangeBegin = sentence.indexOf(rangeEntity);
      final int rangeEnd = rangeBegin + rangeEntity.length();

      Set<String> predicates;
      predicates = app.run(sentence, domain, range, domainBegin, domainEnd, rangeBegin, rangeEnd);

      Assert.assertEquals(1, predicates.size());
      Assert.assertTrue(predicates.contains(p));
    }
  }
}
