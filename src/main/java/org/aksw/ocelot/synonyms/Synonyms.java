package org.aksw.ocelot.synonyms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.ocelot.core.wordembedding.Word2VecBinding;
import org.aksw.simba.knowledgeextraction.commons.nlp.PartOfSpeech;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipe;
import org.aksw.simba.knowledgeextraction.commons.wordnet.WordNetWrapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.CoreLabel;

/**
 * TODO: fetch labels from dbpedia and wikidata, remove stopwords, remove labels with more than one
 * word. apply word2vec to find more. use a score.
 *
 * Using word2vec and wordnet.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public class Synonyms implements ISynonyms {
  protected final static Logger LOG = LogManager.getLogger(Synonyms.class);


  private final WordNetWrapper synonymsWordnet = new WordNetWrapper();
  private final Word2VecBinding synonymsWord2Vec = new Word2VecBinding();

  private int max = 20;

  private final StanfordPipe stanfordNLP = StanfordPipe.instance();

  // cache (word to synonyms)
  private final Map<String, List<String>> cache = new HashMap<>();

  public Synonyms setMax(final int max) {
    this.max = max;
    cache.clear();
    return this;
  }

  @Override
  public List<String> getSynonyms(final String word) {

    final List<CoreLabel> labels = stanfordNLP.getLabels(word);
    final String lemma = getLemma(labels);
    final String pos = getPOS(labels);

    // word2vec synonyms
    List<String> w2v = null;
    if (!cache.containsKey(word)) {
      w2v = synonymsWord2Vec.synonymsWord2Vec(lemma, (max + 1) / 2);
      if (w2v == null) {
        w2v = new ArrayList<>();
      } else {
        w2v = toLemmas(w2v);
      }
      cache.put(word, w2v);
    }
    w2v = cache.get(word);
    LOG.trace("word2ve:  " + w2v.size());

    // wordnet synonyms
    List<String> wordnet = null;
    wordnet = synonymsWordnet.synonymsWordnet(word, PartOfSpeech.get(pos), max / 2);
    if (wordnet == null) {
      wordnet = new ArrayList<>();
    }
    LOG.trace("wordnet:  " + wordnet.size());

    //
    wordnet.addAll(w2v);
    return wordnet;
  }

  @Override
  public boolean checkSynonyms(final String wordA, final String wordB) {
    final List<String> sA = getSynonyms(wordA);
    final List<String> sB = getSynonyms(wordB);
    sA.retainAll(sB);
    return sA.size() > 0 ? true : false;
  }

  // private
  private List<String> toLemmas(final List<String> words) {
    final List<String> lemmas = new ArrayList<>();

    for (final String word : words) {
      final List<CoreLabel> labels = stanfordNLP.getLabels(word);
      final String lemma = getLemma(labels);
      if (!lemma.isEmpty() && !lemmas.contains(lemma)) {
        lemmas.add(lemma);
      }
    }
    return lemmas;
  }

  private String getLemma(final List<CoreLabel> labels) {
    if (!labels.isEmpty()) {
      final String add = stanfordNLP.getLemma(labels.get(0));
      if (add != null && !add.isEmpty()) {
        return add;
      }
    }
    return "";
  }

  private String getPOS(final List<CoreLabel> labels) {
    if (!labels.isEmpty()) {
      final String add = stanfordNLP.getPOS(labels.get(0));
      if (add != null && !add.isEmpty()) {
        return add;
      }
    }
    return "";
  }
}
