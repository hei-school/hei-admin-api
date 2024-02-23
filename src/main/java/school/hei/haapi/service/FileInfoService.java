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
  private final FileInfoRepository fileInfoRepository;
  private final UserService userService;
  private final FileService fileService;
  private final MultipartFileConverter multipartFileConverter;

  public FileInfo uploadFile(
      String fileName, FileType fileType, String userId, MultipartFile fileToUpload) {
    User student = userService.findById(userId);
    // STUDENT/STUDENT_ref/<TRANSCRIPT|DOCUMENT|OTHER>/fileName
    String filePath = getFormattedBucketKey(student, fileType, fileName);
    FileInfo fileInfo =
        FileInfo.builder()
            .fileType(fileType)
            .name(fileName)
            .user(student)
            .filePath(filePath)
            .creationDatetime(Instant.now())
            .build();
    File file = multipartFileConverter.apply(fileToUpload);
    fileService.uploadObjectToS3Bucket(filePath, file);
    return fileInfoRepository.save(fileInfo);
  }
}
