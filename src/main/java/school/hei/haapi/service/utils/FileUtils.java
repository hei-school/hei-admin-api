package school.hei.haapi.service.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils {

  public static File createFileFromBytes(byte[] bytes, String filename, String suffix) {
    try {
      File file = File.createTempFile(filename, suffix);
      org.apache.commons.io.FileUtils.writeByteArrayToFile(file, bytes);
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
