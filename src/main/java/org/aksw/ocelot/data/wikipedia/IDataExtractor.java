package org.aksw.ocelot.data.wikipedia;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Implements Callable to parse Wikipedia documents.
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public interface IDataExtractor extends Callable<List<WikiDoc>> {

  public List<WikiDoc> call(final BufferedReader bufferedReader);
}
