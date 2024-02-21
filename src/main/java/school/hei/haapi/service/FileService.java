package school.hei.haapi.service;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.model.File;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileRepository;

import static school.hei.haapi.service.aws.FileService.getFormattedBucketKey;

@Service
@AllArgsConstructor
public class FileService {
  private final String pathSeparator = "/";
  private final FileRepository fileRepository;
  private final UserService userService;
  private final school.hei.haapi.service.aws.FileService fileService;

  public File uploadFile(String fileName, FileType fileType, String studentId, byte[] fileToUpload) {
    User student = userService.findById(studentId);
    String endPath = fileType + pathSeparator + fileName;
    // STUDENT/ref/TRANSCRIPT | DOCUMENT | OTHER/file
    String fileKeyUrl = getFormattedBucketKey(student, endPath);
    File file = File.builder()
        .fileType(fileType)
        .name(fileName)
        .user(student)
        .fileKeyUrl(fileKeyUrl)
        .creationDatetime(Instant.now())
        .build();
    java.io.File toSend = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(fileKeyUrl, toSend);
    return fileRepository.save(file);
  }

  public File uploadFile(String fileName, FileType fileType, byte[] fileToUpload) {
    String endPath = "REGULATION/" + fileName;
    java.io.File toSend = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(endPath, toSend);
    return File.builder()
        .fileType(fileType)
        .name(fileName)
        .fileKeyUrl(endPath)
        .creationDatetime(Instant.now())
        .build();
  }
}
