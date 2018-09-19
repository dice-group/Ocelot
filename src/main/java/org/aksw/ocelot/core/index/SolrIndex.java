package org.aksw.ocelot.core.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.ocelot.data.Const;
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
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class SolrIndex implements IIndex {
  protected final static Logger LOG = LogManager.getLogger(SolrIndex.class);

  protected SolrClient solrIndex = null;
  protected List<SolrInputDocument> docs = new ArrayList<>();

  /**
   *
   * Constructor.
   *
   */
  public SolrIndex() {
    final String url = Const.SOLR_URL + File.separator + Const.SOLR_CORE_INDEX;
    solrIndex =
        new ConcurrentUpdateSolrClient(url, Const.INDEX_SOLR_QUEUE, Const.INDEX_SOLR_THREADS);
  }

  @Override
  public List<SolrDocument> search(final String query) {
    final List<SolrDocument> docs = new ArrayList<>();

    int current = 0;

    final SolrQuery parameters = new SolrQuery();
    parameters.setQuery("*:*");
    parameters.setFilterQueries(query);
    parameters.setStart(current);
    parameters.setRows(Const.INDEX_SOLR_ROWS);

    try {
      SolrDocumentList list;
      do {
        final QueryResponse queryResponse = solrIndex.query(parameters);
        list = queryResponse.getResults();

        final Iterator<SolrDocument> iter = list.iterator();
        while (iter.hasNext()) {
          final SolrDocument doc = iter.next();
          docs.add(doc);
        }
        current += Const.INDEX_SOLR_ROWS;
        parameters.setStart(current);

      } while (current < list.getNumFound());
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      LOG.error("parameters: " + parameters);
    }
    return docs;
  }

  /**
   *
   */
  @Override
  public boolean add(final SolrInputDocument doc) {
    return docs.add(doc);
  }

  /**
   *
   */
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
}
