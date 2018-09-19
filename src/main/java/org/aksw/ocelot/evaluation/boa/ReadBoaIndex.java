package org.aksw.ocelot.evaluation.boa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * <code>
    Documents size: 62728

    URIs:
    http://dbpedia.org/ontology/leaderName
    http://dbpedia.org/ontology/author
    http://dbpedia.org/ontology/starring
    http://dbpedia.org/ontology/deathPlace
    http://dbpedia.org/ontology/foundationPlace
    http://dbpedia.org/ontology/birthPlace
    http://dbpedia.org/ontology/team
    http://dbpedia.org/ontology/subsidiary
    http://dbpedia.org/ontology/award
    http://dbpedia.org/ontology/spouse
</code>
 */
abstract class BoaIndex {
  protected static Logger LOG = LogManager.getLogger(BoaIndex.class);

  public String file = null;

  /**
   *
   * Constructor.
   *
   * @param file boa index
   */
  public BoaIndex(final String file) {
    this.file = file;
  }

  /**
   *
   * @param id
   * @param doc
   */
  public abstract void process(final int id, final Document doc);

  /**
   *
   * @param searcher
   * @param p
   * @throws IOException
   */
  public abstract void processSearch(final IndexSearcher searcher, final String p)
      throws IOException;

  /*
   * @Deprecated
   *//**
     * Reads each document and calls {@link #process(int, Document)}.
     *
     * @param p predicate filter
     *
     * @throws IOException
     *//*
       * public void reads(final String p) throws IOException {
       *
       * final Directory dir = FSDirectory.open(new File(file)); final IndexReader reader =
       * DirectoryReader.open(dir);
       *
       * final int max = reader.maxDoc(); LOG.info("Max: " + max); for (int id = 0; id < max; id++)
       * { final Document doc = reader.document(id); if (doc != null) { final String uri =
       * doc.get(ReadBoaEnum.URI.getLabel()); if (uri.equals(p) || (p == null)) { process(id, doc);
       * } } } reader.close(); dir.close(); }
       */

  /**
   * Reads each document and calls {@link #process(int, Document)}.
   *
   * @param p predicate filter
   *
   */
  public void searcher(final String p) throws IOException {

    final Directory dir = FSDirectory.open(new File(file));
    final IndexReader indexReader = DirectoryReader.open(dir);
    final IndexSearcher indexSearcher = new IndexSearcher(indexReader);

    if (indexSearcher != null) {
      processSearch(indexSearcher, p);

      indexReader.close();
      dir.close();
    }
  }

}


/**
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public class ReadBoaIndex extends BoaIndex {

  protected List<Document> boaPattern = new ArrayList<>();

  /**
   *
   * Constructor.
   *
   * @param file boa index
   */
  public ReadBoaIndex(final String file) {
    super(file);
  }

  /**
   * Test.
   *
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {

    final String file = "/media/rspeck/store1/Data/boa_backup/solr/data/boa/en/index";
    final ReadBoaIndex index = new ReadBoaIndex(file);
    int c = 0;
    final Set<String> ps = new HashSet<>();
    for (final String p : new HashSet<>(Arrays.asList(//
        /*
         * "http://dbpedia.org/ontology/leaderName", // "http://dbpedia.org/ontology/author", //
         * "http://dbpedia.org/ontology/starring", // "http://dbpedia.org/ontology/deathPlace", //
         * "http://dbpedia.org/ontology/foundationPlace", //
         * "http://dbpedia.org/ontology/birthPlace", // "http://dbpedia.org/ontology/team", //
         * "http://dbpedia.org/ontology/subsidiary", // "http://dbpedia.org/ontology/award", //
         */
        "http://dbpedia.org/ontology/spouse")//
    )) {
      // LOG.info(p);
      index.searcher(p);

      for (final Document doc : index.getPattern()) {

        final String s = doc.getField(ReadBoaEnum.NLR_NO_VAR.getLabel()).stringValue();
        if (s.contains("born")) {
          c++;
          ps.add(p);
          LOG.info(s);
        }

      }
    }
    LOG.info(c);
    LOG.info(ps);
  }

  /**
   * Original implementation:
   * https://github.com/dice-group/FactCheck/blob/proof-extraction/defacto-core/src/main/java/org/
   * aksw/defacto/boa/BoaPatternSearcher.java
   */
  @Override
  public void processSearch(final IndexSearcher searcher, final String p) throws IOException {
    boaPattern.clear();
    final Term term = new Term(ReadBoaEnum.URI.getLabel(), p);

    final BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(term), Occur.MUST);

    final int numResults = Integer.MAX_VALUE;

    final SortField sortField;
    sortField = new SortField(//
        // ReadBoaEnum.BOA_SCORE.getLabel(), //
        ReadBoaEnum.SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM.getLabel(), //
        SortField.Type.DOUBLE, true);

    final Sort sort = new Sort(sortField);

    // search
    final ScoreDoc[] hits = searcher.search(query, numResults, sort).scoreDocs;

    LOG.info("hits:" + hits.length);

    for (int i = 0; (i < hits.length); i++) {
      final Document doc = searcher.doc(hits[i].doc);
      // print(doc);

      boaPattern.add(doc);
    }
  }

  public static void print(final Document doc) {
    LOG.info(""//
        + doc.getField(ReadBoaEnum.NLR_VAR.getLabel()).stringValue()//
        + "/" //
        + doc.getField(ReadBoaEnum.SUPPORT_NUMBER_OF_MAX_PAIRS_LEARNED_FROM.getLabel())
            .stringValue() //
        + "/" //
        + doc.getField(ReadBoaEnum.SPECIFICITY_OCCURRENCE.getLabel()).stringValue() //
        + "/" //
        + doc.getField(ReadBoaEnum.POS.getLabel()).stringValue() //
    );

  }

  @Override
  public void process(final int id, final Document doc) {
    throw new UnsupportedOperationException();
  }

  public List<Document> getPattern() {
    return boaPattern;
  }
  /**
   * <code>
   &#64;Override
   public void process(final int id, final Document doc) {

     current.put(id, doc);

     LOG.trace(""//
         + doc.getField(ReadBoaEnum.NLR_VAR.getLabel()).stringValue()//
         + "/" //
         + doc.getField(ReadBoaEnum.URI.getLabel()).stringValue() //
         + "/" //
         + doc.getField(ReadBoaEnum.SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM.getLabel()).stringValue() //
         + "/" //

         + doc.getField(ReadBoaEnum.SUPPORT_NUMBER_OF_MAX_PAIRS_LEARNED_FROM.getLabel())
             .stringValue() //
     );
   }
   </code>
   */
}
