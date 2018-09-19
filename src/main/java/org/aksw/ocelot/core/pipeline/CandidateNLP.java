package org.aksw.ocelot.core.pipeline;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ocelot.core.indexsearch.CorpusElement;
import org.aksw.ocelot.core.indexsearch.ICorpus;
import org.aksw.ocelot.core.nlp.StanfordPipeExtended;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.share.CandidateTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.Pair;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class CandidateNLP {
  protected final static Logger LOG = LogManager.getLogger(CandidateNLP.class);

  protected StanfordPipeExtended stanfordDEP = null;
  protected ICorpus corpus = null;

  // protected Stats stat = new Stats();
  private final Util util = new Util();

  /** Corpus sentence id to corpus documents **/
  final Map<String, CorpusElement> cache = new HashMap<>();

  /**
   *
   * Constructor.
   *
   * @param corpus
   */
  public CandidateNLP(final ICorpus corpus) {
    this.corpus = corpus;
    stanfordDEP = new StanfordPipeExtended();
  }

  /**
   * for testing only. Do not use me.
   *
   */
  protected CandidateNLP() {

  }

  /**
   * Gets the data from the index. <br>
   * Calls {@link #cleaning(Set)} and then {@link #processing(Set)}.
   *
   * @param triples
   * @return
   */
  public Map<Triple, Set<Map<CandidateTypes, Object>>> getCandidates(final Set<Triple> triples) {
    final int before = triples.size();

    cleaning(triples);
    LOG.info("Triple size before and after cleaning: " + before + "/" + triples.size());

    final Map<Triple, Set<Map<CandidateTypes, Object>>> results;
    results = processing(triples);

    LOG.info("Triple size before and after processing: " + before + "/" + results.size());

    return results;
  }

  /**
   * Removes long sentences and sentences with many punctuations and fills the {@link #cache}
   * (infinite size atm) with these objects.
   */
  protected Set<Triple> cleaning(final Set<Triple> triples) {
    LOG.info("Cleaning ...");
    // all triple
    for (final Iterator<Triple> triplesIter = triples.iterator(); triplesIter.hasNext();) {
      final Triple triple = triplesIter.next();
      // for all candidates
      for (final Iterator<Candidate> candidatesIter = triple.candidate.iterator(); candidatesIter
          .hasNext();) {
        final Candidate candidate = candidatesIter.next();
        // all sentences with sfs
        for (final Iterator<String> sentenceIDsIter =
            candidate.getSentenceIDs().iterator(); sentenceIDsIter.hasNext();) {

          final String sentenceId = sentenceIDsIter.next();
          if (cache.get(sentenceId) == null) {

            // request meta data from corpus
            final CorpusElement element = corpus.getSolrDocument(sentenceId);
            if (element == null) {
              // remove sentence id
              sentenceIDsIter.remove();
            } else {
              // remove sentences with many punctuations
              final Map<String, Long> posCounts = element.getPOS().stream()
                  .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

              if (((posCounts.get(":") != null) && (posCounts.get(":") > Const.MAX_PUNCT)) //
                  || ((posCounts.get(".") != null) && (posCounts.get(".") > Const.MAX_PUNCT))//
                  || ((posCounts.get(",") != null) && (posCounts.get(",") > Const.MAX_PUNCT))//
              ) {
                sentenceIDsIter.remove();
              } else {
                cache.put(sentenceId, element);
              }
            }
          }
        } // end sentence ids

        if (candidate.getSentenceIDs().isEmpty()) {
          candidatesIter.remove();
        }
      } // end candidates
      if (triple.candidate.isEmpty()) {
        LOG.debug("remove empty triple");
        triplesIter.remove();
      }
    } // end triples
    return triples;
  }

  // TODO: move to test section
  public static void main(final String[] args) {
    final CandidateNLP c = new CandidateNLP();

    final String sentence = "Gian Singh, Sergeant Major, Police Department, Hong K.";
    final List<String> token = Arrays.asList(

        "Gian", "Singh", ",", "Sergeant", "Major", ",", "Police", "Department", ",", "Hong", "K",
        ".");
    final List<Integer> index = Arrays.asList(65167, 65172, 65177, 65179, 65188, 65193, 65195,
        65202, 65212, 65214, 65219, 65223);

    final Pair<CorpusIndex, CorpusIndex> closest = new Pair<CorpusIndex, CorpusIndex>();
    closest.first = new CorpusIndex(3, 4);
    closest.second = new CorpusIndex(9, 10);

    final Set<String> ssf = new HashSet<>(Arrays.asList("Sergeant Major"));
    final Set<String> osf = new HashSet<>(Arrays.asList("Hong K."));
    final boolean checked = c.check(sentence, closest, index, token, ssf, osf);
    LOG.info(checked);
  }

  // test end
  /**
   * Checks if the found surface form is in the set of known surface forms of the triple.
   *
   * @param triple
   * @param element
   * @param closest
   *
   * @return
   */
  public boolean check(final Triple triple, final CorpusElement element,
      final Pair<CorpusIndex, CorpusIndex> closest) {

    final String sentence = element.getSentence();
    final List<Integer> index = element.getIndex();
    final List<String> token = element.getToken();
    return check(sentence, closest, index, token, triple.getSubjectSFs(), triple.getObjectSFs());
  }

  protected boolean check(final String sentence, final Pair<CorpusIndex, CorpusIndex> closest,
      final List<Integer> index, final List<String> token, final Set<String> ssf,
      final Set<String> osf) {

    boolean condition = false;
    try {
      final int offset = index.get(0);

      final int subjectIndexStart = index.get(closest.first.getStart()) - offset;
      // length of the last token in surface form
      int lastLength = token.get(closest.first.getEnd()).length();
      final int subjectIndexEnd = (index.get(closest.first.getEnd()) - offset) + lastLength;

      final int objectIndexStart = index.get(closest.second.getStart()) - offset;
      // length of the last token in surface form
      lastLength = token.get(closest.second.getEnd()).length();
      final int objectIndexEnd = (index.get(closest.second.getEnd()) - offset) + lastLength;

      final String subjectMention = sentence.substring(subjectIndexStart, subjectIndexEnd);
      final String objectMention = sentence.substring(objectIndexStart, objectIndexEnd);

      // condition
      final boolean knownSubSF = ssf.contains(subjectMention);
      final boolean knownObjSF = osf.contains(objectMention);
      condition = knownSubSF && knownObjSF;
      if (!condition) {
        LOG.info("Not found " + subjectMention + " and " + objectMention + ".");
        LOG.info(sentence);
        LOG.info("index size: " + index.size());
        LOG.info("closest pair: " + closest);

      }
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    return condition;
  }

  /**
   * Gets for all triples the data in the corpus.
   *
   * @param triples
   * @return
   */
  protected Map<Triple, Set<Map<CandidateTypes, Object>>> processing(final Set<Triple> triples) {

    LOG.info("Processing " + triples.size() + " triples.");

    // the return
    final Map<Triple, Set<Map<CandidateTypes, Object>>> triplesToIndexData = new HashMap<>();

    // all triple
    for (final Iterator<Triple> i = triples.iterator(); i.hasNext();) {
      final Triple triple = i.next();
      triplesToIndexData.put(triple, new HashSet<>());
      // for all candidates
      for (final Iterator<Candidate> ii = triple.candidate.iterator(); ii.hasNext();) {
        final Candidate candidate = ii.next();
        // all sentences with sfs
        for (final Iterator<String> iii = candidate.getSentenceIDs().iterator(); iii.hasNext();) {

          // get corpus data
          final String sentenceId = iii.next();
          final CorpusElement element = cache.get(sentenceId);

          // index of sfs
          final Set<CorpusIndex> subjectIndex;
          final Set<CorpusIndex> objectIndex;
          subjectIndex = util.getIndices(candidate.getSubjectSF(), Const.RELATION_DOMAIN,
              element.getToken(), element.getNER());
          objectIndex = util.getIndices(candidate.getObjectSF(), Const.RELATION_RANGE,
              element.getToken(), element.getNER());

          if (subjectIndex.isEmpty() || objectIndex.isEmpty()
              || ((subjectIndex.size() == 1) && subjectIndex.equals(objectIndex))) {
            iii.remove(); // sentence id
            continue;
          }

          // index of closes subject and object surface form
          final Pair<CorpusIndex, CorpusIndex> closest = util.getClosest(subjectIndex, objectIndex);

          if (closest == null) {
            iii.remove(); // sentence id
          } else {

            final boolean check = check(triple, element, closest);
            if (!check) {
              iii.remove();
            } else {
              // get dependency tree
              final SemanticGraph sg = stanfordDEP.getSemanticGraph(element.getSentence());
              final IndexedWord root = stanfordDEP.checkRoot(sg);

              // sentences with wrong roots
              if (root == null) {
                LOG.warn("Root is not useful, we remove the sentence");
                iii.remove();
                continue;
              }

              // corpus token index to sg index
              final Pair<Integer, Integer> index = stanfordDEP
                  .tokenIndexToSGIndex(closest.first.getStart(), closest.second.getStart());

              List<IndexedWord> shortestPath;
              // get shortest path
              shortestPath = stanfordDEP.getShortestUndirectedPathNodes(sg, index.first.intValue(),
                  index.second.intValue(), element.getToken());
              // clean shortest path
              shortestPath = stanfordDEP.clean(shortestPath, sg, index.first.intValue(),
                  index.second.intValue());

              if ((shortestPath == null) || shortestPath.isEmpty()) {
                iii.remove();
                continue;
              }

              LOG.trace("shortestPath: " + shortestPath);

              final Map<CandidateTypes, Object> object;
              object = getObject(sentenceId, element.getSentence(), sg, shortestPath, root, closest,
                  candidate.getSubjectSF(), candidate.getObjectSF());

              triplesToIndexData.get(triple).add(object);
            }
          }

        } // end sentence ids
        if (candidate.getSentenceIDs().isEmpty()) {
          ii.remove();
        }
      } // end candidates
      if (triple.candidate.isEmpty()) {
        LOG.debug("remove empty triple");
        triplesToIndexData.remove(triple);
        i.remove();
      }
    } // end triples

    LOG.info("processing " + triplesToIndexData.size() + " triples done.");
    return triplesToIndexData;
  }

  /**
   *
   * @param sentenceId
   * @param sentence
   * @param sg
   * @param shortestPath
   * @param root
   * @param closest
   * @param s
   * @param o
   * @return
   */
  private Map<CandidateTypes, Object> getObject(//

      final String sentenceId, final String sentence, final Object sg, final Object shortestPath,
      final Object root, final Pair<CorpusIndex, CorpusIndex> closest, final String s,
      final String o) {

    final Map<CandidateTypes, Object> object = new HashMap<>();
    object.put(CandidateTypes.ID, sentenceId);
    object.put(CandidateTypes.SENTENCE, sentence);
    object.put(CandidateTypes.SEM_GRAPH, sg);
    object.put(CandidateTypes.SP, shortestPath);
    object.put(CandidateTypes.ROOT, root);
    object.put(CandidateTypes.SUBJECT_SF, s);
    object.put(CandidateTypes.OBJECT_SF, o);
    object.put(CandidateTypes.SUBJECT_INDEX_BEGIN, closest.first.getStart());
    object.put(CandidateTypes.SUBJECT_INDEX_END, closest.first.getEnd());
    object.put(CandidateTypes.OBJECT_INDEX_BEGIN, closest.second.getStart());
    object.put(CandidateTypes.OBJECT_INDEX_END, closest.second.getEnd());

    return object;
  }
}
