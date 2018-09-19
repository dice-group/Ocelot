package org.aksw.ocelot.data.kb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.common.io.SparqlExecution;
import org.aksw.ocelot.common.io.WriteAndReadFile;
import org.aksw.ocelot.data.Const;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class DBpediaKB extends SparqlExecution {

  String queryLabelsStringFormat = "";

  /**
   * Initializes with default parameter.
   *
   */
  public DBpediaKB() {
    super(DBpedia.url, DBpedia.graph, DBpedia.pagination, DBpedia.delay);

    try {
      queryLabelsStringFormat = new String(Files.readAllBytes(Paths.get(DBpedia.queryLabels)));
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Runs SPARQL if there is no serialization and serializes the results.
   *
   * @param predicate
   * @param max
   *
   * @return triples
   */
  public Set<Triple> getTriples(final String predicate, final int max) {

    final File file = new File(Const.TMP_FOLDER.concat(File.separator)//
        .concat(predicate.replace("http://", "_")) //
        .concat((max == Integer.MAX_VALUE) ? "all" : String.valueOf(max)) //
        .concat("_triples.json"));

    // read file
    JSONArray ja = WriteAndReadFile.readToJSONArray(file);

    // no file found
    if (ja != null) {
      LOG.info("Using the triples from file, with an array length of: " + ja.length());
    } else {
      try {
        final String query = String.format(//
            new String(Files.readAllBytes(Paths.get(DBpedia.queryTriples))), //
            predicate, max);

        ja = execSelectToJSONArray(DBpedia.PREFIX.concat(" ").concat(query));

        // writes file
        final boolean done = WriteAndReadFile.writeFile(ja, file);
        if (done) {
          LOG.info("File is written.");
        } else {
          LOG.info("Could not write file.");
        }
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return parseTriples(ja, predicate);
  }

  private Set<Triple> parseTriples(final JSONArray ja, final String predicate) {
    LOG.info("parseTriples ...");
    final Set<Triple> triplesSet = new HashSet<>();

    // check parameter
    if ((ja == null) || (ja.length() == 0)) {
      return triplesSet;
    }

    final Map<String, Set<String>> uriToLabels = new HashMap<>();
    final Map<String, Set<String>> subjecToObjecs = new HashMap<>();

    for (int i = 0; i < ja.length(); i++) {
      final String s = ja.getJSONObject(i).getJSONObject("ss").getString("value");
      final String o = ja.getJSONObject(i).getJSONObject("oo").getString("value");

      final String slabel = ja.getJSONObject(i).getJSONObject("slabel").getString("value");
      final String olabel = ja.getJSONObject(i).getJSONObject("olabel").getString("value");

      if (uriToLabels.get(s) == null) {
        uriToLabels.put(s, new HashSet<>());
      }
      if (uriToLabels.get(o) == null) {
        uriToLabels.put(o, new HashSet<>());
      }

      uriToLabels.get(s).add(slabel);
      uriToLabels.get(o).add(olabel);

      if (subjecToObjecs.get(s) == null) {
        subjecToObjecs.put(s, new HashSet<>());
      }
      subjecToObjecs.get(s).add(o);
    } // end for

    for (final Entry<String, Set<String>> entry : subjecToObjecs.entrySet()) {
      final String sub = entry.getKey();
      for (final String obj : entry.getValue()) {
        final Triple triple = new Triple(sub, predicate, obj);
        triple.getSubjectSFs().addAll(uriToLabels.get(sub));
        triple.getObjectSFs().addAll(uriToLabels.get(obj));

        triplesSet.add(triple);
      }
    } // end for

    LOG.info("The triples are paresed with a length of: " + triplesSet.size());
    return triplesSet;
  }

  public Set<String> getLabels(final String uris) {
    final Set<String> set = new HashSet<>();
    set.add(uris);
    return getLabels(set).get(uris);
  }

  /**
   * English labels.
   *
   * @param uris encoded
   * @return encoded uri to labels
   */
  public Map<String, Set<String>> getLabels(final Set<String> uris) {
    final Map<String, Set<String>> uriToLabels = new HashMap<>();
    // build query
    String query = null;
    try {
      {
        // build sparql filter
        final StringBuilder sb = new StringBuilder();
        {
          // ?r = dbr:Berlin || ?r = dbr:Leipzig || ...
          for (final Iterator<String> iter = uris.iterator(); iter.hasNext();) {
            sb.append("?r = <").append(iter.next()).append(">");
            if (iter.hasNext()) {
              sb.append(" || ");
            }
          }
        }

        // query
        query = String.format(queryLabelsStringFormat, sb.toString());
      }
      if (query != null) {
        final JSONArray ja = execSelectToJSONArray(DBpedia.PREFIX.concat(query));

        for (int i = 0; i < ja.length(); i++) {
          final JSONObject jo = ja.getJSONObject(i);
          final String uri = jo.getJSONObject("r").getString("value");
          final String label = jo.getJSONObject("label").getString("value");

          if (uriToLabels.get(uri) == null) {
            uriToLabels.put(uri, new HashSet<>());
          }
          uriToLabels.get(uri).add(label);
        }
      }
    } catch (final Exception e) {
      LOG.debug(e.getLocalizedMessage());
      LOG.info(uris);
    }
    return uriToLabels;
  }
}
