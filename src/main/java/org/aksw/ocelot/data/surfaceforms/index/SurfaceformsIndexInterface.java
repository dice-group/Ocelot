package org.aksw.ocelot.data.surfaceforms.index;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

/**
 * SurfaceformsIndexInterface
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
interface SurfaceformsIndexInterface {
  /**
   * Adds a resource and a list of surfaceforms.
   *
   * @param uri a resource
   * @param sfs list of surfaceforms
   * @return the status of the process
   */
  public boolean add(final Map<String, Set<String>> sfs);

  /**
   * Adds a resource and a list of surfaceforms.
   *
   * @param uri a resource
   * @param sfs list of surfaceforms
   * @return the status of the process
   */
  public boolean add(final String uri, final Set<String> sfs);

  /**
   * Commits the data to the index.
   *
   * @return the status of the process
   */
  public boolean commit();

  /**
   * Search the index.
   *
   * @param query search query
   * @return relevant documents
   */
  public List<SolrDocument> search(final String query);
}
