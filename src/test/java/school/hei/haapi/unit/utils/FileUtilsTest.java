package school.hei.haapi.unit.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.service.utils.FileUtils;

class FileUtilsTest {
  @TempDir Path tempDir;

  @Test
  void shouldConvertFileToMultipartFileSuccessfully() throws IOException {
    String content = "Test file content";
    String fileName = "test.txt";
    File testFile = createTestFile(fileName, content);

    MultipartFile multipartFile = FileUtils.multipartFileFromFile(testFile);

    assertNotNull(multipartFile);
    assertEquals(fileName, multipartFile.getOriginalFilename());
    assertEquals(content, new String(multipartFile.getBytes()));
    assertEquals("text/plain", multipartFile.getContentType());
  }

  @Test
  void shouldHandleDifferentContentTypes() throws IOException {
    String content = "{\"key\":\"value\"}";
    String fileName = "test.json";
    File testFile = createTestFile(fileName, content);

    MultipartFile multipartFile = FileUtils.multipartFileFromFile(testFile);

    assertEquals("application/json", multipartFile.getContentType());
  }

  @Test
  void shouldThrowExceptionWhenFileDoesNotExist() {
    File nonExistentFile = new File(tempDir.toFile(), "nonexistent.txt");

    assertThrows(RuntimeException.class, () -> FileUtils.multipartFileFromFile(nonExistentFile));
  }

  @Test
  void shouldHandleBinaryFiles() throws IOException {
    String fileName = "test.bin";
    byte[] binaryContent = new byte[] {0x01, 0x02, 0x03, 0x04};
    File testFile = createBinaryTestFile(fileName, binaryContent);

    MultipartFile multipartFile = FileUtils.multipartFileFromFile(testFile);

    assertArrayEquals(binaryContent, multipartFile.getBytes());
    assertTrue(multipartFile.getContentType().startsWith("application/octet-stream"));
  }

  private File createTestFile(String fileName, String content) throws IOException {
    Path filePath = tempDir.resolve(fileName);
    Files.writeString(filePath, content);
    return filePath.toFile();
  }

  private File createBinaryTestFile(String fileName, byte[] content) throws IOException {
    Path filePath = tempDir.resolve(fileName);
    Files.write(filePath, content);
    return filePath.toFile();
  }
}
