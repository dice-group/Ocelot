package org.aksw.ocelot.data.surfaceforms.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.data.surfaceforms.ISurfaceForms;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

/**
 * SurfaceformsIndex implements SurfaceformsIndexInterface, ISurfaceForms.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class SurfaceformsIndex implements SurfaceformsIndexInterface, ISurfaceForms {
  public static final Logger LOG = LogManager.getLogger(SurfaceformsIndex.class);

  protected SolrClient solrIndex;
  protected List<SolrInputDocument> docs;
  // final DBpediaKB dbpediaKB = new DBpediaKB();

  /**
   *
   * Constructor.
   *
   */
  public SurfaceformsIndex() {
    final String url = Const.SOLR_URL + File.separator + Const.SOLR_CORE_SURFACEFORMS;
    solrIndex = new ConcurrentUpdateSolrClient(url, Const.SOLR_QUEUE, Const.SOLR_THREADS);
    docs = new ArrayList<>();
  }

  @Override
  public List<SolrDocument> search(final String query) {
    final List<SolrDocument> doclist = new ArrayList<>();

    int current = 0;

    final SolrQuery parameters = new SolrQuery();
    parameters.setQuery("*:*");
    parameters.setFilterQueries(query);
    parameters.setStart(current);
    parameters.setRows(Const.SOLR_ROWS);

    try {
      SolrDocumentList list;
      do {
        final QueryResponse queryResponse = solrIndex.query(parameters);
        list = queryResponse.getResults();

        final Iterator<SolrDocument> iter = list.iterator();
        while (iter.hasNext()) {
          final SolrDocument doc = iter.next();
          doclist.add(doc);
        }
        current += Const.SOLR_ROWS;
        parameters.setStart(current);

      } while (current < list.getNumFound());
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      LOG.error("parameters: " + parameters);
    }
    return doclist;
  }

  @Override
  public boolean add(final Map<String, Set<String>> sfs) {
    boolean good = true;
    for (final Entry<String, Set<String>> e : sfs.entrySet()) {
      good = add(e.getKey(), e.getValue());
      if (!good) {
        break;
      }
    }
    return good;
  }

  @Override
  public boolean add(final String uri, final Set<String> set) {

    Statistic.sfSizeToOccurrence.put(set.size(), //
        Statistic.sfSizeToOccurrence.get(set.size()) == null ? //
            1 : Statistic.sfSizeToOccurrence.get(set.size()) + 1//
    );

    final SolrInputDocument document = new SolrInputDocument();
    document.addField(SurfaceformsIndexEnum.URI.getName(), uri);
    document.addField(SurfaceformsIndexEnum.SFS.getName(), set);
    document.addField(SurfaceformsIndexEnum.SFSSize.getName(), new Integer(set.size()));
    return docs.add(document);
  }

  @Override
  public boolean commit() {
    UpdateResponse response;
    try {
      response = solrIndex.add(docs);
      docs.clear();
      if (response.getStatus() != 0) {
        LOG.warn("Something went wrong");
      } else {
        response = solrIndex.commit();
        if (response.getStatus() != 0) {
          LOG.warn("Something went wrong");
        } else {
          return true;
        }
      }
    } catch (SolrServerException | IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return false;
  }

  @Override
  public Set<String> getSurfaceform(final String uri) {
    final Set<String> results = new LinkedHashSet<String>();

    final String query = SurfaceformsIndexEnum.URI.getName().concat(":\"").concat(uri).concat("\"");
    final List<SolrDocument> list = search(query);

    if (!list.isEmpty()) {
      for (final Object o : list.get(0).getFieldValues(SurfaceformsIndexEnum.SFS.getName())) {
        results.add((String) o);
      }
    }

    /**
     * <code>
     if (results.isEmpty()) {
       LOG.info("call db");
       results = dbpediaKB.getLabels(uri);
     }
     </code>
     */

    return results;
  }

}
