package org.aksw.ocelot.core.indexsearch;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.ocelot.data.Const;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Has a SolrClient to send queries.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class AbstractWikipediaIndex {
  protected static Logger LOG = LogManager.getLogger(AbstractWikipediaIndex.class);

  protected SolrClient index = null;

  /**
   * Queries the index.
   *
   * @param parameters
   * @return
   */
  protected Set<SolrDocument> _query(final SolrQuery parameters) {

    // pagination
    parameters.setStart(0);
    parameters.setRows(Const.SOLR_ROWS);
    int currentRow = 0;

    final Set<SolrDocument> set = new HashSet<>();
    try {
      SolrDocumentList list;

      do {
        final QueryResponse queryResponse = index.query(parameters);
        list = queryResponse.getResults();

        final Iterator<SolrDocument> iter = list.iterator();
        while (iter.hasNext()) {
          set.add(iter.next());
        }

        currentRow += Const.SOLR_ROWS;
        parameters.setStart(currentRow);
      } while ((currentRow < list.getNumFound()) && (currentRow < Const.LIMIT));

    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      LOG.error("parameters: " + parameters);
    }
    return set;
  }
}
