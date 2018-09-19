package org.aksw.ocelot.data.wikipedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Parses Wikipedia dump files to {@link WikiDoc}. Each {@link WikiDoc} is a Wikipedia article.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WikipediaExtractor implements IDataExtractor {
  protected static Logger LOG = LogManager.getLogger(WikipediaExtractor.class);

  protected BufferedReader bufferedReader;

  /**
   *
   * Constructor.
   *
   * @param bufferedReader
   */
  public WikipediaExtractor(final BufferedReader bufferedReader) {
    this.bufferedReader = bufferedReader;
  }

  public WikipediaExtractor() {}

  /**
   * Reads lines from the bufferedReader given in:
   * {@link WikipediaExtractor#WikipediaExtractor(BufferedReader bufferedReader)} and parses the
   * documents.
   */
  @Override
  public List<WikiDoc> call() {
    LOG.info("call");

    // possible head lines (h1 ...h9)
    final Set<String> hStrart = new HashSet<>();
    for (int i = 1; i < 10; i++) {
      hStrart.add(new StringBuilder().append("<h").append(i).append(">").toString());
    }

    final Set<String> heads = new HashSet<>();
    final List<WikiDoc> list = new ArrayList<>();
    StringBuffer text = new StringBuffer();
    WikiDoc wikidoc = null;
    String line = null;
    int section = 0;
    boolean newSection = false;

    try {
      // read line by line
      while ((line = bufferedReader.readLine()) != null) {

        // trim
        line = line.trim();

        // doc start
        if (line.startsWith("<doc ")) {
          try {
            wikidoc = new WikiDoc(getId(line), getUrl(line), getTitle(line));
            continue;
          } catch (final Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            LOG.error(line);
          }

        }

        // short or empty line
        if (line.isEmpty() || (line.length() < "<h1>".length())) {
          continue;
        }

        // lists
        final String liS = "<li>";
        final String liE = "</li>";
        if (line.startsWith(liS)) {
          line = line.substring(liS.length(), line.length() - liE.length());
          if (!(line.endsWith(".") || line.endsWith("!") || line.endsWith("?"))) {
            continue;
          }
          line = line.trim();
        }

        // headline
        if ((line.length() > "<h*>".length())
            && hStrart.contains(line.substring(0, "<h*>".length()))) {
          heads.add(line.substring("<h*>".length(), line.length() - "</h*>".length()));
          newSection = true;
          continue;
        }

        // ignores lines with titles
        if (wikidoc.title.equals(line.trim())) {
          continue;
        }

        // ignores lines with head titles
        boolean found = false;
        for (final Iterator<String> iter = heads.iterator(); iter.hasNext();) {
          final String head = iter.next();
          if (line.startsWith(head.concat("."))) {
            iter.remove();
            found = true;
            break;
          }
        }
        if (found) {
          continue;
        }
        heads.clear();

        if (newSection) {
          // add old section
          final String doc = text.toString().trim();
          if (!doc.isEmpty()) {
            wikidoc.put(section, doc);
          }
          // prepare new section
          text = new StringBuffer();
          section++;
          newSection = false;
        }

        // doc end
        if (line.startsWith("</doc>")) {
          // add section
          final String doc = text.toString().trim();
          if (!doc.isEmpty()) {
            wikidoc.put(section, doc);
          }
          // add doc
          list.add(wikidoc);

          // reset
          text = new StringBuffer();
          wikidoc = null;
          section = 0;
          newSection = false;
          continue;
        }

        // append

        text.append(line.trim()).append(" ");

      } // end while
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    // close resources
    try {
      bufferedReader.close();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    return list;
  }

  @Override
  public List<WikiDoc> call(final BufferedReader bufferedReader) {
    this.bufferedReader = bufferedReader;
    return call();
  }

  private String getId(final String line) {
    return line.substring(line.indexOf("<doc id=\"") + "<doc id=\"".length(),
        line.indexOf("url=\"") - "\" ".length()).trim();
  }

  private String getUrl(final String line) {
    return line
        .substring(line.lastIndexOf("url=\"") + "url=\"".length(), line.lastIndexOf("\" title="))
        .trim();
  }

  private String getTitle(final String line) {
    return line.substring(line.lastIndexOf("\" title=\"") + "\" title=\"".length(),
        line.lastIndexOf("\">")).trim();
  }
}
