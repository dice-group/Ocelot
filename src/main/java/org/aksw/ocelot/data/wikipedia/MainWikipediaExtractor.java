package org.aksw.ocelot.data.wikipedia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.aksw.ocelot.common.io.FileUtil;
import org.aksw.ocelot.data.Const;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// TODO: move to tests
public class MainWikipediaExtractor {
  final static Logger LOG = LogManager.getLogger(MainWikipediaExtractor.class);

  public static void main(final String[] args) {

    final Path path =
        Paths.get(Const.CORPUS_FOLDER).normalize().toAbsolutePath().resolve("small.txt");

    final List<String> paths = new ArrayList<>();
    paths.add(path.toString());

    final IDataExtractor we = new WikipediaExtractor(FileUtil.openFileToRead(paths));

    try {
      final List<WikiDoc> wikidocs = we.call();
      wikidocs.forEach(doc -> {
        doc.sectionText.entrySet().forEach(LOG::info);
      });
    } catch (final Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }
}
