package org.aksw.ocelot.core.wordnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ocelot.common.nlp.pos.PartOfSpeech;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import rita.RiWordNet;

public class WordNetWrapper {
  protected static Logger LOG = LogManager.getLogger(WordNetWrapper.class);

  protected final RiWordNet wordnet;

  public static void main(final String[] a) {

    final String path = "/home/rspeck/Data/WordNet-3.1";

    final WordNetWrapper wordNetWrapper = new WordNetWrapper(path);
    Collection<String> synonyms;
    synonyms = wordNetWrapper.synonymsWordnet("ceo", PartOfSpeech.NOUN, 10);
    synonyms.forEach(LOG::info);
  }

  final Set<PartOfSpeech> verbs = new HashSet<>(Arrays.asList(//
      PartOfSpeech.VERB, //
      PartOfSpeech.VERB_MODAL, //
      PartOfSpeech.VERB_PARTICIPLE_PAST, //
      PartOfSpeech.VERB_PARTICIPLE_PRESENT, //
      PartOfSpeech.VERB_PAST_TENSE, //
      PartOfSpeech.VERB_SINGULAR_PRESENT_NONTHIRD_PERSON, //
      PartOfSpeech.VERB_SINGULAR_PRESENT_THIRD_PERSON));

  final Set<PartOfSpeech> nouns = new HashSet<>(Arrays.asList(//
      PartOfSpeech.NOUN, //
      PartOfSpeech.NOUN_PLURAL, //
      PartOfSpeech.NOUN_PROPER_PLURAL, //
      PartOfSpeech.NOUN_PROPER_SINGULAR));

  /**
   * Get n synonyms.
   *
   * @param word
   * @param pos
   * @param n
   * @return unsorted
   */
  public List<String> synonymsWordnet(final String word, final PartOfSpeech pos, final int n) {
    List<String> synonyms = new ArrayList<>();

    if (verbs.contains(pos)) {
      synonyms.addAll(getSynonymsForVERB(word, n));

    } else if (nouns.contains(pos)) {
      synonyms.addAll(getSynonymsForNOUN(word, n));
    }

    synonyms = synonyms.stream()//
        // .filter(entry -> !entry.contains(" ")) //
        .limit(n)//
        .collect(Collectors.toList());
    return synonyms;
  }

  /**
   *
   * Constructor.
   *
   * @param wordNet directory
   */
  public WordNetWrapper(final String wordNet) {
    wordnet = new RiWordNet(wordNet);
  }

  /**
   * Gets verbs and nouns.
   *
   * @param word
   * @return LinkedHashSet
   */
  public Set<String> getAllSynonyms(final String word, final int max) {
    final Set<String> set = new LinkedHashSet<>();
    set.addAll(getSynonymsForVERB(word, (max + 1) / 2));
    set.addAll(getSynonymsForNOUN(word, max / 2));
    return set;
  }

  /**
   * Gets nouns.
   *
   * @param word
   * @return LinkedHashSet
   */
  public Set<String> getSynonymsForNOUN(final String word, final int maxResults) {
    return getAllSynonyms(word, RiWordNet.NOUN, maxResults);
  }

  /**
   * Gets verbs.
   *
   * @param word
   * @return LinkedHashSet
   */
  public Set<String> getSynonymsForVERB(final String word, final int maxResults) {
    return getAllSynonyms(word, RiWordNet.VERB, maxResults);
  }

  private Set<String> getAllSynonyms(final String word, final String posStr, final int maxResults) {

    final Set<String> set = new LinkedHashSet<>();

    String[] result = wordnet.getAllSynsets(word, posStr);
    addSynsetsToSet(result, set, maxResults);
    LOG.trace(set);
    result = wordnet.getAllHyponyms(word, posStr);
    addSynsetsToSet(result, set, maxResults);
    LOG.trace(set);
    result = wordnet.getAllHypernyms(word, posStr);
    addSynsetsToSet(result, set, maxResults);
    LOG.trace(set);
    result = wordnet.getAllSimilar(word, posStr);
    addSynsetsToSet(result, set, maxResults);
    LOG.trace(set);
    // result = wordnet.getAllAlsoSees(word, posStr);
    // addSynsetsToSet(result, set, maxResults);

    // result = wordnet.getAllCoordinates(word, posStr);
    // addSynsetsToSet(result, set, maxResults);
    return set;
  }

  private void addSynsetsToSet(final String[] synsets, final Set<String> results,
      final int maxResults) {
    for (final String synset : synsets) {
      if (synset.indexOf(RiWordNet.SYNSET_DELIM) > 0) {
        for (final String split : synset.split(RiWordNet.SYNSET_DELIM)) {
          results.add(split);
          if (results.size() >= maxResults) {
            return;
          }
        }
      } else {
        results.add(synset);
        if (results.size() >= maxResults) {
          return;
        }
      }
    }
  }
}
