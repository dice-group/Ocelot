package org.aksw.ocelot.common.lang;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

  /**
   *
   * @param sentence
   * @param word
   * @return
   */
  public static Set<Integer> indices(final String sentence, final String word) {

    final Matcher matcher = Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(sentence);
    final Set<Integer> indices = new HashSet<>();
    while (matcher.find()) {
      indices.add(matcher.start());
    }
    return indices;
  }

  /**
   *
   * @param sentence "A B C D."
   * @param words "C D"
   * @param replace "XX"
   * @return "A B XX XX."
   */
  public static String replaceWords(final String sentence, final String words,
      final String replace) {

    if (sentence.isEmpty() || words.isEmpty()) {
      return sentence;
    }

    final StringBuilder sb = new StringBuilder();
    int tokenCount;
    tokenCount = words.split(" ").length;
    while (tokenCount-- > 0) {
      sb.append(replace).append(" ");
    }
    return sentence.replaceAll("\\b" + words + "\\b", sb.toString().trim());
  }
}
