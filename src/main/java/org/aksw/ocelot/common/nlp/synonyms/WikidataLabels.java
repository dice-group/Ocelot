package org.aksw.ocelot.common.nlp.synonyms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.ocelot.common.io.SparqlExecution;
import org.aksw.ocelot.common.request.Requests;
import org.aksw.ocelot.data.kb.DBpedia;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WikidataLabels extends SparqlExecution {

  protected final static Logger LOG = LogManager.getLogger(WikidataLabels.class);

  String queryFileContent = null;

  /**
   * Test.
   *
   * @param a
   */
  public static void main(final String[] a) {
    final String predicate = "http://dbpedia.org/ontology/birthPlace";
    final WikidataLabels wikidataLabels = new WikidataLabels();

    wikidataLabels.labels(predicate).forEach(LOG::info);
  }

  /**
   *
   * Constructor.
   *
   */
  public WikidataLabels() {
    super(DBpedia.url, DBpedia.graph, DBpedia.pagination, DBpedia.delay);
    try {
      queryFileContent = String.join(" ", Files.readAllLines(Paths.get(DBpedia.queryWikidata)));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Retrieves the 'owl:equivalentProperty' for the given property. <br>
   * Calls wikidata to retrieve labels. <br>
   *
   * @param predicate
   * @return
   */
  public List<String> labels(final String predicate) {
    final String wikidataLink = getWikidataLink(predicate);
    final List<String> labels = getLabels(wikidataLink);
    // labels = wikidataLabels.removeMWUs(labels);
    // labels = removeStopwords(labels);
    // labels = splitElements(labels);
    return labels;
  }

  protected List<String> removeMWUs(final List<String> words) {
    return words.stream().filter(p -> !p.contains(" ")).collect(Collectors.toList());
  }

  /**
   *
   * @param words
   * @return
   */
  protected List<String> splitElements(final List<String> words) {
    final List<String> splited = new ArrayList<>();

    words.forEach(word -> {
      // checks if we already have a part of the words
      boolean add = true;
      for (final String w : word.split(" ")) {
        if (splited.contains(w)) {
          add = false;
          break;
        }
      }
      // if not we add each token
      if (add) {
        for (final String w : word.split(" ")) {
          if (!splited.contains(w)) {
            splited.add(w);
          }
        }
      }
    });
    return splited;
  }

  /**
   * Gets the http://www.wikidata.org link.
   *
   * @param predicate
   * @return wikidata link, e.g., http://www.wikidata.org/entity/P26
   */
  protected String getWikidataLink(final String predicate) {

    String wikidataLink = null;
    if (queryFileContent != null) {
      String query = DBpedia.PREFIX.concat(queryFileContent);
      query = String.format(query, predicate);

      final JSONArray ja = execSelectToJSONArray(query);

      for (int i = 0; i < ja.length(); i++) {
        final JSONObject jo = ja.getJSONObject(i);
        if (jo.has("o")) {
          final JSONObject joo = jo.getJSONObject("o");
          if (joo.has("type") && joo.has("value")) {
            final String uri = joo.getString("value");
            if (uri.startsWith("http://www.wikidata.org")) {
              wikidataLink = uri;
              break;
            }
          }
        }
      } // end for
    } // end if
    else {
      LOG.error("Could not use file becuase queryFileContent is null.");
    }
    return wikidataLink;
  }

  /**
   * Returns the id of a wkidata link.
   *
   * @param wikidataLink, http://www.wikidata.org/entity/P26
   * @return the id, e.g. P26
   */
  protected String getID(final String wikidataLink) {
    return wikidataLink == null ? "" : wikidataLink.replace("http://www.wikidata.org/entity/", "");
  }

  /**
   *
   * Gets a list of aliases labels in wikidata.
   *
   * @param wikidataLink
   * @return
   */
  protected List<String> getLabels(final String wikidataLink) {

    final List<String> labels = new ArrayList<>();
    if (wikidataLink == null) {
      LOG.trace("No wikidataLink given.");
      return labels;
    }
    final String lang = "en";
    final String id = getID(wikidataLink);

    final String url = String.format(
        "https://www.wikidata.org/w/api.php?action=wbgetentities&ids=%s&languages=%s&format=json",
        id, lang);

    try {
      final JSONObject jo = new JSONObject(Requests.get(url));

      if ((jo != null) && jo.has("success") && (jo.getInt("success") == 1) && jo.has("entities")) {

        JSONObject joo = jo.getJSONObject("entities");

        joo = joo.getJSONObject(id);
        joo = joo.getJSONObject("aliases");
        final JSONArray enAliases = joo.getJSONArray(lang);

        for (int i = 0; i < enAliases.length(); i++) {
          final JSONObject ali = enAliases.getJSONObject(i);

          final String va = ali.getString("value");

          labels.add(va);
        }
      } else {
        LOG.trace("no results for " + wikidataLink + ".");
      }

    } catch (JSONException | IOException e) {
      LOG.trace("Could not parse data, seems there were no results.");
    }
    return labels;
  }
}
