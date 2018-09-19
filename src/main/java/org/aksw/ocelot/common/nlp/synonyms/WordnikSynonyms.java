package org.aksw.ocelot.common.nlp.synonyms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;

/**
 * GET /word.json/{word}/relatedWords
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WordnikSynonyms {

  final static Logger LOG = LogManager.getLogger(OxfordDictionariesSynonyms.class);

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {
    final WordnikSynonyms ofd = new WordnikSynonyms();
    ofd.synonyms("spouse").forEach(LOG::info);
  }

  public Set<String> synonyms(final String word) {
    final Set<String> synonyms = new HashSet<>();
    final String app_key = "2d950b731a4440f2b307a006254c7b738236d279206cce2a2";
    try {
      final String url = dictionaryEntries(word, app_key);
      final String results = execute(url);
      final JSONArray jo = new JSONArray(results);

      final JSONArray words = jo.getJSONObject(0).getJSONArray("words");
      for (int i = 0; i < words.length(); i++) {
        synonyms.add(words.getString(i));
      }
    } catch (final Exception e) {
      LOG.warn("seems thre are no results");
    }
    return synonyms;
  }

  private String dictionaryEntries(final String word, final String key) {
    String w = word;
    try {
      w = URLEncoder.encode(word, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      LOG.error(e.getLocalizedMessage());
    }
    final String url =
        "http://api.wordnik.com:80/v4/word.json/%s/relatedWords?useCanonical=true&relationshipTypes=synonym&limitPerRelationshipType=100&api_key=%s";
    return String.format(url, w, key);
  }

  protected String execute(final String... params) {
    try {
      final URL url = new URL(params[0]);
      final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.setRequestProperty("Accept", "application/json");

      final BufferedReader reader;
      reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
      final StringBuilder stringBuilder = new StringBuilder();

      String line = null;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
      return stringBuilder.toString();
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      return "";
    }
  }
}
