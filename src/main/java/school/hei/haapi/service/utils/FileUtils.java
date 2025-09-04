package school.hei.haapi.service.utils;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
  private FileUtils() throws IOException {}

  public static File createFileFromBytes(byte[] bytes, String filename, String suffix) {
    try {
      File file;
      File tempDir;
      if (IS_OS_UNIX) {
        FileAttribute<Set<PosixFilePermission>> attr =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
        tempDir = Files.createTempDirectory("haapi-temp", attr).toFile();
        file = File.createTempFile(filename, suffix, tempDir);
      } else {
        tempDir = Files.createTempDirectory("haapi-temp").toFile();
        var readableDirResult = tempDir.setReadable(true, true);
        var writableDirResult = tempDir.setWritable(true, true);
        if (!(readableDirResult && writableDirResult)) {
          throw new IOException("Cannot set temp dir permission");
        }
        file = File.createTempFile(filename, suffix, tempDir);
        boolean readableResult = file.setReadable(true, true);
        boolean writableResult = file.setWritable(true, true);
        if (!(readableResult && writableResult)) {
          throw new IOException("Cannot set file permission");
        }
      }
      writeByteArrayToFile(file, bytes);
      return file;
    } catch (IOException e) {
      throw new FileCreationException(e.getMessage());
    }
  }

  public static MultipartFile multipartFileFromFile(File file) {
    try {
      FileInputStream input = new FileInputStream(file);
      return new MockMultipartFile(
          file.getName(), file.getName(), Files.probeContentType(file.toPath()), input);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert File to MultipartFile", e);
    }
  }
}
