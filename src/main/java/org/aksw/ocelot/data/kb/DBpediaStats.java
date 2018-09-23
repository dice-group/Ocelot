package org.aksw.ocelot.data.kb;

import java.io.NotSerializableException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.aksw.ocelot.common.io.SparqlExecution;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.json.JSONArray;

/**
 * Finds all properties we use.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class DBpediaStats extends SparqlExecution {

  final int top = 10;
  public final String file = "domainRangeTypesToCounterMap_top" + top + "_triples.data";

  /**
   * TEST
   *
   * @param a
   */
  public static void main(final String[] a) {
    new DBpediaStats().statis();
  }

  /**
   *
   * Constructor.
   *
   */
  public DBpediaStats() {
    super(DBpedia.url, DBpedia.graph, DBpedia.pagination, DBpedia.delay);
  }

  public Map<SimpleEntry<String, String>, Map<String, Integer>> domainRangeTypesToCounterMap() {
    // Serialization
    @SuppressWarnings("unchecked")
    Map<SimpleEntry<String, String>, Map<String, Integer>> domainRangeTypesToCounterMap =
        SerializationUtil.deserialize(file, HashMap.class);

    final Function<JSONArray, String> resultToString = ja -> ja.join(" ")//
        .replaceAll(Pattern.quote("{\"x\":{\"type\":\"uri\",\"value\":\""), "")//
        .replaceAll("\"}}", "")//
        .replaceAll(Pattern.quote("http://dbpedia.org/ontology/"), "dbo:");

    if (domainRangeTypesToCounterMap == null) {

      LOG.info("Gets properties directly with sparql...");

      final Set<String> ner =
          new HashSet<>(Arrays.asList("dbo:Organisation", "dbo:Person", "dbo:Place"));

      LOG.info("subtypes: ");
      final Map<String, String> typeTosubTypes = new HashMap<>();
      {
        for (final String type : ner) {
          final String q = DBpedia.PREFIX //
              + "select distinct (?x) where{"//
              + " ?x rdfs:subClassOf* " + type + "  "//
              + "}";
          final JSONArray ja = execSelectToJSONArray(q);
          final String joinedValues = resultToString.apply(ja);
          LOG.info(joinedValues.split(" ").length + " " + joinedValues);
          typeTosubTypes.put(type, joinedValues);
        }
      }

      LOG.info("Domain and range to predicates");
      Map<SimpleEntry<String, String>, String> domainRangeTypesToPredicates;
      domainRangeTypesToPredicates = new HashMap<>();
      {
        for (final String domain : ner) {
          for (final String range : ner) {
            final String q = DBpedia.PREFIX //
                + "select distinct (?x) where{"//
                + " values ?vd {" + typeTosubTypes.get(domain) + "} "//
                + " values ?vr {" + typeTosubTypes.get(range) + "} "//
                + " ?x a owl:ObjectProperty ." //
                + " ?x rdfs:domain ?vd ." //
                + " ?x rdfs:range ?vr ." //
                + "}";
            final JSONArray ja = execSelectToJSONArray(q);
            final String joinedValues = resultToString.apply(ja);
            domainRangeTypesToPredicates.put(new SimpleEntry<>(domain, range), joinedValues);
          }
        }
        domainRangeTypesToPredicates
            .forEach((k, v) -> LOG.info(k + " " + v.split(" ").length + " " + v));
      }

      domainRangeTypesToCounterMap = new HashMap<>();
      {

        for (final Entry<SimpleEntry<String, String>, String> e : domainRangeTypesToPredicates
            .entrySet()) {
          final String ps = e.getValue();
          final Map<String, Integer> pCounter = new HashMap<>();
          for (final String p : ps.split(" ")) {
            final int i = subjects(p);
            if (i > 0) {
              pCounter.put(p, i);
            }
          }
          domainRangeTypesToCounterMap.put(e.getKey(), pCounter);
        }
      }

      try {
        SerializationUtil.serialize(file, domainRangeTypesToCounterMap);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return domainRangeTypesToCounterMap;
  }

  public void statis() {
    for (final Entry<SimpleEntry<String, String>, Map<String, Integer>> e : domainRangeTypesToCounterMap()
        .entrySet()) {
      final SimpleEntry<String, String> domainAndRange = e.getKey();
      LOG.info(domainAndRange);

      final Map<String, Integer> map = e.getValue();
      final TreeSet<Integer> values = new TreeSet<>(map.values());
      final Iterator<Integer> iter = values.descendingIterator();

      int i = 0;
      int current = -1;
      while (iter.hasNext() && i++ < top) {
        current = iter.next();
      }
      for (final Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue() >= current) {
          LOG.info(entry.getKey() + " " + entry.getValue());
        }
      }
    }
  }

  public int subjects(final String p) {
    final Function<JSONArray, Integer> resultToInt = ja -> Integer.valueOf(ja.join(" ")//
        .replaceAll(Pattern.quote(
            "{\"callret-0\":{\"datatype\":\"http://www.w3.org/2001/XMLSchema#integer\",\"type\":\"typed-literal\",\"value\":\""),
            "")//
        .replaceAll("\"}}", "")//
    );
    final String q = DBpedia.PREFIX //
        + "select (count(distinct ?s)) where{"//
        + " ?s " + p + " ?o ."//
        + "}";
    final JSONArray ja = execSelectToJSONArray(q);
    return resultToInt.apply(ja);
  }
}
