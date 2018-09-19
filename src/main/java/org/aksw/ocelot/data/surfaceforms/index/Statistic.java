package org.aksw.ocelot.data.surfaceforms.index;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Statistic {

  public static Map<Integer, Integer> sfSizeToOccurrence = new HashMap<>();
  // public static Map<String, Integer> uriToSFSize = new HashMap<>();

  private Statistic() {}

  public static JSONObject toJSON() {
    return new JSONObject()//
        .put("sfSizeToOccurrence", new JSONObject(sfSizeToOccurrence));
  }
}
