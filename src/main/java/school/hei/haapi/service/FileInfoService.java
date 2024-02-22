package school.hei.haapi.service;

import static school.hei.haapi.service.aws.FileService.getFormattedBucketKey;

import java.io.File;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
public class FileInfoService {
  private final String pathSeparator = File.pathSeparator;
  private final FileInfoRepository fileInfoRepository;
  private final UserService userService;
  private final FileService fileService;

  public FileInfo uploadFile(
      String fileName, FileType fileType, String userId, MultipartFile fileToUpload) {
    User student = userService.findById(userId);
    String endPath = fileType + pathSeparator + fileName;
    // STUDENT/ref/TRANSCRIPT | DOCUMENT | OTHER/fileName
    String fileKeyUrl = getFormattedBucketKey(student, endPath);
    FileInfo savedFileInfo =
        FileInfo.builder()
            .fileType(fileType)
            .name(fileName)
            .user(student)
            .fileKeyUrl(fileKeyUrl)
            .creationDatetime(Instant.now())
            .build();
    var uploadedFile = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(fileKeyUrl, uploadedFile);
    return fileInfoRepository.save(savedFileInfo);
  }
}
