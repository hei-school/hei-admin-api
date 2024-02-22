package school.hei.haapi.service;

import static school.hei.haapi.service.aws.FileService.getFormattedBucketKey;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileInfoRepository;

@Service
@AllArgsConstructor
public class FileEntityService {
  private final String pathSeparator = "/";
  private final FileInfoRepository fileInfoRepository;
  private final UserService userService;
  private final school.hei.haapi.service.aws.FileService fileService;

  public FileInfo uploadFile(
      String fileName, FileType fileType, String studentId, byte[] fileToUpload) {
    User student = userService.findById(studentId);
    String endPath = fileType + pathSeparator + fileName;
    // STUDENT/ref/TRANSCRIPT | DOCUMENT | OTHER/fileName
    String fileKeyUrl = getFormattedBucketKey(student, endPath);
    FileInfo savingFileInfo =
        FileInfo.builder()
            .fileType(fileType)
            .name(fileName)
            .user(student)
            .fileKeyUrl(fileKeyUrl)
            .creationDatetime(Instant.now())
            .build();
    java.io.File uploadingFile = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(fileKeyUrl, uploadingFile);
    return fileInfoRepository.save(savingFileInfo);
  }
}
