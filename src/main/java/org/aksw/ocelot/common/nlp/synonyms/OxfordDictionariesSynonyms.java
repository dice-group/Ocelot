package org.aksw.ocelot.common.nlp.synonyms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Free - 3,000 requests/month <br>
 * Basic - 10,000 requests/month <br>
 * Pro - 100,000 requests/month <br>
 * Premium - 500,000 requests/month <br>
 * <br>
 * We don't want your application to miss out on any requests, therefore we will notify you by email
 * if you are approaching your current allowance. Please note that all applications are restricted
 * to making 60 requests per minute in order to ensure the performance of the API.
 *
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class OxfordDictionariesSynonyms {

  final static Logger LOG = LogManager.getLogger(OxfordDictionariesSynonyms.class);
  int requests = 0;

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {
    final OxfordDictionariesSynonyms ofd = new OxfordDictionariesSynonyms();
    ofd.synonyms("spouse").forEach(LOG::info);
  }

  // TODO: add cache and serialisation
  public Set<String> synonyms(final String word) {

    final Set<String> synonyms = new HashSet<>();

    final String url = dictionaryEntries(word);

    if (requests++ > 55) {
      try {
        Thread.sleep(60000);
      } catch (final InterruptedException e) {
        LOG.error(e.getLocalizedMessage());
      }
      requests = 0;
    }
    final String results = execute(url);

    try {
      final JSONObject jo = new JSONObject(results);
      JSONArray ja = jo.getJSONArray("results").getJSONObject(0).getJSONArray("lexicalEntries");

      ja = ja.getJSONObject(0).getJSONArray("entries").getJSONObject(0).getJSONArray("senses");

      for (int i = 0; i < ja.length(); i++) {
        final JSONArray array = ja.getJSONObject(i).getJSONArray("synonyms");
        for (int ii = 0; ii < array.length(); ii++) {
          final JSONObject obj = array.getJSONObject(ii);
          synonyms.add(obj.getString("text"));
        }
      }
    } catch (final Exception e) {
      LOG.info("seems there are no results: " + results);
    }

    return synonyms;
  }

  private String dictionaryEntries(final String word) {
    final String language = "en";

    // word id is case sensitive and lowercase is required
    final String word_id = word.toLowerCase();

    String w = word_id;
    try {
      w = URLEncoder.encode(word, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      LOG.error(e.getLocalizedMessage());
    }

    return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + w
        + "/synonyms;antonyms";
  }

  protected String execute(final String... params) {
    final String app_id = "13053b07";
    final String app_key = "92b47a8a0c13ec54a6f90b47a38604e9";
    try {
      final URL url = new URL(params[0]);
      final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
      urlConnection.setRequestProperty("Accept", "application/json");
      urlConnection.setRequestProperty("app_id", app_id);
      urlConnection.setRequestProperty("app_key", app_key);

      final BufferedReader reader;
      reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
      final StringBuilder stringBuilder = new StringBuilder();

      String line = null;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
      return stringBuilder.toString();
    } catch (final Exception e) {
      LOG.warn("Could not find something.");
      return "";
    }
  }
}
