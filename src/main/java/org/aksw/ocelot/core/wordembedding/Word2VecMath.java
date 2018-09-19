package org.aksw.ocelot.core.wordembedding;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Word2VecMath implements Similarity {

  protected static Logger LOG = LogManager.getLogger(Word2VecMath.class);

  public static void main(final String[] args) {

    final float[] vecA = new float[] {11.2f, 1.4f, 1.9f, 0.1f};
    final float[] vecB = new float[] {11f, 1.3f, 1.2f, 1.1f};

    System.out.println(Word2VecMath.cosineSimilarity(vecA, vecB));
    System.out.println(Word2VecMath.cosineSimilarityNormalizedVecs(Word2VecMath.normalize(vecA),
        Word2VecMath.normalize(vecB)));

    System.out.println(Word2VecMath.cosineSimilarity(vecA, vecA));
    System.out.println(Word2VecMath.cosineSimilarityNormalizedVecs(Word2VecMath.normalize(vecA),
        Word2VecMath.normalize(vecA)));

  }

  @Override
  public double similarity(final float[] vectorA, final float[] vectorB) {
    return Word2VecMath.cosineSimilarity(vectorA, vectorB);
  }

  public synchronized static float[] sub(final float[] vectorA, final float[] vectorB) {
    if (vectorA.length == vectorB.length) {
      final float[] add = new float[vectorA.length];
      for (int i = 0; i < vectorA.length; i++) {
        add[i] = vectorA[i] - vectorB[i];
      }
      return add;
    }
    return null;
  }

  public synchronized static float[] add(final float[] vectorA, final float[] vectorB) {
    if (vectorA.length == vectorB.length) {
      final float[] add = new float[vectorA.length];
      for (int i = 0; i < vectorA.length; i++) {
        add[i] = vectorA[i] + vectorB[i];
      }
      return add;
    }
    return null;
  }

  public synchronized static double cosineSimilarity(final float[] vectorA, final float[] vectorB) {

    if (vectorA.length != vectorB.length) {
      LOG.warn("Vectors with different length.");
      return -1f;
    }

    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;

    for (int i = 0; i < vectorA.length; i++) {
      dotProduct += vectorA[i] * vectorB[i];
      normA += vectorA[i] * vectorA[i];
      normB += vectorB[i] * vectorB[i];
    }
    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }

  public synchronized static double cosineSimilarityNormalizedVecs(final float[] vectorA,
      final float[] vectorB) {
    double c = 0.0;
    for (int i = 0; i < vectorA.length; i++) {
      c += vectorA[i] * vectorB[i];
    }
    return c;
  }

  public synchronized static double norm(final float[] vectorA) {
    double normA = 0.0;
    for (int i = 0; i < vectorA.length; i++) {
      normA += vectorA[i] * vectorA[i];
    }
    return Math.sqrt(normA);
  }

  public synchronized static float[] normalize(final float[] vectorA) {
    final Double normA = norm(vectorA);
    for (int i = 0; i < vectorA.length; i++) {
      vectorA[i] /= normA.floatValue();
    }
    return vectorA;
  }

}
