package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;

import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;

public class FileInfoTestData {
  /** A school-wide file: no owner, which is what {@code getSchoolRegulations} lists. */
  public static FileInfo aSchoolFile(String name) {
    return FileInfo.builder()
        .id(randomUUID().toString())
        .user(null)
        .name(name)
        .fileType(FileType.DOCUMENT)
        .filePath("SCHOOL_FILES/%s.pdf".formatted(randomUUID()))
        .build();
  }

  public static FileInfo aUserFile(User user, String name, FileType fileType) {
    return FileInfo.builder()
        .id(randomUUID().toString())
        .user(user)
        .name(name)
        .fileType(fileType)
        .filePath("USER/%s/%s.pdf".formatted(user.getRef(), randomUUID()))
        .build();
  }
}
