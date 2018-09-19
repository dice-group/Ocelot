package org.aksw.ocelot.core.pipeline;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aksw.ocelot.core.indexsearch.ICorpus;
import org.aksw.ocelot.core.indexsearch.WikipediaCorpus;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.DBpediaKB;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.data.properties.BackgroundKnowledge;
import org.aksw.ocelot.data.properties.PropertiesFactory;
import org.aksw.ocelot.data.surfaceforms.ISurfaceForms;
import org.aksw.ocelot.data.surfaceforms.SurfaceFormGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class DriftModel {
  protected final static Logger LOG = LogManager.getLogger(DriftModel.class);

  DBpediaKB dbpediaKB = new DBpediaKB();
  BackgroundKnowledge backgroundKnowledge = PropertiesFactory.getInstance(PropertiesFactory.file);

  ISurfaceForms surfaceForms = new SurfaceFormGenerator();
  // ISurfaceForms surfaceForms = new SurfaceformsIndex();

  ICorpus corpus = new WikipediaCorpus();

  /**
   *
   * @param predicate
   * @return
   */
  public Set<Triple> triples(final String predicate) {
    LOG.info("Get triples ....");
    final Set<Triple> triplesSet = dbpediaKB.getTriples(predicate, Const.maxTriplesperURI);
    LOG.info("Get triples(" + triplesSet.size() + ") done.");
    return triplesSet;
  }

  /**
   * Adds surfaceforms to all triples in {@link #tripleSet}. Used by {@link #triples()}
   *
   * @return set ot triples without surfaceforms
   */
  public void addSurfaceforms(final Set<Triple> tripleSet) {
    final int size = tripleSet.size();
    LOG.debug("Triple set size: " + size);
    LOG.debug("Adds surfaceforms for each triple...");

    // all triples
    int current = 0;
    for (final Iterator<Triple> i = tripleSet.iterator(); i.hasNext();) {

      try {
        // log info
        if ((++current % (size / 4)) == 0) {
          LOG.info(current + "/" + size + " triples done.");
        }
      } catch (final Exception e) {
        LOG.error(e.getLocalizedMessage());
      }

      // add SFs
      final Triple triple = i.next();
      if (Const.useSurfaceforms) {
        final Set<String> sfsSubject = surfaceForms.getSurfaceform(triple.getS());
        final Set<String> sfsObject = surfaceForms.getSurfaceform(triple.getO());

        if ((sfsSubject != null) && (sfsObject != null) //
            && (sfsSubject.size() > 0) && (sfsObject.size() > 0)) {

          triple.getSubjectSFs().addAll(sfsSubject);
          triple.getObjectSFs().addAll(sfsObject);
        }
      }

      // remove small sf
      cleanSF(triple.getObjectSFs());
      cleanSF(triple.getSubjectSFs());

      if ((triple.getSubjectSFs().isEmpty()) || (triple.getObjectSFs().isEmpty())) {
        i.remove();
      }
    } // end triples

    LOG.debug("Triple set size: " + tripleSet.size());
    LOG.debug("Removed short surfaceforms and triples without surfaceforms ("
        + (size - tripleSet.size()) + ").");
  }

  /**
   * Adds candidate sentence IDs to the triple set.
   *
   * @param tripleSet
   */
  public void addCandidates(final Set<Triple> tripleSet) {
    final int size = tripleSet.size();
    LOG.debug("Triple set size: " + size);
    LOG.info("Adds candidates for each triple ...");

    // find for each surfaceform the sentence ids
    final Map<String, Set<String>> sfToSentenceIds;
    {
      // all sfs we use
      final Set<String> sfs = Triple.getAllSurfaceforms(tripleSet);
      sfToSentenceIds = corpus.sentenceIDs(sfs, Const.RELATION_DOMAIN, Const.RELATION_RANGE);
    }

    // all triples
    for (final Iterator<Triple> iter = tripleSet.iterator(); iter.hasNext();) {
      final Triple triple = iter.next();
      // find sentence ids
      // each subject surfaceform
      for (final String subjectSF : triple.getSubjectSFs()) {
        final Set<String> subjectIDs = sfToSentenceIds.get(subjectSF);
        // each object surfaceform
        for (final String objectSF : triple.getObjectSFs()) {
          // sfs differ
          if (!subjectSF.equals(objectSF)) {
            final Set<String> objectIDs = sfToSentenceIds.get(objectSF);
            // check ids
            final boolean checkS = (subjectIDs != null) && (!subjectIDs.isEmpty());
            final boolean checkO = (objectIDs != null) && (!objectIDs.isEmpty());

            if (checkS && checkO) {

              final Set<String> finalIDs = new HashSet<>();
              finalIDs.addAll(subjectIDs);
              finalIDs.retainAll(objectIDs);

              if (!finalIDs.isEmpty()) {
                final boolean done = triple.candidate.add(//
                    new Candidate(subjectSF, objectSF, finalIDs)//
                );
                if (!done) {
                  LOG.error( //
                      String.format("Could not add candidate: %s, %s, %s  ", //
                          subjectSF, objectSF, finalIDs)//
                  );
                } // log
              }
            }
          }
        } // end for o
      } // end for s
      if (triple.candidate.isEmpty()) {
        iter.remove();
      }
    } // end triples
    LOG.debug("Triple set size: " + tripleSet.size());
    LOG.debug("Removed triples without candidates (" + (size - tripleSet.size()) + ").");
  }

  /**
   * Removes short Strings in the Set.
   *
   * @param cleaned set
   */
  private void cleanSF(final Set<String> sfs) {
    for (final Iterator<String> i = sfs.iterator(); i.hasNext();) {
      if (i.next().length() < Const.minSFlength) {
        i.remove();
      }
    }
  }

  // -----
  public BackgroundKnowledge getBackgroundKnowledge() {
    return backgroundKnowledge;
  }

  public ICorpus getCorpus() {
    return corpus;
  }
}
