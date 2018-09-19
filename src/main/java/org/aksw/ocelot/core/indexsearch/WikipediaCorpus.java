package org.aksw.ocelot.core.indexsearch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.share.EnumSolrWikiIndex;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WikipediaCorpus implements ICorpus {

  protected static Logger LOG = LogManager.getLogger(WikipediaCorpus.class);

  protected static String replace = "https://en.wikipedia.org/wiki?curid=";
  protected WikipediaIndex wikipediaIndex = new WikipediaIndex();

  @Override
  public String getSentence(final String id) {
    final Set<SolrDocument> set = wikipediaIndex.search(replace + id, EnumSolrWikiIndex.ID);
    if (set.size() > 1) {
      LOG.warn("Found more than one result to the given id!");
    }
    if (set.size() > 0) {
      return (String) set.iterator().next().getFieldValue(EnumSolrWikiIndex.SENTENCE.getName());
    }
    return null;
  }

  @Override
  public CorpusElement getSolrDocument(final String id) {
    final Set<SolrDocument> set = wikipediaIndex.search(replace + id, EnumSolrWikiIndex.ID);
    if (set.size() > 1) {
      LOG.warn("Found more than one result to the given id!");
    }
    if (set.size() > 0) {
      return new CorpusElement(set.iterator().next());
    }
    return null;
  }

  @Override
  public Set<String> sentenceIDs(final String sf, final String domain, final String range) {
    final Set<SolrDocument> set = wikipediaIndex.searchCandidate(sf, domain, range);
    return getIdsToDocs(set).keySet();
  }

  @Override
  public Map<String, Set<String>> sentenceIDs(final Set<String> sfs, final String domain,
      final String range) {

    final int size = sfs.size();
    int current = 0;
    LOG.info("Surfaceforms size: " + sfs.size());

    final Map<String, Set<String>> sfToSentenceIDs = new ConcurrentHashMap<>();
    // search each surfaceform in the index
    final ExecutorService executorService = Executors.newFixedThreadPool(Const.searchThreadsSF);
    final CompletionService<Set<String>> completionService =
        new ExecutorCompletionService<>(executorService);

    final Map<Future<Set<String>>, String> futures = new HashMap<>();

    int i = 0;
    for (final String sf : sfs) {
      if (sfToSentenceIDs.get(sf) == null) {
        futures.put(completionService.submit(() -> {
          return sentenceIDs(sf, domain, range);
        }), sf);
        i++;
      }
    }
    executorService.shutdown();

    for (int iii = 0; iii < i; ++iii) {
      try {
        final Future<Set<String>> future =
            completionService.poll(Const.searchTimeoutSF, TimeUnit.SECONDS);
        if (future == null) {
          LOG.warn("Timeout ...");
        } else {
          final String sf = futures.get(future);
          final Set<String> sentenceIDs = future.get();
          sfToSentenceIDs.put(sf, sentenceIDs);
          ++current;

          if ((current % (size / 4)) == 0) {
            LOG.info(current + "/" + size + " surfaceforms done.");
          }
        }
      } catch (InterruptedException | ExecutionException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    // remove empty once
    for (final Iterator<Entry<String, Set<String>>> iter =
        sfToSentenceIDs.entrySet().iterator(); iter.hasNext();) {
      if (iter.next().getValue().isEmpty()) {
        iter.remove();
      }
    }
    LOG.info("sfToSentenceIDs: " + sfToSentenceIDs.size());
    return sfToSentenceIDs;
  }

  public Set<String> getDocumentIds(final Set<SolrDocument> docs) {
    final Set<String> ids = new HashSet<>();
    docs.forEach(doc -> ids.add(((String) doc.getFieldValue(EnumSolrWikiIndex.ID.getName()))
        .replaceAll(Pattern.quote(replace), "")));
    return ids;
  }

  public Map<String, SolrDocument> getIdsToDocs(final Set<SolrDocument> docs) {
    final Map<String, SolrDocument> ids = new HashMap<>();
    docs.forEach(doc -> ids.put(((String) doc.getFieldValue(EnumSolrWikiIndex.ID.getName()))
        .replaceAll(Pattern.quote(replace), ""), doc));
    return ids;
  }

  /**
   * Searches for a surfaceform in the index to find candidate sentences. <br>
   * Returns a map of document IDs and document candidates.
   *
   * @param sf surfaceform
   * @return map of document IDs and candidates
   */
  public Map<String, SolrDocument> candidatesMap(final String sf) {
    final Set<SolrDocument> set = wikipediaIndex.search(sf, EnumSolrWikiIndex.SENTENCE);
    return getIdsToDocs(set);
  }
}
