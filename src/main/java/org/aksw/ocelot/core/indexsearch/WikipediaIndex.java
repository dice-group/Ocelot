package org.aksw.ocelot.core.indexsearch;

import java.io.File;
import java.util.Set;

import org.aksw.ocelot.data.Const;
import org.aksw.ocelot.share.EnumSolrWikiIndex;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrDocument;

/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class WikipediaIndex extends AbstractWikipediaIndex {

  /**
   *
   * Constructor.
   *
   */
  public WikipediaIndex() {
    index = new HttpSolrClient(Const.SOLR_URL + File.separator + Const.SOLR_CORE_INDEX);
    ((HttpSolrClient) index).setParser(new XMLResponseParser());
  }

  public SolrQuery getSearchQuery(final String domain, final String range) {
    return new SolrQuery()//
        .setQuery(""
            // sentence has at least two NE with type of domain and range
            .concat(EnumSolrWikiIndex.NER.getName()).concat(":")//
            .concat("\"* ").concat(domain).concat(" * O * ").concat(range).concat(" *\"")//

            .concat(" AND ")//

            // sentence length
            .concat(EnumSolrWikiIndex.SENTENCE_SIZE.getName()).concat(":")//
            .concat("[ ")//
            .concat(String.valueOf(Const.sentenceLengthMin))//
            .concat(" TO ")//
            .concat(String.valueOf(Const.sentenceLengthMax))//
            .concat(" ]")//
    // .concat(EnumSolrWikiIndex.POS.getName()).concat(":")//
    // .concat("(VB OR VBD OR VBG OR VBN OR VBP OR VBZ OR JJ OR JJR OR JJS)")//
    );
  }

  public SolrQuery getSearchQuery2() {
    return new SolrQuery()//
        .setQuery(""
            // sentence length
            .concat(EnumSolrWikiIndex.SENTENCE_SIZE.getName()).concat(":")//
            .concat("[ ")//
            .concat(String.valueOf(Const.sentenceLengthMin))//
            .concat(" TO ")//
            .concat(String.valueOf(Const.sentenceLengthMax))//
            .concat(" ]")//
    );
  }

  /**
   *
   * @param sf
   * @param domain
   * @param range
   * @return
   */
  public Set<SolrDocument> searchCandidate(final String sf, final String domain,
      final String range) {

    final SolrQuery parameters = Const.NER_USE ? getSearchQuery(domain, range) : getSearchQuery2();

    final String filter = EnumSolrWikiIndex.SENTENCE.getName()//
        .concat(":\"").concat(QueryParserBase.escape(sf)).concat("\"");
    parameters.setFilterQueries(filter);
    return _query(parameters);
  }

  /**
   * Search by query and field.
   *
   * @param query
   */
  public Set<SolrDocument> search(final String query, final EnumSolrWikiIndex field) {
    final SolrQuery parameters = new SolrQuery();
    parameters.setQuery("*:*");
    parameters.setFilterQueries( //
        field.getName().concat(":\"").concat(QueryParserBase.escape(query)).concat("\"")//
    );

    return _query(parameters);
  }
}
