package org.aksw.ocelot.common.io;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Compress {
  public static final Logger LOG = LogManager.getLogger(Compress.class);

  public static String gunzipIt(final Path zipPath) {
    return gunzipIt(zipPath.toAbsolutePath().toString());
  }

  public static String gunzipIt(final String zipFile) {
    final byte[] buffer = new byte[1024];
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      final GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(zipFile));
      int len;
      while ((len = gzis.read(buffer)) > 0) {
        baos.write(buffer, 0, len);
      }
      gzis.close();
      baos.close();
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return baos.toString();
  }
}
