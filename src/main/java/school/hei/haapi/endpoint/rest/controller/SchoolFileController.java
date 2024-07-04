package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.FileInfoMapper;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.endpoint.rest.model.ShareInfo;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.OwnCloudService;
import school.hei.haapi.service.SchoolFileService;

@RestController
@AllArgsConstructor
public class SchoolFileController {
  private final SchoolFileService schoolFileService;
  private final FileInfoMapper fileInfoMapper;
  private final OwnCloudService ownCloudService;

  @PostMapping(value = "/school/files/raw", consumes = MULTIPART_FORM_DATA_VALUE)
  public FileInfo uploadSchoolFile(
      @RequestParam(name = "filename") String fileName,
      @RequestParam(name = "file_type") FileType fileType,
      @RequestPart(name = "file_to_upload") MultipartFile fileToUpload) {
    return fileInfoMapper.toRest(
        schoolFileService.uploadSchoolFile(fileName, fileType, fileToUpload));
  }

  @GetMapping("/school/files")
  public List<FileInfo> getSchoolRegulations(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize) {
    return schoolFileService.getSchoolFiles(page, pageSize).stream()
        .map(fileInfoMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/school/files/{id}")
  public FileInfo getSchoolRegulationById(@PathVariable(name = "id") String schoolFileId) {
    return fileInfoMapper.toRest(schoolFileService.getSchoolFileById(schoolFileId));
  }

  @PostMapping("/school/files/share_link")
  public ShareInfo postShareLink(@RequestParam String name, @RequestParam String path) {
    return fileInfoMapper.toShareInfo(ownCloudService.createShareLink(name, path));
  }
}
