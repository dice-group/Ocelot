package org.aksw.ocelot.core.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.Triple;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.Pair;

public class Util {
  protected final static Logger LOG = LogManager.getLogger(Util.class);

  /**
   * Expands the index and gets the closest.
   *
   * @param subjects indices
   * @param objects indices
   *
   * @return closest indices or null
   */
  public Pair<CorpusIndex, CorpusIndex> getClosest(Set<CorpusIndex> subjects,
      Set<CorpusIndex> objects) {

    subjects = expand(subjects);
    objects = expand(objects);

    // intersection
    final Set<CorpusIndex> in = new TreeSet<>();
    in.addAll(subjects);
    in.retainAll(objects);

    final Set<CorpusIndex> all = new TreeSet<>();
    all.addAll(subjects);
    all.addAll(objects);

    // intersection complement
    all.removeAll(in);

    CorpusIndex s = null;
    CorpusIndex o = null;

    for (final CorpusIndex i : all) {

      if (subjects.contains(i)) {
        s = i;
      }
      if (objects.contains(i)) {
        o = i;
        break;
      }
    }
    if ((s == null) || (o == null)) {
      return null;
    }
    return new Pair<>(s, o);
  }

  /**
   * Expands the indices.
   *
   * @param set
   * @return
   */
  public Set<CorpusIndex> expand(final Set<CorpusIndex> set) {

    final Set<CorpusIndex> expanded = new TreeSet<>();
    int start = -1;
    int end = -1;

    for (final CorpusIndex index : new TreeSet<>(set)) {
      final int s = index.getStart();
      final int e = index.getEnd();

      if (start == -1) {
        start = s;
        end = e;
        continue;
      }

      if (e > end) {
        if (((s - 1) > end)) {
          expanded.add(new CorpusIndex(start, end));
          start = s;
        }
        end = e;
      }
    } // end for

    if (start > -1) {
      expanded.add(new CorpusIndex(start, end));
    }
    return expanded;
  }

  /**
   * Gets the indices of sf with a given NER type in the token list. <br>
   * Expands the indices if the types list indicates NER.
   *
   * @param sf
   * @param type
   * @param token
   * @param ner
   * @return set of indices
   */
  public Set<CorpusIndex> getIndices(final String sf, final String type, final List<String> token,
      final List<String> ner) {
    return Const.NER_USE ? getIndicesWithNER(sf, type, token, ner)
        : getIndicesWithoutNER(sf, token);
  }

  public Set<CorpusIndex> getIndicesWithoutNER(final String sf, final List<String> token) {
    final Set<CorpusIndex> indexSet = new TreeSet<>();

    // FIXME: split with the same method as for token i.e.: Stanford NLP core
    final String[] sftoken = sf.split(" ");

    // all token
    int start = -1;
    int end = -1;
    for (int tokenIndex = 0; tokenIndex < token.size(); tokenIndex++) {
      final String word = token.get(tokenIndex);
      // match surfaceforms start token
      try {
        if (sf.concat(" ").startsWith(word.concat(" "))) {
          // check all sfs token
          boolean match = false;
          for (int sfIndex = 0; sfIndex < sftoken.length; sfIndex++) {
            final boolean c = !(token.size() > (tokenIndex + sfIndex));
            final boolean cc = !sftoken[sfIndex].equals(token.get(tokenIndex + sfIndex));
            if (c || cc) {
              match = false;
              break;
            }
            match = true;
            start = tokenIndex;
            end = tokenIndex + sfIndex;
          }
          if (match) {
            indexSet.add(new CorpusIndex(start, end));
          }
        }
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return indexSet;
  }

  public Set<CorpusIndex> getIndicesWithNER(//
      final String sf, final String type, final List<String> token, final List<String> ner) {

    final Set<CorpusIndex> indexSet = new TreeSet<>();

    // FIXME: split with the same method as for token i.e.: Stanford NLP core
    final String[] sftoken = sf.split(" ");

    // all given token
    for (int tokenIndex = 0; tokenIndex < Math.min(token.size(), ner.size()); tokenIndex++) {
      try {
        // match surfaceform start with token list and NER list
        if (sf.concat(" ").startsWith(token.get(tokenIndex).concat(" "))
            && ner.get(tokenIndex).equals(type)) {

          // all surfaceform token check if its in the token list
          boolean match = false;
          for (int sfIndex = 0; sfIndex < sftoken.length; sfIndex++) {
            final boolean c = !(token.size() > (tokenIndex + sfIndex));
            final boolean cc = !sftoken[sfIndex].equals(token.get(tokenIndex + sfIndex));
            if (c || cc) {
              match = false;
              break;
            }
            match = true;
          }
          if (match) {
            // expand index with the help of the NE types
            int start = tokenIndex;
            int end = tokenIndex;

            // go left
            int tmpIndex = tokenIndex - 1;
            while ((-1 < tmpIndex) && ner.get(tmpIndex).equals(type)) {
              start--;
              tmpIndex--;
            }
            // go right
            tmpIndex = tokenIndex + 1;
            while ((tmpIndex < ner.size()) && ner.get(tmpIndex).equals(type)) {
              end++;
              tmpIndex++;
            }
            indexSet.add(new CorpusIndex(start, end));
          }
        }
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return indexSet;
  }

  /**
   * print 1% of the data
   *
   * @param tripleSet
   */
  public static void testPrint(final Set<Triple> tripleSet) {
    LOG.info("print 1% of the data");
    new ArrayList<>(tripleSet).subList(0, new Double((tripleSet.size() * 0.01d) + 0.5).intValue())
        .forEach(LOG::info);
  }
}
