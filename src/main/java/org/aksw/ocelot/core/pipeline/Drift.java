package org.aksw.ocelot.core.pipeline;

import java.io.File;
import java.io.NotSerializableException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.aksw.ocelot.common.io.WriteAndReadFile;
import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.Triple;
import org.aksw.ocelot.data.properties.PropertiesFactory;
import org.aksw.ocelot.share.CandidateTypes;
import org.aksw.simba.knowledgeextraction.commons.io.SerializationUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * Holds the model and executes the pipe.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class Drift {
  protected final static Logger LOG = LogManager.getLogger(Drift.class);
  /** the data */
  protected DriftModel model = null;
  public static Map<Triple, Set<Map<CandidateTypes, Object>>> data = null;

  /**
   *
   * Constructor instantiates a model.
   *
   */
  public Drift() {
    this(new DriftModel());
  }

  /**
   *
   * Constructor reads properties from file and starts the process.
   *
   */
  public Drift(final DriftModel model) {
    this.model = model;
    model.getBackgroundKnowledge().getPredicates().forEach(p -> excecute(p));
  }

  /**
   * Executes on sub sets to reduce memory usage.
   *
   * @param predicate
   */
  protected void excecute(final String predicate) {
    excecute(predicate, Const.TRIPLE_STEPS);
  }

  /**
   * Executes on sub sets to reduce memory usage.
   *
   * @param predicate
   * @param steps
   */
  protected void excecute(final String predicate, int steps) {
    LOG.info("excecute in " + steps + "steps: ".concat(predicate));

    final Map<Triple, Set<Map<CandidateTypes, Object>>> results = Drift.readResults(predicate);
    if (results != null) {
      LOG.info("Results from file for: " + predicate);
      return;
    }
    // get triples
    final Set<Triple> triples = model.triples(predicate);

    // fix step size
    if (steps > triples.size() || steps < 1) {
      steps = triples.size();
    }

    // for each step use a subset of triples
    for (int i = 0; i < triples.size(); i += steps) {

      LOG.info("Subset triples " + i + "-" + (i + steps) + ".");
      final Set<Triple> subset = triples.stream().skip(i).limit(steps).collect(Collectors.toSet());

      // runs on the subset
      final Map<Triple, Set<Map<CandidateTypes, Object>>> objects = excecute(subset);

      try {
        // stores the results
        final String file = getFileName(predicate, steps, triples.size());
        SerializationUtil.serialize(file, objects);

      } catch (final NotSerializableException e) {
        LOG.error(e.getLocalizedMessage(), e);
        LOG.error(//
            String.format("Parameters: predicate=%s, steps=%d, size=%d", predicate, steps,
                triples.size()));
      }
    }
  }

  /**
   * Adds SFs to the triples and the candidate sentences.
   *
   * @param tripleSet
   * @return triples to background data
   */
  protected Map<Triple, Set<Map<CandidateTypes, Object>>> excecute(final Set<Triple> tripleSet) {

    model.addSurfaceforms(tripleSet);
    model.addCandidates(tripleSet);

    Util.testPrint(tripleSet);

    // get shortest path, root node, ...
    final CandidateNLP candidateNLP = new CandidateNLP(model.getCorpus());

    // Sentence id to data
    final Map<Triple, Set<Map<CandidateTypes, Object>>> objects;
    objects = candidateNLP.getCandidates(tripleSet);

    return objects;
  }

  /**
   * Reads all files from TMP folder starting with predicate and a file extension of '.data'.
   *
   * @param predicate
   * @return null if no file found, else the results
   */
  @SuppressWarnings("unchecked")
  public static Map<Triple, Set<Map<CandidateTypes, Object>>> readResults(final String predicate) {

    // get files
    final Set<Path> paths;
    paths = WriteAndReadFile.regularFilesInFolder(Const.TMP_FOLDER.concat(File.separator));
    // LOG.info("file size: " + paths.size());

    // find the file for the predicate and remove the others from 'files'
    for (final Iterator<Path> iter = paths.iterator(); iter.hasNext();) {
      final Path path = iter.next();
      if (!isPath(path, predicate)) {
        iter.remove();
        continue;
      }
      LOG.info(//
          "Reads file (".concat(predicate).concat("): ") + Const.TMP_FOLDER.concat(File.separator)
              + path.toString()//
      );
    }

    if (paths.isEmpty()) {
      return null;
    }
    final Map<Triple, Set<Map<CandidateTypes, Object>>> all = new ConcurrentHashMap<>();
    // file -> all.putAll(SerializationUtil.deserialize(file.toString(), HashMap.class));
    SerializationUtil.setRootFolder(Const.TMP_FOLDER.concat(File.separator));

    paths.stream().parallel()//
        .forEach(path -> all.putAll(SerializationUtil.deserialize(//
            path.toString(), HashMap.class)//
        ));

    LOG.info("Total triple size (" + predicate + "): " + all.size());
    data = all;
    return all;
  }

  /**
   * Reads all files from TMP folder starting with predicate and a file extension of '.data'.
   *
   * @param predicate
   * @return
   */
  public static Map<Triple, Set<Map<CandidateTypes, Object>>> readAllResults() {
    if (data == null) {
      data = new ConcurrentHashMap<>();
      final AtomicInteger s = new AtomicInteger(0);
      PropertiesFactory.getInstance().getPredicates().parallelStream().forEach(pre -> {
        if (!pre.isEmpty()) {
          final Map<Triple, Set<Map<CandidateTypes, Object>>> subdata = readResults(pre);
          if (subdata == null) {
            LOG.warn("No results found: " + pre);
          } else {
            s.set(s.get() + subdata.size());
            data.putAll(subdata);
          }
        }
      });

      if (data.size() != s.get()) {
        LOG.warn("Wrong size!!!!");
      }
    }
    return data;
  }

  public static Map<Triple, Set<Map<CandidateTypes, Object>>> readResults(
      final Set<String> predicates) {

    if (data == null) {
      data = new ConcurrentHashMap<>();
      final AtomicInteger s = new AtomicInteger(0);
      predicates.forEach(pre -> {
        if (!pre.isEmpty()) {
          final Map<Triple, Set<Map<CandidateTypes, Object>>> subdata = readResults(pre);
          if (subdata == null) {
            LOG.warn("No results found: " + pre);
          } else {
            s.set(s.get() + subdata.size());
            data.putAll(subdata);
          }
        }
      });

      if (data.size() != s.get()) {
        LOG.warn("Wrong size!!!!");
      }
    }
    return data;
  }

  private String getFileName(final String predicate, final int current, final int max) {
    return predicate.replace("http://dbpedia.org/ontology/", "") + "_result_" + current + "-" + max
        + ".data";
  }

  /**
   * checks if the path contains the predicate label.
   *
   * @param path e.g. birthPlace_result_81357-81357.data
   * @param predicate http://dbpedia.org/ontology/birthPlace
   *
   * @return true in case the path contains the predicate label.
   */
  protected static boolean isPath(final Path path, final String predicate) {
    return path.toString()
        .startsWith(predicate.replace("http://dbpedia.org/ontology/", "") + "_result_")
        && path.toString().endsWith(".data");
  }
}
