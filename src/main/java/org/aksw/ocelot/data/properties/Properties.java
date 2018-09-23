package org.aksw.ocelot.data.properties;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.NotSerializableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.apache.jena.atlas.web.HttpException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Properties implements BackgroundKnowledge {
  protected static Logger LOG = LogManager.getLogger(Properties.class);

  String PREFIX = "" //
      + "PREFIX dbo: <http://dbpedia.org/ontology/> " //
      + "PREFIX dbp: <http://dbpedia.org/property/> " //
      + "PREFIX owl: <http://www.w3.org/2002/07/owl#> " //
      + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "//
      + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " //
      + "";

  private Set<String> properties = null;
  private QueryExecutionFactory qef;

  /**
   *
   * Constructor.
   *
   * @param url
   * @param graph
   * @param pagination
   * @param delay
   * @param file
   */
  public Properties(final String url, final String graph, final int pagination, final int delay,
      final Path file) {
    // this(url, graph, pagination, delay);
    qef = new QueryExecutionFactoryHttp(url, graph);
    try {
      qef = new QueryExecutionFactoryPaginated(qef, pagination);
      qef = new QueryExecutionFactoryDelay(qef, delay);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    // init properties with file
    getPredicates(file);
  }

  /**
   *
   * @param predicate
   * @return
   */
  public int subjects(final String p) {
    final Function<JSONArray, Integer> resultToInt = ja -> Integer.valueOf(ja.join(" ")//
        .replaceAll(Pattern.quote(
            "{\"callret-0\":{\"datatype\":\"http://www.w3.org/2001/XMLSchema#integer\",\"type\":\"typed-literal\",\"value\":\""),
            "")//
        .replaceAll("\"}}", "")//
    );
    final String q = PREFIX //
        + "select (count(distinct ?s)) where{"//
        + " ?s " + p + " ?o ."//
        + "}";
    final JSONArray ja = getBindings(q);
    return resultToInt.apply(ja);
  }

  @Deprecated
  private JSONArray getBindings(final String q) {
    final ResultSet rs = qef.createQueryExecution(q).execSelect();
    if (rs != null) {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsJSON(baos, rs);
      return new JSONObject(baos.toString()).getJSONObject("results").getJSONArray("bindings");
    }
    return new JSONArray();
  }

  /**
   * Gets Properties from file.
   *
   * @return set of uris
   * @throws FileNotFoundException
   */
  protected Set<String> getPredicates(final Path file) {
    if (properties == null) {
      LOG.info("Read properties from file: " + file.getFileName());
      properties = new HashSet<>(FileUtil.fileToList(file, "#"));
      LOG.info("Properties: " + properties);
    }
    return properties;
  }

  @Override
  public Set<String> getPredicates() {
    return properties;
  }

  /**
   * SparqlTest
   *
   * @param predicate
   * @return subject to all objects
   */
  @Deprecated
  public Map<String, Set<String>> getTriples(final URI predicate, final int max) {
    LOG.info("getTriples ...");

    // Serialization
    @SuppressWarnings("unchecked")
    Map<String, Set<String>> map = SerializationUtil
        .deserialize(getPath(predicate, "_" + max + "_triples.data"), HashMap.class);

    if (map == null) {
      map = new HashMap<>();

      final String q = PREFIX //
          + "select ?s ?o where { " //
          + "?s <" + predicate.toString() + "> ?o. " //
          + "<" + predicate.toString() + "> rdfs:domain ?d. " //
          + "<" + predicate.toString() + "> rdfs:range ?r. " //
          + "?s rdf:type ?d. " //
          + "?o rdf:type ?r." //
          + "}"//
          + "LIMIT ".concat(String.valueOf(max));;

      try {
        final JSONArray ja = getBindings(q);

        LOG.info("SparqlTest found for the predicate: " + ja.length());

        String s = "";
        for (int i = 0; i < ja.length(); i++) {
          s = ja.getJSONObject(i).getJSONObject("s").getString("value");
          final String o = ja.getJSONObject(i).getJSONObject("o").getString("value");

          if (s != null && !s.trim().isEmpty()) {
            if (map.get(s) == null) {
              map.put(s, new HashSet<String>());
            }
            map.get(s).add(o);
          }
        }
      } catch (final HttpException httpe) {
        LOG.error("DBpedia seems to be down!!");

      } catch (final Exception e) {
        LOG.debug("No label for: ".concat(predicate.toString()));
      }

      try {
        SerializationUtil.serialize(getPath(predicate, "_" + max + "_triples.data"), map);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }

    return map;
  }

  private String getPath(final URI predicate, final String file) {
    return predicate.getPath().replaceFirst("/", "") + file;
  }

  @Override
  public Map<String, Set<String>> getTriples(final String predicate, final int max) {
    try {
      return getTriples(new URI(predicate), max);
    } catch (final URISyntaxException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  /**
   * Gets the labels to a given subject.
   */
  @Override
  public Set<String> getLabels(final String uri) {
    final String q = PREFIX //
        + "select distinct ?label where {" //
        + "<" + uri + "> rdfs:label ?label." //
        + "FILTER (lang(?label) = 'en')" //
        + "} ";
    final JSONArray ja = getBindings(q);
    final Set<String> labels = new HashSet<>();
    for (int i = 0; i < ja.length(); i++) {
      labels.add(ja.getJSONObject(i).getJSONObject("label").getString("value"));
    }
    return labels;
  }

}
