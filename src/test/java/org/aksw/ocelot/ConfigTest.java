package org.aksw.ocelot;

import java.io.IOException;

import org.aksw.ocelot.data.Const;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

  final static Logger LOG = LogManager.getLogger(ConfigTest.class);

  @Test
  public void test() throws IOException {

    final String defaultFolder = Const.TMP_FOLDER;
    Assert.assertFalse(defaultFolder.isEmpty());

    final String tmpfolder = ".adepokmn8320";
    FileUtils.copyDirectory(FileUtils.getFile("config"), FileUtils.getFile(tmpfolder));

    Const.setConfigFolder(tmpfolder);
    final String baseFolder = Const.baseFolder;

    Assert.assertEquals(tmpfolder, baseFolder);

    FileUtils.deleteDirectory(FileUtils.getFile(tmpfolder));
  }
}
