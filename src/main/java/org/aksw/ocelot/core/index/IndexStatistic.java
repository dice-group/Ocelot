package org.aksw.ocelot.core.index;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class IndexStatistic {

  public static int sentence_used = 0;
  public static int sentence_total = 0;
  public static int sentences_with_ner = 0;
  public static int sentences_longer_than_three_token = 0;
  public static int sentences_shorter_than_one_hundred_token = 0;

  public static Map<Integer, Integer> sentenceLengthToFrequent = new ConcurrentHashMap<>();

  public static JSONObject toJSON() {
    return new JSONObject()//
        .put("sentence_total", sentence_total)//
        .put("sentences_with_ner", sentences_with_ner)//
        .put("sentence_used", sentence_used)//
        .put("sentences_shorter_than_one_hundred_token", sentences_shorter_than_one_hundred_token)
        .put("sentences_longer_than_three_token", sentences_longer_than_three_token)
        .put("sentenceLengthToFrequent", new JSONObject(sentenceLengthToFrequent));
  }

  private IndexStatistic() {}
}
