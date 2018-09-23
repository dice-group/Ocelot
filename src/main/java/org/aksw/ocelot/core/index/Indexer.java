package org.aksw.ocelot.core.index;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aksw.ocelot.common.io.FileUtil;
import org.aksw.ocelot.common.lang.CollectionUtil;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.wikipedia.IDataExtractor;
import org.aksw.ocelot.data.wikipedia.WikiDoc;
import org.aksw.ocelot.data.wikipedia.WikipediaExtractor;
import org.aksw.ocelot.share.EnumSolrWikiIndex;
import org.aksw.simba.knowledgeextraction.commons.nlp.StanfordPipeExtended;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Indexer {

  protected final static Logger LOG = LogManager.getLogger(Indexer.class);

  protected final IIndex index;
  protected final IDataExtractor wikipediaExtractor;

  /**
   * Initializes a {@link SolrIndex} instance as {@link IIndex} and a {@link WikipediaExtractor}
   * instance as {@link IDataExtractor}.
   */
  public Indexer() {
    index = new SolrIndex();
    wikipediaExtractor = new WikipediaExtractor();
  }

  /**
   * Gets a new instance of {@link StanfordCoreNLP}.
   *
   * @return StanfordCoreNLP instance
   */
  protected StanfordCoreNLP getPipe() {
    final Properties props = new Properties();
    // props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
    props.setProperty("tokenize.language", "en");
    props.setProperty("ner.applyNumericClassifiers", "false");
    props.setProperty("ner.useSUTime", "false");
    // props.setProperty("ner.applyNumericClassifiers", "true"); // en only
    // props.setProperty("ner.useSUTime", "true");
    // props.setProperty("sutime.markTimeRanges", "true");
    // props.setProperty("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
    props.setProperty("ner.model",
        "edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz");
    // props.setProperty("dcoref.score", "false");
    // props.setProperty("dcoref.big.gender.number","edu/stanford/nlp/models/dcoref/gender.map.ser.gz");
    // props.setProperty("dcoref.use.big.gender.number", "true");
    // props.setProperty("dcoref.maxdist", "-1");
    // props.setProperty("dcoref.postprocessing", "true");
    return new StanfordCoreNLP(props);
  }

  /**
   *
   * @param files
   */
  public void createIndex(final Set<Path> files) {
    for (final Path file : files) {
      LOG.info("File:" + file);

      final List<WikiDoc> wikidocs = wikipediaExtractor.call(FileUtil.getBufferedReader(file));
      LOG.info("Wikidocs size: " + wikidocs.size());

      createIndex(wikidocs);
    }
  }

  // parallel
  public void createIndex(final List<WikiDoc> wikidocs) {
    ExecutorService executorServiceW;
    CompletionService<Map<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>>> completionServiceW;
    executorServiceW = Executors.newFixedThreadPool(Const.INDEX_FIXED_THREAD_POOL_SIZE);
    completionServiceW = new ExecutorCompletionService<>(executorServiceW);

    final Map<Future<Map<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>>>, Integer> futures;
    futures = new HashMap<>();

    // parallel process the wiki docs
    for (int i = 0; i < wikidocs.size(); i++) {
      final WikiDoc doc = wikidocs.get(i);
      futures.put(completionServiceW.submit(() -> {
        return annotations(doc.sectionText);
      }), i);
    }
    executorServiceW.shutdown();

    // process the results
    for (int ii = 0; ii < wikidocs.size(); ++ii) {
      try {
        // sentence num to data
        final Future<Map<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>>> future =
            completionServiceW.poll(Const.INDEX_NLP_TIMEOUT, TimeUnit.SECONDS);

        if (future == null) {
          LOG.warn("Timeout ...");
        } else {

          final Map<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>> sentencesToAnnos =
              future.get();

          if (sentencesToAnnos != null) {
            for (final Entry<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>> sentenceToAnnos : sentencesToAnnos
                .entrySet()) {

              // sentenceToAnnos key and value
              final SimpleEntry<Integer, Integer> key = sentenceToAnnos.getKey();
              final int sentenceId = key.getKey();
              final int sec = key.getValue();

              final SimpleEntry<String, Map<String, List<Object>>> annos;
              annos = sentenceToAnnos.getValue();

              final String wikiDocNummber = String.valueOf(futures.get(future));

              // create doc
              final SolrInputDocument inDoc;
              // id
              final String id = wikidocs.get(Integer.parseInt(wikiDocNummber)).url.split("=")[1];
              // doc id + sentence nr
              final String idSent = wikidocs.get(Integer.parseInt(wikiDocNummber)).url.concat("_")
                  .concat(String.valueOf(sentenceId));

              inDoc = createDocument(id, idSent, String.valueOf(sentenceId), sec, annos);
              // add to index
              index.add(inDoc);
            }
          }
          // log
          if (ii % (wikidocs.size() / 10 + 1) == 0 || ii == wikidocs.size()) {
            LOG.info("+ 10% done ... service (sentence) " + ii);
          }
        }
      } catch (final InterruptedException | ExecutionException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }

    }
    LOG.info("CorpusIndex commit.");
    index.commit();
    LOG.info("CorpusIndex commit done.");

  }

  /**
   * Annotates the given text. Ignores sentences longer than 100 and shorter than 4.
   *
   * @param text
   * @return
   */
  protected Map<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>> annotations(
      final Map<Integer, String> sections) {

    final StanfordCoreNLP pipe = getPipe();
    int sentenceNr = 1;

    // stores sentence num and section num -> sentences and annotations
    final Map<SimpleEntry<Integer, Integer>, SimpleEntry<String, Map<String, List<Object>>>> results;
    results = new HashMap<>();

    // for each section
    for (final Integer sec : sections.keySet()) {
      final String text = sections.get(sec);
      // annotate the sentences
      if (text != null && !text.isEmpty()) {
        final Annotation ann = new Annotation(text);
        if (ann != null) {

          // we create new objects because sometimes in a sentence if the 1st word is a NE it will
          // not
          // be found, this tries to be w workaround
          pipe.annotate(ann);

          for (final CoreMap sentence : ann.get(SentencesAnnotation.class)) {
            // IndexStatistic.sentence_total++;

            final Map<String, List<Object>> annos = new HashMap<>();
            annos.put(name(PartOfSpeechAnnotation.class), new ArrayList<>());
            annos.put(name(TokensAnnotation.class), new ArrayList<>());
            annos.put(name(NamedEntityTagAnnotation.class), new ArrayList<>());
            annos.put(name(LemmaAnnotation.class), new ArrayList<>());
            annos.put(name(IndexAnnotation.class), new ArrayList<>());

            // we remember sentences with at least one NE
            // boolean hasEntities = false;
            for (final CoreLabel token : sentence.get(TokensAnnotation.class)) {
              annos.get(name(TokensAnnotation.class)).add(token.originalText());
              annos.get(name(PartOfSpeechAnnotation.class))
                  .add(token.getString(PartOfSpeechAnnotation.class));
              annos.get(name(NamedEntityTagAnnotation.class))
                  .add(token.get(NamedEntityTagAnnotation.class));
              annos.get(name(LemmaAnnotation.class)).add(token.getString(LemmaAnnotation.class));
              annos.get(name(IndexAnnotation.class)).add(token.beginPosition());

              // if (!token.get(NamedEntityTagAnnotation.class).equals(StanfordPipeExtended.NO_NER))
              // {
              // hasEntities = true;
              // }
            }

            // if (anntotations.get(name(TokensAnnotation.class)).size() < 100) {
            // IndexStatistic.sentences_shorter_than_one_hundred_token++;
            // }
            // if (anntotations.get(name(TokensAnnotation.class)).size() > 3) {
            // IndexStatistic.sentences_longer_than_three_token++;
            // }
            // if (hasEntities) {
            // IndexStatistic.sentences_with_ner++;
            // }
            // sentence length frequent
            final int sentencelength = annos.get(name(TokensAnnotation.class)).size();
            // final Integer value = IndexStatistic.sentenceLengthToFrequent.get(sentencelength);
            // IndexStatistic.sentenceLengthToFrequent.put(sentencelength,
            // value == null ? 1 : 1 + sentencelength);

            if (sentencelength < 100 && sentencelength > 3) {
              IndexStatistic.sentence_used++;

              results.put(//
                  new SimpleEntry<>(sentenceNr, sec), //
                  new SimpleEntry<>(//
                      sentence.toString(), //
                      annos//
                  ));

              sentenceNr++;
            }
          } // end each sentence
        }
      }
    } // end each section
    return results;
  }

  public static String name(final Class<?> cl) {
    return cl.getSimpleName();
  }

  /**
   * Creates a {@link SolrInputDocument} object with fields of {@link EnumSolrWikiIndex}.
   *
   * @return {@link SolrInputDocument} object with fields of {@link EnumSolrWikiIndex}.
   */
  protected SolrInputDocument createDocument(final String docnr, final String id,
      final String sentencenr, final int sec,
      final SimpleEntry<String, Map<String, List<Object>>> annos) {

    final String sentence = annos.getKey();
    final Collection<Object> token = annos.getValue().get(name(TokensAnnotation.class));
    final Collection<Object> ner = annos.getValue().get(name(NamedEntityTagAnnotation.class));
    final Collection<Object> lemma = annos.getValue().get(name(LemmaAnnotation.class));
    final Collection<Object> pos = annos.getValue().get(name(PartOfSpeechAnnotation.class));
    final Collection<Object> index = annos.getValue().get(name(IndexAnnotation.class));

    // fills a collection with NEs or words
    final Collection<Object> tokenNER = new ArrayList<>();

    final Iterator<Object> iterToken = token.iterator();
    for (final Iterator<Object> iter = ner.iterator(); iter.hasNext();) {
      final Object entityType = iter.next();
      final Object word = iterToken.next();
      tokenNER.add(entityType.equals(StanfordPipeExtended.NO_NER) ? word : entityType);
    }

    final SolrInputDocument doc = new SolrInputDocument();
    doc.addField(EnumSolrWikiIndex.DOCNR.getName(), docnr);
    doc.addField(EnumSolrWikiIndex.SENTENCENR.getName(), sentencenr);
    doc.addField(EnumSolrWikiIndex.SECTION.getName(), sec);
    doc.addField(EnumSolrWikiIndex.ID.getName(), id);
    doc.addField(EnumSolrWikiIndex.SENTENCE.getName(), sentence);
    doc.addField(EnumSolrWikiIndex.SENTENCE_SIZE.getName(), new Integer(token.size()));
    doc.addField(EnumSolrWikiIndex.TOKEN.getName(), CollectionUtil.objectsToString(token));
    doc.addField(EnumSolrWikiIndex.NER.getName(), CollectionUtil.objectsToString(ner));
    doc.addField(EnumSolrWikiIndex.TOKENNER.getName(), CollectionUtil.objectsToString(tokenNER));
    doc.addField(EnumSolrWikiIndex.LEMMA.getName(), CollectionUtil.objectsToString(lemma));
    doc.addField(EnumSolrWikiIndex.POS.getName(), CollectionUtil.objectsToString(pos));
    doc.addField(EnumSolrWikiIndex.INDEX.getName(), CollectionUtil.objectsToString(index));
    return doc;
  }
}
