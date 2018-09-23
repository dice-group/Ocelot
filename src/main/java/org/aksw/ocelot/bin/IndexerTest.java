package org.aksw.ocelot.bin;

import java.nio.file.Path;
import java.util.Set;

import org.aksw.ocelot.core.index.Indexer;
import org.aksw.ocelot.data.Const;
import org.aksw.simba.knowledgeextraction.commons.io.FileUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class IndexerTest {
  protected final static Logger LOG = LogManager.getLogger(IndexerTest.class);

  public static void main(final String[] args) {
    LOG.info("Start ...");

    final Indexer indexer = new Indexer();
    LOG.info("Read files ...");

    final Set<Path> files = FileUtil.filesInFolderSave(Const.CORPUS_FOLDER);
    LOG.info("# files: " + files.size());

    indexer.createIndex(files);

    // final String text =
    // "Elizabeth Tailboys, 4th Baroness Tailboys of Kyme () was the daughter of Elizabeth Blount
    // and Gilbert Tailboys, 1st Baron Tailboys of Kyme, and the second wife of Ambrose Dudley, 3rd
    // Earl of Warwick";
    // LOG.info(text);

    // final Map<Integer, SimpleEntry<String, Map<String, List<Object>>>> r =
    // indexer.annotations(text);

    // LOG.info(r.get(1).getValue().get(IndexerTest.name(NamedEntityTagAnnotation.class)));
    // indexer.createIndex();
    // LOG.info("\n" + IndexStatistic.toJSON().toString(2));

    LOG.info("End ...");
  }
}
