package org.aksw.ocelot.common.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Is an Enumeration for InputStream.
 *
 * @author rspeck
 *
 */
public class FilesEnumeration implements Enumeration<InputStream> {
  public static final Logger LOG = LogManager.getLogger(FilesEnumeration.class);

  private final String[] files;
  private int current = 0;

  /**
   *
   * @param files path to files
   */
  public FilesEnumeration(final String[] files) {
    this.files = files;
  }

  @Override
  public boolean hasMoreElements() {
    return (current < files.length) ? true : false;
  }

  @Override
  public InputStream nextElement() {
    InputStream in = null;

    if (!hasMoreElements()) {
      throw new NoSuchElementException("No more files.");
    } else {
      try {
        final String file = files[current++];
        in = new FileInputStream(file);
        if (LOG.isDebugEnabled()) {
          LOG.debug("using file: " + file);
        }
      } catch (final FileNotFoundException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    return in;
  }
}
