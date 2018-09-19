package org.aksw.ocelot.classify;

import java.io.NotSerializableException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.application.ApplicationUtil;
import org.aksw.ocelot.common.io.SerializationUtil;
import org.aksw.ocelot.common.nlp.synonyms.OxfordDictionariesSynonyms;
import org.aksw.ocelot.common.nlp.synonyms.WikidataLabels;
import org.aksw.ocelot.common.nlp.synonyms.WordnikSynonyms;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.properties.BackgroundKnowledge;
import org.aksw.ocelot.data.properties.PropertiesFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PredicateSurfaceforms {
  final static Logger LOG = LogManager.getLogger(PredicateSurfaceforms.class);

  protected static String storeFolder = "serial";

  protected static String storeFile = "dbpedialabels.bin";
  protected static String oxfordDictionariesSynonymsFile = "OxfordDictionariesSynonyms.bin";
  protected static String wordnikSynonymsFile = "WordnikSynonyms.bin";

  protected BackgroundKnowledge bgk = PropertiesFactory.getInstance();

  protected final WikidataLabels wd = new WikidataLabels();
  protected final OxfordDictionariesSynonyms ofd = new OxfordDictionariesSynonyms();
  protected final WordnikSynonyms ws = new WordnikSynonyms();

  protected Map<String, Set<String>> dbpediaLabels = null;
  protected Map<String, Set<String>> oxfordLabels = null;
  protected Map<String, Set<String>> wordnikLabels = null;
  protected Map<String, Set<String>> allLabels = null;

  /**
   * wikidata and dbpedia lables.
   *
   * @return
   */
  public Map<String, Set<String>> getDBpediaLabels() {
    return dbpediaLabels;
  }

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {

    final PredicateSurfaceforms psf = new PredicateSurfaceforms();
    psf.getAllLabels().entrySet().forEach(LOG::info);
  }

  /**
   * Gets for all predicates the DBpedia and Wikidata labels and serializes them, in case the
   * serialization not already exists.
   */
  @SuppressWarnings("unchecked")
  public PredicateSurfaceforms() {

    SerializationUtil.setRootFolder(storeFolder);

    dbpediaLabels = SerializationUtil.deserialize(storeFile, HashMap.class);
    if (dbpediaLabels == null) {
      // init folder
      final Path file = Paths.get(Const.RELATION_FILE).normalize();
      final String folder = (file.toString().substring(0, file.toString().indexOf("/")));

      // get the labels
      final Set<Path> predicateFiles = ApplicationUtil.getAllPredicateFiles(folder);
      final Set<String> ps = ApplicationUtil.getAllPredicates(predicateFiles);
      dbpediaLabels = getLabels(ps);

      // store the labels
      try {
        SerializationUtil.serialize(storeFile, dbpediaLabels, false);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
  }

  /**
   * Gets for all DBpedia and Wikidata labels the synonymes from Wordnik and Oxford.
   */
  public Map<String, Set<String>> getAllLabels() {
    getWordnikSynonyms();
    getOxfordDictionariesSynonyms();

    allLabels = new HashMap<>();
    for (final Entry<String, Set<String>> entry : dbpediaLabels.entrySet()) {

      allLabels.put(entry.getKey(), new HashSet<>());
      allLabels.get(entry.getKey()).addAll(entry.getValue());

      final Set<String> x = oxfordLabels.get(entry.getKey());
      final Set<String> y = wordnikLabels.get(entry.getKey());

      if (!x.isEmpty()) {

        allLabels.get(entry.getKey()).addAll(x);
      }
      if (!y.isEmpty()) {
        allLabels.get(entry.getKey()).addAll(y);
      }
    }
    return allLabels;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Set<String>> getWordnikSynonyms() {
    wordnikLabels = SerializationUtil.deserialize(wordnikSynonymsFile, HashMap.class);
    if (wordnikLabels == null) {
      wordnikLabels = new HashMap<>();

      // for all dbpedia
      for (final Entry<String, Set<String>> entry : dbpediaLabels.entrySet()) {

        final Set<String> labels = entry.getValue();
        final String p = entry.getKey();

        wordnikLabels.put(p, new HashSet<>());

        for (final String label : labels) {
          final Set<String> l = ws.synonyms(label);
          wordnikLabels.get(p).addAll(l);
        }
      }

      // store the labels
      try {
        SerializationUtil.serialize(wordnikSynonymsFile, wordnikLabels, false);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return wordnikLabels;
  }

  /**
   * OxfordDictionaries
   */
  @SuppressWarnings("unchecked")
  protected Map<String, Set<String>> getOxfordDictionariesSynonyms() {

    oxfordLabels = SerializationUtil.deserialize(oxfordDictionariesSynonymsFile, HashMap.class);
    if (oxfordLabels == null) {
      oxfordLabels = new HashMap<>();
      // for all dbpedia
      for (final Entry<String, Set<String>> entry : dbpediaLabels.entrySet()) {

        final Set<String> labels = entry.getValue();
        final String p = entry.getKey();

        oxfordLabels.put(p, new HashSet<>());

        for (final String label : labels) {
          final Set<String> l = ofd.synonyms(label);
          oxfordLabels.get(p).addAll(l);
        }
      }
      // store the labels
      try {
        SerializationUtil.serialize(oxfordDictionariesSynonymsFile, oxfordLabels, false);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return oxfordLabels;
  }

  /**
   * Gets the labels from dbpedia and wikidata for the given predicates.
   *
   * @param predicates
   * @return
   */
  protected Map<String, Set<String>> getLabels(final Set<String> predicates) {
    final Map<String, Set<String>> map = new HashMap<>();
    for (final String predicate : predicates) {
      final Set<String> labels = bgk.getLabels(predicate);
      labels.addAll(wd.labels(predicate));
      map.put(predicate, labels);
    }
    return map;
  }

}
