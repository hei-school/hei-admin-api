package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.FileInfoRepository;

@Service
@AllArgsConstructor
public class SchoolFileService {
  private final FileInfoRepository fileInfoRepository;
  private final FileInfoService fileInfoService;

  public FileInfo uploadSchoolFile(String fileName, FileType fileType, MultipartFile fileToUpload) {
    return fileInfoService.uploadSchoolFile(fileName, fileType, fileToUpload);
  }

  public List<FileInfo> getSchoolFiles(PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return fileInfoRepository.findAllByUserIsNull(pageable);
  }

  public FileInfo getSchoolFileById(String id) {
    return fileInfoRepository.findByUserIsNullAndId(id);
  }
}
