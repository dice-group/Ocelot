package org.aksw.ocelot.classify;

import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.common.Stopwords;
import org.aksw.ocelot.common.io.SerializationUtil;
import org.aksw.ocelot.core.wordembedding.Word2VecBinding;
import org.aksw.ocelot.generalisation.GModel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ClassifySentences {
  protected final static Logger LOG = LogManager.getLogger(ClassifySentences.class);

  final GModel gmodel;

  final Stopwords stopwords = new Stopwords();
  final Word2VecBinding w2v = new Word2VecBinding();
  // final Word2VecMath w2math = new Word2VecMath();

  final PredicateSurfaceforms predicateSurfaceforms = new PredicateSurfaceforms();
  PredicateSurfaceformsVec predicateSurfaceformsVec = new PredicateSurfaceformsVec();
  final String predicateVecFile = "predicateVecFile.bin";
  Map<String, float[]> predicateVec = null;
  final String sentenceToVecFile = "sentenceToVec.bin";
  Map<String, float[]> sentenceToVec = null;

  /**
   *
   * @param args
   */
  public static void main(final String[] args) {

    final ClassifySentences classifySentences = new ClassifySentences();
    classifySentences.hashCode();
  }

  /**
   * Read predicateVecFile serialization or serialize it.
   */
  @SuppressWarnings("unchecked")
  public ClassifySentences() {
    LOG.info("ClassifySentences...");

    gmodel = new GModel(predicateSurfaceforms.getAllLabels().keySet());

    // read predicateVecFile serialization or serialize it
    SerializationUtil.setRootFolder(PredicateSurfaceforms.storeFolder);
    predicateVec = SerializationUtil.deserialize(predicateVecFile, HashMap.class);
    if (predicateVec == null) {
      predicateVec = createPredicateVec();
      try {
        SerializationUtil.serialize(predicateVecFile, predicateVec, false);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }

    // read sentenceToVecFile serialization or serialize it
    /**
     * <code>
     sentenceToVec = SerializationUtil.deserialize(sentenceToVecFile, HashMap.class);
     if (sentenceToVec == null) {
    
       sentenceToVec = createCandidateVec();
       try {
         SerializationUtil.serialize(sentenceToVecFile, sentenceToVec, false);
       } catch (final NotSerializableException e) {
         LOG.error(e.getLocalizedMessage(), e);
       }
     }
    </code>
     */

    /**
     * <code>
     // compare
     final Set<String> set = new HashSet<>();
     final Set<String> setused = new HashSet<>();

     // each sentence
     for (final Entry<String, float[]> sentencesToVec : sentenceToVec.entrySet()) {
       final String sentence = sentencesToVec.getKey();
       final float[] vecB = sentencesToVec.getValue();
       if (vecB != null) {

         // each p
         double max = Double.MIN_VALUE;
         String maxpredicate = "";

         for (final Entry<String, float[]> predicatesVec : predicateVec.entrySet()) {
           final String predicate = predicatesVec.getKey();
           setused.add(predicate);
           final float[] vecA = predicatesVec.getValue();
           if (vecA != null) {
             // sim
             final double sim = Word2VecMath//
                 .cosineSimilarityNormalizedVecs(//
                     Word2VecMath.normalize(vecA), Word2VecMath.normalize(vecB)//
             );

             if (sim > max) {
               max = sim;
               maxpredicate = predicate;
             }
           } else {
             LOG.info("No vec for " + predicate);
           }
         } // end for

         if (gmodel.sentenceToPredicates.get(sentence).contains(maxpredicate)) {

           // LOG.info("--");
           // LOG.info(max);
           set.add(maxpredicate);

           // LOG.info(sentence);
           // LOG.info(gmodel.sentenceToPredicates.get(sentence));
         }
       }
     } // end for
     LOG.info(set);
     LOG.info(setused);
     </code>
     */
  }

  protected Map<String, float[]> createPredicateVec() {
    final Map<String, float[]> predicateVec = new HashMap<>();
    // final Map<String, Set<String>> predicatesToLabels = predicateSurfaceforms.getAllLabels();

    final Map<String, Set<String>> predicatesToLabels = predicateSurfaceformsVec.getLabels();
    for (final Entry<String, Set<String>> predicateToLabels : predicatesToLabels.entrySet()) {
      final String predicate = predicateToLabels.getKey();
      Set<String> labels = predicateToLabels.getValue();
      final Iterator<String> iter = labels.iterator();
      final Set<String> splited = new HashSet<>();
      while (iter.hasNext()) {
        final String label = iter.next();
        if (label.contains(" ")) {
          iter.remove();
          for (final String s : label.split(" ")) {
            splited.add(s);
          }
        }
      }
      labels.addAll(splited);
      labels = removeStopwords(labels);
      final float[] vec = w2v.addition(labels);
      predicateVec.put(predicate, vec);
    }
    return predicateVec;
  }

  /**
   * <code>
   protected Map<String, float[]> createCandidateVec() {
     final Map<String, float[]> sentenceToVec = new HashMap<>();
  
     for (final Entry<String, Set<Map<CandidateTypes, Object>>> entry : gmodel.sentenceToCandidates
         .entrySet()) {
  
       final String sentence = entry.getKey();
       final Set<Map<CandidateTypes, Object>> candidates = entry.getValue();
       if (candidates.size() > 1) {
         LOG.info("more than one");
       }
  
       // TODO: here we have several candidates
       final Map<CandidateTypes, Object> candidate = candidates.iterator().next();
       &#64;SuppressWarnings("unchecked")
       final List<IndexedWord> sp = (ArrayList<IndexedWord>) candidate.get(CandidateTypes.SP);
  
       // cleans sp
       Set<String> spclean = new HashSet<>();
       for (final IndexedWord s : sp) {
         final String ss = s.originalText().split("/")[0];
         if (!ss.equals("?D") && !ss.equals("?R")) {
           spclean.add(ss);
         }
       }
       spclean = removeStopwords(spclean);
       final float[] vec = w2v.addition(spclean);
       sentenceToVec.put(sentence, vec);
     }
     return sentenceToVec;
   } </code>
   */

  public Set<String> removeStopwords(final Set<String> words) {
    List<String> list = new ArrayList<>();
    list.addAll(words);
    list = stopwords.removeStopwords(list);
    words.clear();
    words.addAll(list);
    return words;
  }
}
