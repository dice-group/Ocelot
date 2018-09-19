package org.aksw.ocelot.core.measure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.ocelot.core.measure.nlr.INLR;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.share.CandidateTypes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Calculates the support 'sup' of a pattern 'NLR' to a predicate 'p'.<br>
 * <code>sup(NLR,p)=log(max_{(s,o) in I} (l(s,o,NLR,p))) * log(|I(p,NLR)|)</code> <br>
 * <br>
 * Where l is the number of sentences that led to NLR and contain the labels of s and o. <br>
 * The pairs (s,o) in I(p) are the pairs in the KB for p. I(p,NLR) are pairs led to NLR.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Measurement {

  protected final static Logger LOG = LogManager.getLogger(Measurement.class);
  // protected ICorpus wiki;
  protected INLR nlre;
  protected Map<Triple, Set<Map<CandidateTypes, Object>>> data;

  /**
   * Sets the data.
   *
   * @param tripleToCandidates
   */
  public Measurement(final INLR nlre, final Map<Triple, Set<Map<CandidateTypes, Object>>> data) {
    this.nlre = nlre;
    this.data = data;
    // wiki = new WikipediaCorpus();
  }

  /**
   * Inverse document frequency:<br>
   * <code>
       spec(NLR,p) = log( |P| / |M(NLR)| )
   </code><br>
   * <br>
   *
   * , where P is the set of all predicates and M(NLR) number of mappings containing NLR.
   *
   * @return
   */
  public Map<String, Double> specificity() {

    final Map<String, Integer> mappings = mappings();
    final int all = allP();

    final Map<String, Double> specificity = new HashMap<>();

    for (final String nlr : mappings.keySet()) {
      // specificity.put(nlr, Math.log(new Double(all) / new Double(mappings.get(nlr))));
      specificity.put(nlr, (new Double(all) / new Double(mappings.get(nlr))));
    }
    return specificity;
  }

  /**
   * <code>
      |P|
  </code>
   *
   * @return all predicates size
   */
  public int allP() {
    final Set<String> ps = new HashSet<>();
    data.keySet().forEach(p -> ps.add(p.getP()));
    return ps.size();
  }

  public Set<String> getallRoots() {
    final Set<String> allroots = new HashSet<>();
    // all triples
    final Iterator<Triple> i = data.keySet().iterator();
    while (i.hasNext()) {
      final Triple triple = i.next();
      // all candidates
      final Iterator<Map<CandidateTypes, Object>> ii = data.get(triple).iterator();
      while (ii.hasNext()) {
        final Map<CandidateTypes, Object> candidate = ii.next();

        // shortest path
        final String nlr = nlre.getNLR(candidate);
        allroots.add(nlr);
      }
    }
    return allroots;
  }

  /**
   * <code>
      |M(NLR)|
   </code>
   *
   * @return Number of mappings containing NLR
   */
  public Map<String, Integer> mappings() {
    final Map<String, Set<String>> mappings = new HashMap<>();

    // all triples
    final Iterator<Triple> i = data.keySet().iterator();
    while (i.hasNext()) {
      final Triple triple = i.next();
      // all candidates
      final Iterator<Map<CandidateTypes, Object>> ii = data.get(triple).iterator();
      while (ii.hasNext()) {
        final Map<CandidateTypes, Object> candidate = ii.next();

        // shortest path
        final String nlr = nlre.getNLR(candidate);
        // final String sp = Cleansing.getSP(result);

        if (mappings.get(nlr) == null) {
          mappings.put(nlr, new HashSet<>());
        }
        mappings.get(nlr).add(triple.getP());
      }
    } // end all triples

    // maps the set size
    return mappings.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
  }

  /**
   * Measurement:<br>
   * <code>sup(NLR,p)=log(max_{(s,o) in I} (l(s,o,NLR,p))) * log(|I(p,NLR)|)</code>
   *
   * @param p
   * @return map with NLR keys and support as value
   */
  public Map<String, Double> support(final String p) {

    final Map<String, Integer> pairs = pairs(p);
    final Map<String, Integer> max = max(p);

    final Map<String, Double> support = new HashMap<>();

    for (final String nlr : pairs.keySet()) {
      // support.put(nlr, Math.log(max.get(nlr)) * Math.log(pairs.get(nlr)));
      support.put(nlr, Double.valueOf(max.get(nlr)) * Double.valueOf(pairs.get(nlr)));
    }
    return support;
  }

  /**
   *
   * Maps NLRs to the number of (s,o) in KB for a given p.<br>
   * <br>
   * <code>I(p,NLR)</code>
   *
   * @return map with NLR keys and number of triples as value
   */
  public Map<String, Integer> pairs(final String p) {

    // NLR to triples set
    final Map<String, Set<Triple>> pairs = new HashMap<>();
    // NLR to sentence ids
    // final Map<String, Set<String>> sentence = new HashMap<>();

    // all triples
    final Iterator<Triple> i = data.keySet().iterator();
    while (i.hasNext()) {
      final Triple triple = i.next();
      // for p
      if (triple.getP().equals(p)) {
        // all candidates
        final Iterator<Map<CandidateTypes, Object>> ii = data.get(triple).iterator();
        while (ii.hasNext()) {
          final Map<CandidateTypes, Object> candidate = ii.next();

          // shortest path
          final String nlr = nlre.getNLR(candidate);
          // final String sp = Cleansing.getSP(result);

          if (pairs.get(nlr) == null) {
            pairs.put(nlr, new HashSet<>());
            // sentence.put(nlr, new HashSet<>());
          }
          pairs.get(nlr).add(triple);
          // sentence.get(nlr).add(((String) candidate.get(CandidateTypes.ID)));
        }
      } // end if p
    } // end all triples

    // maps the set size
    return pairs.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
  }

  /**
   * Max number of sentences containing 'NLR' for a given predicate and all pair (s,o).<br>
   * <br>
   * <code>max_{(s,o) in I} (l(s,o,NLR,p))</code>
   *
   * @return map with NLR keys and max number of sentences as value
   */
  public Map<String, Integer> max(final String p) {

    final Map<String, Integer> allnlrToMaxSize = new HashMap<>();

    // all triples
    final Iterator<Triple> i = data.keySet().iterator();
    while (i.hasNext()) {
      final Triple triple = i.next();

      // with p
      if (triple.getP().equals(p)) {

        // for (s, o) store all NLR and the occurrence
        final Map<String, Integer> nlrToMaxSize = new HashMap<>();

        // all sentences
        final Iterator<Map<CandidateTypes, Object>> ii = data.get(triple).iterator();
        while (ii.hasNext()) {
          final Map<CandidateTypes, Object> result = ii.next();
          // get NLR
          final String nlr = nlre.getNLR(result);

          // counter
          nlrToMaxSize.put(nlr, (nlrToMaxSize.get(nlr) == null) ? 1 : nlrToMaxSize.get(nlr) + 1);
        } // end sentences

        // prepare for the next triple
        // merge map nlrToMaxSize into allnlrToMaxSize
        for (final String nlr : nlrToMaxSize.keySet()) {
          final Integer all = allnlrToMaxSize.get(nlr);
          final Integer current = nlrToMaxSize.get(nlr);
          if ((all == null) || (all.intValue() < current.intValue())) {
            allnlrToMaxSize.put(nlr, current.intValue());
          }
        }
        // merge done and clear
        nlrToMaxSize.clear();
      } // end if p
    } // end all triples
    return allnlrToMaxSize;
  }

  /**
   * Prints sorted values to the limit.
   *
   * @param map
   * @param limit
   */
  public static void print(final Map<String, Double> map, final int limit) {
    map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .limit(limit).forEach(LOG::info);
  }
}
