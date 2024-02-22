package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.File;
import school.hei.haapi.model.SchoolConfiguration;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileRepository;

@Service
@AllArgsConstructor
public class SchoolFileService {
  private final FileRepository fileRepository;
  private final school.hei.haapi.service.aws.FileService fileService;

  public File uploadSchoolFile(String fileName, FileType fileType, byte[] fileToUpload) {
    User school = new SchoolConfiguration();
    String endPath = "REGULATION/" + fileName;
    java.io.File toSend = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(endPath, toSend);
    return File.builder()
        .fileType(fileType)
        .name(fileName)
        .fileKeyUrl(endPath)
        .user(school)
        .creationDatetime(Instant.now())
        .build();
  }

  public List<File> getSchoolFiles() {
    User school = new SchoolConfiguration();
    return fileRepository.findAllByUser(school);
  }
}
