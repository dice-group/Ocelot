package org.aksw.ocelot.data.wikipedia;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a Wikipedia document.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WikiDoc {

  public String id;
  public String url;
  public String title;
  public Map<Integer, String> sectionText = new HashMap<>();

  public WikiDoc(final String id, final String url, final String title) {
    this.id = id;
    this.url = url;
    this.title = title;
  }

  public String put(final Integer section, final String text) {
    return sectionText.put(section, text);
  }

  @Override
  public String toString() {
    return "WikiDoc [id=" + id + ", url=" + url + ", title=" + title + ", sectionText="
        + sectionText + "]";
  }
}
