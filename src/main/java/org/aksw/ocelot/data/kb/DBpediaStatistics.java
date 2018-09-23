package org.aksw.ocelot.data.kb;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.common.io.SparqlExecution;
import org.aksw.simba.knowledgeextraction.commons.lang.MapUtil;
import org.json.JSONArray;

/**
 * Finds relation with the most instances. <br>
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class DBpediaStatistics extends SparqlExecution {
  Set<String> objectProperties = new HashSet<>();
  Map<String, Integer> objectPropertiesToInstanceCounter = new HashMap<>();

  // print limit
  int limit = 100;

  /**
   * Test.
   *
   * @param args
   */
  public static void main(final String[] args) {

    final DBpediaStatistics dbpediaStatistics = new DBpediaStatistics();
    LOG.info(dbpediaStatistics);

  }

  public DBpediaStatistics() {
    super(DBpedia.url, DBpedia.graph, DBpedia.pagination, DBpedia.delay);

    initObjectProperties();
    initInstanceCounters();
  }

  public JSONArray run(final String query) {
    return execSelectToJSONArray(DBpedia.PREFIX.concat(query));
  }

  protected String getObjectPropertiesQuery() {
    return "select distinct ?p where { ?s ?p ?o. ?p a owl:ObjectProperty. }";
  }

  /**
   * Finds all owl:ObjectProperty and stores in {@link #objectProperties}.
   */
  protected void initObjectProperties() {
    final JSONArray ja = run(getObjectPropertiesQuery());
    for (int i = 0; i < ja.length(); i++) {
      objectProperties.add(ja.getJSONObject(i).getJSONObject("p").getString("value"));
    }
  }

  protected String getInstanceCounterQuery(final String p) {
    return String.format("select ?s ?o  where { ?s <%s> ?o.  }", p);
  }

  protected void initInstanceCounters() {
    objectProperties.parallelStream().forEach(p -> getInstanceCounter(p));
    objectPropertiesToInstanceCounter =
        MapUtil.reverseSortByValue(objectPropertiesToInstanceCounter);
  }

  protected void getInstanceCounter(final String p) {
    final JSONArray ja = run(getInstanceCounterQuery(p));
    final Set<SimpleEntry<String, String>> triple = new HashSet<>();
    for (int i = 0; i < ja.length(); i++) {
      triple.add(new SimpleEntry<>(//
          ja.getJSONObject(i).getJSONObject("s").getString("value"), //
          ja.getJSONObject(i).getJSONObject("o").getString("value")//
      ));
    }
    objectPropertiesToInstanceCounter.put(p, triple.size());
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("\n");
    objectPropertiesToInstanceCounter.entrySet().stream().limit(limit)
        .forEach(e -> sb.append(e.getKey()).append("->").append(e.getValue()).append("\n"));
    return sb.toString();
  }
}
