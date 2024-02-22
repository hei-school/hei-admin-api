package school.hei.haapi.service;

import java.io.File;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
public class SchoolFileService {
  private final FileInfoRepository fileInfoRepository;
  private final FileService fileService;

  public FileInfo uploadSchoolFile(String fileName, FileType fileType, MultipartFile fileToUpload) {
    String endPath = "SCHOOL_FILES/" + fileName;
    File uploadedFile = fileService.createTempFile(fileToUpload);
    fileService.uploadObjectToS3Bucket(endPath, uploadedFile);
    FileInfo savedFileInfo = FileInfo.builder()
        .fileType(fileType)
        .name(fileName)
        .fileKeyUrl(endPath)
        .creationDatetime(Instant.now())
        .build();
    return fileInfoRepository.save(savedFileInfo);
  }

  public List<FileInfo> getSchoolFiles() {
    return fileInfoRepository.findAllByUserIsNull();
  }
}
