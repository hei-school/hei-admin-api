package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.FileType.SCHOOL_FILE;

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
  private final String pathSeparator = File.pathSeparator;
  private final FileInfoRepository fileInfoRepository;
  private final FileService fileService;
  private final MultipartFileConverter multipartFileConverter;

  public FileInfo uploadSchoolFile(String fileName, FileType fileType, MultipartFile fileToUpload) {
    // User would be null because School Files is not attached to a specific user.
    String endPath = SCHOOL_FILE + pathSeparator + fileName;
    File uploadedFile = multipartFileConverter.apply(fileToUpload);
    fileService.uploadObjectToS3Bucket(endPath, uploadedFile);
    FileInfo savedFileInfo =
        FileInfo.builder()
            .fileType(fileType)
            .name(fileName)
            .filePath(endPath)
            .creationDatetime(Instant.now())
            .build();
    return fileInfoRepository.save(savedFileInfo);
  }

  public List<FileInfo> getSchoolFiles() {
    return fileInfoRepository.findAllByUserIsNull();
  }
}
