package org.aksw.ocelot.data.surfaceforms.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.kb.DBpediaKB;
import org.aksw.ocelot.data.surfaceforms.ISurfaceForms;
import org.aksw.ocelot.data.surfaceforms.SurfaceFormGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class MainSurfaceformsSolrIndex {
  protected static Logger LOG = LogManager.getLogger(MainSurfaceformsSolrIndex.class);

  protected final DBpediaKB dbpediaKB = new DBpediaKB();
  protected final SurfaceformsIndexInterface surfaceformsIndex = new SurfaceformsIndex();
  protected final SurfaceFormGenerator surfaceFormGenerator = new SurfaceFormGenerator();

  /**
   *
   * @param a
   */
  public static void main(final String[] a) {
    final String filename = "../ocelot-data/dbpedia/en_surface_forms.tsv";
    final MainSurfaceformsSolrIndex m = new MainSurfaceformsSolrIndex();

    final boolean makeIndex = false;
    if (makeIndex) {
      m.run(filename);
    }

    // search test
    ((ISurfaceForms) m.surfaceformsIndex).getSurfaceform("http://dbpedia.org/resource/Leipzig")
        .forEach(LOG::info);
  }

  public void run(final String filename) {

    final Map<String, Set<String>> surfaceformsMap = surfaceFormGenerator.getSurfaceForms();

    try {
      final List<String> uris = new ArrayList<>(surfaceformsMap.keySet());

      final int range = Const.sfSteps;
      LOG.info("uris with sfs: " + uris.size() + " subsets length we use: " + range);

      final Map<String, Set<String>> labelsMap = new ConcurrentHashMap<>();

      // parallel
      ExecutorService executorServiceW;
      CompletionService<Map<String, Set<String>>> completionServiceW;
      {// FIXME: not working in parallel
        executorServiceW = Executors.newFixedThreadPool(1);
        completionServiceW = new ExecutorCompletionService<>(executorServiceW);
      }

      //
      final Set<Future<Map<String, Set<String>>>> futures = new HashSet<>();
      int n = 0;
      // surfaceforms size
      final int max = uris.size();
      for (int i = 0; i < max; i = i + range) {

        final int end = (i + range) > max ? max : i + range;
        final List<String> sublist = uris.subList(i, end);
        n++;
        futures.add(completionServiceW.submit(() -> {
          // final DBpediaKB dbpediaKB = new DBpediaKB();
          if (((labelsMap.size() + 1) % 10000) == 0) {
            LOG.info(labelsMap.size() + "/" + max);
          }
          // FIXME: we have the labes in the triples already!
          // FIXME: can be removed
          return dbpediaKB.getLabels(new HashSet<>(sublist));
        }));
      }
      executorServiceW.shutdown();

      for (int iii = 0; iii < n; ++iii) {

        try {
          final Future<Map<String, Set<String>>> future =
              completionServiceW.poll(Const.INDEX_NLP_TIMEOUT, TimeUnit.SECONDS);
          if (future == null) {
            LOG.warn("Timeout ...");
          } else {
            final Map<String, Set<String>> map = future.get();
            labelsMap.putAll(map);
          }
        } catch (final Exception e) {
          LOG.info(e.getLocalizedMessage(), e);
        }
      }
      // end parallel

      // merge surface forms map
      for (final Entry<String, Set<String>> e : labelsMap.entrySet()) {
        if (surfaceformsMap.get(e.getKey()) == null) {
          surfaceformsMap.put(e.getKey(), e.getValue());
        } else {
          surfaceformsMap.get(e.getKey()).addAll(e.getValue());
        }
      }
      LOG.info(surfaceformsMap.size());

      // add surfaceforms to index
      surfaceformsIndex.add(surfaceformsMap);

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }

    surfaceformsIndex.commit();

    LOG.info("\n" + Statistic.toJSON().toString(2));
  }
}
