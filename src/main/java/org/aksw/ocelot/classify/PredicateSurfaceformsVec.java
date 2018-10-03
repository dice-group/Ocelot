package org.aksw.ocelot.classify;

import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.core.wordembedding.Word2VecBinding;
import org.aksw.ocelot.core.wordembedding.Word2VecMath;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.aksw.simba.knowledgeextraction.commons.nlp.Stopwords;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PredicateSurfaceformsVec {
  protected final static Logger LOG = LogManager.getLogger(PredicateSurfaceformsVec.class);

  PredicateSurfaceforms predicateSurfaceforms = new PredicateSurfaceforms();
  final Stopwords stopwords = new Stopwords();
  final Word2VecBinding w2v = new Word2VecBinding();

  protected static String storeFile = "cleanedLabels.bin";
  Map<String, Set<String>> cleanedLabels = null;

  public Map<String, Set<String>> getLabels() {
    return cleanedLabels;
  }

  public static void main(final String[] args) {
    final PredicateSurfaceformsVec predicateSurfaceformsVec = new PredicateSurfaceformsVec();
    predicateSurfaceformsVec.getLabels().entrySet().forEach(LOG::info);
  }

  @SuppressWarnings("unchecked")
  public PredicateSurfaceformsVec() {

    SerializationUtil.setRootFolder(PredicateSurfaceforms.storeFolder);

    cleanedLabels = SerializationUtil.deserialize(storeFile, HashMap.class);
    if (cleanedLabels == null) {

      cleanedLabels = new HashMap<>();

      final Map<String, Set<String>> dbpediaLabels = predicateSurfaceforms.getDBpediaLabels();

      // dbpedia and wikidata labels
      final Map<String, float[]> dbpediaVecs = new HashMap<>();
      for (final Entry<String, Set<String>> entry : dbpediaLabels.entrySet()) {
        final String p = entry.getKey();
        final Set<String> l = entry.getValue();
        dbpediaVecs.put(p, createVec(l));
        cleanedLabels.put(p, new HashSet<>(l));
      }

      // all labels
      final Map<String, Set<String>> allLabels = predicateSurfaceforms.getAllLabels();
      for (final Entry<String, Set<String>> entry : allLabels.entrySet()) {
        final String p = entry.getKey();
        final Set<String> labels = entry.getValue();
        labels.removeAll(dbpediaLabels.get(p));

        for (final String word : labels) {
          final float[] vec = createVec(new HashSet<>(Arrays.asList(word)));

          if (vec != null) {
            // for db wiki vecs
            double max = Double.MIN_VALUE;
            String maxPredicate = "";
            for (final Entry<String, float[]> ee : dbpediaVecs.entrySet()) {
              final float[] vv = ee.getValue();

              if (vv != null) {
                final double sim = Word2VecMath//
                    .cosineSimilarityNormalizedVecs(//
                        Word2VecMath.normalize(vv), Word2VecMath.normalize(vec)//
                    );

                if (sim > max) {
                  max = sim;
                  maxPredicate = ee.getKey();
                }
              }
            } // end for

            if (maxPredicate.equals(entry.getKey())) {
              cleanedLabels.get(entry.getKey()).add(word);
            } else {
              cleanedLabels.get(maxPredicate).add(word);
            }
          }
        }
      }
      // store the labels
      try {
        SerializationUtil.serialize(storeFile, cleanedLabels, false);
      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }

    }
  }

  protected float[] createVec(final Set<String> labels) {

    final Set<String> splited = new HashSet<>();

    final Iterator<String> iter = labels.iterator();
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
    return w2v.addition(removeStopwords(labels));
  }

  public Set<String> removeStopwords(final Set<String> words) {
    List<String> list = new ArrayList<>();
    list.addAll(words);
    list = stopwords.removeStopwords(list);
    words.clear();
    words.addAll(list);
    return words;
  }
}
