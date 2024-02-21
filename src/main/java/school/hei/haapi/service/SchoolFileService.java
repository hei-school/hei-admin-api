package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.File;

@Service
@AllArgsConstructor
public class SchoolFileService {
  private final FileService fileService;

  /*
  public File uploadSchoolFile(String fileName, FileType fileType, byte[] fileToUpload) {

  }
   */
}
