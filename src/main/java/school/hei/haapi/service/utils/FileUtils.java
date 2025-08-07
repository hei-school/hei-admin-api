package school.hei.haapi.service.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.apache.commons.lang3.SystemUtils;

public class FileUtils {

  public static File createFileFromBytes(byte[] bytes, String filename, String suffix) {
    try {
      File file;
      if (SystemUtils.IS_OS_UNIX) {
        FileAttribute<Set<PosixFilePermission>> attr =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
        file = Files.createTempFile(filename, suffix, attr).toFile();
      } else {
        file = Files.createTempFile(filename, suffix).toFile();
        file.setReadable(true, true);
        file.setWritable(true, true);
        file.setExecutable(true, true);
      }
      org.apache.commons.io.FileUtils.writeByteArrayToFile(file, bytes);
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
