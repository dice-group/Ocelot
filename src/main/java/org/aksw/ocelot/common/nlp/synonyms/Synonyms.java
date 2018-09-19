package org.aksw.ocelot.common.nlp.synonyms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.data.properties.BackgroundKnowledge;
import org.aksw.ocelot.data.properties.PropertiesFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Deprecated
public class Synonyms {

  private final static Logger LOG = LogManager.getLogger(Synonyms.class);

  private final Map<String, Set<String>> propertyToSynonyms = new HashMap<>();

  private final OxfordDictionariesSynonyms oxford = new OxfordDictionariesSynonyms();
  private final WordnikSynonyms wordnikSynonyms = new WordnikSynonyms();
  private final WikidataLabels wikidataLabels = new WikidataLabels();

  /**
   * Uses {@link WikidataLabels} to get synonyms. In case there are no synonyms, the label of the
   * property is used to get synonyms with the help of {@link OxfordDictionariesSynonyms} and
   * {@link WordnikSynonyms} . <br>
   *
   * The results are stored in {@link #propertyToSynonyms}.
   */
  public Synonyms() {
    final BackgroundKnowledge p = PropertiesFactory.getInstance(PropertiesFactory.file);

    for (final String predicate : p.getPredicates()) {
      LOG.info(predicate);

      propertyToSynonyms.put(predicate, new HashSet<>(wikidataLabels.labels(predicate)));

      final Set<String> tmp = new HashSet<>();
      // each DBpedia label
      for (final String label : p.getLabels(predicate)) {
        tmp.add(label);

        // labels with multiwords
        if (label.contains(" ")) {

          final Map<String, Set<String>> partToSy = new HashMap<>();
          for (final String s : label.split(" ")) {
            partToSy.put(s, oxford.synonyms(s));
          }

        } else {
          Set<String> sy = oxford.synonyms(label);
          tmp.addAll(sy);
          sy = wordnikSynonyms.synonyms(label);
          tmp.addAll(sy);
        }
      }
      propertyToSynonyms.get(predicate).addAll(tmp);
    }
  }

  /**
   * Gets the synonyms for the given predicate.
   *
   * @param predicate a DBpedia predicate
   * @return set of synonyms
   */
  public Set<String> getPropertyToSynonyms(final String predicate) {
    return propertyToSynonyms.get(predicate);
  }

  public static void main(final String[] args) {
    final Synonyms synonyms = new Synonyms();
    synonyms.propertyToSynonyms.entrySet().forEach(LOG::info);
  }
}
