package org.aksw.ocelot.common.io;

import java.io.ByteArrayOutputStream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public abstract class SparqlExecution {

  protected final static Logger LOG = LogManager.getLogger(SparqlExecution.class);
  protected QueryExecutionFactory qef = null;

  /**
   *
   * Constructor.
   *
   * @param url
   * @param graph
   * @param pagination
   * @param delay
   */
  public SparqlExecution(//
      final String url, final String graph, final int pagination, final int delay) {
    try {
      qef = new QueryExecutionFactoryHttp(url, graph);
      qef = new QueryExecutionFactoryPaginated(qef, pagination);
      qef = new QueryExecutionFactoryDelay(qef, delay);
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Executes a select query q and gets bindings in json.
   *
   * @param q
   * @return
   */
  public JSONArray execSelectToJSONArray(final String query) {
    final ResultSet rs = qef.createQueryExecution(query).execSelect();
    if (rs != null) {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ResultSetFormatter.outputAsJSON(baos, rs);
      return new JSONObject(baos.toString()).getJSONObject("results").getJSONArray("bindings");
    }
    return new JSONArray();
  }
}
