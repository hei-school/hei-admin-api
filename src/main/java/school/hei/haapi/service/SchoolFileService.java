package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.repository.FileInfoRepository;

@Service
@AllArgsConstructor
public class SchoolFileService {
  private final FileInfoRepository fileInfoRepository;
  private final FileInfoService fileInfoService;

  public FileInfo uploadSchoolFile(String fileName, FileType fileType, MultipartFile fileToUpload) {
    return fileInfoService.uploadSchoolFile(fileName, fileType, fileToUpload);
  }

  public List<FileInfo> getSchoolFiles() {
    return fileInfoRepository.findAllByUserIsNull();
  }

  public FileInfo getSchoolFileById(String id) {
    return fileInfoRepository.findByUserIsNullAndId(id);
  }
}
