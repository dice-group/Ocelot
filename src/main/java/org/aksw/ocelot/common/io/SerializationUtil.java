package org.aksw.ocelot.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SerializationUtil {

  public static final Logger LOG = LogManager.getLogger(SerializationUtil.class);
  private static String rootFolder = "tmp";

  /**
   *
   * Serializes an object.
   *
   * @param filename full path and file name, creates parent folders if need.
   * @param o object to be serialized
   * @param force overrides files if exists
   *
   * @throws NotSerializableException
   */
  public static void serialize(String filename, final Object o, final boolean force)
      throws NotSerializableException {
    filename =
        rootFolder + (filename.startsWith(File.separator) ? filename : File.separator + filename);
    LOG.info("serialize: " + filename);

    if (!(o instanceof Serializable)) {
      throw new NotSerializableException(o.getClass().getName());
    }

    if (!FileUtil.fileExists(filename)) {
      final File parent = new File(filename).getParentFile();
      if (parent != null) {
        if (!parent.exists() && !parent.mkdirs()) {
          throw new IllegalStateException("Couldn't create dir: ".concat(parent.getAbsolutePath()));
        }
      }
    } else {
      if (!force) {
        throw new IllegalStateException("File exists ".concat(filename));
      }
    }

    ObjectOutputStream obj_out = null;
    try {
      final FileOutputStream f_out = new FileOutputStream(filename);
      obj_out = new ObjectOutputStream(f_out);
      obj_out.writeObject(o);

    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    } finally {
      if (obj_out != null) {
        try {
          obj_out.close();
        } catch (final IOException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  public static void serialize(final String filename, final Object o)
      throws NotSerializableException {
    SerializationUtil.serialize(filename, o, false);
  }

  /**
   *
   * Deserializes an object.
   *
   * @param filename full path and file name, creates parent folders if need.
   * @param classs object to be serialized
   *
   * @return
   */
  public static <T extends Serializable> T deserialize(String filename, final Class<T> classs) {
    filename =
        rootFolder + (filename.startsWith(File.separator) ? filename : File.separator + filename);
    if (new File(filename).exists()) {

      Object obj = null;
      FileInputStream f_in = null;
      ObjectInputStream obj_in = null;
      try {
        f_in = new FileInputStream(filename);
        obj_in = new ObjectInputStream(f_in);
        obj = obj_in.readObject();

      } catch (IOException | ClassNotFoundException e) {
        LOG.error(e.getLocalizedMessage(), e);
      } finally {
        try {
          obj_in.close();
          f_in.close();
        } catch (final IOException e) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }

      if ((obj != null) && (obj instanceof Serializable)) {
        return classs.cast(obj);
      }
    } else {
      LOG.warn("File not exists to unserialize: " + filename);
    }
    return null;
  }

  public static void setRootFolder(final String folder) {
    rootFolder = folder;
  }

  public static String getRootFolder() {
    return rootFolder;
  }
}
