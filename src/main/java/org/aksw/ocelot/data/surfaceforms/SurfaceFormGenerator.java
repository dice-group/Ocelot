package org.aksw.ocelot.data.surfaceforms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.data.Const;
import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.aksw.simba.knowledgeextraction.commons.lang.CollectionUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

/**
 * Creates surface forms from DBpedia files.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class SurfaceFormGenerator implements ISurfaceForms {

  public static final Logger LOG = LogManager.getLogger(SurfaceFormGenerator.class);

  private static final int MAXIMUM_SURFACE_FORM_LENGHT = Integer.MAX_VALUE;

  private static final String LANG = "en";
  private static final String ex = ".tql";

  public static final String REDIRECTS_FILE = Const.DBPEDIA_FOLDER + "redirects_" + LANG + ex;

  public static final String LABELS_FILE = Const.DBPEDIA_FOLDER + //
      "labels_" + LANG + ex;

  public static final String DISAMBIGUATIONS_FILE = Const.DBPEDIA_FOLDER + //
      "disambiguations_" + LANG + ex;

  // public static final String INTERLANGUAGE_LINKS = Const.DBPEDIA_FOLDER + //
  // "interlanguage_links_" + LANG + ".ttl";

  public static final Path STOPWORDS = Paths.get(Const.DBPEDIA_FOLDER + "stopwords.txt");

  // output file
  public static final String SURFACE_FORMS_FILE =
      Const.DBPEDIA_FOLDER + LANG + Const.DBPEDIA_SURFACEFORMS_FILE;

  private static Set<String> LOWERCASE_STOPWORDS = null;
  private Map<String, Set<String>> surfaceForms = null;

  /**
   * Test
   *
   * @param a
   */
  public static void main(final String[] a) {
    final ISurfaceForms s = new SurfaceFormGenerator();
    final Set<String> surfaceForms = s.getSurfaceform("http://dbpedia.org/resource/Will_Smith");
    surfaceForms.forEach(LOG::info);
  }

  /**
   *
   * Constructor.
   *
   */
  public SurfaceFormGenerator() {

    // read stopwords file
    LOWERCASE_STOPWORDS = new HashSet<>(CollectionUtil.toLowerCase(//
        FileUtil.fileToListCatched(STOPWORDS, "UTF-8", "#")//
    ));
    LOG.info(LOWERCASE_STOPWORDS.size() + " lowercase stopwords found.");
  }

  @Override
  public Set<String> getSurfaceform(final String uri) {
    return getSurfaceForms().get(uri);
  }

  public Map<String, Set<String>> getReverseForms(final Map<String, Set<String>> sf) {
    final Map<String, Set<String>> reverse = new HashMap<>();
    for (final Entry<String, Set<String>> entry : sf.entrySet()) {
      for (final String v : entry.getValue()) {
        if (reverse.get(v) == null) {
          reverse.put(v, new HashSet<String>());
        }
        reverse.get(v).add(entry.getKey());
      }
    }
    return reverse;
  }

  protected Map<String, Set<String>> readSurfaceFormsFromFile(final String lang, final Path file) {
    final BufferedReader br = FileUtil.getBufferedReader(file);
    final Map<String, Set<String>> uriToLabel = new HashMap<>();
    br.lines().forEach(line -> {
      if (!line.startsWith("#")) {
        final String[] lineParts = line.split("\t");
        final String[] surfaceFormsPart = Arrays.copyOfRange(lineParts, 1, lineParts.length);
        final Set<String> filteredSurfaceForms = new HashSet<>();
        for (final String surfaceForm : surfaceFormsPart) {
          if (surfaceForm.length() <= MAXIMUM_SURFACE_FORM_LENGHT) {
            filteredSurfaceForms.add(surfaceForm);
          }
        }
        uriToLabel.put(lineParts[0], filteredSurfaceForms);
        uriToLabel.put(lineParts[0].replace("http://en.", "http://"), filteredSurfaceForms);
      }
    });
    LOG.info("Finished intializing surface forms! Found uris with sfs: " + uriToLabel.size());
    return uriToLabel;
  }

  /**
   * Creates concept URIs. <br>
   * Reads subject URIs in {@link #LABELS_FILE} and ignores all URIs that are in
   * {@link #REDIRECTS_FILE} or {@link #DISAMBIGUATIONS_FILE}. .
   *
   * @return set with concept URIs.
   */
  protected Set<String> createConceptUris() {
    final Set<String> badUris = new HashSet<>();
    badUris.addAll(getSubjectsFromNTriple(REDIRECTS_FILE));
    badUris.addAll(getSubjectsFromNTriple(DISAMBIGUATIONS_FILE));

    final Set<String> conceptUris = new HashSet<>();
    final NxParser n3Parser = openNxParser(LABELS_FILE);
    while (n3Parser.hasNext()) {
      final String subjectUri = n3Parser.next()[0].toString();
      final String subjectUriWihtoutPrefix =
          subjectUri.substring(subjectUri.lastIndexOf("/") + "/".length());

      if (isGoodUri(subjectUriWihtoutPrefix) && !badUris.contains(subjectUri)) {
        conceptUris.add(subjectUri);
      }
    }
    LOG.info("Concept Uris total sentences: " + conceptUris.size());
    return conceptUris;
  }

  public boolean isGoodSurfaceForm(final String surfaceForm) {

    final boolean a = surfaceForm.length() > MAXIMUM_SURFACE_FORM_LENGHT;
    final boolean b = surfaceForm.matches("^[\\W\\d]+$");

    String warn = "";
    if (a || b) {
      warn = "Surfaceform: " + surfaceForm
          + " is not a good surface form because its too long or regex match.";
    }

    if (warn.isEmpty()) {
      warn = "Surfaceform: " + surfaceForm
          + " is not a good surface form because it contains only stop words.";

      final Set<String> token = CollectionUtil.toSet(surfaceForm.toLowerCase().split(" "));
      token.removeAll(LOWERCASE_STOPWORDS);
      if (!token.isEmpty()) {
        warn = "";
      }
    }

    if (!warn.isEmpty()) {
      LOG.warn(warn);
    }
    return warn.isEmpty();
  }

  protected String createCleanSurfaceForm(final String label) {
    try {
      String newLabel = URLDecoder.decode(label, "UTF-8");
      newLabel = newLabel.replaceAll("_", " ").replaceAll(" +", " ").trim();
      newLabel = newLabel.replaceAll(" \\(.+?\\)$", "");
      return isGoodSurfaceForm(newLabel) ? newLabel : "";
    } catch (final IllegalArgumentException | UnsupportedEncodingException e) {
      LOG.error(e.getLocalizedMessage(), e);
      return null;
    }
  }

  protected void addSurfaceForm(final Map<String, Set<String>> surfaceForms, final String uri,
      final String value) {

    final String sf = createCleanSurfaceForm(value);

    if (sf != null && !sf.trim().isEmpty()) {
      if (!surfaceForms.containsKey(uri)) {
        surfaceForms.put(uri, new HashSet<String>());
      }
      surfaceForms.get(uri).add(sf);
    }
  }

  public Map<String, Set<String>> getSurfaceForms() {
    if (surfaceForms == null) {
      // deserialize
      if (FileUtil.fileExists(SURFACE_FORMS_FILE)) {
        LOG.info("SurfaceForms exists! Read surfaceforms from file.");
        surfaceForms = readSurfaceFormsFromFile(LANG, Paths.get(SURFACE_FORMS_FILE));
        LOG.info("Done read surfaceforms from file.");
      }
    }
    return surfaceForms != null ? surfaceForms : _getSurfaceForms();
  }

  /**
   *
   */
  protected Map<String, Set<String>> _getSurfaceForms() {
    surfaceForms = new HashMap<>();

    final Set<String> conceptUris = createConceptUris();
    LOG.info("conceptUris size: " + conceptUris.size());

    // first add all uris of the concept uris
    for (final String uri : conceptUris) {
      addSurfaceForm(surfaceForms, uri, uri.substring(uri.lastIndexOf("/") + 1));
    }

    LOG.info("Finished adding all conceptUris: " + surfaceForms.size());

    final List<String[]> subjectToObject =
        getSubjectAndObjectsFromNTriple(DISAMBIGUATIONS_FILE, "");
    subjectToObject.addAll(getSubjectAndObjectsFromNTriple(REDIRECTS_FILE, ""));

    for (final String[] subjectAndObject : subjectToObject) {
      final String subject = subjectAndObject[0];
      final String object = subjectAndObject[1];
      if (conceptUris.contains(object) && !object.contains("%")) {
        addSurfaceForm(surfaceForms, object, subject.substring(subject.lastIndexOf("/") + 1));
      }
    }
    LOG.info("Finished generation of surface forms.");

    LOG.info("size: " + surfaceForms.size());
    // surfaceForms = removeDisamb(surfaceForms);
    // LOG.info("size: " + surfaceForms.size());

    writeFile(surfaceForms);
    LOG.info("Finished writing of surface forms to disk.");

    return surfaceForms;
  }

  protected void writeFile(final Map<String, Set<String>> surfaceforms) {
    // write the file
    final BufferedWriter writer = FileUtil.openFileToWrite(SURFACE_FORMS_FILE, "UTF-8");
    for (final Map.Entry<String, Set<String>> entry : surfaceForms.entrySet()) {
      try {
        writer.write(entry.getKey() + "\t"
            + StringUtils.join(CollectionUtil.addNonAccent(entry.getValue()), "\t") + "\n");
      } catch (final IOException e) {
        LOG.error("\n", e);
      }
    }
    try {
      writer.close();
    } catch (final IOException e) {
      LOG.error("\n", e);
    }

  }

  /**
   * <code>
  protected Map<String, Set<String>> removeDisamb(final Map<String, Set<String>> surfaceforms) {

     final Set<String> remove = new HashSet<>();
     final Map<String, String> surface2uri = new HashMap<>();
     for (final Entry<String, Set<String>> uri2surfaces : surfaceforms.entrySet()) {
  
       for (final String surface : uri2surfaces.getValue()) {
         final String currentUri = surface2uri.get(surface);
         if (currentUri == null) {
           surface2uri.put(surface, uri2surfaces.getKey());
         } else if (!currentUri.equals(uri2surfaces.getKey())) {
           remove.add(surface);
         }
       }
     }
  
     LOG.info("remove size: " + remove.size());
     for (final String r : remove) {
       LOG.info(r);
       surface2uri.remove(r);
     }
  
     surfaceforms = new HashMap<>();
     for (final Entry<String, String> e : surface2uri.entrySet()) {
       if (surfaceforms.get(e.getValue()) == null) {
         surfaceforms.put(e.getValue(), new HashSet<String>());
       }
       surfaceforms.get(e.getValue()).add(e.getKey());
     }
     return surfaceforms;
   }
  </code>
   */

  protected List<String> getSubjectsFromNTriple(final String filename) {
    return getSubjectsFromNTriple(filename, "");
  }

  protected List<String> getSubjectsFromNTriple(final String filename, final String replacePrefix) {
    final List<String> results = new ArrayList<>();
    final NxParser nxp = openNxParser(filename);
    while (nxp.hasNext()) {
      final String ns = nxp.next()[0].toString();
      results.add(replacePrefix.equals("") ? ns : ns.replace(replacePrefix, ""));
    }
    return results;
  }

  protected List<String[]> getSubjectAndObjectsFromNTriple(final String filename) {
    return getSubjectAndObjectsFromNTriple(filename, "");
  }

  protected List<String[]> getSubjectAndObjectsFromNTriple(final String filename,
      final String replacePrefix) {
    final List<String[]> results = new ArrayList<>();
    final NxParser nxp = openNxParser(filename);
    while (nxp.hasNext()) {
      final Node[] ns = nxp.next();
      results.add(new String[] {
          replacePrefix.equals("") ? ns[0].toString() : ns[0].toString().replace(replacePrefix, ""),
          replacePrefix.equals("") ? ns[2].toString()
              : ns[2].toString().replace(replacePrefix, ""),});
    }
    return results;
  }

  protected NxParser openNxParser(final String filename) {
    try {
      return new NxParser(new FileInputStream(filename));
    } catch (final Exception e) {
      final String error = "Could not open file " + filename;
      LOG.error(error, e);
      throw new RuntimeException(error, e);
    }
  }

  protected boolean isGoodUri(final String uri) {
    if (uri.contains("List_of_") || uri.contains("(Disambiguation)") || uri.contains("/")
        || uri.contains("%23") || uri.matches("^[\\W\\d]+$")) {
      LOG.info("not a valid uri " + uri);
      return false;
    }
    return true;
  }
}
