package org.aksw.ocelot.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Stopwords {

  protected final static Logger LOG = LogManager.getLogger(Stopwords.class);

  final String stopwordsenglish = "../ocelot-data/stopwords/english.stop";
  final List<String> stopwords = new ArrayList<>();

  /**
   * Test
   *
   * @param a
   */
  public static void main(final String[] a) {
    final Stopwords stopwords = new Stopwords();

    stopwords//
        .removeStopwords(Arrays.asList("That", "is", "a", "stopword", "test"))//
        .forEach(LOG::info);

    stopwords//
        .removeStopwords(Arrays.asList("That", "is", "another", "stopword", "test"))//
        .forEach(LOG::info);

  }

  public Stopwords() {
    try {
      stopwords.addAll(Files.readAllLines(Paths.get(stopwordsenglish)));
    } catch (final IOException e) {
      LOG.error("Stopwords not used!!", e);
    }
  }

  /**
   * Removes stopwords form the elements in the list.
   *
   * @param words
   * @return words without stopwords
   */
  public List<String> removeStopwords(final List<String> words) {
    final List<String> wordsWithoutStopwords = new ArrayList<>();

    words.forEach(word -> {
      final StringBuilder without = new StringBuilder();
      for (final String s : word.split(" ")) {
        if (!stopwords.contains(s)) {
          without.append(s).append(" ");
        }
      }
      if (without.length() > 0) {
        wordsWithoutStopwords.add(without.toString().trim());
      }
    });
    return wordsWithoutStopwords;
  }
}
