package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DocumentMapper;
import school.hei.haapi.endpoint.rest.model.Document;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.service.SchoolFileService;

@RestController
@AllArgsConstructor
public class SchoolFileController {
  private final SchoolFileService schoolFileService;
  private final DocumentMapper documentMapper;

  @PostMapping("/school/files/raw")
  public Document uploadSchoolFile(
      @RequestParam(name = "file_name") String fileName,
      @RequestParam(name = "file_type") FileType fileType,
      @RequestBody byte[] toUpload) {
    return documentMapper.toRest(schoolFileService.uploadSchoolFile(fileName, fileType, toUpload));
  }

  @GetMapping("/school/files")
  public List<Document> getSchoolRegulations() {
    return schoolFileService.getSchoolFiles().stream()
        .map(documentMapper::toRest)
        .collect(Collectors.toUnmodifiableList());
  }
}
