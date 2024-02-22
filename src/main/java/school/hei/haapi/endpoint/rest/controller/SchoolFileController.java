package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.DocumentMapper;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.service.SchoolFileService;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@AllArgsConstructor
public class SchoolFileController {
  private final SchoolFileService schoolFileService;
  private final DocumentMapper documentMapper;

  @PostMapping(value = "/school/files/raw", consumes = MULTIPART_FORM_DATA_VALUE)
  public FileInfo uploadSchoolFile(
      @RequestParam(name = "file_name") String fileName,
      @RequestParam(name = "file_type") FileType fileType,
      @RequestPart(name = "file") MultipartFile fileToUpload) {
    return documentMapper.toRest(schoolFileService.uploadSchoolFile(fileName, fileType, fileToUpload));
  }

  @GetMapping("/school/files")
  public List<FileInfo> getSchoolRegulations() {
    return schoolFileService.getSchoolFiles().stream()
        .map(documentMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
