package org.aksw.ocelot.core.index;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IIndex {

  public List<SolrDocument> search(final String query);

  public boolean add(SolrInputDocument doc);

  public boolean commit();
}
