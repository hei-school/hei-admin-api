package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileInfoRepository;

@Service
@AllArgsConstructor
public class SchoolFileService {
  private final FileInfoRepository fileInfoRepository;
  private final school.hei.haapi.service.aws.FileService fileService;
  private final UserService userService;
  private final String schoolId = "school_id";

  public FileInfo uploadSchoolFile(String fileName, FileType fileType, byte[] fileToUpload) {
    User school = userService.findById(schoolId);
    String endPath = "REGULATION/" + fileName;
    java.io.File toSend = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(endPath, toSend);
    return FileInfo.builder()
        .fileType(fileType)
        .name(fileName)
        .fileKeyUrl(endPath)
        .user(school)
        .creationDatetime(Instant.now())
        .build();
  }

  public List<FileInfo> getSchoolFiles() {
    User school = userService.findById(schoolId);
    return fileInfoRepository.findAllByUser(school);
  }
}
