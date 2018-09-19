package org.aksw.ocelot.core.wordembedding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ocelot.common.config.Constant;
import org.aksw.ocelot.common.lang.MapUtil;
import org.aksw.ocelot.data.Const;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Word2VecBinding {
  protected final static Logger LOG = LogManager.getLogger(Word2VecBinding.class);

  public static String key = Const.word2vecKey;
  public static String endpoint = Const.word2vecEndpoint;

  /**
   * Addition of all vectors of the given words. I there is no vector for a word, it's ignored.
   *
   * @param words
   * @return vector
   */
  public float[] addition(final Set<String> words) {
    final List<float[]> vecs = vec(words);
    if ((vecs != null) && !vecs.isEmpty()) {
      float[] currentVec = vecs.get(0);
      for (int i = 1; i < vecs.size(); i++) {
        currentVec = add(currentVec, vecs.get(i));
      }
      return currentVec;
    }
    return null;
  }

  /**
   * Addition of two vectors of the same length.
   *
   * @param vectorA
   * @param vectorB
   *
   * @return addition of vectors a and b or null
   */
  protected float[] add(final float[] vectorA, final float[] vectorB) {
    if (vectorA.length == vectorB.length) {
      final float[] add = new float[vectorA.length];
      for (int i = 0; i < vectorA.length; i++) {
        add[i] = vectorA[i] + vectorB[i];
      }
      return add;
    }
    return null;
  }

  /**
   * Request the word2vec service to get the vectors to the given words, using
   * {@link Word2VecBinding#vec(String)}.
   *
   * @param word
   *
   * @return the vectors
   */
  public List<float[]> vec(final Set<String> words) {
    final List<float[]> vecs = new ArrayList<>();
    for (final String word : words) {
      final float[] value = vec(word);
      if (value == null) {
        LOG.warn("No vector for " + word);
      } else {
        vecs.add(value);
      }
    }
    return vecs;
  }

  /**
   * Request the word2vec service to get the vector to the given word.
   *
   * @param word
   *
   * @return the vector or null
   */
  protected float[] vec(final String word) {
    float[] vec = null;
    String w = word;
    try {
      w = URLEncoder.encode(word, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      LOG.error(e.getLocalizedMessage());
    }
    try {
      final String url = new StringBuffer()//
          .append(endpoint).append("/vector?")//
          .append("a=").append(w).append("&")//
          .append("apikey=").append(key)//
          .toString();

      // LOG.info(url);
      final JSONObject response = send(url);
      if (response.has("a")) {
        final JSONArray vecJson = response.getJSONObject("a").getJSONArray("vec");
        vec = jsonToArray(vecJson);
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return vec;
  }

  private float[] jsonToArray(final JSONArray vecJson) {
    final float[] vec = new float[vecJson.length()];
    for (int i = 0; i < vecJson.length(); i++) {
      vec[i] = (float) vecJson.getDouble(i);
    }
    return vec;
  }

  public Double similarity(final String a, final String b) {
    String url;
    try {
      url = new StringBuffer().append(endpoint).append("/similarity?").append("a=")
          .append(URLEncoder.encode(a, Constant.UTF_8.name())).append("&").append("b=")
          .append(URLEncoder.encode(b, Constant.UTF_8.name())).append("&").append("apikey=")
          .append(key).toString();
      final JSONObject jo = send(url);
      if ((jo != null) && jo.has("cos")) {
        return jo.getDouble("cos");
      } else {
        return -1D;
      }
    } catch (final UnsupportedEncodingException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return -1D;
    }
  }

  /**
   * cosine similarity
   *
   *
   * @param a
   * @param n
   * @return
   */
  public Map<String, Double> distance(final String a, final int n) {

    String url;
    final Map<String, Double> cos = new HashMap<>();
    try {
      url = new StringBuffer()//
          .append(endpoint).append("/distance?")//
          .append("a=").append(URLEncoder.encode(a, Constant.UTF_8.name())).append("&")//
          .append("n=").append(n).append("&")//
          .append("apikey=").append(key)//
          .toString();

      final JSONObject jo = send(url);

      if (jo != null) {
        final Iterator<?> keysItr = jo.keys();
        while (keysItr.hasNext()) {
          final String key = (String) keysItr.next();
          cos.put(key, jo.getDouble(key));
        }
      }
    } catch (final UnsupportedEncodingException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return cos;
  }

  protected JSONObject send(final String url) {
    JSONObject jo = new JSONObject();
    try {
      final Response response = Request.Get(url).execute();
      final HttpResponse httpResponse = response.returnResponse();
      final int code = httpResponse.getStatusLine().getStatusCode();
      if (code != 200) {
        LOG.debug("status code: " + code);
      } else {
        final String content = EntityUtils.toString(httpResponse.getEntity());
        jo = new JSONObject(content);
      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      LOG.info("Word2Vec service is running?");
    }
    return jo;
  }

  /**
   * Gets n synonyms with the shortest distance to word. Synonyms without punctuation and space.
   * Includes the given word.
   *
   * @param word
   * @param n
   * @return sorted list
   */
  public List<String> synonymsWord2Vec(final String word, final int n) {
    Map<String, Double> synonyms = distance(word, 2 * n);
    synonyms.put(word, 1D);

    synonyms = MapUtil.reverseSortByValue(synonyms).entrySet().stream()//
        .filter(
            // word without punctuation and space
            // entry -> !entry.getKey().matches(".*\\p{Punct}") && !entry.getKey().contains(" "))//
            entry -> !entry.getKey().matches(".*\\p{Punct}"))//
        .limit(n)//
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return MapUtil.reverseSortByValue(synonyms)//
        .entrySet().stream()//
        .map(Map.Entry::getKey).collect(Collectors.toList());
  }
}
