package org.aksw.ocelot.common.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

public class WriteAndReadFile {
  protected final static Logger LOG = LogManager.getLogger(WriteAndReadFile.class);

  /**
   * Gets all files in the folder and sub folders. Ignores duplicates.
   *
   * @param folder
   * @return files
   */
  public static Set<Path> regularFilesInFolder(final String folder) {
    final Set<Path> path = new HashSet<>();
    try {
      Files.walk(Paths.get(folder)).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
          path.add(filePath.getFileName());
        }
      });
    } catch (final IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    return path;
  }

  /**
   * Reads file.
   *
   * @param file
   * @return content of file or null
   * @throws ReadToJSONArrayException
   */
  public static JSONArray readToJSONArray(final File file) {

    JSONArray ja = null;
    try {
      if (Files.isReadable(file.toPath())) {
        ja = new JSONArray(new String(Files.readAllBytes(file.toPath())));
      }
    } catch (JSONException | IOException e) {
      LOG.error("Couldn't process with parameter: " + file.toString(), e);
    }
    return ja;
  }

  /**
   * Writes a {@link JSONArray} data object with elements of {@link JSONObject} instances to the
   * file.
   *
   * @param data {@link JSONArray}
   * @param file to write
   *
   * @return success
   */
  public static boolean writeFile(final JSONArray data, final File file) {
    try {

      if (file.getParentFile() != null) {
        file.getParentFile().mkdirs();
      }
      file.createNewFile();

      LOG.info("Write file: " + file.toPath().toString() + " array length:" + data.length());

      final FileOutputStream fos = new FileOutputStream(file);
      final OutputStreamWriter osw = new OutputStreamWriter(fos);
      osw.write("[ ");
      for (int i = 0; i < data.length(); i++) {
        osw.write(data.getJSONObject(i).toString());
        if (i < (data.length() - 1)) {
          osw.write(", ");
        }
      }
      osw.write(" ]");
      osw.close();
      fos.close();

    } catch (final Exception e) {
      LOG.error("Parameter: " + data.toString(2) + " \nfile: " + file.toString(), e);
      return false;
    }
    return true;
  }
}
