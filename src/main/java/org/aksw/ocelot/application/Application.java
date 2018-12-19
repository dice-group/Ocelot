package org.aksw.ocelot.application;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.DBpediaStats;
import org.aksw.ocelot.generalisation.graph.ColoredDirectedGraph;
import org.aksw.ocelot.generalisation.graph.isomorphism.VF2SubgraphIsomorphism;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;;

/**
 * Uses Ocelot pattern to find predicates in an input sentence.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Application implements IOcelot {

  final static Logger LOG = LogManager.getLogger(Application.class);

  public final static String P = "PERSON";
  public final static String L = "LOCATION";
  public final static String O = "ORGANIZATION";

  protected ApplicationUtil applicationUtil = null;
  protected Set<String> predicates = null;

  final protected Map<String, SimpleEntry<String, String>> uriToDomainRange = new HashMap<>();

  public static Application instance(final String folder) {
    new Const("data/ocelot/config");
    return new Application(folder);
  }

  /**
   * Reads all supported predicates from a folder.
   *
   */
  protected Application(final String folder) {

    applicationUtil = new ApplicationUtil();
    // get all predicates
    predicates = ApplicationUtil.getAllPredicates(folder);

    LOG.trace(predicates);

    // domain and range from dbpedia
    Map<SimpleEntry<String, String>, Map<String, Integer>> domainRangeTypesToCounterMap;
    domainRangeTypesToCounterMap = new DBpediaStats().domainRangeTypesToCounterMap();

    // cleansing domain and range data
    for (final Entry<SimpleEntry<String, String>, Map<String, Integer>> entry : domainRangeTypesToCounterMap
        .entrySet()) {
      final SimpleEntry<String, String> k = entry.getKey();
      final Map<String, Integer> v = entry.getValue();
      for (final String uri : v.keySet()) {
        final String u = uri.replaceAll(Pattern.quote("dbo:"), "http://dbpedia.org/ontology/");
        uriToDomainRange.put(u, replace(k));
      }
    }
  }

  @Override
  public Set<String> getSupportedPredicates() {
    return predicates;
  }

  /**
   * Replaces DBpedia types to FOX types.
   *
   * @param entry
   * @return
   */
  private SimpleEntry<String, String> replace(final SimpleEntry<String, String> entry) {
    return new SimpleEntry<>(//
        entry.getKey()//
            .replaceAll(Pattern.quote("dbo:Organisation"), O) //
            .replaceAll(Pattern.quote("dbo:Place"), L) //
            .replaceAll(Pattern.quote("dbo:Person"), P), //
        entry.getValue()//
            .replaceAll(Pattern.quote("dbo:Organisation"), O)//
            .replaceAll(Pattern.quote("dbo:Place"), L)//
            .replaceAll(Pattern.quote("dbo:Person"), P)//
    );
  }

  @Override
  public Set<String> run(final String sentence, final String domain, final String range,
      final int domainBegin, final int domainEnd, final int rangeBegin, final int rangeEnd) {

    final ColoredDirectedGraph tree;
    tree = applicationUtil.getColoredDirectedGraph(sentence, domainBegin, domainEnd, rangeBegin,
        rangeEnd);

    final Set<String> predicatesFound = new HashSet<>();
    for (final String predicate : predicates) {

      final SimpleEntry<String, String> dr = uriToDomainRange.get(predicate);

      if (dr != null && dr.getKey().equals(domain) && dr.getValue().equals(range)) {
        final Map<ColoredDirectedGraph, Set<ColoredDirectedGraph>> generalizedTrees;
        generalizedTrees = applicationUtil.loadGeneralizedTrees(predicate);

        // compare
        for (final ColoredDirectedGraph generalizedTree : generalizedTrees.keySet()) {
          final boolean matches;
          matches = VF2SubgraphIsomorphism//
              .isomorphismExists(tree, generalizedTree, dr.getKey(), dr.getValue());

          if (matches) {
            predicatesFound.add(predicate);
          }
        } // end for
      } else {
        LOG.trace("Something wrong with " + predicate);
      }
    }
    return predicatesFound;
  }
}
